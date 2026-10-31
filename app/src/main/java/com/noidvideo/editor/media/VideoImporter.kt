package com.noidvideo.editor.media

import android.content.Context
import android.net.Uri
import com.noidvideo.editor.engine.TimelineClip
import com.noidvideo.editor.engine.TrackKind
import java.util.UUID

object VideoImporter {

    fun toClips(
        context: Context,
        uris: List<Uri>,
        startAtMs: Long = 0L
    ): List<TimelineClip> {
        var cursor = startAtMs
        return uris.mapNotNull { uri ->
            MediaImporter.persistPermission(context, uri)
            val info = MediaImporter.readInfo(context, uri) ?: return@mapNotNull null
            if (info.durationMs <= 0L) return@mapNotNull null

            val clip = TimelineClip(
                id = UUID.randomUUID().toString(),
                uri = uri,
                track = TrackKind.VIDEO,
                startInSourceMs = 0L,
                endInSourceMs = info.durationMs,
                timelineStartMs = cursor
            )
            cursor += clip.trimmedDurationMs
            clip
        }
    }
}
