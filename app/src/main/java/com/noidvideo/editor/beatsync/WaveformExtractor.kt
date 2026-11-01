package com.noidvideo.editor.beatsync

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Extrai a forma de onda (waveform) de um arquivo de áudio usando RMS.
 * RMS = Root Mean Square, a energia real do sinal — não o pico.
 * Retorna amplitudes normalizadas entre 0f e 1f.
 */
object WaveformExtractor {

    private const val TARGET_SAMPLES = 300

    fun extract(context: Context, uri: Uri, targetSamples: Int = TARGET_SAMPLES): FloatArray {
        val extractor = MediaExtractor()
        try {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                extractor.setDataSource(pfd.fileDescriptor)
            } ?: return FloatArray(0)

            var trackIndex = -1
            var format: MediaFormat? = null
            for (i in 0 until extractor.trackCount) {
                val f = extractor.getTrackFormat(i)
                val mime = f.getString(MediaFormat.KEY_MIME) ?: continue
                if (mime.startsWith("audio/")) {
                    trackIndex = i
                    format = f
                    break
                }
            }
            if (trackIndex < 0 || format == null) return FloatArray(0)

            extractor.selectTrack(trackIndex)
            val mime = format.getString(MediaFormat.KEY_MIME) ?: return FloatArray(0)
            val codec = MediaCodec.createDecoderByType(mime)
            codec.configure(format, null, null, 0)
            codec.start()

            // Acumula soma de quadrados por bucket (RMS incremental)
            val sumSquares = DoubleArray(targetSamples)
            val counts = IntArray(targetSamples)
            var totalSamples = 0L

            val info = MediaCodec.BufferInfo()
            var sawInputEOS = false
            var sawOutputEOS = false

            while (!sawOutputEOS) {
                if (!sawInputEOS) {
                    val inIdx = codec.dequeueInputBuffer(10_000)
                    if (inIdx >= 0) {
                        val buf = codec.getInputBuffer(inIdx)!!
                        val size = extractor.readSampleData(buf, 0)
                        if (size < 0) {
                            codec.queueInputBuffer(inIdx, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            sawInputEOS = true
                        } else {
                            codec.queueInputBuffer(inIdx, 0, size, extractor.sampleTime, 0)
                            extractor.advance()
                        }
                    }
                }

                val outIdx = codec.dequeueOutputBuffer(info, 10_000)
                if (outIdx >= 0) {
                    val outBuf: ByteBuffer = codec.getOutputBuffer(outIdx)!!
                    outBuf.order(ByteOrder.LITTLE_ENDIAN)
                    val shorts = ShortArray(info.size / 2)
                    outBuf.asShortBuffer().get(shorts)

                    for (s in shorts) {
                        val norm = s.toDouble() / 32768.0
                        // Bucket proporcional ao total de amostras lidas
                        val bucket = ((totalSamples * targetSamples) / (totalSamples + shorts.size + 1)).toInt()
                            .coerceIn(0, targetSamples - 1)
                        sumSquares[bucket] += norm * norm
                        counts[bucket]++
                        totalSamples++
                    }

                    codec.releaseOutputBuffer(outIdx, false)
                    if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                        sawOutputEOS = true
                    }
                }
            }

            codec.stop()
            codec.release()

            // RMS por bucket
            val result = FloatArray(targetSamples)
            for (i in 0 until targetSamples) {
                result[i] = if (counts[i] > 0) {
                    sqrt(sumSquares[i] / counts[i]).toFloat()
                } else 0f
            }

            // Normaliza para 0..1
            val max = result.maxOrNull() ?: 0f
            if (max > 0f) {
                for (i in result.indices) result[i] = (result[i] / max).coerceIn(0f, 1f)
            }
            return result
        } catch (e: Exception) {
            return FloatArray(0)
        } finally {
            runCatching { extractor.release() }
        }
    }
}
