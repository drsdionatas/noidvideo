package com.noidvideo.editor.export

import android.graphics.Matrix
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.MatrixTransformation
import androidx.media3.effect.ScaleAndRotateTransformation

/**
 * Zoom Punch: começa com zoom 1.12x e assenta em 1.0x.
 * Simula o corte de impacto de highlight reels.
 */
@UnstableApi
object ZoomPunchEffect {

    fun create(): ScaleAndRotateTransformation {
        return ScaleAndRotateTransformation.Builder()
            .setScale(1.12f, 1.12f)
            .build()
    }
}
