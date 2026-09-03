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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iykyk.collage.model.CollageResult
import com.iykyk.collage.model.LayoutTemplate
import com.iykyk.collage.model.PersonIdentity
import com.iykyk.collage.ui.theme.CanvasBg
import com.iykyk.collage.ui.theme.HotPink
import com.iykyk.collage.ui.theme.InkBlack
import com.iykyk.collage.ui.theme.LimeBlock
import com.iykyk.collage.ui.theme.LimeGreen
import com.iykyk.collage.ui.theme.OnSurfaceVariant
import com.iykyk.collage.ui.theme.OutlineBorder
import com.iykyk.collage.ui.theme.PrimaryWhite
import com.iykyk.collage.ui.theme.SkyBlue
import com.iykyk.collage.ui.theme.SubtleText
import com.iykyk.collage.ui.theme.SunshineYellow
import com.iykyk.collage.ui.theme.SurfaceCard
import com.iykyk.collage.ui.theme.SurfaceVariant

private val RingColors = listOf(HotPink, SkyBlue, SunshineYellow, LimeGreen, LimeBlock)

@Composable
fun CollageResultScreen(
    result: CollageResult,
    savedUriName: String?,
    activeAuditPerson: PersonIdentity?,
    selectedTemplate: LayoutTemplate,
    onTemplateChanged: (LayoutTemplate) -> Unit,
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
            .background(CanvasBg)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onReset,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(SurfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back / New Video",
                        tint = InkBlack
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "YOUR COLLAGE",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp,
                        color = InkBlack
                    )
                    Text(
                        text = "${result.identities.size} people found • $totalAppearances total appearances",
                        fontSize = 12.sp,
                        color = SubtleText
                    )
                }
            }

            IconButton(
                onClick = onShareCollage,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(SurfaceVariant)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share Collage",
                    tint = InkBlack
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Story Style Editorial Photo Collage Preview Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(12.dp, shape = RoundedCornerShape(28.dp))
                .clip(RoundedCornerShape(28.dp))
                .border(1.dp, OutlineBorder, RoundedCornerShape(28.dp)),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(9f / 16f)
                    .background(InkBlack),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = result.collageBitmap.asImageBitmap(),
                    contentDescription = "Generated Collage Preview",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Layout Template Selector Section
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "LAYOUT TEMPLATE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                color = OnSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LayoutTemplate.values().forEach { template ->
                    val isSelected = selectedTemplate == template
                    TemplateOptionCard(
                        template = template,
                        isSelected = isSelected,
                        onClick = { onTemplateChanged(template) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Main Action Buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onSaveToGallery,
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
                    Icon(
                        imageVector = if (savedUriName != null) Icons.Default.CheckCircle else Icons.Default.Download,
                        contentDescription = null,
                        tint = PrimaryWhite,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (savedUriName != null) "Saved to Gallery!" else "Save image",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryWhite
                    )
                }
            }

            Button(
                onClick = onShareCollage,
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariant),
                shape = CircleShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = InkBlack,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Share",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = InkBlack
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // People Found Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.People,
                    contentDescription = null,
                    tint = InkBlack,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "PEOPLE FOUND",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    color = InkBlack
                )
            }

            Text(
                text = "${result.identities.size} IDENTITIES",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SubtleText
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Horizontal Row of Person Cards
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
fun TemplateOptionCard(
    template: LayoutTemplate,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) InkBlack else OutlineBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Visual Mini Layout Representation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 5f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceVariant)
                    .padding(3.dp),
                contentAlignment = Alignment.Center
            ) {
                when (template) {
                    LayoutTemplate.EDITORIAL -> {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1.5f)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(if (isSelected) InkBlack else SubtleText.copy(alpha = 0.5f))
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(SubtleText.copy(alpha = 0.3f))
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(SubtleText.copy(alpha = 0.3f))
                                )
                            }
                        }
                    }
                    LayoutTemplate.FILM_STRIP -> {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            repeat(3) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(if (isSelected) InkBlack else SubtleText.copy(alpha = 0.4f))
                                )
                            }
                        }
                    }
                    LayoutTemplate.POLAROID -> {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (isSelected) InkBlack else SubtleText.copy(alpha = 0.4f))
                        )
                    }
                    LayoutTemplate.FULL_BLEED -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSelected) InkBlack else SubtleText.copy(alpha = 0.4f))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = template.label,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) InkBlack else OnSurfaceVariant
            )
        }
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
            .border(1.dp, OutlineBorder, RoundedCornerShape(20.dp))
            .clickable(onClick = onInspectAudit),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                    .size(80.dp)
                    .clip(CircleShape)
                    .border(3.dp, ringColor, CircleShape)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = person.name.lowercase(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = InkBlack
            )

            Spacer(modifier = Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(ringColor.copy(alpha = 0.15f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${person.totalAppearances} appearances",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkBlack
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = SubtleText,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "tap to audit",
                    fontSize = 11.sp,
                    color = SubtleText
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
        containerColor = SurfaceCard,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "audit: ${person.name.lowercase()}",
                    color = InkBlack,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = SubtleText)
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
                            .border(2.dp, InkBlack, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "appearances: ${person.totalAppearances}",
                            color = InkBlack,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "visible: ${"%.1f".format(person.totalVisibleDurationMs / 1000.0f)} seconds",
                            color = SubtleText,
                            fontSize = 13.sp
                        )
                    }
                }

                HorizontalDivider(color = OutlineBorder)
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "appearance timeline:",
                    color = InkBlack,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    person.appearances.forEachIndexed { index, track ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CanvasBg),
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
                                        color = InkBlack,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "time: ${"%.2f".format(track.startTimeMs / 1000f)}s - ${"%.2f".format(track.endTimeMs / 1000f)}s",
                                        color = SubtleText,
                                        fontSize = 12.sp
                                    )
                                }

                                Text(
                                    text = "${track.frameCount} frames",
                                    color = InkBlack,
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
                Text("close", color = InkBlack, fontWeight = FontWeight.Bold)
            }
        }
    )
}
