package com.videorder.downloader.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.videorder.downloader.presentation.common.*
import com.videorder.downloader.utils.Formatters

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val settings = uiState.settings

    var showClearHistoryConfirm by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = BgDark,
        topBar = {
            SettingsTopBar(onNavigateBack)
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // General & Storage Section
            item {
                SettingsSectionHeader("Storage & Downloads")
            }

            item {
                SettingsItem(
                    icon = Icons.Default.Folder,
                    title = "Download Location",
                    subtitle = settings.downloadLocation,
                    onClick = {}
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderDark, RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Max Simultaneous Downloads", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("${settings.maxSimultaneousDownloads}", fontWeight = FontWeight.Bold, color = AccentCyan, fontSize = 15.sp)
                        }
                        Slider(
                            value = settings.maxSimultaneousDownloads.toFloat(),
                            onValueChange = { viewModel.setMaxSimultaneous(it.toInt()) },
                            valueRange = 1f..5f,
                            steps = 3,
                            colors = SliderDefaults.colors(
                                thumbColor = AccentCyan,
                                activeTrackColor = AccentCyan,
                                inactiveTrackColor = SurfaceVariantDark
                            )
                        )
                    }
                }
            }

            // Network Section
            item {
                SettingsSectionHeader("Network Handling")
            }

            item {
                SettingsSwitchItem(
                    icon = Icons.Default.Wifi,
                    title = "Download over Wi-Fi only",
                    subtitle = "Automatically pause if Wi-Fi disconnects and show 'Waiting for Wi-Fi...'",
                    checked = settings.wifiOnly,
                    onCheckedChange = { viewModel.setWifiOnly(it) }
                )
            }

            item {
                SettingsSwitchItem(
                    icon = Icons.Default.NetworkCell,
                    title = "Mobile Data Allowed",
                    subtitle = "Allow downloads when on cellular mobile networks",
                    checked = settings.mobileDataAllowed,
                    onCheckedChange = {}
                )
            }

            item {
                SettingsSwitchItem(
                    icon = Icons.Default.Sync,
                    title = "Auto-Resume Interrupted Downloads",
                    subtitle = "Resume unfinished downloads automatically on network reconnection",
                    checked = settings.autoResume,
                    onCheckedChange = {}
                )
            }

            // Notifications & Appearance
            item {
                SettingsSectionHeader("Appearance & Theme")
            }

            item {
                SettingsItem(
                    icon = Icons.Default.DarkMode,
                    title = "Theme",
                    subtitle = "Dark Mode (Premium Default)",
                    onClick = {}
                )
            }

            // Telegram Account
            item {
                SettingsSectionHeader("Telegram Integration")
            }

            item {
                if (uiState.isTelegramLoggedIn) {
                    SettingsItem(
                        icon = Icons.Default.AccountCircle,
                        title = "Telegram Connected",
                        subtitle = uiState.telegramAccountMasked ?: "Authorized Session",
                        trailing = {
                            TextButton(onClick = { viewModel.logoutTelegram() }) {
                                Text("Logout", color = StatusError, fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                } else {
                    SettingsItem(
                        icon = Icons.Default.Send,
                        title = "Telegram Account",
                        subtitle = "Not connected. Tap to configure official session.",
                        onClick = {}
                    )
                }
            }

            // Data & Privacy
            item {
                SettingsSectionHeader("Privacy & Storage")
            }

            item {
                SettingsItem(
                    icon = Icons.Default.Storage,
                    title = "Device Free Storage",
                    subtitle = Formatters.formatBytes(uiState.freeStorageBytes) + " available",
                    onClick = {}
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Default.DeleteSweep,
                    title = "Clear Download History",
                    subtitle = "Wipe history entries (does not delete downloaded media files)",
                    onClick = { showClearHistoryConfirm = true }
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Default.Security,
                    title = "Privacy Policy & Security",
                    subtitle = "Learn how Videorder operates 100% on-device",
                    onClick = onNavigateToPrivacy
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "About Videorder",
                    subtitle = "v1.0.0 • Your media. Your downloads. Your control.",
                    onClick = onNavigateToAbout
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showClearHistoryConfirm) {
        AlertDialog(
            onDismissRequest = { showClearHistoryConfirm = false },
            containerColor = SurfaceDark,
            shape = RoundedCornerShape(16.dp),
            title = { Text("Clear History", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to clear all download history records?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllHistory()
                        showClearHistoryConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusError)
                ) {
                    Text("Clear", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SettingsTopBar(onNavigateBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
        }
        Text("Settings", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        color = AccentCyan,
        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
    )
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit = {},
    trailing: (@Composable () -> Unit)? = null
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderDark, RoundedCornerShape(14.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(text = subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (trailing != null) {
                trailing()
            } else {
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondaryDark, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderDark, RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(text = subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = BgDark,
                    checkedTrackColor = AccentCyan,
                    uncheckedThumbColor = TextSecondaryDark,
                    uncheckedTrackColor = SurfaceVariantDark
                )
            )
        }
    }
}
