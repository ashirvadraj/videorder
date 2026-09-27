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
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import com.videorder.downloader.domain.models.MediaType
import com.videorder.downloader.domain.models.TelegramChat
import com.videorder.downloader.domain.models.TelegramChatType
import com.videorder.downloader.domain.models.TelegramMediaItem
import com.videorder.downloader.presentation.common.*
import com.videorder.downloader.utils.FileUtils
import com.videorder.downloader.utils.Formatters

@Composable
fun TelegramScreen(
    viewModel: TelegramViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToWeb: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        containerColor = BgDark,
        topBar = {
            TelegramTopBar(
                isLoggedIn = uiState.isLoggedIn,
                accountInfo = uiState.sessionMetadata?.phoneNumberMasked ?: uiState.sessionMetadata?.username,
                onNavigateBack = onNavigateBack,
                onOpenWebClick = onNavigateToWeb,
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
                onOpenWeb = onNavigateToWeb,
                onMoreOptions = { viewModel.openLoginDialog() },
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
                                onDownload = { viewModel.downloadSingle(item) },
                                onForward = {
                                    FileUtils.shareTelegramLink(context, item.downloadUrl, "Telegram Media: ${item.filename}")
                                },
                                onMessageInTelegram = {
                                    FileUtils.messageViaTelegram(
                                        context = context,
                                        caption = "Check out this media: ${item.filename}\n${item.downloadUrl}"
                                    )
                                }
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
            onTabSelected = { viewModel.selectLoginTab(it) },
            onBotTokenChange = { viewModel.onBotTokenChange(it) },
            onSubmitBotToken = { viewModel.submitBotToken() },
            onOpenTelegramWeb = onNavigateToWeb,
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
    onOpenWebClick: () -> Unit,
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
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
            }
            Column {
                Text("Telegram Media", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                if (isLoggedIn && accountInfo != null) {
                    Text(accountInfo, fontSize = 11.sp, color = AccentCyan)
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isLoggedIn) {
                OutlinedButton(
                    onClick = onOpenWebClick,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentCyan),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(AccentCyan.copy(alpha = 0.5f))
                    )
                ) {
                    Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(14.dp), tint = AccentCyan)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Live Web", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(onClick = onLogoutClick) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout", tint = StatusError, modifier = Modifier.size(18.dp))
                }
            } else {
                Button(
                    onClick = onOpenWebClick,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan, contentColor = BgDark),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text("Sign In", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun TelegramLoggedOutView(
    modifier: Modifier = Modifier,
    onOpenWeb: () -> Unit,
    onMoreOptions: () -> Unit,
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
                .background(AccentCyan.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(40.dp))
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
            text = "Sign in directly to your genuine Telegram account to browse your Saved Messages, Channels, and Groups with Videorder's media download sniffer.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onOpenWeb,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentCyan, contentColor = BgDark),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sign in with Official Telegram Web", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Receive real OTP directly from Telegram (777000) or scan official QR code",
            fontSize = 11.sp,
            color = TextSecondaryDark,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onMoreOptions,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Bot API Token", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
            }

            OutlinedButton(
                onClick = onDemoLogin,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                shape = RoundedCornerShape(10.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(AccentCyan)
                )
            ) {
                Text("Demo Sandbox", color = AccentCyan, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun TelegramMediaCard(
    item: TelegramMediaItem,
    isSelected: Boolean,
    onToggleSelect: () -> Unit,
    onDownload: () -> Unit,
    onForward: () -> Unit = {},
    onMessageInTelegram: () -> Unit = {}
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

            // Forward Link
            IconButton(
                onClick = onForward,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = "Forward Link", tint = TextSecondaryDark, modifier = Modifier.size(16.dp))
            }

            // Message in Telegram
            IconButton(
                onClick = onMessageInTelegram,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Message in Telegram", tint = AccentCyan, modifier = Modifier.size(16.dp))
            }

            Spacer(modifier = Modifier.width(4.dp))

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
