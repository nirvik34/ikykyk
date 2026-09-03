package com.iykyk.collage.processor

import android.util.Log
import com.iykyk.collage.model.AppearanceTrack
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

class IdentityClusterer(
    private val distanceThreshold: Float = 0.44f
) {

    companion object {
        private const val TAG = "IdentityClusterer"
    }

    fun clusterIdentities(tracks: List<AppearanceTrack>): List<List<AppearanceTrack>> {
        if (tracks.isEmpty()) return emptyList()
        if (tracks.size == 1) return listOf(tracks)

        Log.d(TAG, "[5] Starting identity clustering for ${tracks.size} appearance tracks with threshold=$distanceThreshold")

        // Step 1: Compute quality-weighted prototype embedding for each track
        for (track in tracks) {
            val frameEmbeddings = mutableListOf<Pair<FloatArray, Float>>()
            for (frame in track.frames) {
                val emb = frame.embedding ?: continue
                val q = max(0.1f, frame.overallQualityScore)
                // Square quality score to give strong preference to high quality front-facing frames
                frameEmbeddings.add(emb to (q * q))
            }

            if (frameEmbeddings.isNotEmpty()) {
                val dim = frameEmbeddings.first().first.size
                val weightedSum = FloatArray(dim)
                var totalWeight = 0.0f
                for ((emb, weight) in frameEmbeddings) {
                    for (i in 0 until dim) {
                        weightedSum[i] += emb[i] * weight
                    }
                    totalWeight += weight
                }
                if (totalWeight > 0f) {
                    track.meanEmbedding = l2Normalize(weightedSum)
                }
            }
            Log.d(
                TAG,
                "Track ${track.trackId}: ${track.frames.size} frames (${track.startTimeMs}ms-${track.endTimeMs}ms), hasEmbedding=${track.meanEmbedding != null}"
            )
        }

        val validTracks = tracks.filter { it.meanEmbedding != null }
        if (validTracks.isEmpty()) return tracks.map { listOf(it) }

        // Start each track in its own cluster
        val clusters = validTracks.map { mutableListOf(it) }.toMutableList()

        // Step 2: Agglomerative clustering with co-occurrence hard negative constraints
        var iteration = 0
        while (clusters.size > 1) {
            iteration++
            var minDistance = Float.MAX_VALUE
            var bestI = -1
            var bestJ = -1

            for (i in 0 until clusters.size) {
                for (j in i + 1 until clusters.size) {
                    val dist = calculateClusterDistance(clusters[i], clusters[j])
                    if (dist < minDistance) {
                        minDistance = dist
                        bestI = i
                        bestJ = j
                    }
                }
            }

            if (bestI != -1 && bestJ != -1 && minDistance <= distanceThreshold) {
                Log.i(
                    TAG,
                    "Iter $iteration: Merging cluster $bestI and $bestJ (distance=${String.format("%.4f", minDistance)} <= $distanceThreshold)"
                )
                clusters[bestI].addAll(clusters[bestJ])
                clusters.removeAt(bestJ)
            } else {
                Log.i(
                    TAG,
                    "Clustering converged at iteration $iteration: Best non-cooccurring pair distance=${if (minDistance < Float.MAX_VALUE) String.format("%.4f", minDistance) else "INF"} > threshold $distanceThreshold"
                )
                break
            }
        }

        Log.i(TAG, "[6] Final clusters created: ${clusters.size}")
        for ((idx, cluster) in clusters.withIndex()) {
            val trackIds = cluster.map { it.trackId }
            val totalFrames = cluster.sumOf { it.frames.size }
            Log.i(TAG, "  Identity ${idx + 1}: ${cluster.size} tracks $trackIds, $totalFrames total frames")
        }

        return clusters
    }

    private fun calculateClusterDistance(
        cluster1: List<AppearanceTrack>,
        cluster2: List<AppearanceTrack>
    ): Float {
        // Enforce hard-negative constraint: co-occurring tracks in the same frame/time window MUST NEVER merge
        if (hasCoOccurrence(cluster1, cluster2)) {
            return Float.MAX_VALUE
        }

        val centroid1 = computeClusterCentroid(cluster1) ?: return Float.MAX_VALUE
        val centroid2 = computeClusterCentroid(cluster2) ?: return Float.MAX_VALUE

        val centroidDist = cosineDistance(centroid1, centroid2)

        // Find minimum pairwise distance between tracks of cluster1 and cluster2
        var minTrackDist = Float.MAX_VALUE
        for (t1 in cluster1) {
            val e1 = t1.meanEmbedding ?: continue
            for (t2 in cluster2) {
                val e2 = t2.meanEmbedding ?: continue
                val d = cosineDistance(e1, e2)
                if (d < minTrackDist) {
                    minTrackDist = d
                }
            }
        }

        return if (minTrackDist < Float.MAX_VALUE) {
            0.5f * centroidDist + 0.5f * minTrackDist
        } else {
            centroidDist
        }
    }

    private fun hasCoOccurrence(
        cluster1: List<AppearanceTrack>,
        cluster2: List<AppearanceTrack>
    ): Boolean {
        // Check exact frame index overlap
        val frames1 = cluster1.flatMap { it.frames }.map { it.frameIndex }.toSet()
        val frames2 = cluster2.flatMap { it.frames }.map { it.frameIndex }.toSet()
        if (frames1.intersect(frames2).isNotEmpty()) {
            return true
        }

        // Check time window overlap (>= 80ms)
        for (t1 in cluster1) {
            for (t2 in cluster2) {
                val overlapStart = max(t1.startTimeMs, t2.startTimeMs)
                val overlapEnd = min(t1.endTimeMs, t2.endTimeMs)
                if (overlapEnd - overlapStart >= 80L) {
                    return true
                }
            }
        }

        return false
    }

    private fun computeClusterCentroid(cluster: List<AppearanceTrack>): FloatArray? {
        val trackEmbeddings = cluster.mapNotNull { it.meanEmbedding }
        if (trackEmbeddings.isEmpty()) return null
        val dim = trackEmbeddings.first().size
        val sum = FloatArray(dim)
        for (emb in trackEmbeddings) {
            for (i in 0 until dim) {
                sum[i] += emb[i]
            }
        }
        return l2Normalize(sum)
    }

    fun cosineDistance(emb1: FloatArray, emb2: FloatArray): Float {
        var dot = 0.0f
        var norm1 = 0.0f
        var norm2 = 0.0f
        val len = minOf(emb1.size, emb2.size)
        for (i in 0 until len) {
            dot += emb1[i] * emb2[i]
            norm1 += emb1[i] * emb1[i]
            norm2 += emb2[i] * emb2[i]
        }
        val denom = sqrt(norm1) * sqrt(norm2)
        if (denom < 1e-8f) return 1.0f
        val sim = (dot / denom).coerceIn(-1.0f, 1.0f)
        return 1.0f - sim
    }

    private fun l2Normalize(vector: FloatArray): FloatArray {
        var sumSq = 0.0f
        for (v in vector) {
            sumSq += v * v
        }
        val norm = sqrt(sumSq)
        if (norm < 1e-8f) return vector

        val result = FloatArray(vector.size)
        for (i in vector.indices) {
            result[i] = vector[i] / norm
        }
        return result
    }
}
