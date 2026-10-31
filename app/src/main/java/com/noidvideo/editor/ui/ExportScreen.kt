package com.noidvideo.editor.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.noidvideo.editor.export.Aspect
import com.noidvideo.editor.export.Resolution
import com.noidvideo.editor.ui.theme.NoidBlack
import com.noidvideo.editor.ui.theme.NoidGreen
import com.noidvideo.editor.ui.theme.NoidSurface
import com.noidvideo.editor.ui.theme.NoidSurfaceHigh
import com.noidvideo.editor.ui.theme.NoidText
import com.noidvideo.editor.ui.theme.NoidTextDim

@Composable
fun ExportScreen(
    onBack: () -> Unit,
    editorViewModel: EditorViewModel,
    viewModel: ExportViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NoidBlack)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = NoidText)
            }
            Text(
                text = "Exportar",
                color = NoidText,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(24.dp))

        Text("Resolução", color = NoidTextDim, fontSize = 12.sp)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Resolution.entries.forEach { res ->
                OptionChip(
                    label = res.name.replace("_", " "),
                    selected = state.config.resolution == res,
                    onClick = { viewModel.updateConfig(state.config.copy(resolution = res)) }
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        Text("Formato", color = NoidTextDim, fontSize = 12.sp)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Aspect.entries.forEach { aspect ->
                OptionChip(
                    label = aspect.label,
                    selected = state.config.aspect == aspect,
                    onClick = { viewModel.updateConfig(state.config.copy(aspect = aspect)) }
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        Text("Frame rate", color = NoidTextDim, fontSize = 12.sp)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(30, 60).forEach { fps ->
                OptionChip(
                    label = "${fps}fps",
                    selected = state.config.frameRate == fps,
                    onClick = { viewModel.updateConfig(state.config.copy(frameRate = fps)) }
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        Text("Codec", color = NoidTextDim, fontSize = 12.sp)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OptionChip(
                label = "HEVC (H.265)",
                selected = state.config.useHevc,
                onClick = { viewModel.updateConfig(state.config.copy(useHevc = true)) }
            )
            OptionChip(
                label = "H.264",
                selected = !state.config.useHevc,
                onClick = { viewModel.updateConfig(state.config.copy(useHevc = false)) }
            )
        }

        Spacer(Modifier.height(32.dp))

        if (state.running) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(color = NoidGreen)
                Spacer(Modifier.height(12.dp))
                Text("Exportando…", color = NoidTextDim, fontSize = 13.sp)
            }
        } else {
            Button(
                onClick = { viewModel.start(editorViewModel.state.value.timeline) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NoidGreen,
                    contentColor = NoidBlack
                )
            ) {
                Text("Iniciar exportação", fontWeight = FontWeight.SemiBold)
            }
        }

        if (state.resultUri != null) {
            Spacer(Modifier.height(16.dp))
            Text("Exportação concluída", color = NoidGreen, fontSize = 13.sp)
        }

        if (state.error != null) {
            Spacer(Modifier.height(16.dp))
            Text("Erro: ${state.error}", color = NoidTextDim, fontSize = 12.sp)
        }
    }
}

@Composable
private fun OptionChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = label,
        color = if (selected) NoidBlack else NoidText,
        fontSize = 12.sp,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        modifier = Modifier
            .background(if (selected) NoidGreen else NoidSurfaceHigh)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    )
}
