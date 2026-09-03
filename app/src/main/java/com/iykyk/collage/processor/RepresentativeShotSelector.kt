package com.iykyk.collage.processor

import android.graphics.Bitmap
import com.iykyk.collage.model.AppearanceTrack
import com.iykyk.collage.model.FaceFrameInfo
import com.iykyk.collage.model.PersonIdentity
import com.iykyk.collage.util.BitmapUtils

class RepresentativeShotSelector {

    /**
     * Selects the highest quality representative face frame for a person identity
     * and generates a generous, high-resolution portrait crop.
     */
    fun selectRepresentativeShot(
        personId: Int,
        personName: String,
        appearances: List<AppearanceTrack>
    ): PersonIdentity {
        val allFrames = appearances.flatMap { it.frames }

        // Find frame with highest overall quality score
        val bestFrame = allFrames.maxByOrNull { calculateDetailedScore(it) }
            ?: appearances.first().frames.first()

        // Generate generous portrait crop (including hair, chin, and upper shoulders)
        val sourceBitmap = bestFrame.frameBitmap
            ?: throw IllegalStateException("Frame bitmap is null for representative shot")

        val generousCrop = BitmapUtils.cropGenerousPortrait(
            source = sourceBitmap,
            faceRect = bestFrame.boundingBox,
            sideMarginFraction = 0.55f,
            topMarginFraction = 0.65f,
            bottomMarginFraction = 0.85f
        )

        return PersonIdentity(
            id = personId,
            name = personName,
            appearances = appearances,
            bestShot = bestFrame,
            croppedFaceBitmap = generousCrop
        )
    }

    private fun calculateDetailedScore(frame: FaceFrameInfo): Float {
        var score = frame.overallQualityScore

        // Heavy penalty for closed eyes (< 0.25 probability)
        val avgEyeOpen = (frame.leftEyeOpenProb + frame.rightEyeOpenProb) / 2.0f
        if (avgEyeOpen < 0.25f) {
            score *= 0.2f
        }

        // Heavy penalty for extreme side/up/down angles (yaw/pitch > 35 deg)
        if (Math.abs(frame.headEulerAngleY) > 35f || Math.abs(frame.headEulerAngleX) > 30f) {
            score *= 0.4f
        }

        // Bonus for smiling / pleasant expression
        if (frame.smileProb > 0.4f) {
            score += 0.15f
        }

        return score
    }
}
