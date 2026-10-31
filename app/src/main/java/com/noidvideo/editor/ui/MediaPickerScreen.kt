package com.noidvideo.editor.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.noidvideo.editor.ui.theme.NoidBlack
import com.noidvideo.editor.ui.theme.NoidGreen
import com.noidvideo.editor.ui.theme.NoidText
import com.noidvideo.editor.ui.theme.NoidTextDim

@Composable
fun MediaPickerScreen(
    onMediaSelected: () -> Unit,
    onBack: () -> Unit,
    viewModel: EditorViewModel = viewModel()
) {
    val videoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            viewModel.addVideos(uris)
            onMediaSelected()
        }
    }

    val audioPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.addAudio(uri)
            viewModel.applyBeatSync(uri)
            onMediaSelected()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NoidBlack)
            .padding(20.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Novo projeto",
            color = NoidText,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Escolha os vídeos e a música. O NoidVideo corta tudo na batida.",
            color = NoidTextDim,
            fontSize = 14.sp
        )

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = { videoPicker.launch("video/*") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = NoidGreen,
                contentColor = NoidBlack
            )
        ) {
            Text("Selecionar vídeos", fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = { audioPicker.launch("audio/*") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = NoidGreen,
                contentColor = NoidBlack
            )
        ) {
            Text("Selecionar música", fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(24.dp))

        TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Voltar", color = NoidTextDim)
        }
    }
}
