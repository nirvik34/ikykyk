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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iykyk.collage.model.CollageResult
import com.iykyk.collage.model.PersonIdentity
import com.iykyk.collage.ui.theme.Charcoal
import com.iykyk.collage.ui.theme.HotPink
import com.iykyk.collage.ui.theme.LimeGreen
import com.iykyk.collage.ui.theme.PrimaryWhite
import com.iykyk.collage.ui.theme.SkyBlue
import com.iykyk.collage.ui.theme.SoftBlack
import com.iykyk.collage.ui.theme.SoftGray
import com.iykyk.collage.ui.theme.SunshineYellow

private val RingColors = listOf(HotPink, SkyBlue, SunshineYellow, LimeGreen)

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
            .background(SoftBlack)
            .verticalScroll(scrollState)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "collage ready!",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    color = PrimaryWhite
                )
                Text(
                    text = "${result.identities.size} people found • $totalAppearances total appearances",
                    fontSize = 14.sp,
                    color = SoftGray
                )
            }

            IconButton(
                onClick = onReset,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Charcoal)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "New Video", tint = PrimaryWhite)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = Charcoal)
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            
            Button(
                onClick = onSaveToGallery,
                colors = ButtonDefaults.buttonColors(containerColor = HotPink),
                shape = CircleShape,
                modifier = Modifier
                    .weight(1.2f)
                    .height(54.dp)
            ) {
                Icon(
                    imageVector = if (savedUriName != null) Icons.Default.CheckCircle else Icons.Default.Download,
                    contentDescription = null,
                    tint = PrimaryWhite,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (savedUriName != null) "saved!" else "save",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryWhite
                )
            }

            OutlinedButton(
                onClick = onShareCollage,
                shape = CircleShape,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryWhite),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Charcoal),
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, tint = PrimaryWhite, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("share", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PrimaryWhite)
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.People, contentDescription = null, tint = SkyBlue, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "people found",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryWhite
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            itemsIndexed(items = result.identities, key = { _, person -> person.id }) { index, person ->
                val ringColor = RingColors[index % RingColors.size]
                PersonCard(
                    person = person,
                    ringColor = ringColor,
                    onInspectAudit = { onSelectAuditPerson(person) }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

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
    ringColor: Color,
    onInspectAudit: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(156.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onInspectAudit),
        colors = CardDefaults.cardColors(containerColor = Charcoal)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                bitmap = person.croppedFaceBitmap.asImageBitmap(),
                contentDescription = person.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(86.dp)
                    .clip(CircleShape)
                    .border(3.dp, ringColor, CircleShape)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = person.name.lowercase(),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryWhite
            )

            Spacer(modifier = Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(ringColor.copy(alpha = 0.2f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${person.totalAppearances} appearances",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ringColor
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null, tint = SoftGray, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "tap to audit",
                    fontSize = 11.sp,
                    color = SoftGray
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
        containerColor = Charcoal,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "audit: ${person.name.lowercase()}",
                    color = PrimaryWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = SoftGray)
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
                            .border(2.dp, HotPink, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "appearances: ${person.totalAppearances}",
                            color = PrimaryWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "visible: ${"%.1f".format(person.totalVisibleDurationMs / 1000.0f)} seconds",
                            color = SoftGray,
                            fontSize = 13.sp
                        )
                    }
                }

                HorizontalDivider(color = SoftGray.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "appearance timeline:",
                    color = SkyBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    person.appearances.forEachIndexed { index, track ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SoftBlack),
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
                                        text = "appearance #${index + 1}",
                                        color = PrimaryWhite,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "time: ${"%.2f".format(track.startTimeMs / 1000f)}s - ${"%.2f".format(track.endTimeMs / 1000f)}s",
                                        color = SoftGray,
                                        fontSize = 12.sp
                                    )
                                }

                                Text(
                                    text = "${track.frameCount} frames",
                                    color = LimeGreen,
                                    fontWeight = FontWeight.Bold,
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
                Text("close", color = HotPink, fontWeight = FontWeight.Bold)
            }
        }
    )
}
