package com.iykyk.collage.ml

import android.graphics.Bitmap
import android.graphics.Rect
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.iykyk.collage.config.FaceConfig
import com.iykyk.collage.model.FaceFrameInfo
import com.iykyk.collage.model.FaceQuality
import com.iykyk.collage.util.BitmapUtils
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class MLKitFaceDetector {

    private val detectorOptions = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .setMinFaceSize(0.08f)
        .build()

    private val detector: FaceDetector = FaceDetection.getClient(detectorOptions)

    suspend fun detectFaces(
        bitmap: Bitmap,
        frameIndex: Int,
        timestampMs: Long
    ): List<FaceFrameInfo> = suspendCancellableCoroutine { continuation ->
        val inputImage = InputImage.fromBitmap(bitmap, 0)
        detector.process(inputImage)
            .addOnSuccessListener { faces ->
                val resultList = faces.map { face ->
                    buildFaceFrameInfo(face, bitmap, frameIndex, timestampMs)
                }
                continuation.resume(resultList)
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                continuation.resume(emptyList())
            }
    }

    fun filterDetections(detections: List<FaceFrameInfo>): List<FaceFrameInfo> {
        return detections.filter {
            it.overallQualityScore >= FaceConfig.detectorConfidence &&
            it.faceWidth >= FaceConfig.minFaceSize &&
            it.faceHeight >= FaceConfig.minFaceSize &&
            it.faceArea >= FaceConfig.minFacePixelArea &&
            it.overallQualityScore >= FaceConfig.minQuality &&
            it.sharpnessScore >= FaceConfig.minSharpness
        }
    }

    private fun buildFaceFrameInfo(
        face: Face,
        frameBitmap: Bitmap,
        frameIndex: Int,
        timestampMs: Long
    ): FaceFrameInfo {
        val bbox = face.boundingBox
        val yaw = face.headEulerAngleY
        val roll = face.headEulerAngleZ
        val pitch = face.headEulerAngleX

        val detectorConf = face.smilingProbability?.coerceIn(0f, 1f) ?: 0.5f

        val leftEyeOpen = face.leftEyeOpenProbability ?: 0.5f
        val rightEyeOpen = face.rightEyeOpenProbability ?: 0.5f
        val smile = face.smilingProbability ?: 0.0f

        val absYaw = kotlin.math.abs(yaw)
        val absRoll = kotlin.math.abs(roll)
        val absPitch = kotlin.math.abs(pitch)
        val poseAngleSum = absYaw + absRoll * 0.5f + absPitch * 0.5f
        val frontalityScore = kotlin.math.max(0.0f, 1.0f - (poseAngleSum / 70.0f))

        val eyesOpenScore = (leftEyeOpen + rightEyeOpen) / 2.0f

        val edgeMarginX = kotlin.math.min(bbox.left, frameBitmap.width - bbox.right)
        val edgeMarginY = kotlin.math.min(bbox.top, frameBitmap.height - bbox.bottom)
        val edgeIntegrityScore = if (edgeMarginX < FaceConfig.edgeMarginThreshold || edgeMarginY < FaceConfig.edgeMarginThreshold) FaceConfig.edgePenaltyFactor else 1.0f

        val faceCrop = try {
            val left = kotlin.math.max(0, bbox.left)
            val top = kotlin.math.max(0, bbox.top)
            val w = kotlin.math.min(frameBitmap.width - left, bbox.width())
            val h = kotlin.math.min(frameBitmap.height - top, bbox.height())
            if (w > 0 && h > 0) Bitmap.createBitmap(frameBitmap, left, top, w, h) else frameBitmap
        } catch (e: Exception) {
            frameBitmap
        }

        val sharpness = BitmapUtils.calculateSharpnessScore(faceCrop)
        val sharpnessNormalized = kotlin.math.min(1.0f, sharpness / 150.0f)

        if (faceCrop != frameBitmap && !faceCrop.isRecycled) {
            faceCrop.recycle()
        }

        val sizeScore = computeSizeScore(bbox, frameBitmap)
        val landmarkScore = computeLandmarkScore(leftEyeOpen, rightEyeOpen, smile)
        val compositionScore = computeCompositionScore(bbox, frameBitmap)

        val edgePenalty = computeEdgePenalty(bbox, frameBitmap)
        val posePenalty = computePosePenalty(absYaw, absPitch)

        val confidenceComponent = detectorConf.coerceIn(0f, 1f)
        val sizeComponent = sizeScore.coerceIn(0f, 1f)
        val sharpnessComponent = sharpnessNormalized.coerceIn(0f, 1f)
        val poseComponent = frontalityScore.coerceIn(0f, 1f)
        val landmarkComponent = landmarkScore.coerceIn(0f, 1f)
        val compositionComponent = compositionScore.coerceIn(0f, 1f)

        val quality = FaceQuality(
            confidenceScore = confidenceComponent,
            sizeScore = sizeComponent,
            sharpnessScore = sharpnessComponent,
            poseScore = poseComponent,
            landmarkScore = landmarkComponent,
            compositionScore = compositionComponent,
            edgePenalty = edgePenalty,
            finalQuality = computeFinalQuality(
                confidenceComponent, sizeComponent, sharpnessComponent,
                poseComponent, landmarkComponent, compositionComponent,
                edgePenalty, posePenalty
            )
        )

        val overallQuality = quality.finalQuality

        return FaceFrameInfo(
            frameIndex = frameIndex,
            timestampMs = timestampMs,
            boundingBox = bbox,
            frameWidth = frameBitmap.width,
            frameHeight = frameBitmap.height,
            headEulerAngleY = yaw,
            headEulerAngleZ = roll,
            headEulerAngleX = pitch,
            leftEyeOpenProb = leftEyeOpen,
            rightEyeOpenProb = rightEyeOpen,
            smileProb = smile,
            sharpnessScore = sharpness,
            overallQualityScore = overallQuality,
            detectorConfidence = detectorConf,
            faceQuality = quality,
            frameBitmap = frameBitmap
        )
    }

    private fun computeSizeScore(bbox: Rect, frameBitmap: Bitmap): Float {
        val frameDiagonal = kotlin.math.sqrt(
            (frameBitmap.width * frameBitmap.width + frameBitmap.height * frameBitmap.height).toFloat()
        )
        val faceDiagonal = kotlin.math.sqrt(
            (bbox.width() * bbox.width() + bbox.height() * bbox.height()).toFloat()
        )
        return (faceDiagonal / frameDiagonal).coerceIn(0f, 1f)
    }

    private fun computeLandmarkScore(leftEyeOpen: Float, rightEyeOpen: Float, smile: Float): Float {
        var score = 0f
        if (leftEyeOpen > 0.3f) score += 0.35f
        if (rightEyeOpen > 0.3f) score += 0.35f
        score += (smile * 0.3f).coerceIn(0f, 0.3f)
        return score.coerceIn(0f, 1f)
    }

    private fun computeCompositionScore(bbox: Rect, frameBitmap: Bitmap): Float {
        val cx = bbox.centerX()
        val cy = bbox.centerY()
        val marginX = minOf(cx, frameBitmap.width - cx).toFloat() / frameBitmap.width
        val marginY = minOf(cy, frameBitmap.height - cy).toFloat() / frameBitmap.height
        return minOf(marginX, marginY).coerceIn(0f, 1f)
    }

    private fun computeEdgePenalty(bbox: Rect, frameBitmap: Bitmap): Float {
        val leftDist = bbox.left
        val rightDist = frameBitmap.width - bbox.right
        val topDist = bbox.top
        val bottomDist = frameBitmap.height - bbox.bottom
        val minDist = minOf(leftDist, rightDist, topDist, bottomDist)
        return if (minDist < FaceConfig.edgeMarginThreshold) FaceConfig.edgePenaltyFactor else 1.0f
    }

    private fun computePosePenalty(absYaw: Float, absPitch: Float): Float {
        return if (absYaw > FaceConfig.maxYawDeg || absPitch > FaceConfig.maxPitchDeg) {
            FaceConfig.extremePosePenalty
        } else if (absYaw < 12f && absPitch < 12f) {
            FaceConfig.goodPoseBonus
        } else {
            1.0f
        }
    }

    private fun computeFinalQuality(
        confidence: Float,
        size: Float,
        sharpness: Float,
        pose: Float,
        landmarks: Float,
        composition: Float,
        edgePenalty: Float,
        posePenalty: Float
    ): Float {
        val score = (
            FaceConfig.weightConfidence * confidence +
            FaceConfig.weightSize * size +
            FaceConfig.weightSharpness * sharpness +
            FaceConfig.weightPose * pose +
            FaceConfig.weightLandmark * landmarks +
            FaceConfig.weightComposition * composition
        )
        val penalty = (1.0f - edgePenalty) * 0.1f + if (posePenalty < 1.0f) (1.0f - posePenalty) * 0.1f else 0f
        return (score - penalty).coerceIn(0f, 1f)
    }

    fun close() {
        detector.close()
    }
}
