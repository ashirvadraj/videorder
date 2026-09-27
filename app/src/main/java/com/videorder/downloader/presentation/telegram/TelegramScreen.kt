package com.videorder.downloader.presentation.telegram

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.videorder.downloader.domain.models.MediaType
import com.videorder.downloader.domain.models.TelegramChat
import com.videorder.downloader.domain.models.TelegramChatType
import com.videorder.downloader.domain.models.TelegramMediaItem
import com.videorder.downloader.presentation.common.*
import com.videorder.downloader.utils.Formatters

@Composable
fun TelegramScreen(
    viewModel: TelegramViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = BgDark,
        topBar = {
            TelegramTopBar(
                isLoggedIn = uiState.isLoggedIn,
                accountInfo = uiState.sessionMetadata?.phoneNumberMasked ?: uiState.sessionMetadata?.username,
                onNavigateBack = onNavigateBack,
                onLoginClick = { viewModel.openLoginDialog() },
                onLogoutClick = { viewModel.logout() }
            )
        },
        bottomBar = {
            // Floating multi-selection download queue bar
            AnimatedVisibility(visible = uiState.selectedMediaIds.isNotEmpty()) {
                Surface(
                    color = SurfaceDark,
                    tonalElevation = 8.dp,
                    modifier = Modifier.border(1.dp, BorderDark)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${uiState.selectedMediaIds.size} files selected",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Queue downloads concurrently",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row {
                            TextButton(onClick = { viewModel.clearMediaSelection() }) {
                                Text("Clear", color = TextSecondaryDark)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { viewModel.downloadSelected() },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan, contentColor = BgDark),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Download Queue", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (!uiState.isLoggedIn) {
            // Not Logged In State
            TelegramLoggedOutView(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                onSignIn = { viewModel.openLoginDialog() },
                onDemoLogin = { viewModel.loginDemoMode() }
            )
        } else {
            // Logged In: Chat list + Media items browser
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Chats category scroll
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = uiState.selectedChatType == null,
                            onClick = { viewModel.loadChats(null) },
                            label = { Text("All Chats") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AccentCyan,
                                selectedLabelColor = BgDark
                            )
                        )
                    }
                    item {
                        FilterChip(
                            selected = uiState.selectedChatType == TelegramChatType.SAVED_MESSAGES,
                            onClick = { viewModel.loadChats(TelegramChatType.SAVED_MESSAGES) },
                            label = { Text("Saved Messages") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = uiState.selectedChatType == TelegramChatType.CHANNEL,
                            onClick = { viewModel.loadChats(TelegramChatType.CHANNEL) },
                            label = { Text("Channels") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = uiState.selectedChatType == TelegramChatType.GROUP,
                            onClick = { viewModel.loadChats(TelegramChatType.GROUP) },
                            label = { Text("Groups") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = uiState.selectedChatType == TelegramChatType.PRIVATE,
                            onClick = { viewModel.loadChats(TelegramChatType.PRIVATE) },
                            label = { Text("Private Chats") }
                        )
                    }
                }

                // Chat selection cards
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.chats) { chat ->
                        val isSelected = uiState.selectedChat?.id == chat.id
                        val borderColor = if (isSelected) AccentCyan else BorderDark
                        val bgColor = if (isSelected) AccentCyan.copy(alpha = 0.12f) else SurfaceDark

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = bgColor),
                            modifier = Modifier
                                .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                                .clickable { viewModel.selectChat(chat) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = when (chat.type) {
                                        TelegramChatType.SAVED_MESSAGES -> Icons.Default.Bookmark
                                        TelegramChatType.CHANNEL -> Icons.Default.VolumeUp
                                        TelegramChatType.GROUP -> Icons.Default.Group
                                        TelegramChatType.PRIVATE -> Icons.Default.Person
                                    },
                                    contentDescription = null,
                                    tint = if (isSelected) AccentCyan else TextSecondaryDark,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = chat.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${chat.mediaCount} files",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Media Type Filters
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    item {
                        MediaFilterPill(
                            title = "All",
                            isSelected = uiState.selectedMediaType == null,
                            onClick = { viewModel.filterMediaType(null) }
                        )
                    }
                    item {
                        MediaFilterPill(
                            title = "Videos",
                            isSelected = uiState.selectedMediaType == MediaType.VIDEO,
                            onClick = { viewModel.filterMediaType(MediaType.VIDEO) }
                        )
                    }
                    item {
                        MediaFilterPill(
                            title = "Photos",
                            isSelected = uiState.selectedMediaType == MediaType.IMAGE,
                            onClick = { viewModel.filterMediaType(MediaType.IMAGE) }
                        )
                    }
                    item {
                        MediaFilterPill(
                            title = "Audio",
                            isSelected = uiState.selectedMediaType == MediaType.AUDIO,
                            onClick = { viewModel.filterMediaType(MediaType.AUDIO) }
                        )
                    }
                    item {
                        MediaFilterPill(
                            title = "Documents",
                            isSelected = uiState.selectedMediaType == MediaType.DOCUMENT,
                            onClick = { viewModel.filterMediaType(MediaType.DOCUMENT) }
                        )
                    }
                }

                // Media Items List
                if (uiState.mediaItems.isEmpty()) {
                    EmptyStateView(
                        icon = Icons.Default.PermMedia,
                        title = "No Media Found",
                        description = "No media files available in this chat category."
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(uiState.mediaItems, key = { it.id }) { item ->
                            val isSelected = uiState.selectedMediaIds.contains(item.id)
                            TelegramMediaCard(
                                item = item,
                                isSelected = isSelected,
                                onToggleSelect = { viewModel.toggleMediaSelection(item.id) },
                                onDownload = { viewModel.downloadSingle(item) }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(72.dp))
                        }
                    }
                }
            }
        }
    }

    if (uiState.showLoginDialog) {
        TelegramLoginDialog(
            state = uiState,
            onPhoneChange = { viewModel.onPhoneChange(it) },
            onCodeChange = { viewModel.onCodeChange(it) },
            onRequestCode = { viewModel.requestVerificationCode() },
            onSubmitCode = { viewModel.submitCode() },
            onDemoLogin = { viewModel.loginDemoMode() },
            onDismiss = { viewModel.closeLoginDialog() }
        )
    }
}

@Composable
fun TelegramTopBar(
    isLoggedIn: Boolean,
    accountInfo: String?,
    onNavigateBack: () -> Unit,
    onLoginClick: () -> Unit,
    onLogoutClick: () -> Unit
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
            Column {
                Text("Telegram Media", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                if (isLoggedIn && accountInfo != null) {
                    Text(accountInfo, fontSize = 11.sp, color = AccentCyan)
                }
            }
        }

        if (isLoggedIn) {
            TextButton(onClick = onLogoutClick) {
                Icon(Icons.Default.Logout, contentDescription = null, tint = StatusError, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Logout", color = StatusError, fontSize = 13.sp)
            }
        } else {
            Button(
                onClick = onLoginClick,
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue, contentColor = Color.White),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text("Sign In", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun TelegramLoggedOutView(
    modifier: Modifier = Modifier,
    onSignIn: () -> Unit,
    onDemoLogin: () -> Unit
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(AccentBlue.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Send, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(40.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Official Telegram Integration",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Sign in to browse and download videos, photos, and files from your Saved Messages, Channels, and Groups.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onSignIn,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentCyan, contentColor = BgDark),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Sign in with Telegram", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onDemoLogin,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = androidx.compose.ui.graphics.SolidColor(AccentCyan)
            )
        ) {
            Text("Explore Demo Sandbox (No Login Required)", color = AccentCyan, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        }
    }
}

@Composable
fun TelegramMediaCard(
    item: TelegramMediaItem,
    isSelected: Boolean,
    onToggleSelect: () -> Unit,
    onDownload: () -> Unit
) {
    val borderColor = if (isSelected) AccentCyan else BorderDark
    val bgColor = if (isSelected) AccentCyan.copy(alpha = 0.08f) else SurfaceDark

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable { onToggleSelect() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggleSelect() },
                colors = CheckboxDefaults.colors(checkedColor = AccentCyan, checkmarkColor = BgDark)
            )

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceVariantDark),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (item.mediaType) {
                        MediaType.VIDEO -> Icons.Default.Movie
                        MediaType.IMAGE -> Icons.Default.Image
                        MediaType.AUDIO -> Icons.Default.MusicNote
                        MediaType.DOCUMENT -> Icons.Default.Description
                        else -> Icons.Default.AttachFile
                    },
                    contentDescription = null,
                    tint = AccentCyan,
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
                    text = "${Formatters.formatBytes(item.sizeBytes)} • ${Formatters.formatDate(item.dateEpochSeconds * 1000L)}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onDownload,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(SurfaceVariantDark)
            ) {
                Icon(Icons.Default.Download, contentDescription = "Download", tint = AccentCyan, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun MediaFilterPill(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) AccentCyan else SurfaceDark
    val textColor = if (isSelected) BgDark else MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .border(1.dp, if (isSelected) AccentCyan else BorderDark, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = textColor)
    }
}
