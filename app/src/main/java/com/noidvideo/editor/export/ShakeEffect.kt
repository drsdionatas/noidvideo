package com.noidvideo.editor.export

import android.graphics.Matrix
import androidx.media3.common.util.Size
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.MatrixTransformation
import kotlin.math.sin

/**
 * Shake: tremor contínuo ao longo de todo o clipe.
 * Frequência alta, amplitude moderada — sensação de câmera na mão.
 */
@UnstableApi
class ShakeEffect(
    private val clipStartUs: Long,
    private val clipDurationMs: Long,
    private val amplitudePx: Float = 12f
) : MatrixTransformation {

    override fun getMatrix(presentationTimeUs: Long): Matrix {
        val elapsedMs = (presentationTimeUs - clipStartUs) / 1000f
        if (elapsedMs < 0f || elapsedMs > clipDurationMs) {
            return Matrix()
        }

        // Tremor contínuo com frequências diferentes em X e Y
        val dx = sin(elapsedMs * 0.08f) * amplitudePx
        val dy = sin(elapsedMs * 0.11f) * amplitudePx * 0.7f

        return Matrix().apply {
            setTranslate(dx, dy)
        }
    }

    override fun configure(inputWidth: Int, inputHeight: Int): Size {
        return Size(inputWidth, inputHeight)
    }
}
