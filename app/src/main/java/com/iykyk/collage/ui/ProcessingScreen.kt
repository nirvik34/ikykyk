package com.iykyk.collage.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iykyk.collage.model.PipelineStage
import com.iykyk.collage.model.ProcessingProgress
import com.iykyk.collage.ui.theme.Emerald400
import com.iykyk.collage.ui.theme.GlassBorder
import com.iykyk.collage.ui.theme.GlassSurface
import com.iykyk.collage.ui.theme.Indigo500
import com.iykyk.collage.ui.theme.Purple500
import com.iykyk.collage.ui.theme.Rose500
import com.iykyk.collage.ui.theme.Slate400
import com.iykyk.collage.ui.theme.Slate800
import com.iykyk.collage.ui.theme.Slate900

@Composable
fun ProcessingScreen(
    progress: ProcessingProgress
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate900)
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Top Pulsing ML Badge
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(Indigo500.copy(alpha = 0.4f), Color.Transparent))),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(GlassSurface)
                    .border(1.5.dp, GlassBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (progress.stage == PipelineStage.ERROR) {
                    Icon(Icons.Default.Error, contentDescription = null, tint = Rose500, modifier = Modifier.size(32.dp))
                } else {
                    CircularProgressIndicator(
                        progress = { progress.progressFraction.coerceIn(0f, 1f) },
                        modifier = Modifier.size(44.dp),
                        color = Indigo500,
                        strokeWidth = 3.dp,
                        trackColor = Slate800,
                    )
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = null,
                        tint = Indigo500,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = when (progress.stage) {
                PipelineStage.ERROR -> "Processing Failed"
                PipelineStage.COMPLETED -> "Processing Complete!"
                else -> "Processing Video..."
            },
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = progress.message,
            fontSize = 15.sp,
            color = Slate400,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Smooth Progress Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(Slate800)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.progressFraction.coerceIn(0.01f, 1f))
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Brush.horizontalGradient(listOf(Indigo500, Purple500)))
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${(progress.progressFraction * 100).toInt()}%",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Indigo500
            )
            Text(
                text = "On-Device ML Engine",
                fontSize = 13.sp,
                color = Slate400
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Pipeline Stages List Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(GlassSurface)
                .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Text(
                text = "Pipeline Steps",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            PipelineStepRow(
                title = "Frame Sampling & Extraction",
                isActive = progress.stage == PipelineStage.EXTRACTING_FRAMES,
                isCompleted = progress.stage.ordinal > PipelineStage.EXTRACTING_FRAMES.ordinal
            )
            PipelineStepRow(
                title = "ML Kit Face & Pose Detection",
                isActive = progress.stage == PipelineStage.DETECTING_FACES,
                isCompleted = progress.stage.ordinal > PipelineStage.DETECTING_FACES.ordinal
            )
            PipelineStepRow(
                title = "Temporal Appearance Tracking",
                isActive = progress.stage == PipelineStage.TRACKING_APPEARANCES,
                isCompleted = progress.stage.ordinal > PipelineStage.TRACKING_APPEARANCES.ordinal
            )
            PipelineStepRow(
                title = "TFLite Face Embeddings (MobileFaceNet)",
                isActive = progress.stage == PipelineStage.COMPUTING_EMBEDDINGS,
                isCompleted = progress.stage.ordinal > PipelineStage.COMPUTING_EMBEDDINGS.ordinal
            )
            PipelineStepRow(
                title = "Cosine Similarity Clustering",
                isActive = progress.stage == PipelineStage.CLUSTERING_IDENTITIES,
                isCompleted = progress.stage.ordinal > PipelineStage.CLUSTERING_IDENTITIES.ordinal
            )
            PipelineStepRow(
                title = "Representative Shot Selection",
                isActive = progress.stage == PipelineStage.SELECTING_SHOTS,
                isCompleted = progress.stage.ordinal > PipelineStage.SELECTING_SHOTS.ordinal
            )
            PipelineStepRow(
                title = "Story Canvas Collage Rendering",
                isActive = progress.stage == PipelineStage.GENERATING_COLLAGE,
                isCompleted = progress.stage.ordinal > PipelineStage.GENERATING_COLLAGE.ordinal
            )
        }

        if (progress.errorDetails != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Rose500.copy(alpha = 0.15f))
                    .border(1.dp, Rose500.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "Error Details: ${progress.errorDetails}",
                    color = Rose500,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun PipelineStepRow(
    title: String,
    isActive: Boolean,
    isCompleted: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(20.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                isCompleted -> {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Emerald400,
                        modifier = Modifier.size(18.dp)
                    )
                }
                isActive -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Indigo500,
                        strokeWidth = 2.dp
                    )
                }
                else -> {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Slate400.copy(alpha = 0.4f))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = if (isActive || isCompleted) FontWeight.SemiBold else FontWeight.Normal,
            color = when {
                isCompleted -> Color.White
                isActive -> Indigo500
                else -> Slate400
            }
        )
    }
}
