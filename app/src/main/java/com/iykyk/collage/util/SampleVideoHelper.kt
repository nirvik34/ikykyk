package com.iykyk.collage.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object SampleVideoHelper {

    fun getSampleVideoUri(context: Context, assetFileName: String): Uri? {
        return try {
            val cacheFile = File(context.cacheDir, assetFileName)
            if (!cacheFile.exists() || cacheFile.length() == 0L) {
                context.assets.open("samples/$assetFileName").use { input ->
                    FileOutputStream(cacheFile).use { output ->
                        input.copyTo(output)
                    }
                }
            }
            Uri.fromFile(cacheFile)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
