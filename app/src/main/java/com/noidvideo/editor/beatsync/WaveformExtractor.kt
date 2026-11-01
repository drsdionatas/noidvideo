package com.noidvideo.editor.beatsync

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.sqrt

/**
 * Extrai a forma de onda (waveform) de um arquivo de áudio usando RMS.
 *
 * Estratégia: acumula RMS em janelas fixas de ~20ms durante a decodificação
 * (memória constante, independente da duração do áudio) e depois reamostra
 * para o número de barras desejado.
 */
object WaveformExtractor {

    private const val TARGET_SAMPLES = 300
    private const val WINDOW_MS = 20

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

            val sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
            val channelCount = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
            val windowSamples = (sampleRate * WINDOW_MS / 1000).coerceAtLeast(1)

            extractor.selectTrack(trackIndex)
            val mime = format.getString(MediaFormat.KEY_MIME) ?: return FloatArray(0)
            val codec = MediaCodec.createDecoderByType(mime)
            codec.configure(format, null, null, 0)
            codec.start()

            // RMS por janela fixa — memória constante
            val windows = ArrayList<Float>(1024)
            var windowSumSq = 0.0
            var windowCount = 0

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

                    // Percorre amostras; a cada windowSamples fecha uma janela
                    var i = 0
                    while (i < shorts.size) {
                        val norm = shorts[i].toDouble() / 32768.0
                        windowSumSq += norm * norm
                        windowCount++
                        if (windowCount >= windowSamples) {
                            windows.add(sqrt(windowSumSq / windowCount).toFloat())
                            windowSumSq = 0.0
                            windowCount = 0
                        }
                        i += channelCount.coerceAtLeast(1)
                    }

                    codec.releaseOutputBuffer(outIdx, false)
                    if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                        sawOutputEOS = true
                    }
                }
            }

            // Fecha a última janela parcial
            if (windowCount > 0) {
                windows.add(sqrt(windowSumSq / windowCount).toFloat())
            }

            codec.stop()
            codec.release()

            if (windows.isEmpty()) return FloatArray(0)

            // Reamostra para targetSamples pegando o RMS médio de cada bucket
            val result = FloatArray(targetSamples)
            val bucketSize = windows.size.toFloat() / targetSamples
            for (i in 0 until targetSamples) {
                val start = (i * bucketSize).toInt().coerceIn(0, windows.size - 1)
                val end = ((i + 1) * bucketSize).toInt().coerceIn(start + 1, windows.size)
                var sum = 0f
                for (j in start until end) sum += windows[j]
                result[i] = sum / (end - start)
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
