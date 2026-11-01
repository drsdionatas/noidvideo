package com.noidvideo.editor.export

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.media3.common.Effect
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.effect.Presentation
import androidx.media3.effect.ScaleAndRotateTransformation
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.EditedMediaItemSequence
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import com.noidvideo.editor.engine.Timeline
import com.noidvideo.editor.engine.TimelineClip
import com.noidvideo.editor.engine.TransitionKind
import java.io.File

class VideoExporter(private val context: Context) {

    private var transformer: Transformer? = null

    fun export(
        timeline: Timeline,
        config: ExportConfig,
        onSuccess: (Uri) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        if (timeline.videoClips.isEmpty()) {
            onError(IllegalStateException("Nenhum clipe de vídeo na timeline"))
            return
        }

        val composition = buildComposition(timeline, config)
        val outFile = File(context.cacheDir, "NoidVideo_${System.currentTimeMillis()}.mp4")

        val builder = Transformer.Builder(context)
            .setVideoMimeType(
                if (config.useHevc) MimeTypes.VIDEO_H265 else MimeTypes.VIDEO_H264
            )
            .setAudioMimeType(MimeTypes.AUDIO_AAC)
            .addListener(object : Transformer.Listener {
                override fun onCompleted(composition: Composition, result: ExportResult) {
                    val saved = runCatching { publishToGallery(outFile) }.getOrNull()
                    if (saved != null) onSuccess(saved)
                    else onError(IllegalStateException("Falha ao salvar na galeria"))
                }

                override fun onError(
                    composition: Composition,
                    result: ExportResult,
                    exception: ExportException
                ) {
                    onError(exception)
                }
            })

        transformer = builder.build()
        transformer?.start(composition, outFile.absolutePath)
    }

    /**
     * Monta a composição respeitando a duração total dos VÍDEOS.
     * O áudio é cortado nessa duração para não estender o vídeo.
     */
    private fun buildComposition(timeline: Timeline, config: ExportConfig): Composition {
        val videoDurationMs = timeline.videoClips.sumOf { it.trimmedDurationMs }

        val videoItems = timeline.videoClips.map { clip ->
            toEditedItem(clip, config, isVideo = true)
        }
        val sequences = mutableListOf(EditedMediaItemSequence.Builder(videoItems).build())

        // Áudio: corta cada clipe para caber na duração total do vídeo
        var remaining = videoDurationMs
        val audioItems = mutableListOf<EditedMediaItem>()
        for (clip in timeline.audioClips) {
            if (remaining <= 0) break
            val take = minOf(clip.trimmedDurationMs, remaining)
            audioItems.add(toEditedItem(clip, config, isVideo = false, maxDurationMs = take))
            remaining -= take
        }
        if (audioItems.isNotEmpty()) {
            sequences.add(EditedMediaItemSequence.Builder(audioItems).build())
        }

        return Composition.Builder(sequences).build()
    }

    private fun toEditedItem(
        clip: TimelineClip,
        config: ExportConfig,
        isVideo: Boolean,
        maxDurationMs: Long? = null
    ): EditedMediaItem {
        val endMs = if (maxDurationMs != null) {
            clip.startInSourceMs + maxDurationMs
        } else {
            clip.endInSourceMs
        }

        val mediaItem = MediaItem.Builder()
            .setUri(clip.uri)
            .setClippingConfiguration(
                MediaItem.ClippingConfiguration.Builder()
                    .setStartPositionMs(clip.startInSourceMs)
                    .setEndPositionMs(endMs)
                    .build()
            )
            .build()

        val videoEffects = mutableListOf<Effect>()
        val audioEffects = mutableListOf<AudioProcessor>()

        if (isVideo) {
            // Aplica resolução/proporção escolhida no export
            videoEffects.add(
                Presentation.createForWidthAndHeight(
                    config.resolution.width,
                    config.resolution.height,
                    Presentation.LAYOUT_SCALE_TO_FIT_WITH_CROP
                )
            )
            // Zoom Punch: impacto de corte esportivo
            if (clip.transition == TransitionKind.CUT_ON_BEAT) {
                videoEffects.add(
                    ScaleAndRotateTransformation.Builder()
                        .setScale(1.12f, 1.12f)
                        .build()
                )
            }
        }

        // Volume por clipe (GainProcessor)
        if (!isVideo && clip.volume != 1f) {
            audioEffects.add(VolumeAudioProcessor(clip.volume))
        }

        // Effects(audioProcessors, videoEffects) — áudio PRIMEIRO
        return EditedMediaItem.Builder(mediaItem)
            .setEffects(Effects(audioEffects, videoEffects))
            .build()
    }

    private fun publishToGallery(file: File): Uri? {
        val name = "NoidVideo_${System.currentTimeMillis()}.mp4"
        val values = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, name)
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/NoidVideo")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Video.Media.IS_PENDING, 1)
            }
        }

        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(collection, values) ?: return null

        resolver.openOutputStream(uri)?.use { out ->
            file.inputStream().use { input -> input.copyTo(out) }
        } ?: return null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.clear()
            values.put(MediaStore.Video.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
        }

        file.delete()
        return uri
    }

    fun cancel() {
        transformer?.cancel()
        transformer = null
    }
}
