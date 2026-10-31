package com.noidvideo.editor.engine

import androidx.media3.common.MediaItem
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.EditedMediaItemSequence
import androidx.media3.transformer.Effects
import com.noidvideo.editor.beatsync.BeatMap

/**
 * Converte a timeline em Composition do Media3.
 * Vídeo = sequência principal. Áudio do usuário = sequência paralela.
 */
class TimelineEngine {

    fun buildComposition(timeline: Timeline): Composition {
        val videoItems = timeline.videoClips.map { toEditedItem(it) }
        val sequences = mutableListOf(EditedMediaItemSequence.Builder(videoItems).build())

        val audioItems = timeline.audioClips.map { toEditedItem(it) }
        if (audioItems.isNotEmpty()) {
            sequences.add(EditedMediaItemSequence.Builder(audioItems).build())
        }

        return Composition.Builder(sequences)
            .setBackgroundColor(timeline.backgroundColor)
            .build()
    }

    private fun toEditedItem(clip: TimelineClip): EditedMediaItem {
        val mediaItem = MediaItem.Builder()
            .setUri(clip.uri)
            .setClippingConfiguration(
                MediaItem.ClippingConfiguration.Builder()
                    .setStartPositionMs(clip.startInSourceMs)
                    .setEndPositionMs(clip.endInSourceMs)
                    .build()
            )
            .build()

        return EditedMediaItem.Builder(mediaItem)
            .setSpeed(clip.speed)
            .setEffects(Effects(emptyList(), emptyList()))
            .build()
    }

    fun split(clip: TimelineClip, atTimelineMs: Long): List<TimelineClip> {
        val offset = atTimelineMs - clip.timelineStartMs
        if (offset <= 0 || offset >= clip.trimmedDurationMs) return listOf(clip)
        val srcSplit = clip.startInSourceMs + (offset * clip.speed).toLong()
        return listOf(
            clip.copy(id = "${clip.id}_a", endInSourceMs = srcSplit),
            clip.copy(
                id = "${clip.id}_b",
                startInSourceMs = srcSplit,
                timelineStartMs = clip.timelineStartMs + offset
            )
        )
    }

    fun reorder(timeline: Timeline, from: Int, to: Int): Timeline {
        val list = timeline.clips.toMutableList()
        if (from !in list.indices || to !in list.indices) return timeline
        list.add(to, list.removeAt(from))
        return timeline.copy(clips = list)
    }

    /**
     * Reposiciona e corta os clipes de vídeo nos pontos de batida.
     * É o coração da edição automática.
     */
    fun applyBeatSync(timeline: Timeline, beatMap: BeatMap): Timeline {
        val videos = timeline.videoClips
        if (videos.isEmpty() || beatMap.beatTimesMs.isEmpty()) return timeline

        val cuts = beatMap.cutPointsFor(videos.size)
        if (cuts.isEmpty()) return timeline

        val rebuilt = videos.mapIndexed { index, clip ->
            val start = cuts.getOrElse(index) { clip.timelineStartMs }
            val end = cuts.getOrElse(index + 1) { beatMap.durationMs }
            val segmentMs = (end - start).coerceAtLeast(200L)
            val sourceSpan = (segmentMs * clip.speed).toLong()
            val maxSpan = clip.endInSourceMs - clip.startInSourceMs
            val finalSpan = sourceSpan.coerceAtMost(maxSpan)

            clip.copy(
                timelineStartMs = start,
                endInSourceMs = clip.startInSourceMs + finalSpan,
                transition = TransitionKind.CUT_ON_BEAT
            )
        }

        val audios = timeline.audioClips
        return timeline.copy(clips = rebuilt + audios)
    }
}
