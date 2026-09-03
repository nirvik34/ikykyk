package com.iykyk.collage.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iykyk.collage.R
import com.iykyk.collage.ui.theme.CanvasBg
import com.iykyk.collage.ui.theme.InkBlack
import com.iykyk.collage.ui.theme.LimeBlock
import com.iykyk.collage.ui.theme.OutlineBorder
import com.iykyk.collage.ui.theme.PrimaryWhite
import com.iykyk.collage.ui.theme.SubtleText
import com.iykyk.collage.ui.theme.SurfaceCard

fun Modifier.dashedBorder(
    color: Color,
    strokeWidth: Dp = 2.dp,
    dashLength: Dp = 6.dp,
    gapLength: Dp = 6.dp,
    cornerRadius: Dp = 16.dp
) = this.drawWithContent {
    drawContent()
    val stroke = Stroke(
        width = strokeWidth.toPx(),
        pathEffect = PathEffect.dashPathEffect(
            floatArrayOf(dashLength.toPx(), gapLength.toPx()),
            0f
        )
    )
    drawRoundRect(
        color = color,
        topLeft = Offset.Zero,
        size = size,
        cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx()),
        style = stroke
    )
}

@Composable
fun HomeScreen(
    selectedVideoUri: Uri?,
    selectedVideoName: String?,
    onVideoSelected: (Uri, String) -> Unit,
    onStartProcessing: () -> Unit
) {
    val scrollState = rememberScrollState()

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            onVideoSelected(uri, "Picked Video")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CanvasBg)
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Top Navigation Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.ic_cameo_logo),
                    contentDescription = "Cameo Logo",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "cameo",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkBlack
                )
            }

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(LimeBlock)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "AI CLUSTER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkBlack
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Hero Title & Statement
        Text(
            text = "TURN VIDEOS INTO PEOPLE COLLAGES.",
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = InkBlack,
            lineHeight = 36.sp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "pick a video and let on-device ML find everyone in it.",
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = SubtleText,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Main Video Picker Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Dropzone Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(CanvasBg)
                        .dashedBorder(color = OutlineBorder, strokeWidth = 2.dp, cornerRadius = 20.dp)
                        .clickable { videoPickerLauncher.launch("video/*") },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        if (selectedVideoUri != null) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = InkBlack,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = selectedVideoName ?: "Video Selected",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = InkBlack,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "tap to change video",
                                fontSize = 12.sp,
                                color = SubtleText,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.VideoFile,
                                contentDescription = null,
                                tint = SubtleText,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "+ SELECT VIDEO",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = InkBlack,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "MP4, MOV up to 4K resolution",
                                fontSize = 12.sp,
                                color = SubtleText,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Create Collage Button
                Button(
                    onClick = {
                        if (selectedVideoUri != null) {
                            onStartProcessing()
                        } else {
                            videoPickerLauncher.launch("video/*")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = InkBlack),
                    shape = CircleShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (selectedVideoUri != null) "Create collage" else "Select a Video",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryWhite
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = PrimaryWhite,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
