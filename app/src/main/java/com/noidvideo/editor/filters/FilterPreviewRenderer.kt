package com.noidvideo.editor.filters

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import com.noidvideo.editor.engine.FilterPreset

/**
 * Aplica o filtro num Bitmap em CPU, gerando a miniatura de preview.
 * Cada preset replica em ColorMatrix o mesmo look do shader GLSL correspondente.
 */
object FilterPreviewRenderer {

    fun render(source: Bitmap, preset: FilterPreset, sizePx: Int = 220): Bitmap {
        val scaled = scaleToSquare(source, sizePx)
        if (preset == FilterPreset.NONE) return scaled

        val out = Bitmap.createBitmap(scaled.width, scaled.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.colorFilter = ColorMatrixColorFilter(matrixFor(preset))
        canvas.drawBitmap(scaled, 0f, 0f, paint)
        return out
    }

    private fun scaleToSquare(src: Bitmap, size: Int): Bitmap {
        val side = minOf(src.width, src.height)
        val x = (src.width - side) / 2
        val y = (src.height - side) / 2
        val cropped = Bitmap.createBitmap(src, x, y, side, side)
        return Bitmap.createScaledBitmap(cropped, size, size, true)
    }

    private fun matrixFor(preset: FilterPreset): ColorMatrix = when (preset) {
        FilterPreset.NONE -> ColorMatrix()

        // (rgb - 0.5) * 1.15 + 0.5  + leve dessaturação
        FilterPreset.CINEMATIC -> ColorMatrix(floatArrayOf(
            1.15f, 0f, 0f, 0f, -0.075f,
            0f, 1.15f, 0f, 0f, -0.075f,
            0f, 0f, 1.15f, 0f, -0.075f,
            0f, 0f, 0f, 1f, 0f
        )).also { it.setSaturation(0.92f) }

        // saturação 1.45 + contraste 1.08
        FilterPreset.VIVID -> ColorMatrix().apply {
            setSaturation(1.45f)
            postConcat(ColorMatrix(floatArrayOf(
                1.08f, 0f, 0f, 0f, -0.04f,
                0f, 1.08f, 0f, 0f, -0.04f,
                0f, 0f, 1.08f, 0f, -0.04f,
                0f, 0f, 0f, 1f, 0f
            )))
        }

        // R*1.12, G*1.03, B*0.92
        FilterPreset.WARM -> ColorMatrix(floatArrayOf(
            1.12f, 0f, 0f, 0f, 0f,
            0f, 1.03f, 0f, 0f, 0f,
            0f, 0f, 0.92f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        ))

        // R*0.92, G*1.02, B*1.14
        FilterPreset.COOL -> ColorMatrix(floatArrayOf(
            0.92f, 0f, 0f, 0f, 0f,
            0f, 1.02f, 0f, 0f, 0f,
            0f, 0f, 1.14f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        ))

        // preto e branco com contraste
        FilterPreset.BW -> ColorMatrix().apply {
            setSaturation(0f)
            postConcat(ColorMatrix(floatArrayOf(
                1.18f, 0f, 0f, 0f, -0.09f,
                0f, 1.18f, 0f, 0f, -0.09f,
                0f, 0f, 1.18f, 0f, -0.09f,
                0f, 0f, 0f, 1f, 0f
            )))
        }

        // sépia 65%
        FilterPreset.VINTAGE -> ColorMatrix(floatArrayOf(
            0.393f * 0.65f + 0.35f, 0.769f * 0.65f, 0.189f * 0.65f, 0f, 0f,
            0.349f * 0.65f, 0.686f * 0.65f + 0.35f, 0.168f * 0.65f, 0f, 0f,
            0.272f * 0.65f, 0.534f * 0.65f, 0.131f * 0.65f + 0.35f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        ))

        // branqueamento dos highlights
        FilterPreset.SNOW -> ColorMatrix(floatArrayOf(
            1.05f, 0f, 0f, 0f, 0.06f,
            0f, 1.05f, 0f, 0f, 0.08f,
            0f, 0f, 1.05f, 0f, 0.12f,
            0f, 0f, 0f, 1f, 0f
        ))

        // azul/verde água + saturação
        FilterPreset.WATER -> ColorMatrix(floatArrayOf(
            0.94f, 0f, 0f, 0f, 0f,
            0f, 1.08f, 0f, 0f, 0f,
            0f, 0f, 1.18f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        )).also { it.setSaturation(1.2f) }
    }
}
