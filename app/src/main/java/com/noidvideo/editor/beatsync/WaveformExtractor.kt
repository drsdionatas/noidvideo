package com.noidvideo.editor.beatsync

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.abs

/**
 * Extrai a forma de onda (waveform) de um arquivo de áudio.
 * Retorna uma lista de amplitudes normalizadas entre 0f e 1f.
 */
object WaveformExtractor {

    private const val TARGET_SAMPLES = 240

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

            val amplitudes = ArrayList<Float>(4096)
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
                        amplitudes.add(abs(s.toInt()) / 32768f)
                    }
                    codec.releaseOutputBuffer(outIdx, false)
                    if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                        sawOutputEOS = true
                    }
                } else if (outIdx == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    // ignora
                }
            }

            codec.stop()
            codec.release()

            if (amplitudes.isEmpty()) return FloatArray(0)

            // Reduz para targetSamples pegando o pico de cada bucket
            val bucketSize = (amplitudes.size / targetSamples).coerceAtLeast(1)
            val result = FloatArray(targetSamples)
            for (i in 0 until targetSamples) {
                val start = i * bucketSize
                val end = minOf(start + bucketSize, amplitudes.size)
                var peak = 0f
                for (j in start until end) {
                    if (amplitudes[j] > peak) peak = amplitudes[j]
                }
                result[i] = peak
            }

            // Normaliza para 0..1
            val max = result.maxOrNull() ?: 1f
            if (max > 0f) {
                for (i in result.indices) result[i] /= max
            }
            return result
        } catch (e: Exception) {
            return FloatArray(0)
        } finally {
            runCatching { extractor.release() }
        }
    }
}
