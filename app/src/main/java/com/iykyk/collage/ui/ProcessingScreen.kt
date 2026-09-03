package com.iykyk.collage.ui

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iykyk.collage.model.PipelineStage
import com.iykyk.collage.model.ProcessingProgress
import com.iykyk.collage.ui.theme.InkBlack
import com.iykyk.collage.ui.theme.ProcessingLilac
import com.iykyk.collage.ui.theme.Rose500
import com.iykyk.collage.ui.theme.SubtleText

@Composable
fun ProcessingScreen(
    progress: ProcessingProgress
) {
    val scrollState = rememberScrollState()

    val friendlyHeadline = when (progress.stage) {
        PipelineStage.EXTRACTING_FRAMES, PipelineStage.DETECTING_FACES -> "FINDING YOUR PEOPLE."
        PipelineStage.TRACKING_APPEARANCES, PipelineStage.COMPUTING_EMBEDDINGS -> "UNDERSTANDING FACES."
        PipelineStage.CLUSTERING_IDENTITIES -> "GROUPING EVERYONE."
        PipelineStage.SELECTING_SHOTS -> "PICKING BEST MOMENTS."
        PipelineStage.GENERATING_COLLAGE -> "BUILDING YOUR COLLAGE."
        PipelineStage.COMPLETED -> "ALL DONE!"
        PipelineStage.ERROR -> "SOMETHING WENT WRONG."
        else -> "PROCESSING VIDEO."
    }

    val friendlyMessage = when (progress.stage) {
        PipelineStage.EXTRACTING_FRAMES -> "Sampling high quality video frames..."
        PipelineStage.DETECTING_FACES -> "Looking through the video for familiar faces..."
        PipelineStage.TRACKING_APPEARANCES -> "Tracking continuous movement and appearances..."
        PipelineStage.COMPUTING_EMBEDDINGS -> "Extracting 128D facial feature vectors..."
        PipelineStage.CLUSTERING_IDENTITIES -> "Grouping face clusters into unique identities..."
        PipelineStage.SELECTING_SHOTS -> "Filtering clearest expressions and sharpest photos..."
        PipelineStage.GENERATING_COLLAGE -> "Stitching editorial story grid preview..."
        PipelineStage.COMPLETED -> "Your collage is ready to view!"
        PipelineStage.ERROR -> progress.message
        else -> "Analyzing video..."
    }

    val progressFraction by animateFloatAsState(
        targetValue = progress.progressFraction.coerceIn(0f, 1f),
        label = "progressAnim"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ProcessingLilac)
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Top App Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "PROCESSING",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = InkBlack.copy(alpha = 0.6f)
            )
        }

        Spacer(modifier = Modifier.height(36.dp))

        // Header & Description
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = friendlyHeadline,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = InkBlack,
                lineHeight = 36.sp,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = friendlyMessage,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = InkBlack.copy(alpha = 0.8f)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Progress Box Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White.copy(alpha = 0.45f))
                .border(1.dp, Color.Black.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "PROGRESS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    color = InkBlack.copy(alpha = 0.6f)
                )

                Text(
                    text = "${(progressFraction * 100).toInt()}%",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkBlack
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Thin Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.1f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressFraction.coerceAtLeast(0.02f))
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(InkBlack)
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Stage Checklist
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "STAGE CHECKLIST",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = InkBlack.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Step 1: Finding faces
            StageChecklistItem(
                stepNumber = 1,
                title = "Finding faces",
                isCompleted = progress.stage.ordinal > PipelineStage.DETECTING_FACES.ordinal,
                isActive = progress.stage == PipelineStage.EXTRACTING_FRAMES || progress.stage == PipelineStage.DETECTING_FACES
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Step 2: Understanding faces
            StageChecklistItem(
                stepNumber = 2,
                title = "Understanding faces",
                isCompleted = progress.stage.ordinal > PipelineStage.COMPUTING_EMBEDDINGS.ordinal,
                isActive = progress.stage == PipelineStage.TRACKING_APPEARANCES || progress.stage == PipelineStage.COMPUTING_EMBEDDINGS
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Step 3: Grouping appearances
            StageChecklistItem(
                stepNumber = 3,
                title = "Grouping appearances",
                isCompleted = progress.stage.ordinal > PipelineStage.CLUSTERING_IDENTITIES.ordinal,
                isActive = progress.stage == PipelineStage.CLUSTERING_IDENTITIES
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Step 4: Choosing best moments
            StageChecklistItem(
                stepNumber = 4,
                title = "Choosing best moments",
                isCompleted = progress.stage.ordinal > PipelineStage.SELECTING_SHOTS.ordinal,
                isActive = progress.stage == PipelineStage.SELECTING_SHOTS
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Step 5: Building your collage
            StageChecklistItem(
                stepNumber = 5,
                title = "Building your collage",
                isCompleted = progress.stage == PipelineStage.COMPLETED,
                isActive = progress.stage == PipelineStage.GENERATING_COLLAGE
            )
        }

        if (progress.errorDetails != null) {
            Spacer(modifier = Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Rose500.copy(alpha = 0.15f))
                    .border(1.dp, Rose500.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = Rose500,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = progress.errorDetails,
                        color = Rose500,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun StageChecklistItem(
    stepNumber: Int,
    title: String,
    isCompleted: Boolean,
    isActive: Boolean
) {
    val containerBg = when {
        isActive -> Color.White.copy(alpha = 0.75f)
        isCompleted -> Color.White.copy(alpha = 0.35f)
        else -> Color.White.copy(alpha = 0.15f)
    }

    val borderColor = when {
        isActive -> Color.Black.copy(alpha = 0.15f)
        isCompleted -> Color.Black.copy(alpha = 0.05f)
        else -> Color.Transparent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(containerBg)
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(22.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                isCompleted -> {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(InkBlack),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = ProcessingLilac,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
                isActive -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = InkBlack,
                        strokeWidth = 2.5.dp
                    )
                }
                else -> {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .border(1.dp, InkBlack.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$stepNumber",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = InkBlack.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = if (isActive) FontWeight.Bold else if (isCompleted) FontWeight.SemiBold else FontWeight.Medium,
            color = if (isActive || isCompleted) InkBlack else InkBlack.copy(alpha = 0.45f)
        )
    }
}
