package com.iykyk.collage.processor

import android.util.Log
import com.iykyk.collage.model.AppearanceTrack
import kotlin.math.sqrt

class IdentityClusterer(
    private val distanceThreshold: Float = 0.45f
) {

    companion object {
        private const val TAG = "IdentityClusterer"
    }

    fun clusterIdentities(tracks: List<AppearanceTrack>): List<List<AppearanceTrack>> {
        if (tracks.isEmpty()) return emptyList()
        if (tracks.size == 1) return listOf(tracks)

        Log.d(TAG, "Clustering ${tracks.size} tracks with threshold=$distanceThreshold")

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
            Log.d(TAG, "Track ${track.trackId}: ${track.frames.size} frames, embedding=${track.meanEmbedding != null}")
        }

        val clusters = tracks.map { mutableListOf(it) }.toMutableList()

        while (true) {
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

            Log.d(TAG, "Best merge candidate: distance=$minDistance (threshold=$distanceThreshold)")

            if (bestI != -1 && bestJ != -1 && minDistance <= distanceThreshold) {
                Log.d(TAG, "Merging cluster $bestI and $bestJ (distance=$minDistance)")
                clusters[bestI].addAll(clusters[bestJ])
                clusters.removeAt(bestJ)
            } else {
                break
            }
        }

        Log.d(TAG, "Final clusters: ${clusters.size}")
        return clusters
    }

    private fun calculateClusterDistance(
        cluster1: List<AppearanceTrack>,
        cluster2: List<AppearanceTrack>
    ): Float {
        if (hasCoOccurrence(cluster1, cluster2)) {
            return Float.MAX_VALUE
        }

        var minDist = Float.MAX_VALUE

        for (t1 in cluster1) {
            val emb1 = t1.meanEmbedding ?: continue
            for (t2 in cluster2) {
                val emb2 = t2.meanEmbedding ?: continue
                val dist = cosineDistance(emb1, emb2)
                if (dist < minDist) {
                    minDist = dist
                }
            }
        }

        if (minDist == Float.MAX_VALUE) return 1.0f
        return minDist
    }

    private fun hasCoOccurrence(
        cluster1: List<AppearanceTrack>,
        cluster2: List<AppearanceTrack>
    ): Boolean {
        val frames1 = cluster1.flatMap { it.frames }.map { it.frameIndex }.toSet()
        val frames2 = cluster2.flatMap { it.frames }.map { it.frameIndex }.toSet()
        return frames1.intersect(frames2).isNotEmpty()
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
