package com.noidvideo.editor.export

/**
 * Perfis de encoder otimizados para Snapdragon 8 Gen 2.
 * HEVC aceita ~60% do bitrate do H.264 para qualidade equivalente.
 */
object EncoderProfiles {

    fun recommendedBitrate(res: Resolution, fps: Int, hevc: Boolean): Int {
        val base = when (res) {
            Resolution.UHD_4K -> 60_000_000
            Resolution.QHD_1440 -> 30_000_000
            Resolution.FHD_1080 -> 16_000_000
            Resolution.HD_720 -> 8_000_000
        }
        val fpsFactor = if (fps >= 60) 1.6f else 1.0f
        val codecFactor = if (hevc) 0.6f else 1.0f
        return (base * fpsFactor * codecFactor).toInt()
    }

    fun dimensions(res: Resolution, aspect: Aspect): Pair<Int, Int> {
        val longSide = maxOf(res.width, res.height)
        val shortSide = minOf(res.width, res.height)
        return when (aspect) {
            Aspect.LANDSCAPE_16_9 -> longSide to shortSide
            Aspect.PORTRAIT_9_16 -> shortSide to longSide
            Aspect.SQUARE_1_1 -> shortSide to shortSide
            Aspect.VERTICAL_4_5 -> (shortSide * 4 / 5) to shortSide
        }
    }
}
