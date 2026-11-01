package com.noidvideo.editor.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.noidvideo.editor.ui.theme.NoidBlack

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

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(NoidBlack)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        color = NoidBlack
    ) {
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
}
