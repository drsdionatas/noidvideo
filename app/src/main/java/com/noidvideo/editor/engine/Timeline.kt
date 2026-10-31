package com.noidvideo.editor.engine

data class Timeline(
    val clips: List<TimelineClip> = emptyList(),
    val backgroundColor: Int = 0xFF000000.toInt()
) {
    val videoClips: List<TimelineClip>
        get() = clips.filter { it.track == TrackKind.VIDEO }.sortedBy { it.timelineStartMs }

    val audioClips: List<TimelineClip>
        get() = clips.filter { it.track == TrackKind.AUDIO }.sortedBy { it.timelineStartMs }

    val totalDurationMs: Long
        get() = clips.maxOfOrNull { it.timelineStartMs + it.trimmedDurationMs } ?: 0L
}
