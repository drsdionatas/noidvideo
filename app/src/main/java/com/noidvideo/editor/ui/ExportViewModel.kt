package com.noidvideo.editor.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import com.noidvideo.editor.engine.Timeline
import com.noidvideo.editor.export.ExportConfig
import com.noidvideo.editor.export.VideoExporter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ExportUiState(
    val config: ExportConfig = ExportConfig(),
    val running: Boolean = false,
    val resultUri: Uri? = null,
    val error: String? = null
)

class ExportViewModel(app: Application) : AndroidViewModel(app) {

    private val exporter = VideoExporter(app)

    private val _state = MutableStateFlow(ExportUiState())
    val state: StateFlow<ExportUiState> = _state.asStateFlow()

    fun updateConfig(config: ExportConfig) = _state.update { it.copy(config = config) }

    fun start(timeline: Timeline) {
        _state.update { it.copy(running = true, error = null, resultUri = null) }
        exporter.export(
            timeline = timeline,
            config = _state.value.config,
            onSuccess = { uri -> _state.update { it.copy(running = false, resultUri = uri) } },
            onError = { throwable -> _state.update { it.copy(running = false, error = throwable.message) } }
        )
    }

    fun cancel() {
        exporter.cancel()
        _state.update { it.copy(running = false) }
    }

    override fun onCleared() {
        super.onCleared()
        exporter.cancel()
    }
}
