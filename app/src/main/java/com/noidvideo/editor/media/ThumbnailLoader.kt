package com.noidvideo.editor.media

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri

object ThumbnailLoader {

    fun load(context: Context, uri: Uri, atMs: Long = 0L): Bitmap? = runCatching {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, uri)
        val frame = retriever.getFrameAtTime(
            atMs * 1000L,
            MediaMetadataRetriever.OPTION_CLOSEST_SYNC
        )
        retriever.release()
        frame
    }.getOrNull()
}
