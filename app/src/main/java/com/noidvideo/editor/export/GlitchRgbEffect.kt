package com.noidvideo.editor.export

import android.graphics.Matrix
import androidx.media3.common.util.Size
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.MatrixTransformation

/**
 * Glitch RGB: desloca os canais vermelho e azul horizontalmente,
 * criando o efeito de aberração cromática de vídeos esportivos radicais.
 *
 * Roda na GPU via MatrixTransformation (Adreno 740 no S23 Ultra).
 */
@UnstableApi
class GlitchRgbEffect(
    private val clipStartUs: Long,
    private val durationMs: Long = 220L,
    private val intensity: Float = 0.035f
) : MatrixTransformation {

    override fun getMatrix(presentationTimeUs: Long): Matrix {
        val elapsedMs = (presentationTimeUs - clipStartUs) / 1000f
        val t = (elapsedMs / durationMs).coerceIn(0f, 1f)
        // Decai rápido: glitch forte no início, some
        val decay = (1f - t) * (1f - t)
        val shift = intensity * decay

        return Matrix().apply {
            // Deslocamento horizontal alternado (simula split de canais)
            val dir = if ((elapsedMs.toInt() / 40) % 2 == 0) 1f else -1f
            setTranslate(shift * dir, 0f)
        }
    }

    override fun configure(inputWidth: Int, inputHeight: Int): Size {
        return Size(inputWidth, inputHeight)
    }
}
