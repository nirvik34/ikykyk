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
import android.graphics.Typeface
import com.iykyk.collage.model.PersonIdentity
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.sqrt

class CollageRenderer(private val context: Context) {

    /**
     * Renders a high-resolution 1080x1920 Instagram Story style collage bitmap.
     */
    fun renderCollage(
        identities: List<PersonIdentity>,
        videoTitle: String = "Portrait Video",
        canvasWidth: Int = 1080,
        canvasHeight: Int = 1920
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. Draw Background Gradient (Deep Slate & Glowing Accents)
        val bgPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, canvasWidth.toFloat(), canvasHeight.toFloat(),
                intArrayOf(
                    Color.parseColor("#0F172A"), // Slate 900
                    Color.parseColor("#1E1B4B"), // Indigo 950
                    Color.parseColor("#0F172A")
                ),
                floatArrayOf(0.0f, 0.5f, 1.0f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, canvasWidth.toFloat(), canvasHeight.toFloat(), bgPaint)

        // Decorative background glowing aura
        val auraPaint = Paint().apply {
            color = Color.parseColor("#312E81")
            alpha = 80
            isAntiAlias = true
        }
        canvas.drawCircle(canvasWidth * 0.8f, canvasHeight * 0.2f, 400f, auraPaint)
        canvas.drawCircle(canvasWidth * 0.2f, canvasHeight * 0.8f, 500f, auraPaint)

        // 2. Draw Header
        val headerTop = 100f
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 54f
        }
        canvas.drawText("iykyk", 80f, headerTop + 40f, textPaint)

        val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#A5B4FC") // Indigo 300
            typeface = Typeface.DEFAULT
            textSize = 34f
        }
        canvas.drawText("UNIQUE PERSON COLLAGE", 80f, headerTop + 90f, subtitlePaint)

        val totalAppearances = identities.sumOf { it.totalAppearances }
        val metaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E2E8F0") // Slate 200
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 38f
        }
        canvas.drawText(
            "${identities.size} People Detected • $totalAppearances Total Appearances",
            80f, headerTop + 150f, metaPaint
        )

        // 3. Grid Layout Parameters
        val gridTop = headerTop + 200f
        val gridBottom = canvasHeight - 160f
        val gridLeft = 60f
        val gridRight = canvasWidth - 60f
        val gridWidth = gridRight - gridLeft
        val gridHeight = gridBottom - gridTop

        val n = max(1, identities.size)
        val cols = when {
            n <= 1 -> 1
            n <= 4 -> 2
            else -> 2
        }
        val rows = ceil(n.toDouble() / cols).toInt()

        val spacing = 28f
        val tileWidth = (gridWidth - (cols - 1) * spacing) / cols
        val tileHeight = (gridHeight - (rows - 1) * spacing) / rows

        // 4. Draw Identity Tiles
        for ((index, identity) in identities.withIndex()) {
            val col = index % cols
            val row = index / cols

            val tileLeft = gridLeft + col * (tileWidth + spacing)
            val tileTop = gridTop + row * (tileHeight + spacing)
            val tileRight = tileLeft + tileWidth
            val tileBottom = tileTop + tileHeight

            drawPersonTile(
                canvas = canvas,
                identity = identity,
                rect = RectF(tileLeft, tileTop, tileRight, tileBottom)
            )
        }

        // 5. Draw Footer Watermark
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#94A3B8") // Slate 400
            typeface = Typeface.DEFAULT
            textSize = 30f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            "Created on-device with iykyk • ML Kit & TFLite",
            canvasWidth / 2f, canvasHeight - 70f, footerPaint
        )

        return bitmap
    }

    private fun drawPersonTile(
        canvas: Canvas,
        identity: PersonIdentity,
        rect: RectF
    ) {
        val cornerRadius = 32f

        // Save canvas state for clipped rounded rectangle drawing
        canvas.save()
        val path = Path().apply {
            addRoundRect(rect, cornerRadius, cornerRadius, Path.Direction.CW)
        }
        canvas.clipPath(path)

        // Draw cropped face bitmap scaled to fill tile (Center Crop)
        val srcBitmap = identity.croppedFaceBitmap
        val bitmapAspect = srcBitmap.width.toFloat() / srcBitmap.height.toFloat()
        val rectAspect = rect.width() / rect.height()

        val srcRect = if (bitmapAspect > rectAspect) {
            val targetW = (srcBitmap.height * rectAspect).toInt()
            val left = (srcBitmap.width - targetW) / 2
            Rect(left, 0, left + targetW, srcBitmap.height)
        } else {
            val targetH = (srcBitmap.width / rectAspect).toInt()
            val top = (srcBitmap.height - targetH) / 2
            Rect(0, top, srcBitmap.width, top + targetH)
        }
        canvas.drawBitmap(srcBitmap, srcRect, rect, Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG))

        // Draw subtle bottom gradient overlay behind text
        val gradientPaint = Paint().apply {
            shader = LinearGradient(
                rect.left, rect.bottom - rect.height() * 0.45f,
                rect.left, rect.bottom,
                intArrayOf(Color.TRANSPARENT, Color.parseColor("#D00F172A")),
                floatArrayOf(0.0f, 1.0f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(rect.left, rect.bottom - rect.height() * 0.45f, rect.right, rect.bottom, gradientPaint)

        canvas.restore() // Restore unclipped canvas for border and overlays

        // Draw Glassmorphism Border
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 5f
            color = Color.parseColor("#40FFFFFF") // 25% White border
        }
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, borderPaint)

        // Draw Pill Badge (Person Label & Appearance Count)
        val badgeHeight = 64f
        val badgeMargin = 20f
        val badgeRect = RectF(
            rect.left + badgeMargin,
            rect.bottom - badgeMargin - badgeHeight,
            rect.right - badgeMargin,
            rect.bottom - badgeMargin
        )

        val badgeBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E60F172A") // 90% Slate 900
        }
        canvas.drawRoundRect(badgeRect, 20f, 20f, badgeBgPaint)

        val badgeBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
            color = Color.parseColor("#6366F1") // Indigo Accent
        }
        canvas.drawRoundRect(badgeRect, 20f, 20f, badgeBorderPaint)

        // Text inside Pill Badge
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 28f
        }
        val countText = "${identity.name} • ${identity.totalAppearances} Appearances"
        canvas.drawText(
            countText,
            badgeRect.left + 24f,
            badgeRect.centerY() + 10f,
            titlePaint
        )
    }
}
