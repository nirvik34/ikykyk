package com.iykyk.collage.model

import android.graphics.Bitmap
import android.graphics.Rect

data class FaceQuality(
    val confidenceScore: Float,
    val sizeScore: Float,
    val sharpnessScore: Float,
    val poseScore: Float,
    val landmarkScore: Float,
    val compositionScore: Float,
    val edgePenalty: Float,
    val finalQuality: Float
)

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
    val detectorConfidence: Float = 0.0f,
    val faceQuality: FaceQuality = FaceQuality(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f),
    val frameBitmap: Bitmap? = null,
    var embedding: FloatArray? = null
) {
    val faceWidth: Int get() = boundingBox.width()
    val faceHeight: Int get() = boundingBox.height()
    val faceArea: Int get() = boundingBox.width() * boundingBox.height()
    val centerX: Int get() = boundingBox.centerX()
    val centerY: Int get() = boundingBox.centerY()

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

data class AppearanceSegment(
    val startFrameIndex: Int,
    val endFrameIndex: Int,
    val startTimestampMs: Long,
    val endTimestampMs: Long,
    val frameCount: Int,
    val durationMs: Long = endTimestampMs - startTimestampMs
)

data class AppearanceTrack(
    val trackId: Int,
    val frames: List<FaceFrameInfo>,
    val startTimeMs: Long,
    val endTimeMs: Long,
    val durationMs: Long = endTimeMs - startTimeMs,
    var meanEmbedding: FloatArray? = null,
    val segments: List<AppearanceSegment> = emptyList()
) {
    val frameCount: Int get() = frames.size
    val appearanceCount: Int get() = if (segments.isNotEmpty()) segments.size else 1

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
    val croppedFaceBitmap: Bitmap,
    val alternateImages: List<Bitmap> = emptyList(),
    val quality: Float = 0f,
    val appearanceCount: Int = appearances.sumOf { it.appearanceCount }
) {
    val totalAppearances: Int get() = appearances.sumOf { it.appearanceCount }
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

data class DebugPipelineInfo(
    val framesProcessed: Int = 0,
    val rawDetections: Int = 0,
    val validDetections: Int = 0,
    val rejectedDetections: Int = 0,
    val tracksCreated: Int = 0,
    val initialIdentities: Int = 0,
    val identitiesMerged: Int = 0,
    val finalIdentities: Int = 0,
    val embeddingsGenerated: Int = 0,
    val bestFrameQuality: Float = 0f,
    val trackDetails: List<TrackDebugInfo> = emptyList(),
    val identityDetails: List<IdentityDebugInfo> = emptyList()
)

data class TrackDebugInfo(
    val trackId: Int,
    val frameCount: Int,
    val startTimeMs: Long,
    val endTimeMs: Long,
    val meanEmbedding: FloatArray? = null,
    val assignedIdentityId: Int = -1
)

data class IdentityDebugInfo(
    val identityId: Int,
    val trackIds: List<Int>,
    val appearanceCount: Int,
    val centroidEmbedding: FloatArray? = null,
    val bestFrameQuality: Float = 0f,
    val mergeSourceCount: Int = 1
)
