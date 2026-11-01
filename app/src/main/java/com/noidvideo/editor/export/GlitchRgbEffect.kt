package com.noidvideo.editor.export

import android.graphics.Matrix
import androidx.media3.common.util.Size
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.MatrixTransformation
import kotlin.math.sin

/**
 * Glitch RGB: pulsos de deslocamento horizontal ao longo de TODO o clipe.
 * Não é só no início — pulsa a cada ~400ms, criando o efeito de sinal instável.
 */
@UnstableApi
class GlitchRgbEffect(
    private val clipStartUs: Long,
    private val clipDurationMs: Long,
    private val intensity: Float = 0.06f
) : MatrixTransformation {

    override fun getMatrix(presentationTimeUs: Long): Matrix {
        val elapsedMs = (presentationTimeUs - clipStartUs) / 1000f
        if (elapsedMs < 0f || elapsedMs > clipDurationMs) {
            return Matrix()
        }

        // Pulso a cada 400ms — senoide rápida
        val pulse = sin(elapsedMs * 0.025f)
        // Só pulsa quando o seno está alto (glitch intermitente)
        val active = if (pulse > 0.3f) pulse else 0f
        val shift = intensity * active

        return Matrix().apply {
            setTranslate(shift, 0f)
        }
    }

    override fun configure(inputWidth: Int, inputHeight: Int): Size {
        return Size(inputWidth, inputHeight)
    }
}
