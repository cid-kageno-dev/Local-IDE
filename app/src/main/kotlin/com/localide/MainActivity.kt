package com.localide

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.localide.ui.auth.AuthScreen
import com.localide.ui.auth.ProfileSheet
import com.localide.ui.editor.CodeEditorScreen
import com.localide.ui.filemanager.FileManagerScreen
import com.localide.ui.server.ServerScreen
import com.localide.ui.terminal.TerminalScreen
import com.localide.ui.theme.LocalIDETheme
import com.localide.viewmodel.AuthState
import com.localide.viewmodel.AuthViewModel

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Editor : Screen("editor", "Editor", Icons.Filled.Code)
    data object Files : Screen("files", "Files", Icons.Filled.Folder)
    data object Terminal : Screen("terminal", "Terminal", Icons.Filled.Terminal)
    data object Server : Screen("server", "Server", Icons.Filled.Storage)
}

val bottomNavItems = listOf(Screen.Editor, Screen.Files, Screen.Terminal, Screen.Server)

class MainActivity : ComponentActivity() {

    private var pendingGitHubCode: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        pendingGitHubCode = intent?.data?.getQueryParameter("code")
        setContent {
            LocalIDETheme {
                val authVm: AuthViewModel = viewModel()
                HandleGitHubCallback(authVm)
                AppRoot(authVm)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val code = intent.data?.getQueryParameter("code")
        if (code != null) {
            pendingGitHubCode = code
        }
    }

    @Composable
    private fun HandleGitHubCallback(authVm: AuthViewModel) {
        val code = pendingGitHubCode
        LaunchedEffect(code) {
            if (code != null) {
                pendingGitHubCode = null
                authVm.handleGitHubCallback(code)
            }
        }
    }
}

@Composable
fun AppRoot(authVm: AuthViewModel) {
    val authState by authVm.authState.collectAsState()

    when (val state = authState) {
        is AuthState.Loading -> {
            // Keep splash screen visible while loading session
        }
        is AuthState.Authenticated -> {
            LocalIDEApp(
                session = state.session,
                onSignOut = authVm::signOut
            )
        }
        is AuthState.Unauthenticated, is AuthState.Error -> {
            AuthScreen(vm = authVm)
        }
    }
}

@Composable
fun LocalIDEApp(
    session: com.localide.auth.UserSession,
    onSignOut: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    var showProfile by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                tonalElevation = androidx.compose.ui.unit.Dp.Unspecified
            ) {
                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label, style = MaterialTheme.typography.labelSmall) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Person, contentDescription = "Profile") },
                    label = { Text("Profile", style = MaterialTheme.typography.labelSmall) },
                    selected = false,
                    onClick = { showProfile = true },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Editor.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Editor.route) { CodeEditorScreen() }
            composable(Screen.Files.route) { FileManagerScreen() }
            composable(Screen.Terminal.route) { TerminalScreen() }
            composable(Screen.Server.route) { ServerScreen() }
        }
    }

    if (showProfile) {
        ProfileSheet(
            session = session,
            onDismiss = { showProfile = false },
            onSignOut = onSignOut
        )
    }
}
