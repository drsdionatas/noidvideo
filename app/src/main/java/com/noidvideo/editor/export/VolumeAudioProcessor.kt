package com.noidvideo.editor.export

import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.BaseAudioProcessor
import java.nio.ByteBuffer

/**
 * Aplica ganho linear (volume) em PCM 16-bit.
 * volume 1.0 = original, 0.0 = mudo, 2.0 = dobro.
 */
class VolumeAudioProcessor(private val volume: Float) : BaseAudioProcessor() {

    override fun onConfigure(inputAudioFormat: AudioProcessor.AudioFormat): AudioProcessor.AudioFormat {
        return inputAudioFormat
    }

    override fun queueInput(inputBuffer: ByteBuffer) {
        val remaining = inputBuffer.remaining()
        if (remaining == 0) return

        val out = replaceOutputBuffer(remaining)
        val gain = volume.coerceIn(0f, 2f)

        while (inputBuffer.remaining() >= 2) {
            val sample = inputBuffer.short.toInt()
            val scaled = (sample * gain).toInt().coerceIn(-32768, 32767)
            out.putShort(scaled.toShort())
        }
        // bytes ímpares (não deveria ocorrer em PCM 16-bit)
        while (inputBuffer.hasRemaining()) {
            out.put(inputBuffer.get())
        }
        out.flip()
    }
}
