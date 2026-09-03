package com.iykyk.collage.ml

import android.content.Context
import android.graphics.Bitmap
import com.iykyk.collage.util.BitmapUtils
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import kotlin.math.sqrt

class TFLiteEmbeddingExtractor(private val context: Context) {

    private var interpreter: Interpreter? = null
    private val modelInputSize = 112 // MobileFaceNet standard input width/height
    private var outputSize = 192    // MobileFaceNet embedding vector dimension

    init {
        try {
            val fileDescriptor = context.assets.openFd("mobilefacenet.tflite")
            val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
            val fileChannel = inputStream.channel
            val startOffset = fileDescriptor.startOffset
            val declaredLength = fileDescriptor.declaredLength
            val modelBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)

            val options = Interpreter.Options().apply {
                setNumThreads(4)
            }
            val interp = Interpreter(modelBuffer, options)
            this.interpreter = interp

            val outputShape = interp.getOutputTensor(0).shape()
            if (outputShape.size >= 2) {
                outputSize = outputShape[1]
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Extracts L2-normalized 192-dimensional face embedding vector for the given cropped face bitmap.
     */
    fun extractEmbedding(faceBitmap: Bitmap): FloatArray {
        val interp = interpreter ?: return FloatArray(outputSize) { 0f }

        val scaled = BitmapUtils.scaleBitmap(faceBitmap, modelInputSize, modelInputSize)
        val imgData = convertBitmapToByteBuffer(scaled)

        val outputArray = Array(1) { FloatArray(outputSize) }
        interp.run(imgData, outputArray)

        if (scaled != faceBitmap) {
            scaled.recycle()
        }

        return l2Normalize(outputArray[0])
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val imgData = ByteBuffer.allocateDirect(1 * modelInputSize * modelInputSize * 3 * 4)
        imgData.order(ByteOrder.nativeOrder())
        imgData.rewind()

        val intValues = IntArray(modelInputSize * modelInputSize)
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        for (i in intValues.indices) {
            val value = intValues[i]
            val r = (value shr 16) and 0xFF
            val g = (value shr 8) and 0xFF
            val b = value and 0xFF

            // Normalize pixels to [-1, 1]
            imgData.putFloat((r - 127.5f) / 128.0f)
            imgData.putFloat((g - 127.5f) / 128.0f)
            imgData.putFloat((b - 127.5f) / 128.0f)
        }

        return imgData
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

    fun close() {
        interpreter?.close()
        interpreter = null
    }
}
