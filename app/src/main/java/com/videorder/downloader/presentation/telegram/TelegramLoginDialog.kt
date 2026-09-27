package com.videorder.downloader.presentation.telegram

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
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
    onPhoneChange: (String) -> Unit,
    onCodeChange: (String) -> Unit,
    onRequestCode: () -> Unit,
    onSubmitCode: () -> Unit,
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
                    Icon(Icons.Default.Send, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (state.authStep == 0) "Telegram Sign In" else "Enter Code",
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
                // Security reassurance badge
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceVariantDark),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = StatusSuccess, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Session keys encrypted in Android KeyStore. Zero credentials sent to third-party servers.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (state.errorMessage != null) {
                    Text(
                        text = state.errorMessage,
                        color = StatusError,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                if (state.authStep == 0) {
                    Text(
                        text = "Enter your phone number registered with Telegram:",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = state.phoneInput,
                        onValueChange = onPhoneChange,
                        placeholder = { Text("+1234567890", fontSize = 14.sp) },
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

                    Spacer(modifier = Modifier.height(16.dp))

                    // Demo Mode button for instant sandbox testing
                    OutlinedButton(
                        onClick = onDemoLogin,
                        shape = RoundedCornerShape(12.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(AccentCyan)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Explore Demo Chats & Media (Instant Sandbox)", color = AccentCyan, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    Text(
                        text = "Enter the 5-digit verification code sent to your official Telegram app:",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = state.codeInput,
                        onValueChange = onCodeChange,
                        placeholder = { Text("12345", fontSize = 14.sp) },
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
        },
        confirmButton = {
            Button(
                onClick = {
                    if (state.authStep == 0) onRequestCode() else onSubmitCode()
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan, contentColor = BgDark)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = BgDark, strokeWidth = 2.dp)
                } else {
                    Text(if (state.authStep == 0) "Send Code" else "Verify & Sign In", fontWeight = FontWeight.Bold)
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
