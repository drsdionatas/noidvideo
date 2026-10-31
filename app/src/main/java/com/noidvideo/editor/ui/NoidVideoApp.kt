package com.noidvideo.editor.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
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
    val editorViewModel: EditorViewModel = viewModel()

    NavHost(navController = navController, startDestination = Routes.PROJECTS) {
        composable(Routes.PROJECTS) {
            ProjectsScreen(
                onNewProject = { navController.navigate(Routes.PICKER) },
                onOpenProject = { navController.navigate(Routes.EDITOR) }
            )
        }
        composable(Routes.PICKER) {
            MediaPickerScreen(
                viewModel = editorViewModel,
                onMediaSelected = { navController.navigate(Routes.EDITOR) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.EDITOR) {
            EditorScreen(
                viewModel = editorViewModel,
                onExport = { navController.navigate(Routes.EXPORT) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.EXPORT) {
            ExportScreen(
                editorViewModel = editorViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
