package com.iykyk.collage.processor

import android.content.Context
import android.net.Uri
import android.util.Log
import com.iykyk.collage.collage.CollageRenderer
import com.iykyk.collage.config.FaceConfig
import com.iykyk.collage.ml.MLKitFaceDetector
import com.iykyk.collage.ml.TFLiteEmbeddingExtractor
import com.iykyk.collage.model.AppearanceTrack
import com.iykyk.collage.model.CollageResult
import com.iykyk.collage.model.FaceFrameInfo
import com.iykyk.collage.model.LayoutTemplate
import com.iykyk.collage.model.PersonIdentity
import com.iykyk.collage.model.PipelineStage
import com.iykyk.collage.model.ProcessingProgress
import com.iykyk.collage.util.BitmapUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class VideoProcessorRepository(private val context: Context) {

    private val _progress = MutableStateFlow(ProcessingProgress())
    val progress: StateFlow<ProcessingProgress> = _progress.asStateFlow()

    private val frameExtractor = VideoFrameExtractor(context)
    private val faceDetector = MLKitFaceDetector()
    private val embeddingExtractor = TFLiteEmbeddingExtractor(context)
    private val segmentTracker = AppearanceSegmentTracker()
    private val identityClusterer = IdentityClusterer()
    private val representativeShotSelector = RepresentativeShotSelector()
    private val collageRenderer = CollageRenderer(context)

    suspend fun processVideo(videoUri: Uri): CollageResult? = withContext(Dispatchers.Default) {
        try {
            FaceConfig.logConfig()

            _progress.value = ProcessingProgress(
                stage = PipelineStage.EXTRACTING_FRAMES,
                progressFraction = 0.05f,
                message = "Extracting video frames..."
            )

            val extractedFrames = frameExtractor.extractFrames(videoUri, sampleEveryMs = FaceConfig.samplingFps) { currentMs, totalMs, count ->
                val frac = (currentMs.toFloat() / totalMs.toFloat()).coerceIn(0.0f, 1.0f) * 0.25f
                _progress.value = ProcessingProgress(
                    stage = PipelineStage.EXTRACTING_FRAMES,
                    currentStep = count,
                    totalSteps = 100,
                    progressFraction = 0.05f + frac,
                    message = "Extracted $count frames ($currentMs ms / $totalMs ms)"
                )
            }

            if (extractedFrames.isEmpty()) {
                throw IllegalStateException("Failed to extract video frames.")
            }

            _progress.value = ProcessingProgress(
                stage = PipelineStage.DETECTING_FACES,
                progressFraction = 0.30f,
                message = "Detecting faces & analyzing quality metrics..."
            )

            val frameFacesMap = mutableMapOf<Int, List<FaceFrameInfo>>()
            var rawDetectionCount = 0
            var validDetectionCount = 0
            var rejectedCount = 0

            for ((idx, frame) in extractedFrames.withIndex()) {
                val faces = faceDetector.detectFaces(
                    bitmap = frame.bitmap,
                    frameIndex = frame.frameIndex,
                    timestampMs = frame.timestampMs
                )
                rawDetectionCount += faces.size

                val filteredFaces = faceDetector.filterDetections(faces)
                validDetectionCount += filteredFaces.size
                rejectedCount += faces.size - filteredFaces.size

                if (filteredFaces.isNotEmpty()) {
                    frameFacesMap[frame.frameIndex] = filteredFaces
                }

                val frac = (idx.toFloat() / extractedFrames.size.toFloat()) * 0.25f
                _progress.value = ProcessingProgress(
                    stage = PipelineStage.DETECTING_FACES,
                    currentStep = idx + 1,
                    totalSteps = extractedFrames.size,
                    progressFraction = 0.30f + frac,
                    message = "Detected $validDetectionCount faces ($rejectedCount rejected) across ${idx + 1}/${extractedFrames.size} frames"
                )
            }

            _progress.value = ProcessingProgress(
                stage = PipelineStage.TRACKING_APPEARANCES,
                progressFraction = 0.58f,
                message = "Tracking continuous visible appearance segments..."
            )

            val rawTracks = segmentTracker.trackAppearances(frameFacesMap)

            _progress.value = ProcessingProgress(
                stage = PipelineStage.COMPUTING_EMBEDDINGS,
                progressFraction = 0.65f,
                message = "Generating on-device face embeddings..."
            )

            var embeddingsCount = 0
            val allGoodFrames = mutableListOf<FaceFrameInfo>()

            for (track in rawTracks) {
                for (frame in track.frames) {
                    val frameBitmap = frame.frameBitmap ?: continue
                    val faceCrop = BitmapUtils.cropForEmbedding(
                        source = frameBitmap,
                        faceRect = frame.boundingBox,
                        paddingFraction = FaceConfig.embeddingPaddingFraction
                    )
                    if (faceCrop != null) {
                        val embedding = embeddingExtractor.extractEmbedding(faceCrop)
                        frame.embedding = embedding
                        embeddingsCount++
                        allGoodFrames.add(frame)
                        if (faceCrop != frameBitmap && !faceCrop.isRecycled) {
                            faceCrop.recycle()
                        }
                    }
                }
            }

            _progress.value = ProcessingProgress(
                stage = PipelineStage.CLUSTERING_IDENTITIES,
                progressFraction = 0.80f,
                message = "Clustering unique person identities..."
            )

            val clusteredTrackGroups = identityClusterer.clusterIdentities(rawTracks)

            _progress.value = ProcessingProgress(
                stage = PipelineStage.SELECTING_SHOTS,
                progressFraction = 0.90f,
                message = "Selecting crisp representative shots..."
            )

            val identities = mutableListOf<PersonIdentity>()
            for ((index, trackGroup) in clusteredTrackGroups.withIndex()) {
                val personId = index + 1
                val personName = "Person $personId"
                val identity = representativeShotSelector.selectRepresentativeShot(
                    personId = personId,
                    personName = personName,
                    appearances = trackGroup,
                    allFrames = allGoodFrames
                )
                identities.add(identity)
            }

            logPipelineDiagnostics(
                framesProcessed = extractedFrames.size,
                rawDetections = rawDetectionCount,
                validDetections = validDetectionCount,
                rejectedDetections = rejectedCount,
                rawTracks = rawTracks,
                clusteredTrackGroups = clusteredTrackGroups,
                identities = identities,
                embeddingsCount = embeddingsCount
            )

            _progress.value = ProcessingProgress(
                stage = PipelineStage.GENERATING_COLLAGE,
                progressFraction = 0.95f,
                message = "Rendering shareable collage..."
            )

            verifyIdentityImages(identities)

            val collageBitmap = collageRenderer.renderCollage(identities, LayoutTemplate.EDITORIAL)

            _progress.value = ProcessingProgress(
                stage = PipelineStage.COMPLETED,
                progressFraction = 1.0f,
                message = "Processing complete! ${identities.size} unique people identified."
            )

            return@withContext CollageResult(
                identities = identities,
                collageBitmap = collageBitmap
            )
        } catch (e: Exception) {
            e.printStackTrace()
            _progress.value = ProcessingProgress(
                stage = PipelineStage.ERROR,
                message = "Processing error",
                errorDetails = e.localizedMessage ?: "Unknown error"
            )
            return@withContext null
        }
    }

    private fun verifyIdentityImages(identities: List<PersonIdentity>) {
        val tag = "IYKYK_IMAGE_VERIFY"
        Log.i(tag, "=== IDENTITY IMAGE VERIFICATION ===")
        val imageKeys = mutableMapOf<String, Int>()
        for (identity in identities) {
            val bestFrame = identity.bestShot
            val frameIndex = bestFrame.frameIndex
            val bbox = bestFrame.boundingBox
            val quality = BitmapUtils.computeFaceQualityScore(bestFrame)
            val imageKey = "frame$frameIndex|${bbox.left},${bbox.top},${bbox.width()},${bbox.height()}"
            Log.i(tag, "${identity.name}: frame=$frameIndex bbox=$bbox quality=$quality")
            if (imageKeys.containsKey(imageKey)) {
                Log.w(tag, "DUPLICATE: ${identity.name} shares image with person ${imageKeys[imageKey]}")
            } else {
                imageKeys[imageKey] = identity.id
            }
        }
        Log.i(tag, "Unique image references: ${imageKeys.size}/${identities.size}")
        Log.i(tag, "=== END IMAGE VERIFICATION ===")
    }

    private fun logPipelineDiagnostics(
        framesProcessed: Int,
        rawDetections: Int,
        validDetections: Int,
        rejectedDetections: Int,
        rawTracks: List<AppearanceTrack>,
        clusteredTrackGroups: List<List<AppearanceTrack>>,
        identities: List<PersonIdentity>,
        embeddingsCount: Int
    ) {
        val tag = "IYKYK_DIAGNOSTICS"
        Log.i(tag, "=================== IYKYK PIPELINE DIAGNOSTICS ===================")
        Log.i(tag, "Frames processed: $framesProcessed")
        Log.i(tag, "Raw detections: $rawDetections")
        Log.i(tag, "Valid detections: $validDetections")
        Log.i(tag, "Rejected detections: $rejectedDetections")
        Log.i(tag, "Embeddings generated: $embeddingsCount")
        Log.i(tag, "Appearance tracks: ${rawTracks.size}")
        for (t in rawTracks) {
            Log.i(tag, "   - Track ${t.trackId}: ${t.frames.size} frames (${t.startTimeMs}ms - ${t.endTimeMs}ms, ${t.appearanceCount} appearances)")
        }
        Log.i(tag, "Initial identities (clusters): ${clusteredTrackGroups.size}")

        val mergeCount = rawTracks.size - clusteredTrackGroups.sumOf { it.size }
        Log.i(tag, "Identities merged: $mergeCount")

        for ((idx, group) in clusteredTrackGroups.withIndex()) {
            val trackIds = group.map { it.trackId }
            val totalFrames = group.sumOf { it.frames.size }
            val totalApp = group.sumOf { it.appearanceCount }
            Log.i(tag, "   - Identity ${idx + 1}: tracks $trackIds, frames: $totalFrames, appearances: $totalApp")
        }

        Log.i(tag, "Final identities: ${identities.size}")
        Log.i(tag, "Representative shots quality:")
        for (id in identities) {
            val score = BitmapUtils.computeFaceQualityScore(id.bestShot)
            Log.i(tag, "   - ${id.name}: frameIndex=${id.bestShot.frameIndex}, quality=${String.format("%.4f", score)}, appearances=${id.totalAppearances}")
        }

        Log.i(tag, "==================================================================")
    }

    fun release() {
        faceDetector.close()
        embeddingExtractor.close()
    }
}
