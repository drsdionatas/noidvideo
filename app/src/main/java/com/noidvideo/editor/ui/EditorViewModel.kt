package com.noidvideo.editor.ui

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.noidvideo.editor.beatsync.BeatDetector
import com.noidvideo.editor.beatsync.BeatMap
import com.noidvideo.editor.engine.FilterPreset
import com.noidvideo.editor.engine.Timeline
import com.noidvideo.editor.engine.TimelineEngine
import com.noidvideo.editor.engine.TransitionKind
import com.noidvideo.editor.filters.FilterPreviewRenderer
import com.noidvideo.editor.media.AudioImporter
import com.noidvideo.editor.media.ThumbnailLoader
import com.noidvideo.editor.media.VideoImporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class EditorUiState(
    val timeline: Timeline = Timeline(),
    val playheadMs: Long = 0L,
    val selectedClipId: String? = null,
    val beatMap: BeatMap? = null,
    val beatSyncBusy: Boolean = false,
    val isExporting: Boolean = false,
    val message: String? = null,
    val filterPreviews: Map<FilterPreset, Bitmap?> = emptyMap(),
    val previewsLoading: Boolean = false
)

class EditorViewModel(app: Application) : AndroidViewModel(app) {

    private val engine = TimelineEngine()

    private val _state = MutableStateFlow(EditorUiState())
    val state: StateFlow<EditorUiState> = _state.asStateFlow()

    fun addVideos(uris: List<Uri>) {
        val clips = VideoImporter.toClips(getApplication(), uris, _state.value.timeline.totalDurationMs)
        _state.update { it.copy(timeline = it.timeline.copy(clips = it.timeline.clips + clips)) }
        loadFilterPreviews()
    }

    fun addAudio(uri: Uri) {
        val clip = AudioImporter.toClip(getApplication(), uri) ?: return
        _state.update { it.copy(timeline = it.timeline.copy(clips = it.timeline.clips + clip)) }
    }

    fun selectClip(id: String?) = _state.update { it.copy(selectedClipId = id) }

    fun setPlayhead(ms: Long) = _state.update { it.copy(playheadMs = ms) }

    /**
     * Gera as miniaturas de todos os presets a partir do primeiro frame
     * do primeiro clipe de vídeo da timeline.
     */
    fun loadFilterPreviews() {
        val firstVideo = _state.value.timeline.videoClips.firstOrNull() ?: return
        if (_state.value.previewsLoading) return

        viewModelScope.launch {
            _state.update { it.copy(previewsLoading = true) }
            val ctx = getApplication<Application>()

            val frame = withContext(Dispatchers.IO) {
                ThumbnailLoader.load(ctx, firstVideo.uri, firstVideo.startInSourceMs + 500L)
            }

            if (frame == null) {
                _state.update { it.copy(previewsLoading = false) }
                return@launch
            }

            val previews = withContext(Dispatchers.Default) {
                FilterPreset.entries.associateWith { preset ->
                    runCatching { FilterPreviewRenderer.render(frame, preset) }.getOrNull()
                }
            }

            _state.update { it.copy(filterPreviews = previews, previewsLoading = false) }
        }
    }

    fun setFilter(preset: FilterPreset) {
        val id = _state.value.selectedClipId ?: return
        _state.update { s ->
            val clips = s.timeline.clips.map { clip ->
                if (clip.id != id) clip else clip.copy(filter = preset)
            }
            s.copy(timeline = s.timeline.copy(clips = clips))
        }
    }

    fun splitAtPlayhead() {
        val current = _state.value
        val target = current.timeline.videoClips.firstOrNull {
            current.playheadMs > it.timelineStartMs &&
                current.playheadMs < it.timelineStartMs + it.trimmedDurationMs
        } ?: return

        val parts = engine.split(target, current.playheadMs)
        if (parts.size < 2) return

        val others = current.timeline.clips.filter { it.id != target.id }
        _state.update { it.copy(timeline = it.timeline.copy(clips = others + parts)) }
    }

    fun cycleSpeed() {
        val id = _state.value.selectedClipId ?: return
        val speeds = listOf(0.5f, 1f, 1.5f, 2f)
        _state.update { s ->
            val clips = s.timeline.clips.map { clip ->
                if (clip.id != id) clip
                else clip.copy(speed = speeds[(speeds.indexOf(clip.speed) + 1) % speeds.size])
            }
            s.copy(timeline = s.timeline.copy(clips = clips))
        }
    }

    fun cycleTransition() {
        val id = _state.value.selectedClipId ?: return
        val kinds = TransitionKind.entries
        _state.update { s ->
            val clips = s.timeline.clips.map { clip ->
                if (clip.id != id) clip
                else clip.copy(transition = kinds[(kinds.indexOf(clip.transition) + 1) % kinds.size])
            }
            s.copy(timeline = s.timeline.copy(clips = clips))
        }
    }

    fun applyBeatSync(audioUri: Uri) {
        viewModelScope.launch {
            _state.update { it.copy(beatSyncBusy = true, message = null) }
            val map = BeatDetector.analyze(getApplication(), audioUri)
            if (map == null) {
                _state.update { it.copy(beatSyncBusy = false, message = "Não foi possível detectar a batida") }
                return@launch
            }
            val synced = engine.applyBeatSync(_state.value.timeline, map)
            _state.update {
                it.copy(
                    timeline = synced,
                    beatMap = map,
                    beatSyncBusy = false,
                    message = "BPM detectado: ${map.bpm.toInt()}"
                )
            }
        }
    }

    fun clearMessage() = _state.update { it.copy(message = null) }
}
