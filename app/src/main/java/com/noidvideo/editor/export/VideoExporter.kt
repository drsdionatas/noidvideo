package com.noidvideo.editor.export

import android.content.Context
import android.net.Uri
import androidx.media3.common.MimeTypes
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import com.noidvideo.editor.engine.Timeline
import com.noidvideo.editor.engine.TimelineEngine
import java.io.File

class VideoExporter(private val context: Context) {

    private var transformer: Transformer? = null

    fun export(
        timeline: Timeline,
        config: ExportConfig,
        onSuccess: (Uri) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val composition = TimelineEngine().buildComposition(timeline)
        val outFile = File(context.cacheDir, "NoidVideo_${System.currentTimeMillis()}.mp4")

        val builder = Transformer.Builder(context)
            .setVideoMimeType(
                if (config.useHevc) MimeTypes.VIDEO_H265 else MimeTypes.VIDEO_H264
            )
            .setAudioMimeType(MimeTypes.AUDIO_AAC)
            .addListener(object : Transformer.Listener {
                override fun onCompleted(composition: androidx.media3.transformer.Composition, result: ExportResult) {
                    onSuccess(Uri.fromFile(outFile))
                }

                override fun onError(
                    composition: androidx.media3.transformer.Composition,
                    result: ExportResult,
                    exception: ExportException
                ) {
                    onError(exception)
                }
            })

        transformer = builder.build()
        transformer?.start(composition, outFile.absolutePath)
    }

    fun cancel() {
        transformer?.cancel()
        transformer = null
    }
}
