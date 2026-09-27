package com.videorder.downloader.presentation.telegram

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.videorder.downloader.presentation.common.*

@Composable
fun TelegramLoginDialog(
    state: TelegramUiState,
    onTabSelected: (Int) -> Unit,
    onBotTokenChange: (String) -> Unit,
    onSubmitBotToken: () -> Unit,
    onOpenTelegramWeb: () -> Unit,
    onDemoLogin: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(AccentCyan.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = null,
                        tint = AccentCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Official Telegram Sign In",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                // Tab Selection: Official Web vs Bot Token vs Demo
                TabRow(
                    selectedTabIndex = state.loginTab,
                    containerColor = SurfaceVariantDark,
                    contentColor = AccentCyan,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .padding(bottom = 14.dp)
                ) {
                    Tab(
                        selected = state.loginTab == 0,
                        onClick = { onTabSelected(0) },
                        text = { Text("Official Web", fontWeight = if (state.loginTab == 0) FontWeight.Bold else FontWeight.Normal, fontSize = 11.sp) }
                    )
                    Tab(
                        selected = state.loginTab == 1,
                        onClick = { onTabSelected(1) },
                        text = { Text("Bot API", fontWeight = if (state.loginTab == 1) FontWeight.Bold else FontWeight.Normal, fontSize = 11.sp) }
                    )
                    Tab(
                        selected = state.loginTab == 2,
                        onClick = { onTabSelected(2) },
                        text = { Text("Sandbox", fontWeight = if (state.loginTab == 2) FontWeight.Bold else FontWeight.Normal, fontSize = 11.sp) }
                    )
                }

                // Error Banner if present
                if (state.errorMessage != null) {
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = StatusError.copy(alpha = 0.12f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = StatusError, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = state.errorMessage,
                                color = StatusError,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Tab 0: Official Telegram Web (Real Login & Real OTP)
                if (state.loginTab == 0) {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceVariantDark),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, AccentCyan.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "100% Genuine Telegram Connection",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "• Receive genuine verification codes directly from Telegram servers (from 777000) or via SMS.\n• Or scan the official QR code using your Telegram mobile app (Settings → Devices → Link Desktop Device).\n• Browse all your real chats, channels, and saved media with Videorder's download sniffer.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    onDismiss()
                                    onOpenTelegramWeb()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AccentCyan,
                                    contentColor = BgDark
                                )
                            ) {
                                Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Open Official Telegram Login", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Tab 1: Official Bot Token API
                else if (state.loginTab == 1) {
                    Text(
                        text = "Enter your Telegram Bot Token from @BotFather:",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = state.botTokenInput,
                        onValueChange = onBotTokenChange,
                        placeholder = { Text("123456:ABC-DEF1234ghIkl...", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.SmartToy, contentDescription = null, tint = AccentBlue) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = BorderDark,
                            focusedContainerColor = SurfaceVariantDark,
                            unfocusedContainerColor = SurfaceVariantDark
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Direct HTTPS verification with Telegram Cloud API (https://api.telegram.org). Allows downloading media from channels and chats.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onSubmitBotToken,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentBlue,
                            contentColor = Color.White
                        )
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text("Authorize Bot Token", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Tab 2: Offline Sandbox
                else {
                    Text(
                        text = "Explore Videorder's download features using a pre-populated offline sample channel without entering any credentials.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = {
                            onDismiss()
                            onDemoLogin()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(AccentCyan)
                        )
                    ) {
                        Text("Launch Offline Sandbox", color = AccentCyan, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}
