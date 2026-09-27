package com.videorder.downloader.presentation.files

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.videorder.downloader.domain.models.FileItem
import com.videorder.downloader.domain.models.MediaType
import com.videorder.downloader.presentation.common.*
import com.videorder.downloader.presentation.telegram.MediaFilterPill
import com.videorder.downloader.utils.FileUtils
import com.videorder.downloader.utils.Formatters

@Composable
fun FilesScreen(
    viewModel: FilesViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        containerColor = BgDark,
        topBar = {
            FilesTopBar(
                onNavigateBack = onNavigateBack,
                onRefresh = { viewModel.refreshFiles() }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Category Filter Pills
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    MediaFilterPill(
                        title = "All Files",
                        isSelected = uiState.selectedCategory == null,
                        onClick = { viewModel.selectCategory(null) }
                    )
                }
                item {
                    MediaFilterPill(
                        title = "Videos",
                        isSelected = uiState.selectedCategory == MediaType.VIDEO,
                        onClick = { viewModel.selectCategory(MediaType.VIDEO) }
                    )
                }
                item {
                    MediaFilterPill(
                        title = "Images",
                        isSelected = uiState.selectedCategory == MediaType.IMAGE,
                        onClick = { viewModel.selectCategory(MediaType.IMAGE) }
                    )
                }
                item {
                    MediaFilterPill(
                        title = "Audio",
                        isSelected = uiState.selectedCategory == MediaType.AUDIO,
                        onClick = { viewModel.selectCategory(MediaType.AUDIO) }
                    )
                }
                item {
                    MediaFilterPill(
                        title = "Documents",
                        isSelected = uiState.selectedCategory == MediaType.DOCUMENT,
                        onClick = { viewModel.selectCategory(MediaType.DOCUMENT) }
                    )
                }
            }

            if (uiState.files.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.FolderOpen,
                    title = "No Downloaded Files",
                    description = "Files downloaded with Videorder will be listed and organized here."
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.files, key = { it.id }) { file ->
                        FileItemCard(
                            file = file,
                            onClick = { viewModel.openFileDetails(file) },
                            onOpen = { FileUtils.openFile(context, file.localPath, file.mimeType) },
                            onShare = { FileUtils.shareFile(context, file.localPath, file.mimeType) },
                            onRename = { viewModel.startRename(file) },
                            onDelete = { viewModel.deleteFile(file) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }

    // Video Details / Preview Modal
    if (uiState.selectedFileForDetails != null) {
        val file = uiState.selectedFileForDetails!!
        VideoPlayerDialog(
            file = file,
            onDismiss = { viewModel.closeFileDetails() },
            onPlay = { FileUtils.openFile(context, file.localPath, file.mimeType) },
            onShare = { FileUtils.shareFile(context, file.localPath, file.mimeType) },
            onRename = {
                viewModel.closeFileDetails()
                viewModel.startRename(file)
            },
            onDelete = { viewModel.deleteFile(file) }
        )
    }

    // Rename Dialog
    if (uiState.showRenameDialog) {
        RenameFileDialog(
            filename = uiState.renameInput,
            onFilenameChange = { viewModel.onRenameInputChange(it) },
            onConfirm = { viewModel.confirmRename() },
            onDismiss = { viewModel.cancelRename() }
        )
    }
}

@Composable
fun FilesTopBar(
    onNavigateBack: () -> Unit,
    onRefresh: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
            }
            Text("Downloaded Files", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }

        IconButton(onClick = onRefresh) {
            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = AccentCyan)
        }
    }
}

@Composable
fun FileItemCard(
    file: FileItem,
    onClick: () -> Unit,
    onOpen: () -> Unit,
    onShare: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderDark, RoundedCornerShape(14.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceVariantDark),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (file.mediaType) {
                        MediaType.VIDEO -> Icons.Default.Movie
                        MediaType.IMAGE -> Icons.Default.Image
                        MediaType.AUDIO -> Icons.Default.MusicNote
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
                    text = file.filename,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${Formatters.formatBytes(file.sizeBytes)} • ${Formatters.formatDate(file.lastModified)}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = TextSecondaryDark)
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(SurfaceDark)
                ) {
                    DropdownMenuItem(
                        text = { Text("Open") },
                        onClick = {
                            onOpen()
                            showMenu = false
                        },
                        leadingIcon = { Icon(Icons.Default.PlayArrow, contentDescription = null, tint = AccentCyan) }
                    )
                    DropdownMenuItem(
                        text = { Text("Share") },
                        onClick = {
                            onShare()
                            showMenu = false
                        },
                        leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Rename") },
                        onClick = {
                            onRename()
                            showMenu = false
                        },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = StatusError) },
                        onClick = {
                            onDelete()
                            showMenu = false
                        },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = StatusError) }
                    )
                }
            }
        }
    }
}

@Composable
fun RenameFileDialog(
    filename: String,
    onFilenameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        shape = RoundedCornerShape(16.dp),
        title = { Text("Rename File", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
        text = {
            OutlinedTextField(
                value = filename,
                onValueChange = onFilenameChange,
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentCyan,
                    unfocusedBorderColor = BorderDark,
                    focusedContainerColor = SurfaceVariantDark,
                    unfocusedContainerColor = SurfaceVariantDark
                ),
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan, contentColor = BgDark),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}
