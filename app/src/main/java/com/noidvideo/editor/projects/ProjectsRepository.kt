package com.noidvideo.editor.projects

import kotlinx.coroutines.flow.Flow

class ProjectsRepository(private val dao: ProjectDao) {

    fun observeAll(): Flow<List<ProjectEntity>> = dao.getAll()

    suspend fun get(id: Long): ProjectEntity? = dao.getById(id)

    suspend fun save(project: ProjectEntity): Long =
        if (project.id == 0L) dao.insert(project)
        else {
            dao.update(project.copy(lastModified = System.currentTimeMillis()))
            project.id
        }

    suspend fun delete(project: ProjectEntity) = dao.delete(project)

    suspend fun deleteById(id: Long) = dao.deleteById(id)
}
