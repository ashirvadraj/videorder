package com.videorder.downloader.downloader.core

sealed class VideorderException(message: String) : Exception(message)

class UnsupportedUrlException(message: String = "This website or URL format is not currently supported.") :
    VideorderException(message)

class DrmProtectedException(message: String = "This media is protected by DRM (e.g. Widevine/FairPlay) and cannot be downloaded by Videorder.") :
    VideorderException(message)

class AuthenticationRequiredException(message: String = "Please sign in to the service using its supported official authentication method.") :
    VideorderException(message)

class NetworkInterruptedException(message: String = "Connection interrupted. Download will resume when network is available.") :
    VideorderException(message)

class StorageAccessException(message: String = "Storage permission or directory access is required to save this file.") :
    VideorderException(message)
