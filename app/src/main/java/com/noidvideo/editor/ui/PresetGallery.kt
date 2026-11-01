package com.noidvideo.editor.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noidvideo.editor.engine.FilterPreset
import com.noidvideo.editor.ui.theme.NoidBlack
import com.noidvideo.editor.ui.theme.NoidGreen
import com.noidvideo.editor.ui.theme.NoidSurface
import com.noidvideo.editor.ui.theme.NoidSurfaceHigh
import com.noidvideo.editor.ui.theme.NoidText
import com.noidvideo.editor.ui.theme.NoidTextDim

/**
 * Galeria horizontal de presets de cor com preview real do frame.
 * Cada card mostra o mesmo frame do vídeo com o filtro aplicado.
 */
@Composable
fun PresetGallery(
    previews: Map<FilterPreset, Bitmap?>,
    selected: FilterPreset,
    onSelect: (FilterPreset) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "FILTROS",
            color = NoidTextDim,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(FilterPreset.entries.toList(), key = { it.name }) { preset ->
                PresetCard(
                    preset = preset,
                    preview = previews[preset],
                    selected = preset == selected,
                    onClick = { onSelect(preset) }
                )
            }
        }
    }
}

@Composable
private fun PresetCard(
    preset: FilterPreset,
    preview: Bitmap?,
    selected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (selected) NoidGreen else Color.Transparent
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
            if (preview != null) {
                Image(
                    bitmap = preview.asImageBitmap(),
                    contentDescription = presetLabel(preset),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(84.dp)
                )
            } else {
                Text(
                    text = "…",
                    color = NoidTextDim,
                    fontSize = 18.sp
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = presetLabel(preset),
            color = if (selected) NoidGreen else NoidText,
            fontSize = 9.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

fun presetLabel(preset: FilterPreset): String = when (preset) {
    FilterPreset.NONE -> "Original"
    FilterPreset.CINEMATIC -> "Cinematic"
    FilterPreset.VIVID -> "Vivid"
    FilterPreset.WARM -> "Warm"
    FilterPreset.COOL -> "Cool"
    FilterPreset.BW -> "P&B"
    FilterPreset.VINTAGE -> "Vintage"
    FilterPreset.SNOW -> "Snow"
    FilterPreset.WATER -> "Water"
}
