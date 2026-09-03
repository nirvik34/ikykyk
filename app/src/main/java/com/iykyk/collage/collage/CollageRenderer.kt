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

        // 1. Draw Solid SoftBlack Background (#080808)
        val bgPaint = Paint().apply {
            color = Color.parseColor("#080808")
        }
        canvas.drawRect(0f, 0f, canvasWidth.toFloat(), canvasHeight.toFloat(), bgPaint)

        // Playful background blob aura (HotPink & SkyBlue accents)
        val auraPink = Paint().apply {
            color = Color.parseColor("#FF2490")
            alpha = 35
            isAntiAlias = true
        }
        val auraBlue = Paint().apply {
            color = Color.parseColor("#25A9E8")
            alpha = 35
            isAntiAlias = true
        }
        canvas.drawCircle(canvasWidth * 0.85f, canvasHeight * 0.15f, 380f, auraPink)
        canvas.drawCircle(canvasWidth * 0.15f, canvasHeight * 0.85f, 420f, auraBlue)

        // 2. Draw Header (Lowercase Microcopy)
        val headerTop = 100f
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 58f
        }
        canvas.drawText("iykyk", 80f, headerTop + 40f, textPaint)

        val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FF2490") // HotPink Accent
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 34f
        }
        canvas.drawText("unique person collage", 80f, headerTop + 90f, subtitlePaint)

        val totalAppearances = identities.sumOf { it.totalAppearances }
        val metaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#A8A8A8") // Soft Gray
            typeface = Typeface.DEFAULT
            textSize = 36f
        }
        canvas.drawText(
            "${identities.size} people detected • $totalAppearances appearances",
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
        val candyColors = listOf("#FF2490", "#25A9E8", "#FFD83D", "#A8F02D")
        for ((index, identity) in identities.withIndex()) {
            val col = index % cols
            val row = index / cols

            val tileLeft = gridLeft + col * (tileWidth + spacing)
            val tileTop = gridTop + row * (tileHeight + spacing)
            val tileRight = tileLeft + tileWidth
            val tileBottom = tileTop + tileHeight

            val colorHex = candyColors[index % candyColors.size]

            drawPersonTile(
                canvas = canvas,
                identity = identity,
                rect = RectF(tileLeft, tileTop, tileRight, tileBottom),
                accentColorHex = colorHex
            )
        }

        // 5. Draw Footer Watermark
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#A8A8A8") // Soft Gray
            typeface = Typeface.DEFAULT
            textSize = 30f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            "created on-device with iykyk • on-device ml",
            canvasWidth / 2f, canvasHeight - 70f, footerPaint
        )


        return bitmap
    }

    private fun drawPersonTile(
        canvas: Canvas,
        identity: PersonIdentity,
        rect: RectF,
        accentColorHex: String = "#FF2490"
    ) {
        val cornerRadius = 36f

        // Save canvas state for clipped rounded rectangle drawing
        canvas.save()
        val path = Path().apply {
            addRoundRect(rect, cornerRadius, cornerRadius, Path.Direction.CW)
        }
        canvas.clipPath(path)

        // Draw Charcoal Card Background
        val cardBgPaint = Paint().apply {
            color = Color.parseColor("#242424")
        }
        canvas.drawRect(rect, cardBgPaint)

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
                intArrayOf(Color.TRANSPARENT, Color.parseColor("#E6080808")),
                floatArrayOf(0.0f, 1.0f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(rect.left, rect.bottom - rect.height() * 0.45f, rect.right, rect.bottom, gradientPaint)

        canvas.restore() // Restore unclipped canvas for border and overlays

        // Draw Candy Accent Border
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 6f
            color = Color.parseColor(accentColorHex)
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
            color = Color.parseColor("#FA242424") // Charcoal
        }
        canvas.drawRoundRect(badgeRect, 20f, 20f, badgeBgPaint)

        val badgeBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
            color = Color.parseColor(accentColorHex)
        }
        canvas.drawRoundRect(badgeRect, 20f, 20f, badgeBorderPaint)

        // Text inside Pill Badge
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 28f
        }
        val countText = "${identity.name.lowercase()} • ${identity.totalAppearances} appearances"
        canvas.drawText(
            countText,
            badgeRect.left + 24f,
            badgeRect.centerY() + 10f,
            titlePaint
        )
    }
}

