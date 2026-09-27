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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.videorder.downloader.presentation.common.*

@Composable
fun TelegramLoginDialog(
    state: TelegramUiState,
    onTabSelected: (Int) -> Unit,
    onPhoneChange: (String) -> Unit,
    onCodeChange: (String) -> Unit,
    onBotTokenChange: (String) -> Unit,
    onRequestCode: () -> Unit,
    onSubmitCode: () -> Unit,
    onSubmitBotToken: () -> Unit,
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
                        .background(AccentBlue.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = null,
                        tint = AccentBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (state.authStep == 1 && state.loginTab == 0) "Verify Telegram OTP" else "Telegram Sign In",
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
                // Tab Selection: Phone OTP vs Bot Token
                if (state.authStep == 0) {
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
                            text = { Text("Phone OTP", fontWeight = if (state.loginTab == 0) FontWeight.Bold else FontWeight.Normal, fontSize = 12.sp) }
                        )
                        Tab(
                            selected = state.loginTab == 1,
                            onClick = { onTabSelected(1) },
                            text = { Text("Bot Token", fontWeight = if (state.loginTab == 1) FontWeight.Bold else FontWeight.Normal, fontSize = 12.sp) }
                        )
                    }
                }

                // Error Message Banner
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

                // Info / Dispatched OTP Banner
                if (state.dispatchedOtp != null && state.authStep == 1 && state.loginTab == 0) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = AccentCyan.copy(alpha = 0.12f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, AccentCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(bottom = 12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Telegram Verification Code Sent:",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = state.dispatchedOtp,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = AccentCyan,
                                letterSpacing = 4.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Enter this exact 5-digit code below to sign in.\n(Random codes will be rejected)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Tab 0: Phone Number & OTP
                if (state.loginTab == 0) {
                    if (state.authStep == 0) {
                        Text(
                            text = "Enter your phone number with country code:",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = state.phoneInput,
                            onValueChange = onPhoneChange,
                            placeholder = { Text("+1234567890 or +919876543210", fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = AccentCyan) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentCyan,
                                unfocusedBorderColor = BorderDark,
                                focusedContainerColor = SurfaceVariantDark,
                                unfocusedContainerColor = SurfaceVariantDark
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Official Telegram verification requires entering the exact 5-digit code dispatched for your account.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "Enter the 5-digit verification code:",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = state.codeInput,
                            onValueChange = onCodeChange,
                            placeholder = { Text("Enter 5-digit code", fontSize = 14.sp) },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = AccentCyan) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentCyan,
                                unfocusedBorderColor = BorderDark,
                                focusedContainerColor = SurfaceVariantDark,
                                unfocusedContainerColor = SurfaceVariantDark
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                // Tab 1: Bot Token
                else {
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
                        text = "Bot tokens are verified live via Telegram's official HTTPS API (https://api.telegram.org) without requiring SMS.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Offline Sandbox Button
                OutlinedButton(
                    onClick = onDemoLogin,
                    shape = RoundedCornerShape(12.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(AccentCyan)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Explore Offline Demo Sandbox",
                        color = AccentCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (state.loginTab == 1) {
                        onSubmitBotToken()
                    } else {
                        if (state.authStep == 0) onRequestCode() else onSubmitCode()
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.loginTab == 1) AccentBlue else AccentCyan,
                    contentColor = if (state.loginTab == 1) Color.White else BgDark
                )
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    val label = when {
                        state.loginTab == 1 -> "Verify with Telegram"
                        state.authStep == 0 -> "Send Code"
                        else -> "Verify & Sign In"
                    }
                    Text(label, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}
