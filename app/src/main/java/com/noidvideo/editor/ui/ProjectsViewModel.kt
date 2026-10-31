package com.noidvideo.editor.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.noidvideo.editor.NoidVideoApp
import com.noidvideo.editor.projects.ProjectEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProjectsViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = (app as NoidVideoApp).projectsRepository

    val projects: StateFlow<List<ProjectEntity>> = repository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun delete(project: ProjectEntity) {
        viewModelScope.launch { repository.delete(project) }
    }
}
