package com.iykyk.collage.collage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import com.iykyk.collage.config.FaceConfig
import com.iykyk.collage.model.CollageResult
import com.iykyk.collage.model.PersonIdentity
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

class CollageRenderer(private val context: Context) {

    fun renderCollage(
        identities: List<PersonIdentity>,
        canvasWidth: Int = 1080,
        canvasHeight: Int = 1920
    ): Bitmap {
        val n = max(1, identities.size)
        val layout = getLayoutConfig(n, canvasWidth)

        return when {
            layout.columns == 1 && n == 1 -> renderSinglePortrait(identities, canvasWidth, canvasHeight)
            else -> renderGridLayout(identities, canvasWidth, canvasHeight, layout)
        }
    }

    private fun getLayoutConfig(count: Int, width: Int): LayoutConfig {
        return when {
            count == 1 -> LayoutConfig(columns = 1, aspectRatio = 4f / 5f)
            count <= 4 -> LayoutConfig(columns = 2, aspectRatio = 4f / 5f)
            count <= 9 -> LayoutConfig(columns = 3, aspectRatio = 4f / 5f)
            else -> LayoutConfig(columns = if (width < 600) 2 else 4, aspectRatio = 4f / 5f)
        }
    }

    private fun renderGridLayout(
        identities: List<PersonIdentity>,
        canvasWidth: Int,
        canvasHeight: Int,
        layout: LayoutConfig
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        drawDarkBackground(canvas, canvasWidth, canvasHeight)

        val headerTop = 100f
        drawHeader(canvas, headerTop, identities, canvasWidth)

        val gridTop = headerTop + 180f
        val gridBottom = canvasHeight - 140f
        val gridLeft = 40f
        val gridRight = canvasWidth - 40f
        val gridWidth = gridRight - gridLeft
        val gridHeight = gridBottom - gridTop

        val cols = layout.columns
        val rows = ceil(identities.size.toDouble() / cols).toInt()
        val spacing = 24f

        val tileWidth = (gridWidth - (cols - 1) * spacing) / cols
        val tileHeight = (gridHeight - (rows - 1) * spacing) / rows
        val cardAspect = FaceConfig.cropTargetAspectRatio
        val finalTileHeight = min(tileWidth / cardAspect, tileHeight)

        val candyColors = listOf("#FF2490", "#25A9E8", "#FFD83D", "#A8F02D", "#FF6B9D", "#4ECDC4")

        for ((index, identity) in identities.withIndex()) {
            val col = index % cols
            val row = index / cols

            val tileLeft = gridLeft + col * (tileWidth + spacing)
            val tileTop = gridTop + row * (finalTileHeight + spacing)
            val tileRight = tileLeft + tileWidth
            val tileBottom = tileTop + finalTileHeight

            drawPersonTile(
                canvas = canvas,
                identity = identity,
                rect = RectF(tileLeft, tileTop, tileRight, tileBottom),
                accentColorHex = candyColors[index % candyColors.size]
            )
        }

        drawFooter(canvas, canvasWidth, canvasHeight)
        return bitmap
    }

    private fun renderSinglePortrait(
        identities: List<PersonIdentity>,
        canvasWidth: Int,
        canvasHeight: Int
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        drawDarkBackground(canvas, canvasWidth, canvasHeight)

        val identity = identities.first()
        val srcBitmap = identity.croppedFaceBitmap

        val targetAspect = FaceConfig.cropTargetAspectRatio
        val photoWidth = canvasWidth * 0.85f
        val photoHeight = photoWidth / targetAspect
        val photoLeft = (canvasWidth - photoWidth) / 2f
        val photoTop = (canvasHeight - photoHeight) / 2f
        val photoRight = photoLeft + photoWidth
        val photoBottom = photoTop + photoHeight

        canvas.drawRect(photoLeft, photoTop, photoRight, photoBottom, Paint().apply { color = Color.parseColor("#1A1A1A") })

        val srcRect = computeSrcRect(srcBitmap, RectF(photoLeft, photoTop, photoRight, photoBottom))
        canvas.drawBitmap(srcBitmap, srcRect, RectF(photoLeft, photoTop, photoRight, photoBottom), Paint(Paint.FILTER_BITMAP_FLAG))

        val labelY = photoBottom + 40f
        val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            textSize = 36f
        }
        canvas.drawText(identity.name.lowercase(), photoLeft + 20f, labelY, namePaint)

        val countPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#A8A8A8")
            typeface = android.graphics.Typeface.DEFAULT
            textSize = 28f
        }
        canvas.drawText("  ${identity.totalAppearances}×", photoLeft + 20f, labelY + 40f, countPaint)

        drawFooter(canvas, canvasWidth, canvasHeight)
        return bitmap
    }

    private fun computeSrcRect(srcBitmap: Bitmap, destRect: RectF): Rect {
        val bitmapAspect = srcBitmap.width.toFloat() / srcBitmap.height.toFloat()
        val rectAspect = destRect.width() / destRect.height()
        return if (bitmapAspect > rectAspect) {
            val targetW = (srcBitmap.height * rectAspect).toInt()
            val left = (srcBitmap.width - targetW) / 2
            Rect(left, 0, left + targetW, srcBitmap.height)
        } else {
            val targetH = (srcBitmap.width / rectAspect).toInt()
            val top = (srcBitmap.height - targetH) / 2
            Rect(0, top, srcBitmap.width, top + targetH)
        }
    }

    private fun drawPersonTile(
        canvas: Canvas,
        identity: PersonIdentity,
        rect: RectF,
        accentColorHex: String = "#FF2490",
        cornerRadius: Float = 20f
    ) {
        canvas.save()
        val path = Path().apply {
            addRoundRect(rect, cornerRadius, cornerRadius, Path.Direction.CW)
        }
        canvas.clipPath(path)

        val cardBgPaint = Paint().apply { color = Color.parseColor("#242424") }
        canvas.drawRect(rect, cardBgPaint)

        val srcBitmap = identity.croppedFaceBitmap
        val srcRect = computeSrcRect(srcBitmap, rect)
        canvas.drawBitmap(srcBitmap, srcRect, rect, Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG))

        val gradientPaint = Paint().apply {
            shader = LinearGradient(
                rect.left, rect.bottom - rect.height() * 0.4f,
                rect.left, rect.bottom,
                intArrayOf(0, 0xCC000000.toInt()),
                floatArrayOf(0.0f, 1.0f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(rect.left, rect.bottom - rect.height() * 0.4f, rect.right, rect.bottom, gradientPaint)
        canvas.restore()

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 4f
            color = Color.parseColor(accentColorHex)
        }
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, borderPaint)

        val labelY = rect.bottom - 50f
        val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            textSize = 22f
        }
        canvas.drawText(identity.name.lowercase(), rect.left + 16f, labelY, namePaint)

        val countPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#A8A8A8")
            typeface = android.graphics.Typeface.DEFAULT
            textSize = 18f
        }
        canvas.drawText("  ${identity.totalAppearances}×", rect.left + 16f, labelY + 22f, countPaint)
    }

    private fun drawDarkBackground(canvas: Canvas, canvasWidth: Int, canvasHeight: Int) {
        val bgPaint = Paint().apply { color = Color.parseColor("#080808") }
        canvas.drawRect(0f, 0f, canvasWidth.toFloat(), canvasHeight.toFloat(), bgPaint)
    }

    private fun drawHeader(canvas: Canvas, headerTop: Float, identities: List<PersonIdentity>, canvasWidth: Int) {
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            textSize = 58f
        }
        canvas.drawText("cameo", 80f, headerTop + 40f, textPaint)

        val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FF2490")
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            textSize = 34f
        }
        canvas.drawText("unique person collage", 80f, headerTop + 90f, subtitlePaint)

        val totalAppearances = identities.sumOf { it.totalAppearances }
        val metaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#A8A8A8")
            typeface = android.graphics.Typeface.DEFAULT
            textSize = 30f
        }
        canvas.drawText("${identities.size} people • $totalAppearances appearances", 80f, headerTop + 140f, metaPaint)
    }

    private fun drawFooter(canvas: Canvas, canvasWidth: Int, canvasHeight: Int) {
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#A8A8A8")
            typeface = android.graphics.Typeface.DEFAULT
            textSize = 26f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("created on-device with cameo", canvasWidth / 2f, canvasHeight - 50f, footerPaint)
    }
}

data class LayoutConfig(
    val columns: Int,
    val aspectRatio: Float
)
