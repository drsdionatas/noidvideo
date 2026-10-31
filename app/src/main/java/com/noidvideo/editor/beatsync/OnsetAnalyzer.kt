package com.noidvideo.editor.beatsync

import kotlin.math.max

/**
 * Análise de onsets — funções puras, sem dependência de Android.
 * Testável com sinais sintéticos.
 */
object OnsetAnalyzer {

    const val FRAME = 1024
    const val HOP = 512

    /**
     * Envelope de onsets por fluxo de energia positivo.
     * Retorna 1f onde há transiente forte, 0f caso contrário.
     */
    fun computeOnsetEnvelope(pcm: FloatArray): FloatArray {
        val frames = (pcm.size - FRAME) / HOP
        if (frames <= 0) return FloatArray(0)

        val energies = FloatArray(frames)
        for (f in 0 until frames) {
            var sum = 0f
            val start = f * HOP
            for (i in 0 until FRAME) {
                val s = pcm[start + i]
                sum += s * s
            }
            energies[f] = sum / FRAME
        }

        val flux = FloatArray(frames)
        for (f in 1 until frames) {
            flux[f] = max(0f, energies[f] - energies[f - 1])
        }

        val window = 16
        val out = FloatArray(frames)
        for (f in flux.indices) {
            val from = max(0, f - window)
            val to = minOf(frames - 1, f + window)
            val sorted = FloatArray(to - from + 1) { flux[from + it] }
            sorted.sort()
            val median = sorted[sorted.size / 2]
            val threshold = median * 2.5f + 1e-6f
            out[f] = if (flux[f] > threshold) 1f else 0f
        }
        return out
    }
}
