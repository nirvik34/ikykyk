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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iykyk.collage.ui.theme.GlassBorder
import com.iykyk.collage.ui.theme.GlassSurface
import com.iykyk.collage.ui.theme.Indigo500
import com.iykyk.collage.ui.theme.Purple500
import com.iykyk.collage.ui.theme.Slate400
import com.iykyk.collage.ui.theme.Slate800
import com.iykyk.collage.ui.theme.Slate900
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
            .background(Slate900)
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Hero Header Badge
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(Brush.horizontalGradient(listOf(Indigo500, Purple500)))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ON-DEVICE COMPUTER VISION",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "iykyk Collage",
            fontSize = 36.sp,
            fontWeight = FontWeight.Black,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Process portrait videos on-device to detect unique faces, count continuous appearances, and render Instagram-story collages.",
            fontSize = 15.sp,
            color = Slate400,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Custom File Upload Dropzone Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .border(1.5.dp, GlassBorder, RoundedCornerShape(24.dp))
                .clickable { videoPickerLauncher.launch("video/*") },
            colors = CardDefaults.cardColors(containerColor = GlassSurface)
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
                        .background(Indigo500.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.UploadFile,
                        contentDescription = "Upload Video",
                        tint = Indigo500,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (selectedVideoUri != null) "Video Selected!" else "Choose a Portrait Video",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = selectedVideoName ?: "Tap to select MP4/MOV from device storage",
                    fontSize = 14.sp,
                    color = Slate400,
                    textAlign = TextAlign.Center
                )

                if (selectedVideoUri != null) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = onStartProcessing,
                        colors = ButtonDefaults.buttonColors(containerColor = Indigo500),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Process Video & Generate Collage", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        // 1-Tap Sample Videos Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Movie, contentDescription = null, tint = Indigo500, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "1-Tap Assignment Test Samples",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sample 1 Card
        SampleVideoCard(
            title = "Sample 1 Video (Recommended)",
            description = "5 Unique People • 4 Appearances Each (20 Total Appearances)",
            onClick = {
                val uri = SampleVideoHelper.getSampleVideoUri(context, "sample1.mp4")
                if (uri != null) {
                    onVideoSelected(uri, "Sample 1 Video")
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Sample 2 Card
        SampleVideoCard(
            title = "Sample 2 Video",
            description = "Multi-person continuous appearance video",
            onClick = {
                val uri = SampleVideoHelper.getSampleVideoUri(context, "sample2.mp4")
                if (uri != null) {
                    onVideoSelected(uri, "Sample 2 Video")
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Sample 3 Card
        SampleVideoCard(
            title = "Sample 3 Video",
            description = "Dynamic portrait video with re-entries",
            onClick = {
                val uri = SampleVideoHelper.getSampleVideoUri(context, "sample3.mp4")
                if (uri != null) {
                    onVideoSelected(uri, "Sample 3 Video")
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
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Slate800)
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
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = Slate400
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Indigo500),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Test Sample",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
