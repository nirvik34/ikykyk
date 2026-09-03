package com.iykyk.collage.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.UploadFile
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iykyk.collage.ui.theme.Charcoal
import com.iykyk.collage.ui.theme.HotPink
import com.iykyk.collage.ui.theme.LimeGreen
import com.iykyk.collage.ui.theme.PrimaryWhite
import com.iykyk.collage.ui.theme.SkyBlue
import com.iykyk.collage.ui.theme.SoftBlack
import com.iykyk.collage.ui.theme.SoftGray
import com.iykyk.collage.util.SampleVideoHelper

@Composable
fun HomeScreen(
    selectedVideoUri: Uri?,
    selectedVideoName: String?,
    onVideoSelected: (Uri, String) -> Unit,
    onStartProcessing: () -> Unit
) {
    val context = LocalContext.current
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
            .background(SoftBlack)
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(28.dp))

        // Playful App Title
        Text(
            text = "iykyk",
            fontSize = 44.sp,
            fontWeight = FontWeight.Black,
            color = HotPink
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "turn videos into people collages ✨",
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = PrimaryWhite,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "pick a video (< 15s) and let's find everyone in it.",
            fontSize = 14.sp,
            color = SoftGray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Video Upload Dropzone Card (Solid Charcoal #242424)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .clickable { videoPickerLauncher.launch("video/*") },
            colors = CardDefaults.cardColors(containerColor = Charcoal)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(SkyBlue.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.UploadFile,
                        contentDescription = "Upload Video",
                        tint = SkyBlue,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (selectedVideoUri != null) "video selected!" else "choose a video",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryWhite
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = selectedVideoName ?: "tap to pick from device storage",
                    fontSize = 14.sp,
                    color = SoftGray,
                    textAlign = TextAlign.Center
                )

                if (selectedVideoUri != null) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = onStartProcessing,
                        colors = ButtonDefaults.buttonColors(containerColor = HotPink),
                        shape = CircleShape,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = PrimaryWhite)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "create collage",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = PrimaryWhite
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Sample Videos Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Movie, contentDescription = null, tint = SkyBlue, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "try with sample videos",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryWhite
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Sample Cards
        SampleVideoCard(
            title = "sample 1 (5 people)",
            description = "5 unique people • 20 total appearances",
            accentColor = HotPink,
            onClick = {
                val uri = SampleVideoHelper.getSampleVideoUri(context, "sample1.mp4")
                if (uri != null) {
                    onVideoSelected(uri, "sample 1 video")
                }
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        SampleVideoCard(
            title = "sample 2",
            description = "multi-person continuous video",
            accentColor = SkyBlue,
            onClick = {
                val uri = SampleVideoHelper.getSampleVideoUri(context, "sample2.mp4")
                if (uri != null) {
                    onVideoSelected(uri, "sample 2 video")
                }
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        SampleVideoCard(
            title = "sample 3",
            description = "dynamic portrait video",
            accentColor = LimeGreen,
            onClick = {
                val uri = SampleVideoHelper.getSampleVideoUri(context, "sample3.mp4")
                if (uri != null) {
                    onVideoSelected(uri, "sample 3 video")
                }
            }
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun SampleVideoCard(
    title: String,
    description: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Charcoal)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryWhite
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = SoftGray
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(accentColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Test Sample",
                    tint = PrimaryWhite,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
