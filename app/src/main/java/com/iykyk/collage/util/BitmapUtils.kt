package com.iykyk.collage.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import com.iykyk.collage.config.FaceConfig

object BitmapUtils {

    fun cropFacePortrait(
        source: Bitmap,
        faceRect: Rect,
        paddingFraction: Float = FaceConfig.cropPadding
    ): Bitmap {
        val cx = faceRect.centerX()
        val cy = faceRect.centerY()
        val faceW = faceRect.width()
        val faceH = faceRect.height()

        val padding = (max(faceW, faceH) * paddingFraction).toInt()

        var left = max(0, cx - faceW / 2 - padding)
        var top = max(0, cy - faceH / 2 - padding)
        var right = min(source.width, cx + faceW / 2 + padding)
        var bottom = min(source.height, cy + faceH / 2 + padding)

        var cropW = max(1, right - left)
        var cropH = max(1, bottom - top)

        val targetRatio = FaceConfig.cropTargetAspectRatio
        val currentRatio = cropW.toFloat() / cropH.toFloat()

        var finalLeft = left
        var finalTop = top
        var finalRight = right
        var finalBottom = bottom

        if (currentRatio > targetRatio) {
            val newH = (cropW / targetRatio).toInt()
            finalTop = max(0, cy - newH / 2)
            finalBottom = min(source.height, finalTop + newH)
        } else {
            val newW = (cropH * targetRatio).toInt()
            finalLeft = max(0, cx - newW / 2)
            finalRight = min(source.width, finalLeft + newW)
        }

        val finalW = max(1, finalRight - finalLeft)
        val finalH = max(1, finalBottom - finalTop)

        return Bitmap.createBitmap(source, finalLeft, finalTop, finalW, finalH)
    }

    fun cropGenerousPortrait(
        source: Bitmap,
        faceRect: Rect,
        sideMarginFraction: Float = 0.50f,
        topMarginFraction: Float = 0.60f,
        bottomMarginFraction: Float = 0.80f
    ): Bitmap {
        val width = faceRect.width()
        val height = faceRect.height()
        val sidePadding = (width * sideMarginFraction).toInt()
        val topPadding = (height * topMarginFraction).toInt()
        val bottomPadding = (height * bottomMarginFraction).toInt()

        var left = max(0, faceRect.left - sidePadding)
        var top = max(0, faceRect.top - topPadding)
        var right = min(source.width, faceRect.right + sidePadding)
        var bottom = min(source.height, faceRect.bottom + bottomPadding)

        val cropW = max(1, right - left)
        val cropH = max(1, bottom - top)

        return Bitmap.createBitmap(source, left, top, cropW, cropH)
    }

    fun cropForEmbedding(
        source: Bitmap,
        faceRect: Rect,
        paddingFraction: Float = FaceConfig.embeddingPaddingFraction
    ): Bitmap? {
        val cx = faceRect.centerX()
        val cy = faceRect.centerY()
        val maxDim = max(faceRect.width(), faceRect.height())
        val side = (maxDim * (1.0f + paddingFraction)).toInt()
        val halfSide = side / 2

        var left = cx - halfSide
        var top = cy - halfSide
        var right = left + side
        var bottom = top + side

        if (left < 0) { right -= left; left = 0 }
        if (top < 0) { bottom -= top; top = 0 }
        if (right > source.width) { left -= (right - source.width); right = source.width }
        if (bottom > source.height) { top -= (bottom - source.height); bottom = source.height }

        left = max(0, left)
        top = max(0, top)
        right = min(source.width, right)
        bottom = min(source.height, bottom)

        val cropW = max(1, right - left)
        val cropH = max(1, bottom - top)

        return Bitmap.createBitmap(source, left, top, cropW, cropH)
    }

    fun scaleBitmap(bitmap: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
    }

    fun calculateSharpnessScore(bitmap: Bitmap): Float {
        try {
            val width = min(bitmap.width, 120)
            val height = min(bitmap.height, 120)
            val scaled = Bitmap.createScaledBitmap(bitmap, width, height, false)
            val pixels = IntArray(width * height)
            scaled.getPixels(pixels, 0, width, 0, 0, width, height)

            val gray = FloatArray(width * height)
            for (i in pixels.indices) {
                val p = pixels[i]
                val r = (p shr 16) and 0xFF
                val g = (p shr 8) and 0xFF
                val b = p and 0xFF
                gray[i] = 0.299f * r + 0.587f * g + 0.114f * b
            }

            var sum = 0.0
            var sumSq = 0.0
            var count = 0
            for (y in 1 until height - 1) {
                for (x in 1 until width - 1) {
                    val idx = y * width + x
                    val laplacian = (
                        gray[idx - width] + gray[idx + width] +
                        gray[idx - 1] + gray[idx + 1] - 4f * gray[idx]
                    ).toDouble()
                    sum += laplacian
                    sumSq += laplacian * laplacian
                    count++
                }
            }
            if (count == 0) return 0f
            val mean = sum / count
            val variance = (sumSq / count) - (mean * mean)
            if (scaled != bitmap) scaled.recycle()
            return max(0.0, variance).toFloat()
        } catch (e: Exception) {
            return 50.0f
        }
    }

    fun computeFaceQualityScore(frame: com.iykyk.collage.model.FaceFrameInfo): Float {
        val confidenceComponent = frame.detectorConfidence.coerceIn(0f, 1f)
        val sizeScore = computeSizeScore(frame)
        val sharpnessComponent = (frame.sharpnessScore / 150.0f).coerceIn(0f, 1f)
        val poseScore = computePoseScore(frame)
        val landmarkScore = computeLandmarkScore(frame)
        val compositionScore = computeCompositionScore(frame)

        val edgePenalty = computeEdgePenalty(frame)
        val posePenalty = computePosePenalty(frame)

        val rawScore = (
            FaceConfig.weightConfidence * confidenceComponent +
            FaceConfig.weightSize * sizeScore +
            FaceConfig.weightSharpness * sharpnessComponent +
            FaceConfig.weightPose * poseScore +
            FaceConfig.weightLandmark * landmarkScore +
            FaceConfig.weightComposition * compositionScore
        )
        val penalty = (1.0f - edgePenalty) * 0.1f + if (posePenalty < 1.0f) (1.0f - posePenalty) * 0.1f else 0f
        return (rawScore - penalty).coerceIn(0f, 1f)
    }

    private fun computeSizeScore(frame: com.iykyk.collage.model.FaceFrameInfo): Float {
        val frameDiagonal = kotlin.math.sqrt(
            (frame.frameWidth * frame.frameWidth + frame.frameHeight * frame.frameHeight).toFloat()
        )
        val faceDiagonal = kotlin.math.sqrt(
            (frame.boundingBox.width() * frame.boundingBox.width() + frame.boundingBox.height() * frame.boundingBox.height()).toFloat()
        )
        return (faceDiagonal / frameDiagonal).coerceIn(0f, 1f)
    }

    private fun computePoseScore(frame: com.iykyk.collage.model.FaceFrameInfo): Float {
        val absYaw = kotlin.math.abs(frame.headEulerAngleY)
        val absPitch = kotlin.math.abs(frame.headEulerAngleX)
        val poseAngleSum = absYaw + absPitch * 0.5f
        val frontality = kotlin.math.max(0.0f, 1.0f - (poseAngleSum / 70.0f))
        return frontality
    }

    private fun computeLandmarkScore(frame: com.iykyk.collage.model.FaceFrameInfo): Float {
        var score = 0f
        if (frame.leftEyeOpenProb > 0.3f) score += 0.25f
        if (frame.rightEyeOpenProb > 0.3f) score += 0.25f
        score += (frame.smileProb * 0.5f).coerceIn(0f, 0.5f)
        return score.coerceIn(0f, 1f)
    }

    private fun computeCompositionScore(frame: com.iykyk.collage.model.FaceFrameInfo): Float {
        val cx = frame.boundingBox.centerX()
        val cy = frame.boundingBox.centerY()
        val marginX = minOf(cx, frame.frameWidth - cx).toFloat() / frame.frameWidth
        val marginY = minOf(cy, frame.frameHeight - cy).toFloat() / frame.frameHeight
        return minOf(marginX, marginY).coerceIn(0f, 1f)
    }

    private fun computeEdgePenalty(frame: com.iykyk.collage.model.FaceFrameInfo): Float {
        val leftDist = frame.boundingBox.left
        val rightDist = frame.frameWidth - frame.boundingBox.right
        val topDist = frame.boundingBox.top
        val bottomDist = frame.frameHeight - frame.boundingBox.bottom
        val minDist = minOf(leftDist, rightDist, topDist, bottomDist)
        return if (minDist < FaceConfig.edgeMarginThreshold) FaceConfig.edgePenaltyFactor else 1.0f
    }

    private fun computePosePenalty(frame: com.iykyk.collage.model.FaceFrameInfo): Float {
        val absYaw = kotlin.math.abs(frame.headEulerAngleY)
        val absPitch = kotlin.math.abs(frame.headEulerAngleX)
        return if (absYaw > FaceConfig.maxYawDeg || absPitch > FaceConfig.maxPitchDeg) {
            FaceConfig.extremePosePenalty
        } else if (absYaw < 12f && absPitch < 12f) {
            FaceConfig.goodPoseBonus
        } else {
            1.0f
        }
    }
}
