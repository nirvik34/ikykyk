package com.iykyk.collage.ml

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.iykyk.collage.model.FaceFrameInfo
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

        val leftEyeOpen = face.leftEyeOpenProbability ?: 0.5f
        val rightEyeOpen = face.rightEyeOpenProbability ?: 0.5f
        val smile = face.smilingProbability ?: 0.0f

        // Frontality score: 1.0 = perfect frontal, decreases with head rotation
        val poseAngleSum = Math.abs(yaw) + Math.abs(pitch) + Math.abs(roll) * 0.5f
        val frontalityScore = kotlin.math.max(0.0f, 1.0f - (poseAngleSum / 70.0f))

        // Eyes open average
        val eyesOpenScore = (leftEyeOpen + rightEyeOpen) / 2.0f

        // Bounding box margin check (penalize faces partially cropped by frame edge)
        val edgeMarginX = kotlin.math.min(bbox.left, frameBitmap.width - bbox.right)
        val edgeMarginY = kotlin.math.min(bbox.top, frameBitmap.height - bbox.bottom)
        val edgeIntegrityScore = if (edgeMarginX < 5 || edgeMarginY < 5) 0.3f else 1.0f

        // Crop face for sharpness calculation
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

        // Weighted Overall Quality Score
        val overallQuality = (
            frontalityScore * 0.35f +
            eyesOpenScore * 0.30f +
            sharpnessNormalized * 0.20f +
            (smile * 0.10f) +
            (edgeIntegrityScore * 0.05f)
        )

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
            frameBitmap = frameBitmap
        )
    }

    fun close() {
        detector.close()
    }
}
