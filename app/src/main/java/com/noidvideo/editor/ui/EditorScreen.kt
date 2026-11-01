package com.noidvideo.editor.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.noidvideo.editor.engine.TimelineClip
import com.noidvideo.editor.engine.TrackKind
import com.noidvideo.editor.ui.theme.NoidBlack
import com.noidvideo.editor.ui.theme.NoidGreen
import com.noidvideo.editor.ui.theme.NoidSurface
import com.noidvideo.editor.ui.theme.NoidSurfaceHigh
import com.noidvideo.editor.ui.theme.NoidText
import com.noidvideo.editor.ui.theme.NoidTextDim

@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    onExport: () -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showFilters by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NoidBlack)
            .padding(horizontal = 12.dp)
    ) {
        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = NoidText)
            }
            Text(
                text = "Editor",
                color = NoidText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = onExport,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NoidGreen,
                    contentColor = NoidBlack
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Exportar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(NoidSurface, RoundedCornerShape(12.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                Text("Timeline", color = NoidTextDim, fontSize = 11.sp)
                Spacer(Modifier.height(8.dp))

                TrackRow(
                    label = "VÍDEO",
                    clips = state.timeline.videoClips,
                    selectedId = state.selectedClipId,
                    onSelect = { viewModel.selectClip(it) }
                )

                Spacer(Modifier.height(10.dp))

                TrackRow(
                    label = "ÁUDIO",
                    clips = state.timeline.audioClips,
                    selectedId = state.selectedClipId,
                    onSelect = { viewModel.selectClip(it) }
                )

                Spacer(Modifier.height(12.dp))

                if (state.beatSyncBusy) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            color = NoidGreen,
                            modifier = Modifier.width(16.dp).height(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Analisando batida...", color = NoidTextDim, fontSize = 11.sp)
                    }
                } else if (state.message != null) {
                    Text(state.message!!, color = NoidGreen, fontSize = 11.sp)
                }

                Spacer(Modifier.weight(1f))

                val selected = state.timeline.clips.firstOrNull { it.id == state.selectedClipId }
                if (selected != null) {
                    Text(
                        text = "Selecionado: ${selected.track.name} - ${selected.speed}x - ${presetLabel(selected.filter)} - ${selected.transition.name}",
                        color = NoidTextDim,
                        fontSize = 10.sp
                    )
                } else {
                    Text(
                        text = "Toque em um clipe para selecionar",
                        color = NoidTextDim,
                        fontSize = 10.sp
                    )
                }
                Spacer(Modifier.height(6.dp))
            }
        }

        Spacer(Modifier.height(10.dp))

        if (showFilters) {
            val selectedClip = state.timeline.clips.firstOrNull { it.id == state.selectedClipId }
            if (selectedClip == null) {
                Text(
                    text = "Selecione um clipe primeiro",
                    color = NoidTextDim,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                PresetGallery(
                    previews = state.filterPreviews,
                    selected = selectedClip.filter,
                    onSelect = { viewModel.setFilter(it) }
                )
                Spacer(Modifier.height(8.dp))
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ToolButton("Dividir") { viewModel.splitAtPlayhead() }
            ToolButton("Veloc.") { viewModel.cycleSpeed() }
            ToolButton(
                label = "Filtro",
                active = showFilters
            ) { showFilters = !showFilters }
            ToolButton("Trans.") { viewModel.cycleTransition() }
        }

        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun TrackRow(
    label: String,
    clips: List<TimelineClip>,
    selectedId: String?,
    onSelect: (String) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            color = NoidTextDim,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(44.dp)
        )
        if (clips.isEmpty()) {
            Text("-", color = NoidTextDim, fontSize = 11.sp)
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(clips, key = { it.id }) { clip ->
                    ClipChip(
                        clip = clip,
                        selected = clip.id == selectedId,
                        onClick = { onSelect(clip.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ClipChip(clip: TimelineClip, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) NoidGreen else NoidSurfaceHigh
    val fg = if (selected) NoidBlack else NoidText
    Column(
        modifier = Modifier
            .background(bg, RoundedCornerShape(6.dp))
            .border(
                width = if (selected) 2.dp else 0.dp,
                color = if (selected) NoidGreen else Color.Transparent,
                shape = RoundedCornerShape(6.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Text(
            text = if (clip.track == TrackKind.VIDEO) "VIDEO" else "AUDIO",
            color = fg,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "${clip.trimmedDurationMs / 1000}s",
            color = fg,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun ToolButton(
    label: String,
    active: Boolean = false,
    onClick: () -> Unit
) {
    TextButton(onClick = onClick) {
        Text(
            text = label,
            color = if (active) NoidGreen else NoidText,
            fontSize = 12.sp,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
        )
    }
}
