package com.iykyk.collage.processor

import android.util.Log
import com.iykyk.collage.model.AppearanceTrack
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

class IdentityClusterer(
    private val distanceThreshold: Float = 0.38f
) {

    companion object {
        private const val TAG = "IdentityClusterer"
    }

    fun clusterIdentities(tracks: List<AppearanceTrack>): List<List<AppearanceTrack>> {
        if (tracks.isEmpty()) return emptyList()
        if (tracks.size == 1) return listOf(tracks)

        Log.d(TAG, "[5] Starting identity clustering for ${tracks.size} appearance tracks with threshold=$distanceThreshold")

        for (track in tracks) {
            val embeddings = track.frames.mapNotNull { it.embedding }
            if (embeddings.isNotEmpty()) {
                val dim = embeddings.first().size
                val mean = FloatArray(dim)
                for (emb in embeddings) {
                    for (i in 0 until dim) {
                        mean[i] += emb[i]
                    }
                }
                for (i in 0 until dim) {
                    mean[i] /= embeddings.size.toFloat()
                }
                track.meanEmbedding = l2Normalize(mean)
            }
            Log.d(TAG, "Track ${track.trackId}: ${track.frames.size} frames (${track.startTimeMs}ms-${track.endTimeMs}ms), hasEmbedding=${track.meanEmbedding != null}")
        }

        val validTracks = tracks.filter { it.meanEmbedding != null }
        if (validTracks.isEmpty()) return tracks.map { listOf(it) }

        val clusters = validTracks.map { mutableListOf(it) }.toMutableList()

        while (clusters.size > 1) {
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

            Log.d(TAG, "Best merge candidate: cluster $bestI & $bestJ, distance=$minDistance (threshold=$distanceThreshold)")

            if (bestI != -1 && bestJ != -1 && minDistance <= distanceThreshold) {
                Log.d(TAG, "Merging cluster $bestI and $bestJ (distance=$minDistance <= $distanceThreshold)")
                clusters[bestI].addAll(clusters[bestJ])
                clusters.removeAt(bestJ)
            } else {
                Log.d(TAG, "No further clusters mergeable below threshold $distanceThreshold")
                break
            }
        }

        Log.d(TAG, "[6] Final clusters created: ${clusters.size}")
        for ((idx, cluster) in clusters.withIndex()) {
            val totalFrames = cluster.sumOf { it.frames.size }
            Log.d(TAG, "  Cluster ${idx + 1}: ${cluster.size} tracks, $totalFrames frames")
        }

        return clusters
    }

    private fun calculateClusterDistance(
        cluster1: List<AppearanceTrack>,
        cluster2: List<AppearanceTrack>
    ): Float {
        if (hasCoOccurrence(cluster1, cluster2)) {
            return Float.MAX_VALUE
        }

        val centroid1 = computeClusterCentroid(cluster1) ?: return Float.MAX_VALUE
        val centroid2 = computeClusterCentroid(cluster2) ?: return Float.MAX_VALUE

        return cosineDistance(centroid1, centroid2)
    }

    private fun hasCoOccurrence(
        cluster1: List<AppearanceTrack>,
        cluster2: List<AppearanceTrack>
    ): Boolean {
        val frames1 = cluster1.flatMap { it.frames }.map { it.frameIndex }.toSet()
        val frames2 = cluster2.flatMap { it.frames }.map { it.frameIndex }.toSet()
        if (frames1.intersect(frames2).isNotEmpty()) {
            return true
        }

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
        val embeddings = cluster.mapNotNull { it.meanEmbedding }
        if (embeddings.isEmpty()) return null
        val dim = embeddings.first().size
        val sum = FloatArray(dim)
        for (emb in embeddings) {
            for (i in 0 until dim) {
                sum[i] += emb[i]
            }
        }
        return l2Normalize(sum)
    }

    private fun cosineDistance(emb1: FloatArray, emb2: FloatArray): Float {
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
