package com.iykyk.collage.processor

import android.content.Context
import android.net.Uri
import com.iykyk.collage.collage.CollageRenderer
import com.iykyk.collage.ml.MLKitFaceDetector
import com.iykyk.collage.ml.TFLiteEmbeddingExtractor
import com.iykyk.collage.model.CollageResult
import com.iykyk.collage.model.FaceFrameInfo
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
    private val identityClusterer = IdentityClusterer(distanceThreshold = 0.40f)
    private val representativeShotSelector = RepresentativeShotSelector()
    private val collageRenderer = CollageRenderer(context)

    suspend fun processVideo(videoUri: Uri): CollageResult? = withContext(Dispatchers.Default) {
        try {
            
            _progress.value = ProcessingProgress(
                stage = PipelineStage.EXTRACTING_FRAMES,
                progressFraction = 0.05f,
                message = "Extracting video frames..."
            )

            val extractedFrames = frameExtractor.extractFrames(videoUri, sampleEveryMs = 160L) { currentMs, totalMs, count ->
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
            var totalFacesDetected = 0

            for ((idx, frame) in extractedFrames.withIndex()) {
                val faces = faceDetector.detectFaces(
                    bitmap = frame.bitmap,
                    frameIndex = frame.frameIndex,
                    timestampMs = frame.timestampMs
                )
                if (faces.isNotEmpty()) {
                    frameFacesMap[frame.frameIndex] = faces
                    totalFacesDetected += faces.size
                }

                val frac = (idx.toFloat() / extractedFrames.size.toFloat()) * 0.25f
                _progress.value = ProcessingProgress(
                    stage = PipelineStage.DETECTING_FACES,
                    currentStep = idx + 1,
                    totalSteps = extractedFrames.size,
                    progressFraction = 0.30f + frac,
                    message = "Detected $totalFacesDetected faces across ${idx + 1}/${extractedFrames.size} frames"
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
                message = "Generating on-device TFLite face embeddings..."
            )

            for (track in rawTracks) {
                for (frame in track.frames) {
                    val frameBitmap = frame.frameBitmap ?: continue
                    val faceCrop = BitmapUtils.cropGenerousPortrait(
                        source = frameBitmap,
                        faceRect = frame.boundingBox,
                        sideMarginFraction = 0.2f,
                        topMarginFraction = 0.2f,
                        bottomMarginFraction = 0.2f
                    )
                    val embedding = embeddingExtractor.extractEmbedding(faceCrop)
                    frame.embedding = embedding
                    if (faceCrop != frameBitmap && !faceCrop.isRecycled) {
                        faceCrop.recycle()
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
                    appearances = trackGroup
                )
                identities.add(identity)
            }

            _progress.value = ProcessingProgress(
                stage = PipelineStage.GENERATING_COLLAGE,
                progressFraction = 0.95f,
                message = "Rendering shareable collage..."
            )

            val collageBitmap = collageRenderer.renderCollage(identities)

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

    fun release() {
        faceDetector.close()
        embeddingExtractor.close()
    }
}

