package com.noidvideo.editor.media

import android.content.Context
import android.net.Uri
import com.noidvideo.editor.engine.TimelineClip
import com.noidvideo.editor.engine.TrackKind
import java.util.UUID

object AudioImporter {

    fun toClip(context: Context, uri: Uri): TimelineClip? {
        MediaImporter.persistPermission(context, uri)
        val info = MediaImporter.readInfo(context, uri) ?: return null
        if (info.durationMs <= 0L) return null

        return TimelineClip(
            id = UUID.randomUUID().toString(),
            uri = uri,
            track = TrackKind.AUDIO,
            startInSourceMs = 0L,
            endInSourceMs = info.durationMs,
            timelineStartMs = 0L
        )
    }
}
