package com.videorder.downloader.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.videorder.downloader.presentation.common.*

@Composable
fun PrivacyScreen(onNavigateBack: () -> Unit) {
    Scaffold(
        containerColor = BgDark,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                }
                Text("Privacy & Security Policy", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderDark, RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = StatusSuccess, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("100% On-Device Operation", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Videorder operates entirely on your device. We do not operate remote relay servers, tracking services, or media upload queues. Your media never passes through any third-party infrastructure operated by Videorder.",
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                PrivacyPolicyPoint(
                    title = "Hardware Keystore Encryption",
                    description = "When you authenticate with Telegram, all authorization tokens and session keys are encrypted using AES-256 GCM backed by the Android KeyStore hardware security module. Tokens are never stored in plaintext or logged."
                )
            }

            item {
                PrivacyPolicyPoint(
                    title = "Strict DRM & Copyright Policy",
                    description = "Videorder strictly respects digital rights management (DRM), platform access controls, and authentication protections. The app does not circumvent Widevine, FairPlay, PlayReady, paywalls, or CAPTCHA systems."
                )
            }

            item {
                PrivacyPolicyPoint(
                    title = "Zero Telemetry & Zero Data Selling",
                    description = "We do not collect telemetry, advertise, sell user data, or profile your downloads. Your download history is stored solely inside your local Room database."
                )
            }

            item {
                PrivacyPolicyPoint(
                    title = "Full Data Control & Instant Wipe",
                    description = "You can clear your history or delete all stored media and session credentials at any time directly through the app settings."
                )
            }
        }
    }
}

@Composable
fun PrivacyPolicyPoint(title: String, description: String) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderDark, RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = AccentCyan)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = description, fontSize = 12.sp, lineHeight = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
