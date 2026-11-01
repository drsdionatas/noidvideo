package com.noidvideo.editor.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noidvideo.editor.engine.TimelineClip
import com.noidvideo.editor.ui.theme.NoidGreen
import com.noidvideo.editor.ui.theme.NoidSurfaceHigh
import com.noidvideo.editor.ui.theme.NoidTextDim

/**
 * Timeline real: régua de tempo, blocos de clipe, waveform, beats e playhead.
 */
@Composable
fun WaveformTimeline(
    waveform: FloatArray,
    beatTimesMs: List<Long>,
    clips: List<TimelineClip>,
    totalDurationMs: Long,
    playheadMs: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TIMELINE",
                color = NoidTextDim,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "${formatTime(playheadMs)} / ${formatTime(totalDurationMs)}",
                color = NoidGreen,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(NoidSurfaceHigh)
                .pointerInput(totalDurationMs) {
                    detectTapGestures { offset ->
                        val ratio = (offset.x / size.width).coerceIn(0f, 1f)
                        onSeek((ratio * totalDurationMs).toLong())
                    }
                }
                .pointerInput(totalDurationMs) {
                    detectDragGestures { change, _ ->
                        val ratio = (change.position.x / size.width).coerceIn(0f, 1f)
                        onSeek((ratio * totalDurationMs).toLong())
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(110.dp)) {
                val w = size.width
                val h = size.height
                val rulerH = 16f
                val clipH = 22f
                val waveTop = rulerH + clipH + 4f
                val waveH = h - waveTop
                val midY = waveTop + waveH / 2f

                // --- Régua de tempo ---
                if (totalDurationMs > 0) {
                    val stepMs = pickRulerStep(totalDurationMs)
                    var t = 0L
                    while (t <= totalDurationMs) {
                        val x = (t.toFloat() / totalDurationMs) * w
                        drawLine(
                            color = NoidTextDim.copy(alpha = 0.4f),
                            start = Offset(x, 0f),
                            end = Offset(x, rulerH),
                            strokeWidth = 1f
                        )
                        t += stepMs
                    }
                }

                // --- Blocos de clipe ---
                if (totalDurationMs > 0 && clips.isNotEmpty()) {
                    val palette = listOf(
                        Color(0xFF2E7D32), Color(0xFF1565C0), Color(0xFF6A1B9A),
                        Color(0xFFC62828), Color(0xFFEF6C00), Color(0xFF00838F)
                    )
                    var acc = 0L
                    clips.forEachIndexed { idx, clip ->
                        val startX = (acc.toFloat() / totalDurationMs) * w
                        val endX = ((acc + clip.trimmedDurationMs).toFloat() / totalDurationMs) * w
                        val blockW = (endX - startX).coerceAtLeast(1f)
                        drawRect(
                            color = palette[idx % palette.size].copy(alpha = 0.85f),
                            topLeft = Offset(startX, rulerH + 2f),
                            size = Size(blockW - 1f, clipH)
                        )
                        // Separador entre clipes
                        drawLine(
                            color = Color.Black.copy(alpha = 0.6f),
                            start = Offset(endX, rulerH + 2f),
                            end = Offset(endX, rulerH + 2f + clipH),
                            strokeWidth = 1.5f
                        )
                        acc += clip.trimmedDurationMs
                    }
                }

                // --- Waveform espelhada ---
                if (waveform.isNotEmpty()) {
                    val barW = w / waveform.size
                    for (i in waveform.indices) {
                        val amp = waveform[i]
                        val barH = (amp * waveH * 0.45f).coerceAtLeast(0.5f)
                        drawRect(
                            color = NoidGreen.copy(alpha = 0.75f),
                            topLeft = Offset(i * barW, midY - barH),
                            size = Size(barW * 0.65f, barH)
                        )
                        drawRect(
                            color = NoidGreen.copy(alpha = 0.45f),
                            topLeft = Offset(i * barW, midY),
                            size = Size(barW * 0.65f, barH)
                        )
                    }
                }

                // --- Beats ---
                if (totalDurationMs > 0) {
                    for (beat in beatTimesMs) {
                        val x = (beat.toFloat() / totalDurationMs) * w
                        drawLine(
                            color = Color(0xFFFFD54F).copy(alpha = 0.55f),
                            start = Offset(x, waveTop),
                            end = Offset(x, h),
                            strokeWidth = 1f
                        )
                    }
                }

                // --- Playhead ---
                if (totalDurationMs > 0) {
                    val px = (playheadMs.toFloat() / totalDurationMs) * w
                    drawLine(
                        color = Color.White,
                        start = Offset(px, 0f),
                        end = Offset(px, h),
                        strokeWidth = 2.5f
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 5f,
                        center = Offset(px, 0f)
                    )
                }
            }
        }
    }
}

private fun pickRulerStep(totalMs: Long): Long {
    val sec = totalMs / 1000
    return when {
        sec <= 10 -> 1000L
        sec <= 30 -> 5000L
        sec <= 60 -> 10000L
        else -> 30000L
    }
}

fun formatTime(ms: Long): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return "%d:%02d".format(min, sec)
}
