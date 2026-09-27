package com.videorder.downloader.presentation.common

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.videorder.downloader.domain.models.DownloadStatus

@Composable
fun StatusBadge(status: DownloadStatus) {
    val (bgColor, textColor, label) = when (status) {
        DownloadStatus.DOWNLOADING -> Triple(AccentCyan.copy(alpha = 0.15f), AccentCyan, "Downloading")
        DownloadStatus.PAUSED -> Triple(StatusPaused.copy(alpha = 0.15f), StatusPaused, "Paused")
        DownloadStatus.WAITING_FOR_WIFI -> Triple(StatusWarning.copy(alpha = 0.15f), StatusWarning, "Waiting for Wi-Fi")
        DownloadStatus.COMPLETED -> Triple(StatusSuccess.copy(alpha = 0.15f), StatusSuccess, "Completed")
        DownloadStatus.FAILED -> Triple(StatusError.copy(alpha = 0.15f), StatusError, "Failed")
        DownloadStatus.CANCELLED -> Triple(TextSecondaryDark.copy(alpha = 0.15f), TextSecondaryDark, "Cancelled")
        DownloadStatus.QUEUED -> Triple(AccentBlue.copy(alpha = 0.15f), AccentBlue, "Queued")
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun ProgressBarWithStats(
    progress: Float,
    status: DownloadStatus,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        label = "progress"
    )

    val progressColor = when (status) {
        DownloadStatus.DOWNLOADING -> AccentCyan
        DownloadStatus.PAUSED -> StatusPaused
        DownloadStatus.WAITING_FOR_WIFI -> StatusWarning
        DownloadStatus.COMPLETED -> StatusSuccess
        DownloadStatus.FAILED -> StatusError
        else -> AccentBlue
    }

    LinearProgressIndicator(
        progress = { animatedProgress },
        modifier = modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(CircleShape),
        color = progressColor,
        trackColor = SurfaceVariantDark
    )
}

@Composable
fun EmptyStateView(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    actionButton: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(SurfaceVariantDark),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AccentCyan,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (actionButton != null) {
            Spacer(modifier = Modifier.height(20.dp))
            actionButton()
        }
    }
}
