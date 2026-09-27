package com.videorder.downloader.presentation.telegram

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.DownloadListener
import android.webkit.JavascriptInterface
import android.webkit.URLUtil
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.ui.viewinterop.AndroidView
import com.videorder.downloader.presentation.common.*

data class DetectedWebMedia(
    val url: String,
    val title: String,
    val mimeType: String
)

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun TelegramWebScreen(
    onNavigateBack: () -> Unit,
    onStartDownload: (url: String, title: String, mimeType: String) -> Unit,
    onSessionAuthenticated: (label: String) -> Unit
) {
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var currentUrl by remember { mutableStateOf("https://web.telegram.org/k/") }
    var pageTitle by remember { mutableStateOf("Telegram Web") }
    var loadingProgress by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var canGoBack by remember { mutableStateOf(false) }
    var canGoForward by remember { mutableStateOf(false) }
    var detectedMedia by remember { mutableStateOf<DetectedWebMedia?>(null) }
    var isAuthenticated by remember { mutableStateOf(false) }

    // Intercept hardware/system back button
    BackHandler {
        if (webViewRef?.canGoBack() == true) {
            webViewRef?.goBack()
        } else {
            onNavigateBack()
        }
    }

    Scaffold(
        containerColor = BgDark,
        topBar = {
            Column {
                TelegramWebTopBar(
                    title = pageTitle,
                    currentUrl = currentUrl,
                    canGoBack = canGoBack,
                    canGoForward = canGoForward,
                    isLoading = isLoading,
                    isAuthenticated = isAuthenticated,
                    onBackClick = {
                        if (webViewRef?.canGoBack() == true) {
                            webViewRef?.goBack()
                        } else {
                            onNavigateBack()
                        }
                    },
                    onForwardClick = { webViewRef?.goForward() },
                    onRefreshClick = { webViewRef?.reload() },
                    onSwitchClient = {
                        val target = if (currentUrl.contains("/k/")) "https://web.telegram.org/a/" else "https://web.telegram.org/k/"
                        currentUrl = target
                        webViewRef?.loadUrl(target)
                    },
                    onClose = onNavigateBack
                )

                if (isLoading) {
                    LinearProgressIndicator(
                        progress = { loadingProgress / 100f },
                        modifier = Modifier.fillMaxWidth().height(2.dp),
                        color = AccentCyan,
                        trackColor = SurfaceVariantDark
                    )
                }
            }
        },
        bottomBar = {
            // Floating sniffer banner when video/file is detected
            AnimatedVisibility(
                visible = detectedMedia != null,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                detectedMedia?.let { media ->
                    Surface(
                        color = SurfaceDark,
                        tonalElevation = 8.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, AccentCyan.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(AccentCyan.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (media.mimeType.contains("video")) Icons.Default.Movie else Icons.Default.AttachFile,
                                    contentDescription = null,
                                    tint = AccentCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Media Detected in Telegram",
                                    fontSize = 11.sp,
                                    color = AccentCyan,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = media.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    onStartDownload(media.url, media.title, media.mimeType)
                                    detectedMedia = null
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AccentCyan,
                                    contentColor = BgDark
                                ),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Download", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            IconButton(
                                onClick = { detectedMedia = null },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = TextSecondaryDark, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    WebView(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )

                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            databaseEnabled = true
                            cacheMode = WebSettings.LOAD_DEFAULT
                            setSupportZoom(true)
                            builtInZoomControls = true
                            displayZoomControls = false
                            mediaPlaybackRequiresUserGesture = false
                            useWideViewPort = true
                            loadWithOverviewMode = true
                            userAgentString = "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Mobile Safari/537.36"
                        }

                        val cookieManager = CookieManager.getInstance()
                        cookieManager.setAcceptCookie(true)
                        cookieManager.setAcceptThirdPartyCookies(this, true)

                        // Direct Download Listener
                        setDownloadListener { url, _, contentDisposition, mimetype, _ ->
                            val filename = URLUtil.guessFileName(url, contentDisposition, mimetype)
                            detectedMedia = DetectedWebMedia(
                                url = url,
                                title = filename,
                                mimeType = mimetype ?: "video/mp4"
                            )
                        }

                        // JavaScript Sniffer Bridge
                        addJavascriptInterface(object {
                            @JavascriptInterface
                            fun onMediaDetected(url: String, mimeType: String, title: String) {
                                if (url.isNotBlank() && !url.startsWith("blob:")) {
                                    post {
                                        detectedMedia = DetectedWebMedia(url, title, mimeType)
                                    }
                                }
                            }

                            @JavascriptInterface
                            fun onAuthenticated(userLabel: String) {
                                post {
                                    if (!isAuthenticated) {
                                        isAuthenticated = true
                                        onSessionAuthenticated(userLabel.ifBlank { "Official Telegram Web Session" })
                                    }
                                }
                            }
                        }, "VideorderBridge")

                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                loadingProgress = newProgress
                                isLoading = newProgress < 100
                            }

                            override fun onReceivedTitle(view: WebView?, title: String?) {
                                if (!title.isNullOrBlank()) {
                                    pageTitle = title
                                }
                            }
                        }

                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                currentUrl = url ?: currentUrl
                                canGoBack = view?.canGoBack() == true
                                canGoForward = view?.canGoForward() == true
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                currentUrl = url ?: currentUrl
                                canGoBack = view?.canGoBack() == true
                                canGoForward = view?.canGoForward() == true

                                // Inject Video/Media observer and Auth detector
                                val snifferScript = """
                                    (function() {
                                        if (window.__videorder_injected) return;
                                        window.__videorder_injected = true;

                                        // Sniff video playing/loading
                                        document.addEventListener('play', function(e) {
                                            if (e.target && e.target.tagName === 'VIDEO') {
                                                var src = e.target.currentSrc || e.target.src;
                                                if (src && !src.startsWith('blob:')) {
                                                    var name = 'Telegram_Video_' + Math.floor(Date.now() / 1000) + '.mp4';
                                                    window.VideorderBridge.onMediaDetected(src, 'video/mp4', name);
                                                }
                                            }
                                        }, true);

                                        // Check authentication state periodically
                                        var checkAuth = function() {
                                            if (document.querySelector('.chatlist') || document.querySelector('#column-left') || document.querySelector('.chat-list') || window.location.hash.length > 2) {
                                                window.VideorderBridge.onAuthenticated('Official Telegram Web');
                                            }
                                        };
                                        setInterval(checkAuth, 3000);
                                        checkAuth();
                                    })();
                                """.trimIndent()
                                view?.evaluateJavascript(snifferScript, null)
                            }
                        }

                        loadUrl(currentUrl)
                        webViewRef = this
                    }
                },
                update = { webView ->
                    webViewRef = webView
                }
            )
        }
    }
}

@Composable
fun TelegramWebTopBar(
    title: String,
    currentUrl: String,
    canGoBack: Boolean,
    canGoForward: Boolean,
    isLoading: Boolean,
    isAuthenticated: Boolean,
    onBackClick: () -> Unit,
    onForwardClick: () -> Unit,
    onRefreshClick: () -> Unit,
    onSwitchClient: () -> Unit,
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceDark)
            .padding(horizontal = 6.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick, modifier = Modifier.size(36.dp)) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        IconButton(
            onClick = onForwardClick,
            enabled = canGoForward,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Forward",
                tint = if (canGoForward) MaterialTheme.colorScheme.onSurface else BorderDark
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Center URL / Security Badge
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(20.dp))
                .background(SurfaceVariantDark)
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = if (isAuthenticated) AccentCyan else Color(0xFF4CAF50),
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "web.telegram.org (Official)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (isAuthenticated) "Signed in • Live Media Sniffer Active" else "Scan QR or Log in with Phone",
                        fontSize = 9.sp,
                        color = if (isAuthenticated) AccentCyan else TextSecondaryDark,
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(4.dp))

        IconButton(onClick = onRefreshClick, modifier = Modifier.size(36.dp)) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Reload",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(18.dp)
            )
        }

        IconButton(onClick = onSwitchClient, modifier = Modifier.size(36.dp)) {
            Icon(
                imageVector = Icons.Default.SwapHoriz,
                contentDescription = "Switch Web K/A",
                tint = AccentCyan,
                modifier = Modifier.size(18.dp)
            )
        }

        IconButton(onClick = onClose, modifier = Modifier.size(36.dp)) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = TextSecondaryDark,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
