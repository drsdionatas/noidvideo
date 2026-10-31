package com.noidvideo.editor.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

object Routes {
    const val PROJECTS = "projects"
    const val PICKER = "picker"
    const val EDITOR = "editor"
    const val EXPORT = "export"
}

@Composable
fun NoidVideoApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.PROJECTS) {
        composable(Routes.PROJECTS) {
            ProjectsScreen(
                onNewProject = { navController.navigate(Routes.PICKER) },
                onOpenProject = { navController.navigate(Routes.EDITOR) }
            )
        }
        composable(Routes.PICKER) {
            MediaPickerScreen(
                onMediaSelected = { navController.navigate(Routes.EDITOR) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.EDITOR) {
            EditorScreen(
                onExport = { navController.navigate(Routes.EXPORT) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.EXPORT) {
            ExportScreen(onBack = { navController.popBackStack() })
        }
    }
}
