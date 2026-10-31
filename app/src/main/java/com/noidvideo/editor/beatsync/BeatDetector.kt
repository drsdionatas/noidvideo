package com.noidvideo.editor.beatsync

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Detecção de batida on-device.
 *
 * Pipeline:
 *  1. Decodifica o áudio para PCM mono 22.05 kHz
 *  2. Calcula o envelope de onsets (OnsetAnalyzer)
 *  3. Estima o BPM por autocorrelação (BpmEstimator)
 *  4. Filtra batidas estáveis -> BeatMap
 */
object BeatDetector {

    private const val SAMPLE_RATE = 22050

    suspend fun analyze(context: Context, uri: Uri): BeatMap? =
        withContext(Dispatchers.Default) {
            val pcm = decodeToMonoPcm(context, uri) ?: return@withContext null
            val onsets = OnsetAnalyzer.computeOnsetEnvelope(pcm)
            if (onsets.isEmpty()) return@withContext null

            val frameRate = SAMPLE_RATE.toFloat() / OnsetAnalyzer.HOP
            val bpm = BpmEstimator.estimate(onsets, frameRate)
            if (bpm <= 0f) return@withContext null

            val beats = BpmEstimator.pickBeats(onsets, bpm, frameRate)
            if (beats.isEmpty()) return@withContext null

            val durationMs = (pcm.size.toLong() * 1000L) / SAMPLE_RATE

            BeatMap(
                bpm = bpm,
                beatTimesMs = beats,
                firstBeatMs = beats.first(),
                durationMs = durationMs
            )
        }

    private fun decodeToMonoPcm(context: Context, uri: Uri): FloatArray? {
        var extractor: MediaExtractor? = null
        var codec: MediaCodec? = null
        return runCatching {
            extractor = MediaExtractor()
            context.contentResolver.openFileDescriptor(uri, "r")!!.use { pfd ->
                extractor!!.setDataSource(pfd.fileDescriptor)
            }

            var trackIndex = -1
            var format: MediaFormat? = null
            for (i in 0 until extractor!!.trackCount) {
                val f = extractor!!.getTrackFormat(i)
                if (f.getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true) {
                    trackIndex = i
                    format = f
                    break
                }
            }
            if (trackIndex < 0 || format == null) return null
            extractor!!.selectTrack(trackIndex)

            val sampleRate = format!!.getInteger(MediaFormat.KEY_SAMPLE_RATE)
            val channels = format!!.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
            val mime = format!!.getString(MediaFormat.KEY_MIME)!!

            codec = MediaCodec.createDecoderByType(mime)
            codec!!.configure(format, null, null, 0)
            codec!!.start()

            val bufferInfo = MediaCodec.BufferInfo()
            val out = ArrayList<Float>(sampleRate * 60)

            while (true) {
                val inIdx = extractor!!.sampleTrackIndex
                val inputIndex = codec!!.dequeueInputBuffer(10_000)
                if (inputIndex >= 0) {
                    val buf = codec!!.getInputBuffer(inputIndex)!!
                    val size = if (inIdx >= 0) extractor!!.readSampleData(buf, 0) else -1
                    if (size < 0) {
                        codec!!.queueInputBuffer(
                            inputIndex, 0, 0, 0,
                            MediaCodec.BUFFER_FLAG_END_OF_STREAM
                        )
                    } else {
                        codec!!.queueInputBuffer(inputIndex, 0, size, extractor!!.sampleTime, 0)
                        extractor!!.advance()
                    }
                }

                val outIndex = codec!!.dequeueOutputBuffer(bufferInfo, 10_000)
                if (outIndex >= 0) {
                    val buf = codec!!.getOutputBuffer(outIndex)!!
                    val shorts = ShortArray(bufferInfo.size / 2)
                    buf.asShortBuffer().get(shorts)
                    for (i in shorts.indices step channels) {
                        var acc = 0
                        for (c in 0 until channels) {
                            if (i + c < shorts.size) acc += shorts[i + c]
                        }
                        out.add(acc.toFloat() / (channels * Short.MAX_VALUE))
                    }
                    codec!!.releaseOutputBuffer(outIndex, false)
                }
                if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) break
            }

            if (out.isEmpty()) null else resample(out.toFloatArray(), sampleRate)
        }.getOrNull().also {
            runCatching { codec?.stop() }
            runCatching { codec?.release() }
            runCatching { extractor?.release() }
        }
    }

    private fun resample(input: FloatArray, fromRate: Int): FloatArray {
        if (fromRate == SAMPLE_RATE) return input
        val ratio = fromRate.toFloat() / SAMPLE_RATE
        val outSize = (input.size / ratio).toInt()
        return FloatArray(outSize) { i ->
            val src = (i * ratio).toInt()
            if (src < input.size) input[src] else 0f
        }
    }
}
