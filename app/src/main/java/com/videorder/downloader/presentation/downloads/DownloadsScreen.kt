package com.videorder.downloader.presentation.downloads

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
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.videorder.downloader.domain.models.DownloadStatus
import com.videorder.downloader.domain.models.DownloadTask
import com.videorder.downloader.domain.models.MediaType
import com.videorder.downloader.presentation.common.*
import com.videorder.downloader.utils.FileUtils
import com.videorder.downloader.utils.Formatters

@Composable
fun DownloadsScreen(
    viewModel: DownloadsViewModel,
    onNavigateBack: () -> Unit,
    onOpenFileDetails: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        containerColor = BgDark,
        topBar = {
            DownloadsTopBar(
                onNavigateBack = onNavigateBack,
                activeCount = uiState.activeTasks.size,
                onPauseAll = { viewModel.pauseAll() },
                onResumeAll = { viewModel.resumeAll() },
                onCancelAll = { viewModel.cancelAll() }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tab Selector: Active vs Completed
            TabRow(
                selectedTabIndex = uiState.selectedTab,
                containerColor = SurfaceDark,
                contentColor = AccentCyan,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[uiState.selectedTab]),
                        color = AccentCyan
                    )
                }
            ) {
                Tab(
                    selected = uiState.selectedTab == 0,
                    onClick = { viewModel.selectTab(0) },
                    text = {
                        Text(
                            text = "Active & Queue (${uiState.activeTasks.size})",
                            fontWeight = if (uiState.selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = uiState.selectedTab == 1,
                    onClick = { viewModel.selectTab(1) },
                    text = {
                        Text(
                            text = "Completed (${uiState.completedTasks.size})",
                            fontWeight = if (uiState.selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }

            if (uiState.selectedTab == 0) {
                // Active & Queued Tasks List
                if (uiState.activeTasks.isEmpty()) {
                    EmptyStateView(
                        icon = Icons.Default.DownloadDone,
                        title = "No Active Downloads",
                        description = "Paste a media URL on the Home screen or browse Telegram to start downloading."
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.activeTasks, key = { it.id }) { task ->
                            ActiveDownloadCard(
                                task = task,
                                onPause = { viewModel.pauseTask(task.id) },
                                onResume = { viewModel.resumeTask(task.id) },
                                onCancel = { viewModel.cancelTask(task.id) },
                                onDelete = { viewModel.deleteTask(task.id, deleteFile = true) }
                            )
                        }
                    }
                }
            } else {
                // Completed Tasks List
                if (uiState.completedTasks.isEmpty()) {
                    EmptyStateView(
                        icon = Icons.Default.FolderOpen,
                        title = "No Completed Downloads",
                        description = "Your completed files will appear here."
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.completedTasks, key = { it.id }) { task ->
                            CompletedDownloadCard(
                                task = task,
                                onOpen = {
                                    if (task.localPath != null) {
                                        FileUtils.openFile(context, task.localPath, task.mimeType)
                                    }
                                },
                                onShare = {
                                    if (task.localPath != null) {
                                        FileUtils.shareFile(context, task.localPath, task.mimeType)
                                    }
                                },
                                onDelete = { viewModel.deleteTask(task.id, deleteFile = true) },
                                onClickDetails = {
                                    if (task.localPath != null) {
                                        onOpenFileDetails(task.localPath)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DownloadsTopBar(
    onNavigateBack: () -> Unit,
    activeCount: Int,
    onPauseAll: () -> Unit,
    onResumeAll: () -> Unit,
    onCancelAll: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "Downloads",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (activeCount > 0) {
                IconButton(onClick = onPauseAll) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = "Pause All",
                        tint = StatusPaused
                    )
                }
                IconButton(onClick = onResumeAll) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Resume All",
                        tint = AccentCyan
                    )
                }
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(SurfaceDark)
                ) {
                    DropdownMenuItem(
                        text = { Text("Pause All") },
                        onClick = {
                            onPauseAll()
                            showMenu = false
                        },
                        leadingIcon = { Icon(Icons.Default.Pause, contentDescription = null, tint = StatusPaused) }
                    )
                    DropdownMenuItem(
                        text = { Text("Resume All") },
                        onClick = {
                            onResumeAll()
                            showMenu = false
                        },
                        leadingIcon = { Icon(Icons.Default.PlayArrow, contentDescription = null, tint = AccentCyan) }
                    )
                    DropdownMenuItem(
                        text = { Text("Cancel All", color = StatusError) },
                        onClick = {
                            onCancelAll()
                            showMenu = false
                        },
                        leadingIcon = { Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = StatusError) }
                    )
                }
            }
        }
    }
}

@Composable
fun ActiveDownloadCard(
    task: DownloadTask,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderDark, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Media Icon
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceVariantDark),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (task.mediaType) {
                            MediaType.VIDEO -> Icons.Default.Movie
                            MediaType.AUDIO -> Icons.Default.MusicNote
                            MediaType.IMAGE -> Icons.Default.Image
                            MediaType.DOCUMENT -> Icons.Default.Description
                            else -> Icons.Default.InsertDriveFile
                        },
                        contentDescription = null,
                        tint = AccentCyan,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.filename,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${Formatters.formatBytes(task.downloadedBytes)} / ${if (task.totalBytes > 0) Formatters.formatBytes(task.totalBytes) else "--"}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))
                StatusBadge(status = task.status)
            }

            Spacer(modifier = Modifier.height(12.dp))
            ProgressBarWithStats(progress = task.progress, status = task.status)
            Spacer(modifier = Modifier.height(8.dp))

            // Stats row & action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${(task.progress * 100).toInt()}% • Speed: ${Formatters.formatSpeed(task.speed)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (task.etaSeconds > 0 && task.status == DownloadStatus.DOWNLOADING) {
                        Text(
                            text = "Remaining: ${Formatters.formatEta(task.etaSeconds)}",
                            fontSize = 11.sp,
                            color = AccentCyan
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    when (task.status) {
                        DownloadStatus.DOWNLOADING -> {
                            IconButton(
                                onClick = onPause,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceVariantDark)
                            ) {
                                Icon(Icons.Default.Pause, contentDescription = "Pause", tint = StatusPaused, modifier = Modifier.size(18.dp))
                            }
                        }
                        DownloadStatus.PAUSED, DownloadStatus.WAITING_FOR_WIFI -> {
                            IconButton(
                                onClick = onResume,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceVariantDark)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Resume", tint = AccentCyan, modifier = Modifier.size(18.dp))
                            }
                        }
                        DownloadStatus.QUEUED -> {
                            // Waiting indicator
                            Text("Waiting...", fontSize = 11.sp, color = TextSecondaryDark, modifier = Modifier.padding(end = 6.dp))
                        }
                        else -> {}
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = onCancel,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SurfaceVariantDark)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel", tint = TextSecondaryDark, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CompletedDownloadCard(
    task: DownloadTask,
    onOpen: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    onClickDetails: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderDark, RoundedCornerShape(16.dp))
            .clickable { onClickDetails() }
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceVariantDark),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (task.mediaType) {
                            MediaType.VIDEO -> Icons.Default.Movie
                            MediaType.AUDIO -> Icons.Default.MusicNote
                            MediaType.IMAGE -> Icons.Default.Image
                            MediaType.DOCUMENT -> Icons.Default.Description
                            else -> Icons.Default.InsertDriveFile
                        },
                        contentDescription = null,
                        tint = StatusSuccess,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.filename,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${Formatters.formatBytes(task.totalBytes)} • ${task.qualityLabel ?: "Original"}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                StatusBadge(status = DownloadStatus.COMPLETED)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onOpen) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp), tint = AccentCyan)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Open", color = AccentCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                TextButton(onClick = onShare) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share", color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
                }

                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = StatusError, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
