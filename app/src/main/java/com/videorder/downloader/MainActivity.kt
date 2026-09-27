package com.videorder.downloader

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.videorder.downloader.presentation.common.BgDark
import com.videorder.downloader.presentation.common.VideorderTheme
import com.videorder.downloader.presentation.navigation.VideorderNavGraph
import com.videorder.downloader.utils.UrlValidator

class MainActivity : ComponentActivity() {

    private var sharedUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleIncomingIntent(intent)

        val app = application as VideorderApp

        setContent {
            val settings by app.settingsRepository.getSettings().collectAsState(
                initial = com.videorder.downloader.domain.models.AppSettings(downloadLocation = "Downloads/Videorder")
            )
            val isDark = when (settings.themeMode) {
                "LIGHT" -> false
                else -> true
            }

            VideorderTheme(darkTheme = isDark) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BgDark
                ) {
                    val navController = rememberNavController()
                    VideorderNavGraph(
                        navController = navController,
                        app = app,
                        hasCompletedOnboarding = settings.hasCompletedOnboarding,
                        initialSharedUrl = sharedUrl
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return

        when (intent.action) {
            Intent.ACTION_SEND -> {
                if (intent.type == "text/plain") {
                    val rawText = intent.getStringExtra(Intent.EXTRA_TEXT)
                    sharedUrl = UrlValidator.extractUrlFromText(rawText)
                }
            }
            Intent.ACTION_VIEW -> {
                val dataUri = intent.dataString
                if (!dataUri.isNullOrBlank()) {
                    sharedUrl = dataUri
                }
            }
        }
    }
}
