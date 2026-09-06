package com.iykyk.collage.processor

import android.graphics.Bitmap
import android.util.Log
import com.iykyk.collage.config.FaceConfig
import com.iykyk.collage.model.AppearanceTrack
import com.iykyk.collage.model.FaceFrameInfo
import com.iykyk.collage.model.PersonIdentity
import com.iykyk.collage.util.BitmapUtils

class RepresentativeShotSelector {

    companion object {
        private const val TAG = "RepresentativeShotSelector"
    }

    fun selectRepresentativeShot(
        personId: Int,
        personName: String,
        appearances: List<AppearanceTrack>,
        allFrames: List<FaceFrameInfo>
    ): PersonIdentity {
        val identityFrameIndices = appearances.flatMap { it.frames.map { it.frameIndex } }.toSet()

        val identityFrames = allFrames.filter { frame ->
            frame.frameIndex in identityFrameIndices &&
            frame.overallQualityScore >= FaceConfig.minQuality &&
            frame.embedding != null &&
            frame.faceWidth >= FaceConfig.minFaceSize &&
            frame.faceHeight >= FaceConfig.minFaceSize
        }

        if (identityFrames.isEmpty()) {
            val fallbackFrames = appearances.flatMap { it.frames }.filter { it.overallQualityScore > 0 }
            if (fallbackFrames.isEmpty()) {
                throw IllegalStateException("No valid frames for $personName")
            }
            val bestFrame = fallbackFrames.maxByOrNull { it.overallQualityScore } ?: fallbackFrames.first()
            val sourceBitmap = bestFrame.frameBitmap ?: throw IllegalStateException("Frame bitmap null")
            val crop = BitmapUtils.cropFacePortrait(sourceBitmap, bestFrame.boundingBox, FaceConfig.cropPadding)
            return PersonIdentity(
                id = personId, name = personName, appearances = appearances,
                bestShot = bestFrame, croppedFaceBitmap = crop ?: sourceBitmap, quality = 0f
            )
        }

        val bestFrame = identityFrames.maxByOrNull { BitmapUtils.computeFaceQualityScore(it) }!!

        val score = BitmapUtils.computeFaceQualityScore(bestFrame)
        Log.i(TAG, "Selected best shot for $personName (ID $personId): frameIndex=${bestFrame.frameIndex}, quality=$score")

        val sourceBitmap = bestFrame.frameBitmap
            ?: throw IllegalStateException("Frame bitmap is null for representative shot")

        val portraitCrop = BitmapUtils.cropFacePortrait(
            source = sourceBitmap,
            faceRect = bestFrame.boundingBox,
            paddingFraction = FaceConfig.cropPadding
        )

        val alternateImages = selectAlternateImages(
            allFrames = allFrames,
            identityFrameIndices = identityFrameIndices,
            bestFrame = bestFrame,
            sourceBitmap = sourceBitmap
        )

        return PersonIdentity(
            id = personId,
            name = personName,
            appearances = appearances,
            bestShot = bestFrame,
            croppedFaceBitmap = portraitCrop,
            alternateImages = alternateImages,
            quality = score
        )
    }

    private fun selectAlternateImages(
        allFrames: List<FaceFrameInfo>,
        identityFrameIndices: Set<Int>,
        bestFrame: FaceFrameInfo,
        sourceBitmap: Bitmap
    ): List<Bitmap> {
        val alternates = mutableListOf<Bitmap>()
        val usedFrames = mutableListOf<FaceFrameInfo>()

        val identityFrames = allFrames.filter {
            it.frameIndex in identityFrameIndices && it.frameIndex != bestFrame.frameIndex
        }

        val sortedByQuality = identityFrames
            .sortedByDescending { BitmapUtils.computeFaceQualityScore(it) }

        for (frame in sortedByQuality) {
            if (alternates.size >= 3) break

            val isDuplicate = usedFrames.any { prev ->
                cosineSimilarity(frame.embedding ?: floatArrayOf(), prev.embedding ?: floatArrayOf()) > FaceConfig.duplicateSimilarityThreshold
            }
            if (isDuplicate) continue

            val temporalGap = kotlin.math.abs(frame.timestampMs - bestFrame.timestampMs)
            if (temporalGap < FaceConfig.appearanceGapMs) continue

            val crop = BitmapUtils.cropFacePortrait(
                source = sourceBitmap,
                faceRect = frame.boundingBox,
                paddingFraction = FaceConfig.cropPadding
            )
            if (!crop.isRecycled) {
                alternates.add(crop)
                usedFrames.add(frame)
            }
        }

        return alternates
    }

    private fun cosineSimilarity(emb1: FloatArray, emb2: FloatArray): Float {
        if (emb1.isEmpty() || emb2.isEmpty()) return 0f
        var dot = 0.0f
        var norm1 = 0.0f
        var norm2 = 0.0f
        val len = minOf(emb1.size, emb2.size)
        for (i in 0 until len) {
            dot += emb1[i] * emb2[i]
            norm1 += emb1[i] * emb1[i]
            norm2 += emb2[i] * emb2[i]
        }
        val denom = kotlin.math.sqrt(norm1) * kotlin.math.sqrt(norm2)
        if (denom < 1e-8f) return 0f
        return (dot / denom).coerceIn(-1.0f, 1.0f)
    }
}
