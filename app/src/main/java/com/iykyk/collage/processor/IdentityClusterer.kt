package com.iykyk.collage.processor

import com.iykyk.collage.model.AppearanceTrack
import kotlin.math.sqrt

class IdentityClusterer(
    private val distanceThreshold: Float = 0.40f // Cosine distance threshold for MobileFaceNet embeddings
) {

    /**
     * Clusters appearance tracks into distinct unique person identities.
     * Returns a list of clusters (each cluster is a list of AppearanceTracks for 1 person).
     */
    fun clusterIdentities(tracks: List<AppearanceTrack>): List<List<AppearanceTrack>> {
        if (tracks.isEmpty()) return emptyList()
        if (tracks.size == 1) return listOf(tracks)

        // Compute mean L2-normalized embedding for each track
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
                track.meanEmbedding = l2Normalize(mean)
            }
        }

        // Initialize each track into its own cluster
        val clusters = tracks.map { mutableListOf(it) }.toMutableList()

        while (true) {
            var minDistance = Float.MAX_VALUE
            var bestI = -1
            var bestJ = -1

            // Find closest pair of clusters (Average linkage distance)
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

            // Merge if minimum distance is within similarity threshold
            if (bestI != -1 && bestJ != -1 && minDistance <= distanceThreshold) {
                clusters[bestI].addAll(clusters[bestJ])
                clusters.removeAt(bestJ)
            } else {
                break
            }
        }

        return clusters
    }

    private fun calculateClusterDistance(
        cluster1: List<AppearanceTrack>,
        cluster2: List<AppearanceTrack>
    ): Float {
        var totalDist = 0.0f
        var count = 0

        for (t1 in cluster1) {
            val emb1 = t1.meanEmbedding ?: continue
            for (t2 in cluster2) {
                val emb2 = t2.meanEmbedding ?: continue
                val dist = cosineDistance(emb1, emb2)
                totalDist += dist
                count++
            }
        }

        if (count == 0) return 1.0f
        return totalDist / count
    }

    private fun cosineDistance(emb1: FloatArray, emb2: FloatArray): Float {
        var dot = 0.0f
        val len = minOf(emb1.size, emb2.size)
        for (i in 0 until len) {
            dot += emb1[i] * emb2[i]
        }
        val sim = dot.coerceIn(-1.0f, 1.0f)
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
