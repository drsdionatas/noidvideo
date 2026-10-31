package com.noidvideo.editor.ui

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.noidvideo.editor.ui.theme.NoidBlack
import com.noidvideo.editor.ui.theme.NoidGreen
import com.noidvideo.editor.ui.theme.NoidSurface
import com.noidvideo.editor.ui.theme.NoidSurfaceHigh
import com.noidvideo.editor.ui.theme.NoidText
import com.noidvideo.editor.ui.theme.NoidTextDim

@Composable
fun EditorScreen(
    onExport: () -> Unit,
    onBack: () -> Unit,
    viewModel: EditorViewModel
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NoidBlack)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = NoidText)
            }
            Text(
                text = "Editor",
                color = NoidText,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = onExport,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NoidGreen,
                    contentColor = NoidBlack
                )
            ) {
                Text("Exportar", fontWeight = FontWeight.SemiBold)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(NoidSurface),
            contentAlignment = Alignment.Center
        ) {
            if (state.beatSyncBusy) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = NoidGreen)
                    Spacer(Modifier.height(12.dp))
                    Text("Analisando a batida…", color = NoidTextDim, fontSize = 13.sp)
                }
            } else if (state.timeline.clips.isEmpty()) {
                Text(
                    text = "Nenhum clipe na timeline",
                    color = NoidTextDim,
                    fontSize = 14.sp
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Timeline",
                        color = NoidTextDim,
                        fontSize = 12.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(state.timeline.clips, key = { it.id }) { clip ->
                            val selected = clip.id == state.selectedClipId
                            Column(
                                modifier = Modifier
                                    .width(96.dp)
                                    .height(72.dp)
                                    .background(if (selected) NoidGreen else NoidSurfaceHigh)
                                    .clickable { viewModel.selectClip(clip.id) }
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = clip.track.name,
                                    color = if (selected) NoidBlack else NoidText,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${clip.trimmedDurationMs / 1000}s",
                                    color = if (selected) NoidBlack else NoidTextDim,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "${clip.speed}x",
                                    color = if (selected) NoidBlack else NoidTextDim,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        if (state.message != null) {
            Text(
                text = state.message!!,
                color = NoidGreen,
                fontSize = 13.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.clearMessage() }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(NoidSurface)
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ToolButton("Dividir") { viewModel.splitAtPlayhead() }
            ToolButton("Veloc.") { viewModel.cycleSpeed() }
            ToolButton("Filtro") { viewModel.cycleFilter() }
            ToolButton("Trans.") { viewModel.cycleTransition() }
        }
    }
}

@Composable
private fun ToolButton(label: String, onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Text(label, color = NoidText, fontSize = 12.sp)
    }
}
