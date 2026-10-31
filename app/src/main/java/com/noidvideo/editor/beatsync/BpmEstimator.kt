package com.noidvideo.editor.beatsync

/**
 * Estimativa de BPM por autocorrelação do envelope de onsets.
 * Funções puras, testáveis.
 */
object BpmEstimator {

    const val BPM_MIN = 60f
    const val BPM_MAX = 200f

    fun estimate(onsets: FloatArray, frameRate: Float): Float {
        if (onsets.isEmpty()) return 0f

        val minLag = (frameRate * 60f / BPM_MAX).toInt().coerceAtLeast(1)
        val maxLag = (frameRate * 60f / BPM_MIN).toInt().coerceAtMost(onsets.size - 1)
        if (maxLag <= minLag) return 0f

        var bestLag = minLag
        var bestScore = -1f

        for (lag in minLag..maxLag) {
            var score = 0f
            var count = 0
            for (i in 0 until onsets.size - lag) {
                score += onsets[i] * onsets[i + lag]
                count++
            }
            if (count > 0) {
                val norm = score / count
                if (norm > bestScore) {
                    bestScore = norm
                    bestLag = lag
                }
            }
        }
        return (frameRate * 60f) / bestLag
    }

    /**
     * Filtra onsets pelo período do BPM estimado, gerando batidas estáveis.
     */
    fun pickBeats(onsets: FloatArray, bpm: Float, frameRate: Float): List<Long> {
        if (bpm <= 0f) return emptyList()
        val frameMs = 1000f / frameRate
        val beatIntervalMs = 60_000f / bpm
        val tolerance = beatIntervalMs * 0.35f
        val result = ArrayList<Long>()

        var lastBeat = -beatIntervalMs
        for (i in onsets.indices) {
            if (onsets[i] < 0.5f) continue
            val t = i * frameMs
            if (t - lastBeat >= beatIntervalMs - tolerance) {
                result.add(t.toLong())
                lastBeat = t
            }
        }
        return result
    }
}
