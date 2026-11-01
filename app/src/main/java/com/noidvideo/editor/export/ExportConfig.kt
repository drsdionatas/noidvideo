package com.noidvideo.editor.export

enum class Resolution(val width: Int, val height: Int) {
    UHD_4K(3840, 2160),
    QHD_1440(2560, 1440),
    FHD_1080(1920, 1080),
    HD_720(1280, 720)
}

enum class Aspect(val label: String) {
    PORTRAIT_9_16("9:16"),
    LANDSCAPE_16_9("16:9"),
    SQUARE_1_1("1:1"),
    VERTICAL_4_5("4:5")
}

data class ExportConfig(
    val resolution: Resolution = Resolution.UHD_4K,
    val aspect: Aspect = Aspect.LANDSCAPE_16_9,
    val frameRate: Int = 60,
    val videoBitrate: Int = 100_000_000,
    val audioBitrate: Int = 320_000,
    val useHevc: Boolean = true
) {
    /** Largura real considerando o aspect escolhido. */
    val outWidth: Int
        get() = when (aspect) {
            Aspect.PORTRAIT_9_16 -> (resolution.height * 9 / 16)
            Aspect.LANDSCAPE_16_9 -> resolution.width
            Aspect.SQUARE_1_1 -> resolution.height
            Aspect.VERTICAL_4_5 -> (resolution.height * 4 / 5)
        }

    /** Altura real considerando o aspect escolhido. */
    val outHeight: Int
        get() = when (aspect) {
            Aspect.PORTRAIT_9_16 -> resolution.height
            Aspect.LANDSCAPE_16_9 -> resolution.height
            Aspect.SQUARE_1_1 -> resolution.height
            Aspect.VERTICAL_4_5 -> resolution.height
        }
}
