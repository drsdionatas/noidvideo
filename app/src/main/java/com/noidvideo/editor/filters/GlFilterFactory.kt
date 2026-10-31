package com.noidvideo.editor.filters

import com.noidvideo.editor.engine.FilterPreset

/**
 * Mapeia FilterPreset para o shader GLSL correspondente.
 */
object GlFilterFactory {

    fun shaderFor(preset: FilterPreset): String? = when (preset) {
        FilterPreset.NONE -> null
        FilterPreset.CINEMATIC -> ColorFilterShaders.CINEMATIC
        FilterPreset.VIVID -> ColorFilterShaders.VIVID
        FilterPreset.WARM -> ColorFilterShaders.WARM
        FilterPreset.COOL -> ColorFilterShaders.COOL
        FilterPreset.BW -> ColorFilterShaders.BW
        FilterPreset.VINTAGE -> ColorFilterShaders.VINTAGE
        FilterPreset.SNOW -> ColorFilterShaders.SNOW
        FilterPreset.WATER -> ColorFilterShaders.WATER
    }
}
