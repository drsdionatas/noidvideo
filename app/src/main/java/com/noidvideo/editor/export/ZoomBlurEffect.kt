package com.noidvideo.editor.export

import android.graphics.Matrix
import androidx.media3.common.util.Size
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.MatrixTransformation

/**
 * Zoom Blur: zoom radial rápido nos primeiros 280ms.
 * Sensação de "whip" de câmera esportiva. Roda na GPU.
 */
@UnstableApi
class ZoomBlurEffect(
    private val clipStartUs: Long,
    private val durationMs: Long = 280L,
    private val peakScale: Float = 1.35f
) : MatrixTransformation {

    override fun getMatrix(presentationTimeUs: Long): Matrix {
        val elapsedMs = (presentationTimeUs - clipStartUs) / 1000f
        val t = (elapsedMs / durationMs).coerceIn(0f, 1f)
        // Sobe rápido e volta: pico no meio
        val curve = if (t < 0.5f) t * 2f else (1f - t) * 2f
        val scale = 1f + (peakScale - 1f) * curve

        return Matrix().apply {
            setScale(scale, scale)
        }
    }

    override fun configure(inputWidth: Int, inputHeight: Int): Size {
        return Size(inputWidth, inputHeight)
    }
}
