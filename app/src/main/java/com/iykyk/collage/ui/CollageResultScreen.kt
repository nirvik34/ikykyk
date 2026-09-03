package com.iykyk.collage.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iykyk.collage.model.PersonIdentity
import com.iykyk.collage.model.CollageResult
import com.iykyk.collage.ui.theme.Emerald400
import com.iykyk.collage.ui.theme.GlassBorder
import com.iykyk.collage.ui.theme.GlassSurface
import com.iykyk.collage.ui.theme.Indigo500
import com.iykyk.collage.ui.theme.Purple500
import com.iykyk.collage.ui.theme.Slate400
import com.iykyk.collage.ui.theme.Slate800
import com.iykyk.collage.ui.theme.Slate900

@Composable
fun CollageResultScreen(
    result: CollageResult,
    savedUriName: String?,
    activeAuditPerson: PersonIdentity?,
    onSaveToGallery: () -> Unit,
    onShareCollage: () -> Unit,
    onSelectAuditPerson: (PersonIdentity?) -> Unit,
    onReset: () -> Unit
) {
    val scrollState = rememberScrollState()
    val totalAppearances = result.identities.sumOf { it.totalAppearances }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate900)
            .verticalScroll(scrollState)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Title Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Collage Generated!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "${result.identities.size} unique people • $totalAppearances total appearances",
                    fontSize = 14.sp,
                    color = Slate400
                )
            }

            IconButton(
                onClick = onReset,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(GlassSurface)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "New Video", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Rendered Collage Preview Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .border(1.5.dp, GlassBorder, RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = Slate800)
        ) {
            Image(
                bitmap = result.collageBitmap.asImageBitmap(),
                contentDescription = "Generated Collage",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(9f / 16f)
                    .clip(RoundedCornerShape(24.dp))
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Action Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onSaveToGallery,
                colors = ButtonDefaults.buttonColors(containerColor = Indigo500),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = if (savedUriName != null) Icons.Default.CheckCircle else Icons.Default.Download,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (savedUriName != null) "Saved to Gallery" else "Save Image",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            OutlinedButton(
                onClick = onShareCollage,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Share Story", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // People Breakdown & Audit Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.People, contentDescription = null, tint = Indigo500, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Identified People & Audit Tracks",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Horizontal List of People Cards
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(items = result.identities, key = { it.id }) { person ->
                PersonCard(
                    person = person,
                    onInspectAudit = { onSelectAuditPerson(person) }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // Appearance Segment Audit Dialog
    if (activeAuditPerson != null) {
        AuditDialog(
            person = activeAuditPerson,
            onDismiss = { onSelectAuditPerson(null) }
        )
    }
}

@Composable
fun PersonCard(
    person: PersonIdentity,
    onInspectAudit: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, GlassBorder, RoundedCornerShape(18.dp))
            .clickable(onClick = onInspectAudit),
        colors = CardDefaults.cardColors(containerColor = GlassSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                bitmap = person.croppedFaceBitmap.asImageBitmap(),
                contentDescription = person.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .border(2.dp, Indigo500, CircleShape)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = person.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(2.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Indigo500.copy(alpha = 0.2f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${person.totalAppearances} Appearances",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Indigo500
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null, tint = Slate400, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Tap to Audit",
                    fontSize = 11.sp,
                    color = Slate400
                )
            }
        }
    }
}

@Composable
fun AuditDialog(
    person: PersonIdentity,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Slate900,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Appearance Audit: ${person.name}",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Slate400)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Image(
                        bitmap = person.croppedFaceBitmap.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, Indigo500, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Total Appearances: ${person.totalAppearances}",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Total Visible: ${"%.1f".format(person.totalVisibleDurationMs / 1000.0f)} seconds",
                            color = Slate400,
                            fontSize = 13.sp
                        )
                    }
                }

                HorizontalDivider(color = GlassBorder)
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Continuous Appearance Tracks:",
                    color = Indigo500,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    person.appearances.forEachIndexed { index, track ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Slate800),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Appearance #${index + 1}",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "Time: ${"%.2f".format(track.startTimeMs / 1000f)}s - ${"%.2f".format(track.endTimeMs / 1000f)}s",
                                        color = Slate400,
                                        fontSize = 12.sp
                                    )
                                }

                                Text(
                                    text = "${track.frameCount} frames",
                                    color = Emerald400,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close Audit", color = Indigo500, fontWeight = FontWeight.Bold)
            }
        }
    )
}
