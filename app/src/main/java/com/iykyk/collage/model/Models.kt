package com.iykyk.collage.model

import android.graphics.Bitmap
import android.graphics.Rect

data class FaceFrameInfo(
    val frameIndex: Int,
    val timestampMs: Long,
    val boundingBox: Rect,
    val frameWidth: Int,
    val frameHeight: Int,
    val headEulerAngleY: Float, 
    val headEulerAngleZ: Float, 
    val headEulerAngleX: Float, 
    val leftEyeOpenProb: Float,
    val rightEyeOpenProb: Float,
    val smileProb: Float,
    val sharpnessScore: Float,
    val overallQualityScore: Float,
    val frameBitmap: Bitmap? = null,
    var embedding: FloatArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as FaceFrameInfo
        return frameIndex == other.frameIndex && timestampMs == other.timestampMs
    }

    override fun hashCode(): Int {
        var result = frameIndex
        result = 31 * result + timestampMs.hashCode()
        return result
    }
}

data class AppearanceTrack(
    val trackId: Int,
    val frames: List<FaceFrameInfo>,
    val startTimeMs: Long,
    val endTimeMs: Long,
    val durationMs: Long = endTimeMs - startTimeMs,
    var meanEmbedding: FloatArray? = null
) {
    val frameCount: Int get() = frames.size

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as AppearanceTrack
        return trackId == other.trackId
    }

    override fun hashCode(): Int {
        return trackId
    }
}

data class PersonIdentity(
    val id: Int,
    val name: String, 
    val appearances: List<AppearanceTrack>,
    val bestShot: FaceFrameInfo,
    val croppedFaceBitmap: Bitmap
) {
    val totalAppearances: Int get() = appearances.size
    val totalVisibleDurationMs: Long get() = appearances.sumOf { it.durationMs }
}

enum class LayoutTemplate(val label: String) {
    EDITORIAL("Editorial"),
    FILM_STRIP("Film Strip"),
    POLAROID("Polaroid"),
    FULL_BLEED("Full Bleed")
}

enum class PipelineStage {
    IDLE,
    EXTRACTING_FRAMES,
    DETECTING_FACES,
    TRACKING_APPEARANCES,
    COMPUTING_EMBEDDINGS,
    CLUSTERING_IDENTITIES,
    SELECTING_SHOTS,
    GENERATING_COLLAGE,
    COMPLETED,
    ERROR
}

data class ProcessingProgress(
    val stage: PipelineStage = PipelineStage.IDLE,
    val currentStep: Int = 0,
    val totalSteps: Int = 100,
    val progressFraction: Float = 0.0f,
    val message: String = "Ready to process",
    val errorDetails: String? = null
)

data class CollageResult(
    val identities: List<PersonIdentity>,
    val collageBitmap: Bitmap,
    val layoutTemplate: LayoutTemplate = LayoutTemplate.EDITORIAL
)

