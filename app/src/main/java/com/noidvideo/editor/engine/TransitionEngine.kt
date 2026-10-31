package com.noidvideo.editor.engine

/**
 * Duração de transição derivada do BPM.
 * Meia batida para crossfade, uma batida para fade completo.
 */
object TransitionEngine {

    fun crossfadeDurationMs(bpm: Float): Long {
        if (bpm <= 0f) return 300L
        return ((60_000f / bpm) / 2f).toLong().coerceIn(150L, 1500L)
    }

    fun fadeDurationMs(bpm: Float): Long {
        if (bpm <= 0f) return 600L
        return (60_000f / bpm).toLong().coerceIn(300L, 3000L)
    }

    fun durationFor(kind: TransitionKind, bpm: Float): Long = when (kind) {
        TransitionKind.NONE, TransitionKind.CUT_ON_BEAT -> 0L
        TransitionKind.CROSSFADE -> crossfadeDurationMs(bpm)
        TransitionKind.FADE_BLACK -> fadeDurationMs(bpm)
        TransitionKind.LIGHT_LEAK -> crossfadeDurationMs(bpm) * 2
    }
}
