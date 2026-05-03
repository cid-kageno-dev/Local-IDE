package com.localide

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.localide.ui.editor.CodeEditorScreen
import com.localide.ui.filemanager.FileManagerScreen
import com.localide.ui.server.ServerScreen
import com.localide.ui.terminal.TerminalScreen
import com.localide.ui.theme.LocalIDETheme

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Editor : Screen("editor", "Editor", Icons.Filled.Code)
    data object Files : Screen("files", "Files", Icons.Filled.Folder)
    data object Terminal : Screen("terminal", "Terminal", Icons.Filled.Terminal)
    data object Server : Screen("server", "Server", Icons.Filled.Storage)
}

val bottomNavItems = listOf(Screen.Editor, Screen.Files, Screen.Terminal, Screen.Server)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LocalIDETheme {
                LocalIDEApp()
            }
        }
    }
}

@Composable
fun LocalIDEApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

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
}
