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
import com.iykyk.collage.model.LayoutTemplate
import com.iykyk.collage.model.PersonIdentity
import kotlin.math.ceil
import kotlin.math.max

class CollageRenderer(private val context: Context) {

    fun renderCollage(
        identities: List<PersonIdentity>,
        layoutTemplate: LayoutTemplate = LayoutTemplate.EDITORIAL,
        canvasWidth: Int = 1080,
        canvasHeight: Int = 1920
    ): Bitmap {
        return when (layoutTemplate) {
            LayoutTemplate.EDITORIAL -> renderEditorial(identities, canvasWidth, canvasHeight)
            LayoutTemplate.FILM_STRIP -> renderFilmStrip(identities, canvasWidth, canvasHeight)
            LayoutTemplate.POLAROID -> renderPolaroid(identities, canvasWidth, canvasHeight)
            LayoutTemplate.FULL_BLEED -> renderFullBleed(identities, canvasWidth, canvasHeight)
        }
    }

    // -------------------------------------------------------------------
    // EDITORIAL layout (original grid-based layout)
    // -------------------------------------------------------------------
    private fun renderEditorial(
        identities: List<PersonIdentity>,
        canvasWidth: Int,
        canvasHeight: Int
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        drawDarkBackground(canvas, canvasWidth, canvasHeight)
        drawAuraGlows(canvas, canvasWidth, canvasHeight)

        val headerTop = 100f
        drawHeader(canvas, headerTop, identities, canvasWidth)

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

        val candyColors = listOf("#FF2490", "#25A9E8", "#FFD83D", "#A8F02D")
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
                rect = RectF(tileLeft, tileTop, tileRight, tileBottom),
                accentColorHex = candyColors[index % candyColors.size]
            )
        }

        drawFooter(canvas, canvasWidth, canvasHeight)
        return bitmap
    }

    // -------------------------------------------------------------------
    // FILM STRIP layout (horizontal rows stacked vertically)
    // -------------------------------------------------------------------
    private fun renderFilmStrip(
        identities: List<PersonIdentity>,
        canvasWidth: Int,
        canvasHeight: Int
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        drawDarkBackground(canvas, canvasWidth, canvasHeight)

        // Film strip sprocket holes effect
        val sprockerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#333333")
        }
        val holeSize = 18f
        val holeSpacing = 50f
        for (y in 0 until canvasHeight step holeSpacing.toInt()) {
            canvas.drawRoundRect(
                RectF(16f, y.toFloat(), 16f + holeSize, y + holeSize * 1.5f),
                4f, 4f, sprockerPaint
            )
            canvas.drawRoundRect(
                RectF(canvasWidth - 16f - holeSize, y.toFloat(), canvasWidth - 16f, y + holeSize * 1.5f),
                4f, 4f, sprockerPaint
            )
        }

        val headerTop = 80f
        drawHeader(canvas, headerTop, identities, canvasWidth)

        val stripTop = headerTop + 200f
        val stripBottom = canvasHeight - 160f
        val stripLeft = 60f
        val stripRight = canvasWidth - 60f
        val stripWidth = stripRight - stripLeft

        val n = max(1, identities.size)
        val spacing = 20f
        val stripHeight = ((stripBottom - stripTop) - (n - 1) * spacing) / n

        val candyColors = listOf("#FF2490", "#25A9E8", "#FFD83D", "#A8F02D")
        for ((index, identity) in identities.withIndex()) {
            val top = stripTop + index * (stripHeight + spacing)
            val rect = RectF(stripLeft, top, stripRight, top + stripHeight)

            drawPersonTile(
                canvas = canvas,
                identity = identity,
                rect = rect,
                accentColorHex = candyColors[index % candyColors.size],
                cornerRadius = 16f
            )
        }

        drawFooter(canvas, canvasWidth, canvasHeight)
        return bitmap
    }

    // -------------------------------------------------------------------
    // POLAROID layout (scattered polaroid-style cards)
    // -------------------------------------------------------------------
    private fun renderPolaroid(
        identities: List<PersonIdentity>,
        canvasWidth: Int,
        canvasHeight: Int
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Warm cream background
        val bgPaint = Paint().apply { color = Color.parseColor("#F5F0E8") }
        canvas.drawRect(0f, 0f, canvasWidth.toFloat(), canvasHeight.toFloat(), bgPaint)

        val headerTop = 80f
        val darkTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1A1A1A")
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 58f
        }
        canvas.drawText("cameo", 80f, headerTop + 40f, darkTextPaint)

        val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FF2490")
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 34f
        }
        canvas.drawText("polaroid collection", 80f, headerTop + 90f, subtitlePaint)

        val n = max(1, identities.size)
        val polaroidWidth = when {
            n <= 2 -> canvasWidth * 0.7f
            n <= 4 -> canvasWidth * 0.42f
            else -> canvasWidth * 0.42f
        }
        val polaroidHeight = polaroidWidth * 1.25f
        val photoPadding = 24f
        val bottomPadding = 80f

        val cols = if (n <= 2) 1 else 2
        val rows = ceil(n.toDouble() / cols).toInt()

        val totalWidth = cols * polaroidWidth + (cols - 1) * 30f
        val totalHeight = rows * polaroidHeight + (rows - 1) * 30f
        val startX = (canvasWidth - totalWidth) / 2f
        val startY = headerTop + 160f

        val rotations = listOf(-4f, 3f, -2f, 5f, -3f, 1f)

        for ((index, identity) in identities.withIndex()) {
            val col = index % cols
            val row = index / cols
            val px = startX + col * (polaroidWidth + 30f)
            val py = startY + row * (polaroidHeight + 30f)

            canvas.save()
            canvas.rotate(rotations[index % rotations.size], px + polaroidWidth / 2f, py + polaroidHeight / 2f)

            // Polaroid shadow
            val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#30000000")
            }
            canvas.drawRoundRect(
                RectF(px + 6f, py + 6f, px + polaroidWidth + 6f, py + polaroidHeight + 6f),
                8f, 8f, shadowPaint
            )

            // Polaroid white card
            val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
            }
            val cardRect = RectF(px, py, px + polaroidWidth, py + polaroidHeight)
            canvas.drawRoundRect(cardRect, 8f, 8f, cardPaint)

            // Photo area
            val photoRect = RectF(
                px + photoPadding,
                py + photoPadding,
                px + polaroidWidth - photoPadding,
                py + polaroidHeight - bottomPadding
            )

            canvas.save()
            val photoPath = Path().apply {
                addRoundRect(photoRect, 4f, 4f, Path.Direction.CW)
            }
            canvas.clipPath(photoPath)

            val srcBitmap = identity.croppedFaceBitmap
            val bitmapAspect = srcBitmap.width.toFloat() / srcBitmap.height.toFloat()
            val rectAspect = photoRect.width() / photoRect.height()
            val srcRect = if (bitmapAspect > rectAspect) {
                val targetW = (srcBitmap.height * rectAspect).toInt()
                val left = (srcBitmap.width - targetW) / 2
                Rect(left, 0, left + targetW, srcBitmap.height)
            } else {
                val targetH = (srcBitmap.width / rectAspect).toInt()
                val top = (srcBitmap.height - targetH) / 2
                Rect(0, top, srcBitmap.width, top + targetH)
            }
            canvas.drawBitmap(srcBitmap, srcRect, photoRect, Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG))
            canvas.restore()

            // Name label below photo
            val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#333333")
                typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
                textSize = 26f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(
                identity.name.lowercase(),
                px + polaroidWidth / 2f,
                py + polaroidHeight - 24f,
                namePaint
            )

            canvas.restore()
        }

        // Footer
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#999999")
            typeface = Typeface.DEFAULT
            textSize = 28f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            "cameo polaroid collection",
            canvasWidth / 2f, canvasHeight - 70f, footerPaint
        )

        return bitmap
    }

    // -------------------------------------------------------------------
    // FULL BLEED layout (single large hero image with overlay info)
    // -------------------------------------------------------------------
    private fun renderFullBleed(
        identities: List<PersonIdentity>,
        canvasWidth: Int,
        canvasHeight: Int
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        drawDarkBackground(canvas, canvasWidth, canvasHeight)

        if (identities.isEmpty()) {
            drawFooter(canvas, canvasWidth, canvasHeight)
            return bitmap
        }

        // Draw the primary person as full-bleed background
        val primaryIdentity = identities.first()
        val srcBitmap = primaryIdentity.croppedFaceBitmap
        val bitmapAspect = srcBitmap.width.toFloat() / srcBitmap.height.toFloat()
        val canvasAspect = canvasWidth.toFloat() / canvasHeight.toFloat()

        val srcRect = if (bitmapAspect > canvasAspect) {
            val targetW = (srcBitmap.height * canvasAspect).toInt()
            val left = (srcBitmap.width - targetW) / 2
            Rect(left, 0, left + targetW, srcBitmap.height)
        } else {
            val targetH = (srcBitmap.width / canvasAspect).toInt()
            val top = (srcBitmap.height - targetH) / 2
            Rect(0, top, srcBitmap.width, top + targetH)
        }

        val destRect = RectF(0f, 0f, canvasWidth.toFloat(), canvasHeight.toFloat())
        canvas.drawBitmap(srcBitmap, srcRect, destRect, Paint(Paint.FILTER_BITMAP_FLAG))

        // Top gradient
        val topGradient = Paint().apply {
            shader = LinearGradient(
                0f, 0f, 0f, canvasHeight * 0.3f,
                intArrayOf(Color.parseColor("#CC080808"), Color.TRANSPARENT),
                floatArrayOf(0.0f, 1.0f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, canvasWidth.toFloat(), canvasHeight * 0.3f, topGradient)

        // Bottom gradient
        val bottomGradient = Paint().apply {
            shader = LinearGradient(
                0f, canvasHeight * 0.55f, 0f, canvasHeight.toFloat(),
                intArrayOf(Color.TRANSPARENT, Color.parseColor("#E6080808")),
                floatArrayOf(0.0f, 1.0f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, canvasHeight * 0.55f, canvasWidth.toFloat(), canvasHeight.toFloat(), bottomGradient)

        // Header overlay at top
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 58f
        }
        canvas.drawText("cameo", 60f, 120f, titlePaint)

        val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FF2490")
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 34f
        }
        canvas.drawText("full bleed", 60f, 170f, subtitlePaint)

        // Bottom overlay: person info + small thumbnails of other people
        val bottomY = canvasHeight - 300f

        val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 48f
        }
        canvas.drawText(primaryIdentity.name.lowercase(), 60f, bottomY, namePaint)

        val totalAppearances = identities.sumOf { it.totalAppearances }
        val metaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#CCFFFFFF")
            textSize = 30f
        }
        canvas.drawText(
            "${identities.size} people detected  •  $totalAppearances appearances",
            60f, bottomY + 50f, metaPaint
        )

        // Small circular thumbnails of other people at bottom
        if (identities.size > 1) {
            val thumbSize = 80f
            val thumbSpacing = 16f
            val thumbY = bottomY + 80f
            var thumbX = 60f

            val candyColors = listOf("#FF2490", "#25A9E8", "#FFD83D", "#A8F02D")
            for ((index, identity) in identities.drop(1).withIndex()) {
                if (index >= 6) break

                // Ring
                val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = 4f
                    color = Color.parseColor(candyColors[index % candyColors.size])
                }
                canvas.drawCircle(
                    thumbX + thumbSize / 2f,
                    thumbY + thumbSize / 2f,
                    thumbSize / 2f + 2f,
                    ringPaint
                )

                // Circular clip for face
                canvas.save()
                val circlePath = Path().apply {
                    addCircle(thumbX + thumbSize / 2f, thumbY + thumbSize / 2f, thumbSize / 2f, Path.Direction.CW)
                }
                canvas.clipPath(circlePath)

                val thumbBitmap = identity.croppedFaceBitmap
                val thumbSrcRect = Rect(0, 0, thumbBitmap.width, thumbBitmap.height)
                val thumbDestRect = RectF(thumbX, thumbY, thumbX + thumbSize, thumbY + thumbSize)
                canvas.drawBitmap(thumbBitmap, thumbSrcRect, thumbDestRect, Paint(Paint.FILTER_BITMAP_FLAG))
                canvas.restore()

                thumbX += thumbSize + thumbSpacing
            }
        }

        // Footer
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#88FFFFFF")
            typeface = Typeface.DEFAULT
            textSize = 26f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            "created on-device with cameo",
            canvasWidth / 2f, canvasHeight - 40f, footerPaint
        )

        return bitmap
    }

    // -------------------------------------------------------------------
    // Shared helper methods
    // -------------------------------------------------------------------
    private fun drawDarkBackground(canvas: Canvas, canvasWidth: Int, canvasHeight: Int) {
        val bgPaint = Paint().apply { color = Color.parseColor("#080808") }
        canvas.drawRect(0f, 0f, canvasWidth.toFloat(), canvasHeight.toFloat(), bgPaint)
    }

    private fun drawAuraGlows(canvas: Canvas, canvasWidth: Int, canvasHeight: Int) {
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
    }

    private fun drawHeader(
        canvas: Canvas,
        headerTop: Float,
        identities: List<PersonIdentity>,
        canvasWidth: Int
    ) {
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 58f
        }
        canvas.drawText("cameo", 80f, headerTop + 40f, textPaint)

        val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FF2490")
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 34f
        }
        canvas.drawText("unique person collage", 80f, headerTop + 90f, subtitlePaint)

        val totalAppearances = identities.sumOf { it.totalAppearances }
        val metaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#A8A8A8")
            typeface = Typeface.DEFAULT
            textSize = 36f
        }
        canvas.drawText(
            "${identities.size} people detected • $totalAppearances appearances",
            80f, headerTop + 150f, metaPaint
        )
    }

    private fun drawFooter(canvas: Canvas, canvasWidth: Int, canvasHeight: Int) {
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#A8A8A8")
            typeface = Typeface.DEFAULT
            textSize = 30f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            "created on-device with cameo",
            canvasWidth / 2f, canvasHeight - 70f, footerPaint
        )
    }

    private fun drawPersonTile(
        canvas: Canvas,
        identity: PersonIdentity,
        rect: RectF,
        accentColorHex: String = "#FF2490",
        cornerRadius: Float = 36f
    ) {
        canvas.save()
        val path = Path().apply {
            addRoundRect(rect, cornerRadius, cornerRadius, Path.Direction.CW)
        }
        canvas.clipPath(path)

        val cardBgPaint = Paint().apply {
            color = Color.parseColor("#242424")
        }
        canvas.drawRect(rect, cardBgPaint)

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

        canvas.restore()

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 6f
            color = Color.parseColor(accentColorHex)
        }
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, borderPaint)

        val badgeHeight = 64f
        val badgeMargin = 20f
        val badgeRect = RectF(
            rect.left + badgeMargin,
            rect.bottom - badgeMargin - badgeHeight,
            rect.right - badgeMargin,
            rect.bottom - badgeMargin
        )

        val badgeBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FA242424")
        }
        canvas.drawRoundRect(badgeRect, 20f, 20f, badgeBgPaint)

        val badgeBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
            color = Color.parseColor(accentColorHex)
        }
        canvas.drawRoundRect(badgeRect, 20f, 20f, badgeBorderPaint)

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
