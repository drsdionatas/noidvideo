package com.noidvideo.editor.export

import android.graphics.Matrix
import androidx.media3.common.util.Size
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.MatrixTransformation

/**
 * Zoom Punch animado: começa em 1.18x e assenta em 1.0x nos primeiros 350ms
 * DO CLIPE (não da composição inteira).
 *
 * @param clipStartUs tempo de início do clipe na composição, em microssegundos.
 *                   O Media3 passa presentationTimeUs absoluto; subtraímos o offset
 *                   para que a animação rode no começo de cada clipe.
 */
@UnstableApi
class PunchZoomEffect(
    private val clipStartUs: Long,
    private val durationMs: Long = 350L,
    private val startScale: Float = 1.18f
) : MatrixTransformation {

    override fun getMatrix(presentationTimeUs: Long): Matrix {
        val elapsedMs = (presentationTimeUs - clipStartUs) / 1000f
        val t = (elapsedMs / durationMs).coerceIn(0f, 1f)
        // ease-out cubic: rápido no início, assenta suave
        val eased = 1f - (1f - t) * (1f - t) * (1f - t)
        val scale = startScale + (1f - startScale) * eased

        return Matrix().apply {
            setScale(scale, scale)
        }
    }

    override fun configure(inputWidth: Int, inputHeight: Int): Size {
        return Size(inputWidth, inputHeight)
    }
}
