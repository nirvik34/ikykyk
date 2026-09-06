package com.iykyk.collage.processor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.MediaMetadataRetriever
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ExtractedFrame(
    val frameIndex: Int,
    val timestampMs: Long,
    val bitmap: Bitmap
)

class VideoFrameExtractor(private val context: Context) {

    suspend fun extractFrames(
        videoUri: Uri,
        sampleEveryMs: Long = 160L,
        onProgress: (currentMs: Long, totalMs: Long, frameCount: Int) -> Unit
    ): List<ExtractedFrame> = withContext(Dispatchers.IO) {
        val retriever = MediaMetadataRetriever()
        val frameList = mutableListOf<ExtractedFrame>()

        try {
            retriever.setDataSource(context, videoUri)
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val durationMs = durationStr?.toLongOrNull() ?: 30000L

            val rotationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
            val rotation = rotationStr?.toIntOrNull() ?: 0

            var currentMs = 0L
            var frameIndex = 0

            while (currentMs < durationMs) {
                val timeUs = currentMs * 1000L
                val rawBitmap = retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST)

                if (rawBitmap != null) {
                    val orientedBitmap = if (rotation != 0) {
                        rotateBitmap(rawBitmap, rotation.toFloat())
                    } else {
                        rawBitmap
                    }
                    frameList.add(ExtractedFrame(frameIndex, currentMs, orientedBitmap))
                    frameIndex++
                }

                onProgress(currentMs, durationMs, frameList.size)
                currentMs += sampleEveryMs
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                retriever.release()
            } catch (e: Exception) {
                
            }
        }

        frameList
    }

    private fun rotateBitmap(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        val rotated = Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
        if (rotated != source) {
            source.recycle()
        }
        return rotated
    }
}
