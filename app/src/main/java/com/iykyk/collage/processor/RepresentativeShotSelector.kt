package com.iykyk.collage.processor

import android.graphics.Bitmap
import android.util.Log
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
        appearances: List<AppearanceTrack>
    ): PersonIdentity {
        val allFrames = appearances.flatMap { it.frames }

        val bestFrame = allFrames.maxByOrNull { calculateDetailedScore(it) }
            ?: appearances.first().frames.first()

        val score = calculateDetailedScore(bestFrame)
        Log.i(
            TAG,
            "Selected best shot for $personName (ID $personId): frameIndex=${bestFrame.frameIndex}, timestampMs=${bestFrame.timestampMs}ms, score=${String.format("%.4f", score)}"
        )

        val sourceBitmap = bestFrame.frameBitmap
            ?: throw IllegalStateException("Frame bitmap is null for representative shot")

        val generousCrop = BitmapUtils.cropGenerousPortrait(
            source = sourceBitmap,
            faceRect = bestFrame.boundingBox,
            sideMarginFraction = 0.50f,
            topMarginFraction = 0.60f,
            bottomMarginFraction = 0.80f
        )

        return PersonIdentity(
            id = personId,
            name = personName,
            appearances = appearances,
            bestShot = bestFrame,
            croppedFaceBitmap = generousCrop
        )
    }

    fun calculateDetailedScore(frame: FaceFrameInfo): Float {
        var score = frame.overallQualityScore

        val avgEyeOpen = (frame.leftEyeOpenProb + frame.rightEyeOpenProb) / 2.0f
        if (avgEyeOpen < 0.25f) {
            score *= 0.10f
        }

        val absYaw = Math.abs(frame.headEulerAngleY)
        val absPitch = Math.abs(frame.headEulerAngleX)
        if (absYaw > 35f || absPitch > 30f) {
            score *= 0.30f
        } else if (absYaw < 12f && absPitch < 12f) {
            score *= 1.25f
        }

        if (frame.smileProb > 0.3f) {
            score += 0.20f
        }

        val area = frame.boundingBox.width() * frame.boundingBox.height()
        if (area > 15000) {
            score *= 1.15f
        }

        return score
    }
}
