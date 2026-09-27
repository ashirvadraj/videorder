package com.videorder.downloader.presentation.home

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.videorder.downloader.presentation.common.*
import com.videorder.downloader.utils.Formatters

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToDownloads: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToTelegram: () -> Unit,
    onNavigateToFiles: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        containerColor = BgDark,
        topBar = {
            HomeTopBar(onNavigateToSettings)
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // URL Input Card
            item {
                UrlInputSection(
                    url = uiState.urlInput,
                    isDetecting = uiState.isDetecting,
                    onUrlChange = { viewModel.onUrlChange(it) },
                    onPaste = { viewModel.pasteFromClipboard(context) },
                    onDetect = { viewModel.detectMedia() },
                    onDirectDownload = { viewModel.detectMedia() }
                )
            }

            // Error Banner if present
            if (uiState.errorMessage != null) {
                item {
                    ErrorBanner(
                        message = uiState.errorMessage!!,
                        onDismiss = { viewModel.dismissDialog() }
                    )
                }
            }

            // Quick Hub Navigation Cards
            item {
                Text(
                    text = "Quick Navigation",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickHubCard(
                        title = "Downloads",
                        subtitle = "${uiState.activeTasks.size} active",
                        icon = Icons.Default.Download,
                        accentColor = AccentCyan,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToDownloads
                    )
                    QuickHubCard(
                        title = "Telegram",
                        subtitle = "Authorized media",
                        icon = Icons.Default.Send,
                        accentColor = AccentBlue,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToTelegram
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickHubCard(
                        title = "My Files",
                        subtitle = "Videos & Audio",
                        icon = Icons.Default.Folder,
                        accentColor = Color(0xFF00E676),
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToFiles
                    )
                    QuickHubCard(
                        title = "History",
                        subtitle = "${uiState.completedTasks.size} saved",
                        icon = Icons.Default.History,
                        accentColor = AccentPurple,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToHistory
                    )
                }
            }

            // Active Downloads Preview
            if (uiState.activeTasks.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Active Downloads",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        TextButton(onClick = onNavigateToDownloads) {
                            Text("View All", color = AccentCyan, fontSize = 13.sp)
                        }
                    }
                }

                items(uiState.activeTasks.take(2)) { task ->
                    ActiveTaskMiniCard(task = task, onClick = onNavigateToDownloads)
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Media Quality Selection Modal
    if (uiState.detectedMedia != null) {
        MediaSelectionDialog(
            mediaItem = uiState.detectedMedia!!,
            selectedQuality = uiState.selectedQuality,
            onQualitySelected = { viewModel.selectQuality(it) },
            onConfirmDownload = { viewModel.confirmDownload(it) },
            onDismiss = { viewModel.dismissDialog() }
        )
    }
}

@Composable
fun HomeTopBar(onSettingsClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceVariantDark),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = null,
                    tint = AccentCyan,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Videorder",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AccentCyan
                )
                Text(
                    text = "Universal Media Downloader",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier
                .clip(CircleShape)
                .background(SurfaceVariantDark)
                .size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun UrlInputSection(
    url: String,
    isDetecting: Boolean,
    onUrlChange: (String) -> Unit,
    onPaste: () -> Unit,
    onDetect: () -> Unit,
    onDirectDownload: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderDark, RoundedCornerShape(18.dp))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Download from URL",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = url,
                onValueChange = onUrlChange,
                placeholder = { Text("Paste video or media URL...", fontSize = 14.sp) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                trailingIcon = {
                    if (url.isNotEmpty()) {
                        IconButton(onClick = { onUrlChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextSecondaryDark)
                        }
                    } else {
                        IconButton(onClick = onPaste) {
                            Icon(Icons.Default.ContentPaste, contentDescription = "Paste", tint = AccentCyan)
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentCyan,
                    unfocusedBorderColor = BorderDark,
                    focusedContainerColor = SurfaceVariantDark,
                    unfocusedContainerColor = SurfaceVariantDark
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDetect,
                    enabled = !isDetecting && url.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AccentCyan
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(if (url.isNotBlank()) AccentCyan else BorderDark)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    if (isDetecting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = AccentCyan,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Detect", fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = onDirectDownload,
                    enabled = !isDetecting && url.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentCyan,
                        contentColor = BgDark
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Download", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun QuickHubCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        modifier = modifier
            .border(1.dp, BorderDark, RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ActiveTaskMiniCard(
    task: com.videorder.downloader.domain.models.DownloadTask,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderDark, RoundedCornerShape(14.dp))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.filename,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                StatusBadge(status = task.status)
            }

            Spacer(modifier = Modifier.height(8.dp))
            ProgressBarWithStats(progress = task.progress, status = task.status)
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${(task.progress * 100).toInt()}% • ${Formatters.formatBytes(task.downloadedBytes)}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = Formatters.formatSpeed(task.speed),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AccentCyan
                )
            }
        }
    }
}

@Composable
fun ErrorBanner(message: String, onDismiss: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = StatusError.copy(alpha = 0.12f)),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, StatusError.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = StatusError,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = message,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = TextSecondaryDark,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
