package com.noidvideo.editor

import android.app.Application
import com.noidvideo.editor.projects.ProjectDatabase
import com.noidvideo.editor.projects.ProjectsRepository

class NoidVideoApp : Application() {

    val database by lazy { ProjectDatabase.get(this) }
    val projectsRepository by lazy { ProjectsRepository(database.projectDao()) }
}
