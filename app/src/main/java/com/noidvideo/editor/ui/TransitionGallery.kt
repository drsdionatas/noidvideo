package com.noidvideo.editor.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noidvideo.editor.engine.TransitionKind
import com.noidvideo.editor.ui.theme.NoidGreen
import com.noidvideo.editor.ui.theme.NoidSurface
import com.noidvideo.editor.ui.theme.NoidSurfaceHigh
import com.noidvideo.editor.ui.theme.NoidText
import com.noidvideo.editor.ui.theme.NoidTextDim

/**
 * Galeria horizontal de transições com preview animado.
 * Cada card simula visualmente o efeito entre dois clipes.
 */
@Composable
fun TransitionGallery(
    selected: TransitionKind,
    onSelect: (TransitionKind) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "TRANSIÇÕES",
            color = NoidTextDim,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(TransitionKind.entries.toList(), key = { it.name }) { kind ->
                TransitionCard(
                    kind = kind,
                    selected = kind == selected,
                    onClick = { onSelect(kind) }
                )
            }
        }
    }
}

@Composable
private fun TransitionCard(
    kind: TransitionKind,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(84.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(84.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(NoidSurfaceHigh)
                .border(
                    width = if (selected) 2.dp else 1.dp,
                    color = if (selected) NoidGreen else NoidSurface,
                    shape = RoundedCornerShape(10.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            TransitionPreview(kind)
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = transitionLabel(kind),
            color = if (selected) NoidGreen else NoidText,
            fontSize = 9.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
private fun TransitionPreview(kind: TransitionKind) {
    val transition = rememberInfiniteTransition(label = "preview")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "progress"
    )

    val colorA = Color(0xFF2E7D32) // verde
    val colorB = Color(0xFF1565C0) // azul

    when (kind) {
        TransitionKind.NONE -> {
            // Corte seco: metade verde, metade azul
            Box(Modifier.fillMaxSize()) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                0f to colorA,
                                0.5f to colorA,
                                0.5f to colorB,
                                1f to colorB
                            )
                        )
                )
            }
        }

        TransitionKind.GLITCH_RGB -> {
            Box(Modifier.fillMaxSize().background(colorA)) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .alpha(progress)
                        .background(colorB)
                )
            }
        }

        TransitionKind.SHAKE -> {
            Box(Modifier.fillMaxSize().background(colorA)) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .alpha(if (progress < 0.5f) progress * 2f else (1f - progress) * 2f)
                        .background(Color.Black)
                )
            }
        }

        TransitionKind.CUT_ON_BEAT -> {
            // Pisca rápido entre os dois
            Box(
                Modifier
                    .fillMaxSize()
                    .background(if (progress < 0.5f) colorA else colorB)
            )
        }

        TransitionKind.ZOOM_BLUR -> {
            Box(Modifier.fillMaxSize().background(colorA)) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                (progress * 1.2f - 0.2f) to Color.Transparent,
                                (progress * 1.2f) to Color(0xFFFFE082),
                                (progress * 1.2f + 0.2f) to Color.Transparent
                            )
                        )
                )
            }
        }

        TransitionKind.FLASH -> {
            Box(Modifier.fillMaxSize().background(colorA)) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .alpha(if (progress < 0.3f) 1f - progress / 0.3f else 0f)
                        .background(Color.White)
                )
            }
        }
    }
}

fun transitionLabel(kind: TransitionKind): String = when (kind) {
    TransitionKind.NONE -> "Corte"
    TransitionKind.CUT_ON_BEAT -> "No Beat"
    TransitionKind.GLITCH_RGB -> "Glitch"
    TransitionKind.SHAKE -> "Tremor"
    TransitionKind.ZOOM_BLUR -> "Zoom Whip"
    TransitionKind.FLASH -> "Flash"
}
