package com.videorder.downloader.presentation.downloads

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.videorder.downloader.data.database.entities.DownloadHistoryEntity
import com.videorder.downloader.domain.models.DownloadStatus
import com.videorder.downloader.domain.models.MediaType
import com.videorder.downloader.presentation.common.*
import com.videorder.downloader.utils.FileUtils
import com.videorder.downloader.utils.Formatters

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        containerColor = BgDark,
        topBar = {
            HistoryTopBar(
                onNavigateBack = onNavigateBack,
                onClearHistory = { viewModel.openClearDialog() }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                placeholder = { Text("Search download history...", fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AccentCyan) },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextSecondaryDark)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentCyan,
                    unfocusedBorderColor = BorderDark,
                    focusedContainerColor = SurfaceDark,
                    unfocusedContainerColor = SurfaceDark
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (uiState.historyItems.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.History,
                    title = "No History Found",
                    description = "Completed downloads will be safely preserved here."
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    uiState.groupedHistory.forEach { (bucketTitle, items) ->
                        item {
                            Text(
                                text = bucketTitle,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = AccentCyan,
                                modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                            )
                        }

                        items(items, key = { it.id }) { item ->
                            HistoryItemCard(
                                item = item,
                                onOpen = { FileUtils.openFile(context, item.localPath, item.mimeType) },
                                onShare = { FileUtils.shareFile(context, item.localPath, item.mimeType) },
                                onDelete = { viewModel.deleteItem(item.id) }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }

    if (uiState.showClearDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.closeClearDialog() },
            containerColor = SurfaceDark,
            shape = RoundedCornerShape(16.dp),
            title = { Text("Clear Download History", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
            text = {
                Column {
                    Text(
                        "Are you sure you want to clear your download history?",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = uiState.alsoDeleteFiles,
                            onCheckedChange = { viewModel.toggleAlsoDeleteFiles(it) },
                            colors = CheckboxDefaults.colors(checkedColor = StatusError, checkmarkColor = Color.White)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Also delete downloaded files from storage",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmClearHistory() },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusError, contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Clear History", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closeClearDialog() }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}

@Composable
fun HistoryTopBar(
    onNavigateBack: () -> Unit,
    onClearHistory: () -> Unit
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
            Text("Download History", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }

        IconButton(onClick = onClearHistory) {
            Icon(Icons.Default.DeleteSweep, contentDescription = "Clear History", tint = StatusError)
        }
    }
}

@Composable
fun HistoryItemCard(
    item: DownloadHistoryEntity,
    onOpen: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderDark, RoundedCornerShape(14.dp))
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
                    imageVector = Icons.Default.Movie,
                    contentDescription = null,
                    tint = StatusSuccess,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.filename,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${item.quality ?: "Original"} • ${Formatters.formatBytes(item.fileSize)} • Completed",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onOpen) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = AccentCyan, modifier = Modifier.size(20.dp))
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Close, contentDescription = "Remove", tint = TextSecondaryDark, modifier = Modifier.size(18.dp))
            }
        }
    }
}
