package com.videorder.downloader.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.videorder.downloader.VideorderApp
import com.videorder.downloader.presentation.downloads.DownloadsScreen
import com.videorder.downloader.presentation.downloads.DownloadsViewModel
import com.videorder.downloader.presentation.downloads.HistoryScreen
import com.videorder.downloader.presentation.downloads.HistoryViewModel
import com.videorder.downloader.presentation.files.FilesScreen
import com.videorder.downloader.presentation.files.FilesViewModel
import com.videorder.downloader.presentation.home.HomeScreen
import com.videorder.downloader.presentation.home.HomeViewModel
import com.videorder.downloader.presentation.onboarding.OnboardingScreen
import com.videorder.downloader.presentation.settings.AboutScreen
import com.videorder.downloader.presentation.settings.PrivacyScreen
import com.videorder.downloader.presentation.settings.SettingsScreen
import com.videorder.downloader.presentation.settings.SettingsViewModel
import com.videorder.downloader.presentation.splash.SplashScreen
import com.videorder.downloader.presentation.telegram.TelegramScreen
import com.videorder.downloader.presentation.telegram.TelegramViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun VideorderNavGraph(
    navController: NavHostController,
    app: VideorderApp,
    hasCompletedOnboarding: Boolean,
    initialSharedUrl: String? = null
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                hasCompletedOnboarding = hasCompletedOnboarding,
                onNavigateNext = { completed ->
                    val destination = if (completed) Screen.Home.route else Screen.Onboarding.route
                    navController.navigate(destination) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinish = {
                    CoroutineScope(Dispatchers.IO).launch {
                        app.settingsRepository.setOnboardingCompleted()
                    }
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            val homeViewModel = rememberViewModel {
                HomeViewModel(
                    app.detectMediaUseCase,
                    app.startDownloadUseCase,
                    app.downloadRepository
                )
            }

            // Handle incoming shared URL if present
            androidx.compose.runtime.LaunchedEffect(initialSharedUrl) {
                if (!initialSharedUrl.isNullOrBlank()) {
                    homeViewModel.onUrlChange(initialSharedUrl)
                    homeViewModel.detectMedia(initialSharedUrl)
                }
            }

            HomeScreen(
                viewModel = homeViewModel,
                onNavigateToDownloads = { navController.navigate(Screen.Downloads.route) },
                onNavigateToHistory = { navController.navigate(Screen.History.route) },
                onNavigateToTelegram = { navController.navigate(Screen.Telegram.route) },
                onNavigateToFiles = { navController.navigate(Screen.Files.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.Downloads.route) {
            val downloadsViewModel = rememberViewModel {
                DownloadsViewModel(
                    app.downloadRepository,
                    app.manageDownloadUseCase
                )
            }
            DownloadsScreen(
                viewModel = downloadsViewModel,
                onNavigateBack = { navController.popBackStack() },
                onOpenFileDetails = { path ->
                    navController.navigate(Screen.Files.route)
                }
            )
        }

        composable(Screen.History.route) {
            val historyViewModel = rememberViewModel {
                HistoryViewModel(app.historyRepository)
            }
            HistoryScreen(
                viewModel = historyViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Telegram.route) {
            val telegramViewModel = rememberViewModel {
                TelegramViewModel(
                    app.telegramAuthUseCase,
                    app.telegramBrowseUseCase
                )
            }
            TelegramScreen(
                viewModel = telegramViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Files.route) {
            val filesViewModel = rememberViewModel {
                FilesViewModel(app.fileRepository)
            }
            FilesScreen(
                viewModel = filesViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            val settingsViewModel = rememberViewModel {
                SettingsViewModel(
                    app.settingsRepository,
                    app.telegramRepository,
                    app.historyRepository
                )
            }
            SettingsScreen(
                viewModel = settingsViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPrivacy = { navController.navigate(Screen.Privacy.route) },
                onNavigateToAbout = { navController.navigate(Screen.About.route) }
            )
        }

        composable(Screen.Privacy.route) {
            PrivacyScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.About.route) {
            AboutScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}

@Composable
inline fun <reified VM : androidx.lifecycle.ViewModel> rememberViewModel(
    crossinline factory: () -> VM
): VM {
    return androidx.lifecycle.viewmodel.compose.viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return factory() as T
            }
        }
    )
}
