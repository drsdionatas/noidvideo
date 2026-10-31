package com.noidvideo.editor.engine

import android.net.Uri

enum class TrackKind { VIDEO, AUDIO }

enum class TransitionKind { NONE, CUT_ON_BEAT, CROSSFADE, FADE_BLACK, LIGHT_LEAK }

enum class FilterPreset { NONE, CINEMATIC, VIVID, WARM, COOL, BW, VINTAGE, SNOW, WATER }

data class TimelineClip(
    val id: String,
    val uri: Uri,
    val track: TrackKind,
    val startInSourceMs: Long,
    val endInSourceMs: Long,
    val timelineStartMs: Long,
    val speed: Float = 1f,
    val volume: Float = 1f,
    val filter: FilterPreset = FilterPreset.NONE,
    val transition: TransitionKind = TransitionKind.NONE
) {
    val trimmedDurationMs: Long
        get() = ((endInSourceMs - startInSourceMs) / speed).toLong()
}
