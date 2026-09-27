package com.videorder.downloader.domain.models

enum class DownloadStatus {
    QUEUED,
    DOWNLOADING,
    PAUSED,
    WAITING_FOR_WIFI,
    COMPLETED,
    FAILED,
    CANCELLED
}
