package com.iykyk.collage.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

object BitmapUtils {

    fun cropGenerousPortrait(
        source: Bitmap,
        faceRect: Rect,
        sideMarginFraction: Float = 0.5f,
        topMarginFraction: Float = 0.6f,
        bottomMarginFraction: Float = 0.8f
    ): Bitmap {
        val width = faceRect.width()
        val height = faceRect.height()

        val sidePadding = (width * sideMarginFraction).toInt()
        val topPadding = (height * topMarginFraction).toInt()
        val bottomPadding = (height * bottomMarginFraction).toInt()

        var left = max(0, faceRect.left - sidePadding)
        var top = max(0, faceRect.top - topPadding)
        var right = min(source.width, faceRect.right + sidePadding)
        var bottom = min(source.height, faceRect.bottom + bottomPadding)

        val cropW = max(1, right - left)
        val cropH = max(1, bottom - top)

        return Bitmap.createBitmap(source, left, top, cropW, cropH)
    }

    fun calculateSharpnessScore(bitmap: Bitmap): Float {
        try {
            val width = min(bitmap.width, 120)
            val height = min(bitmap.height, 120)
            val scaled = Bitmap.createScaledBitmap(bitmap, width, height, false)

            val pixels = IntArray(width * height)
            scaled.getPixels(pixels, 0, width, 0, 0, width, height)

            val gray = FloatArray(width * height)
            for (i in pixels.indices) {
                val p = pixels[i]
                val r = (p shr 16) and 0xFF
                val g = (p shr 8) and 0xFF
                val b = p and 0xFF
                gray[i] = 0.299f * r + 0.587f * g + 0.114f * b
            }

            var sum = 0.0
            var sumSq = 0.0
            var count = 0

            for (y in 1 until height - 1) {
                for (x in 1 until width - 1) {
                    val idx = y * width + x
                    
                    val laplacian = (
                        gray[idx - width] +
                        gray[idx + width] +
                        gray[idx - 1] +
                        gray[idx + 1] -
                        4f * gray[idx]
                    ).toDouble()

                    sum += laplacian
                    sumSq += laplacian * laplacian
                    count++
                }
            }

            if (count == 0) return 0f
            val mean = sum / count
            val variance = (sumSq / count) - (mean * mean)

            if (scaled != bitmap) scaled.recycle()

            return max(0.0, variance).toFloat()
        } catch (e: Exception) {
            return 50.0f
        }
    }

    fun scaleBitmap(bitmap: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
    }
}
