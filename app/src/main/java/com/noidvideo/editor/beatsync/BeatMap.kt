package com.noidvideo.editor.beatsync

import kotlin.math.abs

/**
 * Resultado da análise de batida de uma faixa de áudio.
 */
data class BeatMap(
    val bpm: Float,
    val beatTimesMs: List<Long>,
    val firstBeatMs: Long,
    val durationMs: Long
) {
    /**
     * Distribui N pontos de corte alinhados aos onsets mais próximos.
     * É isso que faz os clipes trocarem exatamente na batida.
     */
    fun cutPointsFor(clipCount: Int): List<Long> {
        if (beatTimesMs.isEmpty() || clipCount <= 0) return emptyList()
        val usable = durationMs - firstBeatMs
        if (usable <= 0) return emptyList()
        val step = usable / clipCount
        return (0 until clipCount).map { i ->
            nearestOnset(firstBeatMs + (i * step))
        }
    }

    private fun nearestOnset(target: Long): Long =
        beatTimesMs.minByOrNull { abs(it - target) } ?: target
}
