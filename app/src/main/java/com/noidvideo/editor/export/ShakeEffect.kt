package com.noidvideo.editor.export

import android.graphics.Matrix
import androidx.media3.common.util.Size
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.MatrixTransformation
import kotlin.math.sin

/**
 * Shake: tremor de câmera nos primeiros 300ms do clipe.
 * Simula impacto de batida forte. Roda na GPU.
 */
@UnstableApi
class ShakeEffect(
    private val clipStartUs: Long,
    private val durationMs: Long = 300L,
    private val amplitudePx: Float = 18f
) : MatrixTransformation {

    override fun getMatrix(presentationTimeUs: Long): Matrix {
        val elapsedMs = (presentationTimeUs - clipStartUs) / 1000f
        val t = (elapsedMs / durationMs).coerceIn(0f, 1f)
        val decay = (1f - t) * (1f - t)
        // Frequência alta = tremor rápido
        val dx = sin(elapsedMs * 0.09f) * amplitudePx * decay
        val dy = sin(elapsedMs * 0.13f) * amplitudePx * decay * 0.6f

        return Matrix().apply {
            setTranslate(dx, dy)
        }
    }

    override fun configure(inputWidth: Int, inputHeight: Int): Size {
        return Size(inputWidth, inputHeight)
    }
}
