package com.noidvideo.editor.export

import android.graphics.Matrix
import androidx.media3.common.util.Size
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.MatrixTransformation

/**
 * Flash: pulso de escala branco-simulado nos primeiros 120ms.
 * O Media3 não permite overlay de cor via MatrixTransformation,
 * então simulamos com um "punch" de escala muito rápido que
 * cria a sensação de flash no corte.
 */
@UnstableApi
class FlashEffect(
    private val clipStartUs: Long,
    private val durationMs: Long = 120L
) : MatrixTransformation {

    override fun getMatrix(presentationTimeUs: Long): Matrix {
        val elapsedMs = (presentationTimeUs - clipStartUs) / 1000f
        val t = (elapsedMs / durationMs).coerceIn(0f, 1f)
        // Pico de 1.08x no início, assenta rápido
        val scale = 1f + 0.08f * (1f - t)

        return Matrix().apply {
            setScale(scale, scale)
        }
    }

    override fun configure(inputWidth: Int, inputHeight: Int): Size {
        return Size(inputWidth, inputHeight)
    }
}
