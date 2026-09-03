package com.iykyk.collage.ui

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.iykyk.collage.ui.theme.Charcoal
import com.iykyk.collage.ui.theme.HotPink
import com.iykyk.collage.ui.theme.LimeGreen
import com.iykyk.collage.ui.theme.PrimaryWhite
import com.iykyk.collage.ui.theme.Rose500
import com.iykyk.collage.ui.theme.SkyBlue
import com.iykyk.collage.ui.theme.SoftBlack
import com.iykyk.collage.ui.theme.SoftGray

@Composable
fun ProcessingScreen(
    progress: ProcessingProgress
) {
    val scrollState = rememberScrollState()

    val friendlyMessage = when (progress.stage) {
        PipelineStage.EXTRACTING_FRAMES -> "watching video..."
        PipelineStage.DETECTING_FACES -> "finding faces..."
        PipelineStage.TRACKING_APPEARANCES -> "matching who is who..."
        PipelineStage.COMPUTING_EMBEDDINGS -> "almost got it..."
        PipelineStage.CLUSTERING_IDENTITIES -> "grouping everyone..."
        PipelineStage.SELECTING_SHOTS -> "picking best photos..."
        PipelineStage.GENERATING_COLLAGE -> "making collage..."
        PipelineStage.COMPLETED -> "all done!"
        PipelineStage.ERROR -> "oops, something went wrong"
        else -> "working magic..."
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftBlack)
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(Charcoal),
            contentAlignment = Alignment.Center
        ) {
            if (progress.stage == PipelineStage.ERROR) {
                Icon(Icons.Default.Error, contentDescription = null, tint = Rose500, modifier = Modifier.size(36.dp))
            } else {
                CircularProgressIndicator(
                    progress = { progress.progressFraction.coerceIn(0f, 1f) },
                    modifier = Modifier.size(54.dp),
                    color = HotPink,
                    strokeWidth = 4.dp,
                    trackColor = SoftBlack
                )
                Icon(
                    imageVector = Icons.Default.Face,
                    contentDescription = null,
                    tint = HotPink,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = friendlyMessage,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryWhite,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = progress.message.lowercase(),
            fontSize = 14.sp,
            color = SoftGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(CircleShape)
                .background(Charcoal)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.progressFraction.coerceIn(0.01f, 1f))
                    .height(10.dp)
                    .clip(CircleShape)
                    .background(HotPink)
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
                color = HotPink
            )
            Text(
                text = "on-device analysis",
                fontSize = 13.sp,
                color = SoftGray
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Charcoal)
                .padding(20.dp)
        ) {
            Text(
                text = "progress steps",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryWhite
            )

            Spacer(modifier = Modifier.height(14.dp))

            PipelineStepRow(
                title = "sampling video frames",
                isActive = progress.stage == PipelineStage.EXTRACTING_FRAMES,
                isCompleted = progress.stage.ordinal > PipelineStage.EXTRACTING_FRAMES.ordinal
            )
            PipelineStepRow(
                title = "finding faces & poses",
                isActive = progress.stage == PipelineStage.DETECTING_FACES,
                isCompleted = progress.stage.ordinal > PipelineStage.DETECTING_FACES.ordinal
            )
            PipelineStepRow(
                title = "tracking continuous appearances",
                isActive = progress.stage == PipelineStage.TRACKING_APPEARANCES,
                isCompleted = progress.stage.ordinal > PipelineStage.TRACKING_APPEARANCES.ordinal
            )
            PipelineStepRow(
                title = "extracting face embeddings",
                isActive = progress.stage == PipelineStage.COMPUTING_EMBEDDINGS,
                isCompleted = progress.stage.ordinal > PipelineStage.COMPUTING_EMBEDDINGS.ordinal
            )
            PipelineStepRow(
                title = "grouping unique people",
                isActive = progress.stage == PipelineStage.CLUSTERING_IDENTITIES,
                isCompleted = progress.stage.ordinal > PipelineStage.CLUSTERING_IDENTITIES.ordinal
            )
            PipelineStepRow(
                title = "picking best shots",
                isActive = progress.stage == PipelineStage.SELECTING_SHOTS,
                isCompleted = progress.stage.ordinal > PipelineStage.SELECTING_SHOTS.ordinal
            )
            PipelineStepRow(
                title = "rendering story collage",
                isActive = progress.stage == PipelineStage.GENERATING_COLLAGE,
                isCompleted = progress.stage.ordinal > PipelineStage.GENERATING_COLLAGE.ordinal
            )
        }

        if (progress.errorDetails != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Rose500.copy(alpha = 0.15f))
                    .padding(16.dp)
            ) {
                Text(
                    text = "error: ${progress.errorDetails}",
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
            .padding(vertical = 6.dp),
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
                        tint = LimeGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }
                isActive -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = HotPink,
                        strokeWidth = 2.dp
                    )
                }
                else -> {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(SoftGray.copy(alpha = 0.3f))
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
                isCompleted -> PrimaryWhite
                isActive -> HotPink
                else -> SoftGray
            }
        )
    }
}
