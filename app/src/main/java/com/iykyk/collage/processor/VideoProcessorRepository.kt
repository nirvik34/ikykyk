package com.iykyk.collage.processor

import android.content.Context
import android.net.Uri
import android.util.Log
import com.iykyk.collage.collage.CollageRenderer
import com.iykyk.collage.ml.MLKitFaceDetector
import com.iykyk.collage.ml.TFLiteEmbeddingExtractor
import com.iykyk.collage.model.AppearanceTrack
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
    private val identityClusterer = IdentityClusterer()
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

            var validCropsCount = 0
            var embeddingsCount = 0

            for (track in rawTracks) {
                for (frame in track.frames) {
                    val frameBitmap = frame.frameBitmap ?: continue
                    val faceCrop = BitmapUtils.cropSquareFaceForEmbedding(
                        source = frameBitmap,
                        faceRect = frame.boundingBox,
                        paddingFraction = 0.25f
                    )
                    if (faceCrop != null) {
                        validCropsCount++
                        val embedding = embeddingExtractor.extractEmbedding(faceCrop)
                        frame.embedding = embedding
                        embeddingsCount++
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
                    appearances = trackGroup
                )
                identities.add(identity)
            }

            logPipelineDiagnostics(
                sampledFramesCount = extractedFrames.size,
                totalDetectedFaces = totalFacesDetected,
                validCropsCount = validCropsCount,
                embeddingsCount = embeddingsCount,
                rawTracks = rawTracks,
                clusteredTrackGroups = clusteredTrackGroups,
                identities = identities
            )

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

    private fun logPipelineDiagnostics(
        sampledFramesCount: Int,
        totalDetectedFaces: Int,
        validCropsCount: Int,
        embeddingsCount: Int,
        rawTracks: List<AppearanceTrack>,
        clusteredTrackGroups: List<List<AppearanceTrack>>,
        identities: List<PersonIdentity>
    ) {
        val tag = "IYKYK_DIAGNOSTICS"
        Log.i(tag, "=================== IYKYK PIPELINE DIAGNOSTICS ===================")
        Log.i(tag, "1. SAMPLED FRAMES: $sampledFramesCount frames extracted (sampled every 160ms)")
        Log.i(tag, "2. DETECTED FACES: $totalDetectedFaces total faces across all frames")
        Log.i(tag, "3. FACE CROPS & EMBEDDINGS:")
        Log.i(tag, "   - Square Face Crops: $validCropsCount valid square face crops")
        Log.i(tag, "   - TFLite Embeddings: $embeddingsCount valid 192-d L2-normalized embeddings generated")
        Log.i(tag, "4. APPEARANCE TRACKS: ${rawTracks.size} continuous appearance segments")
        for (t in rawTracks) {
            Log.i(
                tag,
                "   - Track ${t.trackId}: ${t.frames.size} frames (${t.startTimeMs}ms - ${t.endTimeMs}ms, duration ${t.durationMs}ms)"
            )
        }
        Log.i(tag, "5. IDENTITY CLUSTERS: ${clusteredTrackGroups.size} unique person identities created")
        for ((idx, group) in clusteredTrackGroups.withIndex()) {
            val trackIds = group.map { it.trackId }
            val totalFrames = group.sumOf { it.frames.size }
            Log.i(tag, "   - Identity ${idx + 1} ('Person ${idx + 1}'): tracks $trackIds, total frames: $totalFrames")
        }
        Log.i(tag, "6. REPRESENTATIVE SHOTS:")
        for (id in identities) {
            val shot = id.bestShot
            val score = representativeShotSelector.calculateDetailedScore(shot)
            Log.i(
                tag,
                "   - ${id.name}: best frameIndex=${shot.frameIndex}, timestampMs=${shot.timestampMs}ms, qualityScore=${String.format("%.4f", score)}"
            )
        }

        Log.i(tag, "7. PAIRWISE COSINE DISTANCE MATRIX (Representative Embeddings):")
        if (identities.isNotEmpty()) {
            val n = identities.size
            val header = StringBuilder("               ")
            for (i in 1..n) {
                header.append(String.format(" [P%d]  ", i))
            }
            Log.i(tag, header.toString())

            for (i in 0 until n) {
                val row = StringBuilder(String.format("[Person %d]   ", i + 1))
                val embI = identities[i].bestShot.embedding
                for (j in 0 until n) {
                    val embJ = identities[j].bestShot.embedding
                    if (embI != null && embJ != null) {
                        val dist = identityClusterer.cosineDistance(embI, embJ)
                        row.append(String.format(" %.4f", dist))
                    } else {
                        row.append("   N/A  ")
                    }
                }
                Log.i(tag, row.toString())
            }
        }
        Log.i(tag, "==================================================================")
    }

    fun release() {
        faceDetector.close()
        embeddingExtractor.close()
    }
}
