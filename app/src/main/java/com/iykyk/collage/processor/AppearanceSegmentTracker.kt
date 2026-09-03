package com.iykyk.collage.processor

import android.graphics.Rect
import com.iykyk.collage.model.AppearanceTrack
import com.iykyk.collage.model.FaceFrameInfo
import kotlin.math.max
import kotlin.math.min

class AppearanceSegmentTracker {

    fun trackAppearances(
        allFrameFaces: Map<Int, List<FaceFrameInfo>>
    ): List<AppearanceTrack> {
        val activeTracks = mutableListOf<MutableTrack>()
        val completedTracks = mutableListOf<AppearanceTrack>()
        var nextTrackId = 1

        val sortedFrameIndices = allFrameFaces.keys.sorted()

        for (frameIdx in sortedFrameIndices) {
            val facesInFrame = allFrameFaces[frameIdx] ?: continue

            val assignedTrackIndices = HashSet<Int>()
            val assignedFaceIndices = HashSet<Int>()

            for ((faceIdx, face) in facesInFrame.withIndex()) {
                var bestTrackIdx = -1
                var bestIou = 0.0f

                for ((trackIdx, track) in activeTracks.withIndex()) {
                    if (assignedTrackIndices.contains(trackIdx)) continue

                    val lastFace = track.frames.last()
                    val timeDeltaMs = face.timestampMs - lastFace.timestampMs

                    if (timeDeltaMs <= 600L) {
                        val iou = calculateIoU(face.boundingBox, lastFace.boundingBox)
                        if (iou > 0.22f && iou > bestIou) {
                            bestIou = iou
                            bestTrackIdx = trackIdx
                        }
                    }
                }

                if (bestTrackIdx != -1) {
                    activeTracks[bestTrackIdx].frames.add(face)
                    assignedTrackIndices.add(bestTrackIdx)
                    assignedFaceIndices.add(faceIdx)
                }
            }

            val tracksToRemove = mutableListOf<Int>()
            for ((trackIdx, track) in activeTracks.withIndex()) {
                if (!assignedTrackIndices.contains(trackIdx)) {
                    val lastTimestamp = track.frames.last().timestampMs
                    val currentTimestamp = facesInFrame.firstOrNull()?.timestampMs ?: 0L
                    if (currentTimestamp - lastTimestamp > 600L) {
                        tracksToRemove.add(trackIdx)
                    }
                }
            }

            for (idx in tracksToRemove.sortedDescending()) {
                val closedTrack = activeTracks.removeAt(idx)
                val appTrack = convertToAppearanceTrack(closedTrack)
                if (appTrack != null) {
                    completedTracks.add(appTrack)
                }
            }

            for ((faceIdx, face) in facesInFrame.withIndex()) {
                if (!assignedFaceIndices.contains(faceIdx)) {
                    activeTracks.add(
                        MutableTrack(
                            trackId = nextTrackId++,
                            frames = mutableListOf(face)
                        )
                    )
                }
            }
        }

        for (track in activeTracks) {
            val appTrack = convertToAppearanceTrack(track)
            if (appTrack != null) {
                completedTracks.add(appTrack)
            }
        }

        return completedTracks
    }

    private fun convertToAppearanceTrack(track: MutableTrack): AppearanceTrack? {
        if (track.frames.size < 2) {
            
            return null
        }

        val startMs = track.frames.first().timestampMs
        val endMs = track.frames.last().timestampMs
        val durationMs = endMs - startMs

        if (durationMs < 300L && track.frames.size < 3) {
            return null
        }

        val avgSharpness = track.frames.map { it.sharpnessScore }.average()
        if (avgSharpness < 15.0) {
            
            return null
        }

        return AppearanceTrack(
            trackId = track.trackId,
            frames = track.frames,
            startTimeMs = startMs,
            endTimeMs = endMs,
            durationMs = durationMs
        )
    }

    private fun calculateIoU(rect1: Rect, rect2: Rect): Float {
        val intersectionLeft = max(rect1.left, rect2.left)
        val intersectionTop = max(rect1.top, rect2.top)
        val intersectionRight = min(rect1.right, rect2.right)
        val intersectionBottom = min(rect1.bottom, rect2.bottom)

        val intersectionW = max(0, intersectionRight - intersectionLeft)
        val intersectionH = max(0, intersectionBottom - intersectionTop)
        val intersectionArea = intersectionW * intersectionH

        val area1 = rect1.width() * rect1.height()
        val area2 = rect2.width() * rect2.height()
        val unionArea = area1 + area2 - intersectionArea

        if (unionArea <= 0) return 0.0f
        return intersectionArea.toFloat() / unionArea.toFloat()
    }

    private data class MutableTrack(
        val trackId: Int,
        val frames: MutableList<FaceFrameInfo>
    )
}
