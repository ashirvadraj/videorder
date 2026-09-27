package com.videorder.downloader.presentation.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object Downloads : Screen("downloads")
    object History : Screen("history")
    object Telegram : Screen("telegram")
    object TelegramWeb : Screen("telegram_web")
    object Files : Screen("files")
    object VideoPlayer : Screen("video_player/{filePath}") {
        fun createRoute(filePath: String): String {
            val encoded = java.net.URLEncoder.encode(filePath, "UTF-8")
            return "video_player/$encoded"
        }
    }
    object Settings : Screen("settings")
    object Privacy : Screen("privacy")
    object About : Screen("about")
}
