package com.iykyk.collage.config

object FaceConfig {

    const val detectorConfidence: Float = 0.60f
    const val minFaceSize: Int = 48
    const val minFacePixelArea: Int = 2304

    const val onlineMatchThreshold: Float = 0.65f
    const val mergeThreshold: Float = 0.68f
    const val uncertainLow: Float = 0.55f

    const val minQuality: Float = 0.55f
    const val minSharpness: Float = 2.0f

    const val samplingFps: Long = 160L

    const val appearanceGapSeconds: Float = 2.0f
    const val appearanceGapMs: Long = 2000L

    const val cropPadding: Float = 0.45f
    const val cropTargetAspectRatio: Float = 4f / 5f
    const val embeddingPaddingFraction: Float = 0.35f

    const val trackMaxAgeMs: Long = 3000L
    const val trackMatchIoU: Float = 0.15f

    const val weightConfidence: Float = 0.25f
    const val weightSize: Float = 0.20f
    const val weightSharpness: Float = 0.20f
    const val weightPose: Float = 0.15f
    const val weightLandmark: Float = 0.10f
    const val weightComposition: Float = 0.10f

    const val edgeMarginThreshold: Int = 5
    const val edgePenaltyFactor: Float = 0.3f

    const val maxYawDeg: Float = 35f
    const val maxPitchDeg: Float = 30f
    const val extremePosePenalty: Float = 0.7f
    const val goodPoseBonus: Float = 0.25f

    const val duplicateSimilarityThreshold: Float = 0.95f
    const val maxClusterIterations: Int = 100

    private const val TAG = "FaceConfig"

    fun logConfig() {
        android.util.Log.i(TAG, "FACE_CONFIG: detectorConfidence=$detectorConfidence, " +
            "minFaceSize=$minFaceSize, onlineMatchThreshold=$onlineMatchThreshold, " +
            "mergeThreshold=$mergeThreshold, minQuality=$minQuality, " +
            "samplingFps=$samplingFps, appearanceGapSeconds=$appearanceGapSeconds, " +
            "cropPadding=$cropPadding, cropTargetAspectRatio=$cropTargetAspectRatio, " +
            "embeddingPaddingFraction=$embeddingPaddingFraction")
    }
}
