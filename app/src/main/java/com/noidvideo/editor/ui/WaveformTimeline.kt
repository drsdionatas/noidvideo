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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import com.noidvideo.editor.ui.theme.NoidGreen
import com.noidvideo.editor.ui.theme.NoidSurface
import com.noidvideo.editor.ui.theme.NoidSurfaceHigh
import com.noidvideo.editor.ui.theme.NoidText
import com.noidvideo.editor.ui.theme.NoidTextDim

/**
 * Timeline real com waveform, marcadores de beat e playhead arrastável.
 */
@Composable
fun WaveformTimeline(
    waveform: FloatArray,
    beatTimesMs: List<Long>,
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
                .height(72.dp)
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
            Canvas(modifier = Modifier.fillMaxWidth().height(72.dp)) {
                val w = size.width
                val h = size.height
                val midY = h / 2f

                // Waveform
                if (waveform.isNotEmpty()) {
                    val barW = w / waveform.size
                    for (i in waveform.indices) {
                        val amp = waveform[i]
                        val barH = (amp * h * 0.85f).coerceAtLeast(1f)
                        drawRect(
                            color = NoidGreen.copy(alpha = 0.55f),
                            topLeft = Offset(i * barW, midY - barH / 2f),
                            size = Size(barW * 0.7f, barH)
                        )
                    }
                }

                // Marcadores de beat
                if (totalDurationMs > 0) {
                    for (beat in beatTimesMs) {
                        val x = (beat.toFloat() / totalDurationMs) * w
                        drawLine(
                            color = Color(0xFFFFD54F).copy(alpha = 0.7f),
                            start = Offset(x, 0f),
                            end = Offset(x, h),
                            strokeWidth = 1.5f
                        )
                    }
                }

                // Playhead
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

fun formatTime(ms: Long): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return "%d:%02d".format(min, sec)
}
