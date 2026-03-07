package com.deenbase.app

import android.os.Build
import android.os.Bundle
import com.deenbase.app.BuildConfig
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.first

import com.deenbase.app.R
import com.deenbase.app.features.dhikr.ui.DhikrDetailScreen
import com.deenbase.app.features.dhikr.ui.DhikrScreen
import com.deenbase.app.features.dhikr.ui.getBismillahContent
import com.deenbase.app.features.dhikr.ui.getRadituContent
import com.deenbase.app.features.hadith.ui.HadithScreen
import com.deenbase.app.features.hadith.ui.HadithDetailScreen
import com.deenbase.app.features.hadith.ui.ChaptersScreen
import com.deenbase.app.features.hadith.ui.HadithListScreen
import com.deenbase.app.features.hadith.ui.HadithSearchScreen
import com.deenbase.app.features.hadith.viewmodel.HadithViewModel
import com.deenbase.app.features.onboarding.ui.OnboardingScreen
import com.deenbase.app.features.home.ui.HomeScreen
import com.deenbase.app.features.quran.ui.QuranScreen
import com.deenbase.app.features.quran.ui.SavedVersesScreen
import com.deenbase.app.features.settings.ui.AboutScreen
import com.deenbase.app.features.settings.ui.AppPreferencesScreen
import com.deenbase.app.features.settings.ui.NotificationSettingsScreen
import com.deenbase.app.features.settings.ui.QuranGoalScreen
import com.deenbase.app.features.settings.ui.QuranSettingsScreen
import com.deenbase.app.features.settings.ui.SettingsScreen
import com.deenbase.app.features.tasbih.ui.TasbihScreen
import com.deenbase.app.features.tasbih.ui.SubhanallahScreen
import com.deenbase.app.features.tasbih.viewmodel.TasbihViewModel
import com.deenbase.app.ui.theme.DeenBaseTheme
import com.deenbase.app.ui.LocalHapticsEnabled
import com.deenbase.app.data.SettingsManager
import com.deenbase.app.update.UpdateViewModel

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home     : Screen("home",     "Home",     Icons.Filled.Home)
    object Quran    : Screen("quran",    "Quran",    Icons.AutoMirrored.Filled.MenuBook)
    object Dhikr    : Screen("dhikr",    "Dhikr",    Icons.Filled.HistoryEdu)
    object Settings : Screen("settings", "Settings", Icons.Filled.Settings)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge()
        com.deenbase.app.notifications.NotificationHelper.createChannels(this)
        com.deenbase.app.notifications.NotificationHelper.scheduleAdhkarAlarms(this)
        val navigateTo = intent?.getStringExtra("navigate_to")
        setContent {
            val settingsManager = remember { SettingsManager(applicationContext) }
            val themeMode by settingsManager.themeMode.collectAsStateWithLifecycle(initialValue = "system")
            val oledMode by settingsManager.oledMode.collectAsStateWithLifecycle(initialValue = false)
            val hapticsEnabled by settingsManager.hapticsEnabled.collectAsStateWithLifecycle(initialValue = true)
            val darkTheme = when (themeMode) {
                "dark"  -> true
                "light" -> false
                else    -> isSystemInDarkTheme()
            }
            CompositionLocalProvider(LocalHapticsEnabled provides hapticsEnabled) {
                DeenBaseTheme(darkTheme = darkTheme, oledMode = oledMode) {
                    val view = LocalView.current
                    SideEffect {
                        val window = (view.context as ComponentActivity).window
                        WindowInsetsControllerCompat(window, view).isAppearanceLightStatusBars    = !darkTheme
                        WindowInsetsControllerCompat(window, view).isAppearanceLightNavigationBars = !darkTheme
                    }

                    // ── Update check ──────────────────────────────────────────
                    val updateViewModel: UpdateViewModel = viewModel()
                    val updateState by updateViewModel.state.collectAsStateWithLifecycle()
                    val context = LocalContext.current

                    if (updateState.showDialog) {
                        AlertDialog(
                            onDismissRequest = { updateViewModel.dismissDialog() },
                            title = { Text("Update Available") },
                            text  = { Text("Version ${updateState.latestVersion} is ready. Update now for the latest features and fixes.") },
                            confirmButton = { Button(onClick = { updateViewModel.startDownload(context) }) { Text("Update Now") } },
                            dismissButton = { TextButton(onClick = { updateViewModel.dismissDialog() }) { Text("Remind Me Later") } }
                        )
                    }

                    if (updateState.noUpdateFound) {
                        AlertDialog(
                            onDismissRequest = { updateViewModel.dismissNoUpdate() },
                            title = { Text("You're up to date") },
                            text  = { Text("DeenBase ${BuildConfig.VERSION_NAME} is the latest version.") },
                            confirmButton = { Button(onClick = { updateViewModel.dismissNoUpdate() }) { Text("OK") } }
                        )
                    }

                    val navController = rememberNavController()
                    val screens = listOf(Screen.Home, Screen.Quran, Screen.Dhikr, Screen.Settings)

                    // ── Onboarding gate ───────────────────────────────────────
                    val onboardingDone by settingsManager.onboardingDone
                        .collectAsStateWithLifecycle(initialValue = null)

                    when (onboardingDone) {
                        null -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.background)
                            )
                        }
                        false -> {
                            OnboardingScreen(onFinish = { /* recompose handles transition */ })
                        }
                        else -> {

                    LaunchedEffect(navigateTo) {
                        when (navigateTo) {
                            "goal_quran" -> {
                                val sm = SettingsManager(applicationContext)
                                val surahId = sm.goalSurahId.first()
                                val verse   = sm.goalVerse.first()
                                navController.navigate("goal_surah/$surahId/$verse")
                            }
                            "subhanallah" -> navController.navigate("subhanallah")
                            "dhikr"       -> navController.navigate("dhikr")
                        }
                    }

                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    var immediateRoute by remember { mutableStateOf(currentRoute) }
                    DisposableEffect(navController) {
                        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
                            immediateRoute = destination.route
                        }
                        navController.addOnDestinationChangedListener(listener)
                        onDispose { navController.removeOnDestinationChangedListener(listener) }
                    }

                    val isBottomBarVisible = currentRoute?.startsWith("browse_surah") != true &&
                        currentRoute?.startsWith("goal_surah")   != true &&
                        currentRoute?.startsWith("dhikr_detail") != true &&
                        currentRoute != "quran_settings" &&
                        currentRoute != "quran_goal" &&
                        currentRoute != "app_preferences" &&
                        currentRoute != "notification_settings" &&
                        currentRoute != "tasbih" &&
                        currentRoute != "subhanallah" &&
                        currentRoute != "hadith" &&
                        currentRoute != "hadith_chapters" &&
                        currentRoute != "hadith_list" &&
                        currentRoute != "hadith_detail" &&
                        currentRoute != "hadith_search" &&
                        currentRoute != "saved_verses" &&
                        currentRoute != "about"

                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        Box(modifier = Modifier.fillMaxSize()) {
                            NavHost(
                                navController = navController,
                                startDestination = Screen.Home.route,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                composable(Screen.Home.route) {
                                    HomeScreen(
                                        onReadQuranClick = { surahId, verse -> navController.navigate("goal_surah/$surahId/$verse") },
                                        onHadithClick = { navController.navigate("hadith") }
                                    )
                                }
                                composable(
                                    route = "tasbih",
                                    enterTransition = { slideInHorizontally { it } + fadeIn(tween(300)) },
                                    popExitTransition = { slideOutHorizontally { it } + fadeOut(tween(300)) }
                                ) {
                                    val tasbihViewModel: TasbihViewModel = viewModel()
                                    TasbihScreen(viewModel = tasbihViewModel)
                                }
                                composable(
                                    route = "subhanallah",
                                    enterTransition = { slideInHorizontally { it } + fadeIn(tween(300)) },
                                    popExitTransition = { slideOutHorizontally { it } + fadeOut(tween(300)) }
                                ) {
                                    SubhanallahScreen(onNavigateBack = { navController.popBackStack() })
                                }
                                composable(Screen.Quran.route) {
                                    QuranScreen(
                                        onSurahClick = { surahId, surahName ->
                                            navController.navigate("browse_surah/$surahId/${surahName.replace(" ", "_")}")
                                        },
                                        onSavedClick = { navController.navigate("saved_verses") }
                                    )
                                }
                                composable(
                                    route = "saved_verses",
                                    enterTransition = { slideInHorizontally { it } + fadeIn(tween(300)) },
                                    popExitTransition = { slideOutHorizontally { it } + fadeOut(tween(300)) }
                                ) {
                                    SavedVersesScreen(
                                        onNavigateBack = { navController.popBackStack() },
                                        onVerseClick = { surahId, verseNumber, surahName ->
                                            navController.navigate("browse_surah/$surahId/${surahName.replace(" ", "_")}")
                                        }
                                    )
                                }
                                composable(
                                    route = "browse_surah/{surahId}/{surahName}",
                                    enterTransition = { slideInHorizontally { it } + fadeIn(tween(300)) },
                                    popExitTransition = { slideOutHorizontally { it } + fadeOut(tween(300)) }
                                ) { backStackEntry ->
                                    val surahId   = backStackEntry.arguments?.getString("surahId")?.toIntOrNull() ?: 1
                                    val surahName = backStackEntry.arguments?.getString("surahName")?.replace("_", " ") ?: ""
                                    com.deenbase.app.features.quran.ui.ReadingScreen(
                                        surahId = surahId,
                                        surahName = surahName,
                                        startVerse = 1,
                                        trackGoal = false,
                                        onNavigateBack = { navController.popBackStack() }
                                    )
                                }
                                composable(
                                    route = "goal_surah/{surahId}/{startVerse}",
                                    enterTransition = { slideInHorizontally { it } + fadeIn(tween(300)) },
                                    popExitTransition = { slideOutHorizontally { it } + fadeOut(tween(300)) }
                                ) { backStackEntry ->
                                    val surahId    = backStackEntry.arguments?.getString("surahId")?.toIntOrNull() ?: 1
                                    val startVerse = backStackEntry.arguments?.getString("startVerse")?.toIntOrNull() ?: 1
                                    com.deenbase.app.features.quran.ui.ReadingScreen(
                                        surahId = surahId,
                                        startVerse = startVerse,
                                        trackGoal = true,
                                        onNavigateBack = { navController.popBackStack() },
                                        onNextSurah = { nextId ->
                                            navController.popBackStack()
                                            navController.navigate("goal_surah/$nextId/1")
                                        }
                                    )
                                }
                                composable(Screen.Dhikr.route) {
                                    DhikrScreen(
                                        onDhikrClick = { dhikrId, period ->
                                            navController.navigate("dhikr_detail/$dhikrId/$period")
                                        },
                                        onSubhanallahClick = { navController.navigate("subhanallah") },
                                        onTasbihClick = { navController.navigate("tasbih") }
                                    )
                                }
                                composable(
                                    route = "dhikr_detail/{dhikrId}/{period}",
                                    enterTransition = { slideInHorizontally { it } + fadeIn(tween(300)) },
                                    popExitTransition = { slideOutHorizontally { it } + fadeOut(tween(300)) }
                                ) { backStackEntry ->
                                    val dhikrId = backStackEntry.arguments?.getString("dhikrId") ?: "bismillah"
                                    val period  = backStackEntry.arguments?.getString("period")  ?: "morning"
                                    val content = when (dhikrId) {
                                        "bismillah" -> getBismillahContent(period)
                                        "raditu"    -> getRadituContent(period)
                                        else        -> getBismillahContent(period)
                                    }
                                    DhikrDetailScreen(
                                        content = content,
                                        onNavigateBack = { navController.popBackStack() }
                                    )
                                }
                                composable(Screen.Settings.route) {
                                    SettingsScreen(
                                        onNavigateBack = { navController.popBackStack() },
                                        onQuranSettingsClick = { navController.navigate("quran_settings") },
                                        onQuranGoalClick = { navController.navigate("quran_goal") },
                                        onAppPreferencesClick = { navController.navigate("app_preferences") },
                                        onNotificationSettingsClick = { navController.navigate("notification_settings") },
                                        onAboutClick = { navController.navigate("about") },
                                        onCheckForUpdatesClick = { updateViewModel.checkForUpdate(manual = true) },
                                        isCheckingForUpdates = updateState.isChecking
                                    )
                                }
                                composable(
                                    route = "about",
                                    enterTransition = { slideInHorizontally { it } + fadeIn(tween(300)) },
                                    popExitTransition = { slideOutHorizontally { it } + fadeOut(tween(300)) }
                                ) {
                                    AboutScreen(onNavigateBack = { navController.popBackStack() })
                                }
                                composable(
                                    route = "app_preferences",
                                    enterTransition = { slideInHorizontally { it } + fadeIn(tween(300)) },
                                    popExitTransition = { slideOutHorizontally { it } + fadeOut(tween(300)) }
                                ) {
                                    AppPreferencesScreen(onNavigateBack = { navController.popBackStack() })
                                }
                                composable(
                                    route = "quran_settings",
                                    enterTransition = { slideInHorizontally { it } + fadeIn(tween(300)) },
                                    popExitTransition = { slideOutHorizontally { it } + fadeOut(tween(300)) }
                                ) {
                                    QuranSettingsScreen(onNavigateBack = { navController.popBackStack() })
                                }
                                composable(
                                    route = "quran_goal",
                                    enterTransition = { slideInHorizontally { it } + fadeIn(tween(300)) },
                                    popExitTransition = { slideOutHorizontally { it } + fadeOut(tween(300)) }
                                ) {
                                    QuranGoalScreen(onNavigateBack = { navController.popBackStack() })
                                }
                                composable(
                                    route = "notification_settings",
                                    enterTransition = { slideInHorizontally { it } + fadeIn(tween(300)) },
                                    popExitTransition = { slideOutHorizontally { it } + fadeOut(tween(300)) }
                                ) {
                                    NotificationSettingsScreen(onNavigateBack = { navController.popBackStack() })
                                }
                                composable(
                                    route = "hadith",
                                    enterTransition   = { slideInHorizontally { it } + fadeIn(tween(300)) },
                                    exitTransition    = { slideOutHorizontally { -it } + fadeOut(tween(300)) },
                                    popEnterTransition = { slideInHorizontally { -it } + fadeIn(tween(300)) },
                                    popExitTransition  = { slideOutHorizontally { it } + fadeOut(tween(300)) }
                                ) {
                                    val hadithViewModel: HadithViewModel = viewModel()
                                    HadithScreen(
                                        onNavigateBack = { navController.popBackStack() },
                                        onBookClick = { book ->
                                            hadithViewModel.selectBook(book)
                                            navController.navigate("hadith_chapters")
                                        },
                                        onSearchClick = { navController.navigate("hadith_search") },
                                        viewModel = hadithViewModel
                                    )
                                }
                                composable(
                                    route = "hadith_chapters",
                                    enterTransition   = { slideInHorizontally { it } + fadeIn(tween(300)) },
                                    exitTransition    = { slideOutHorizontally { -it } + fadeOut(tween(300)) },
                                    popEnterTransition = { slideInHorizontally { -it } + fadeIn(tween(300)) },
                                    popExitTransition  = { slideOutHorizontally { it } + fadeOut(tween(300)) }
                                ) {
                                    val hadithViewModel: HadithViewModel = viewModel(
                                        viewModelStoreOwner = navController.getBackStackEntry("hadith")
                                    )
                                    ChaptersScreen(
                                        onNavigateBack = { navController.popBackStack() },
                                        onChapterClick = { chapter ->
                                            hadithViewModel.selectChapter(chapter)
                                            navController.navigate("hadith_list")
                                        },
                                        viewModel = hadithViewModel
                                    )
                                }
                                composable(
                                    route = "hadith_list",
                                    enterTransition   = { slideInHorizontally { it } + fadeIn(tween(300)) },
                                    exitTransition    = { slideOutHorizontally { -it } + fadeOut(tween(300)) },
                                    popEnterTransition = { slideInHorizontally { -it } + fadeIn(tween(300)) },
                                    popExitTransition  = { slideOutHorizontally { it } + fadeOut(tween(300)) }
                                ) {
                                    val hadithViewModel: HadithViewModel = viewModel(
                                        viewModelStoreOwner = navController.getBackStackEntry("hadith")
                                    )
                                    HadithListScreen(
                                        onNavigateBack = { navController.popBackStack() },
                                        onHadithClick = { hadith ->
                                            hadithViewModel.selectHadith(hadith)
                                            navController.navigate("hadith_detail")
                                        },
                                        viewModel = hadithViewModel
                                    )
                                }
                                composable(
                                    route = "hadith_search",
                                    enterTransition   = { slideInHorizontally { it } + fadeIn(tween(300)) },
                                    exitTransition    = { slideOutHorizontally { -it } + fadeOut(tween(300)) },
                                    popEnterTransition = { slideInHorizontally { -it } + fadeIn(tween(300)) },
                                    popExitTransition  = { slideOutHorizontally { it } + fadeOut(tween(300)) }
                                ) {
                                    val hadithViewModel: HadithViewModel = viewModel(
                                        viewModelStoreOwner = navController.getBackStackEntry("hadith")
                                    )
                                    HadithSearchScreen(
                                        onNavigateBack = { navController.popBackStack() },
                                        onHadithClick = { hadith ->
                                            hadithViewModel.selectHadith(hadith)
                                            navController.navigate("hadith_detail")
                                        },
                                        viewModel = hadithViewModel
                                    )
                                }
                                composable(
                                    route = "hadith_detail",
                                    enterTransition   = { slideInHorizontally { it } + fadeIn(tween(300)) },
                                    exitTransition    = { slideOutHorizontally { -it } + fadeOut(tween(300)) },
                                    popEnterTransition = { slideInHorizontally { -it } + fadeIn(tween(300)) },
                                    popExitTransition  = { slideOutHorizontally { it } + fadeOut(tween(300)) }
                                ) {
                                    val hadithViewModel: HadithViewModel = viewModel(
                                        viewModelStoreOwner = navController.getBackStackEntry("hadith")
                                    )
                                    val selectedHadith = hadithViewModel.selectedHadith.collectAsState().value
                                    selectedHadith?.let {
                                        HadithDetailScreen(
                                            hadith = it,
                                            onNavigateBack = { navController.popBackStack() }
                                        )
                                    }
                                }
                            }

                            // ── Bottom bar ────────────────────────────────────
                            AnimatedVisibility(
                                visible = isBottomBarVisible,
                                enter = slideInVertically(animationSpec = tween(durationMillis = 350, easing = androidx.compose.animation.core.FastOutSlowInEasing)) { it } + fadeIn(tween(350)),
                                exit  = slideOutVertically(animationSpec = tween(durationMillis = 350, easing = androidx.compose.animation.core.FastOutSlowInEasing)) { it } + fadeOut(tween(350)),
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .navigationBarsPadding()
                                    .padding(bottom = 16.dp)
                            ) {
                                FloatingBottomBar(screens = screens, navController = navController)
                            }
                        }
                    } // end Scaffold
                    } // end else (onboarding done)
                    } // end when (onboardingDone)
                } // end DeenBaseTheme
            } // end CompositionLocalProvider
        }
    }
}

@Composable
fun FloatingBottomBar(
    screens: List<Screen>,
    navController: androidx.navigation.NavController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val selectedIndex = screens.indexOfFirst { screen ->
        val isExactMatch   = currentDestination?.route == screen.route
        val isReadingSurah = screen.route == Screen.Quran.route &&
            (currentDestination?.route?.startsWith("browse_surah") == true ||
             currentDestination?.route?.startsWith("goal_surah")   == true ||
             currentDestination?.route == "saved_verses")
        isExactMatch || isReadingSurah
    }.coerceAtLeast(0)

    val itemWidth    = 72.dp
    val spacing      = 12.dp
    val outerPadding = 8.dp
    val barWidth     = (itemWidth * screens.size) + (spacing * (screens.size - 1)) + (outerPadding * 2)

    val indicatorOffset by animateDpAsState(
        targetValue = (itemWidth + spacing) * selectedIndex,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
        label = "IndicatorSlide"
    )

    Surface(
        modifier = modifier.width(barWidth).height(64.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 6.dp,
        shadowElevation = 8.dp
    ) {
        Box(modifier = Modifier.padding(outerPadding)) {
            Box(
                modifier = Modifier
                    .offset(x = indicatorOffset)
                    .width(itemWidth)
                    .height(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
            )
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                screens.forEach { screen ->
                    val isSelected = currentDestination?.route == screen.route ||
                        (screen.route == Screen.Quran.route &&
                            (currentDestination?.route?.startsWith("browse_surah") == true ||
                             currentDestination?.route?.startsWith("goal_surah")   == true)) ||
                        (screen.route == Screen.Dhikr.route &&
                            currentDestination?.route?.startsWith("dhikr_detail") == true)
                    val interactionSource = remember { MutableInteractionSource() }
                    Box(
                        modifier = Modifier
                            .width(itemWidth)
                            .height(48.dp)
                            .clip(CircleShape)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null,
                                onClick = {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState    = true
                                    }
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (screen.route == Screen.Dhikr.route) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_dhikr),
                                contentDescription = screen.label,
                                tint = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer
                                       else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.label,
                                tint = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer
                                       else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
