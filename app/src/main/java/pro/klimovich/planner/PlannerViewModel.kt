package pro.klimovich.planner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pro.klimovich.planner.data.PlannerRepository
import pro.klimovich.planner.data.TaskEntity
import pro.klimovich.planner.data.TaskPriority
import pro.klimovich.planner.data.AttachmentEntity
import pro.klimovich.planner.data.CommentEntity

class PlannerViewModel(private val repository: PlannerRepository) : ViewModel() {
    val tasks = repository.tasks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val projects = repository.projects.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addTask(title: String, dueAt: Long? = null, projectId: Long? = null, priority: TaskPriority = TaskPriority.NORMAL) {
        if (title.isBlank()) return
        viewModelScope.launch { repository.addTask(title, dueAt, projectId, priority) }
    }

    fun toggleDone(task: TaskEntity) = viewModelScope.launch { repository.toggleDone(task) }

    fun updateTask(task: TaskEntity) = viewModelScope.launch { repository.updateTask(task) }

    fun addSubtask(parentId: Long, title: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.addDetailedTask(title, "", null, null, null, parentId, TaskPriority.NORMAL)
        }
    }

    fun comments(taskId: Long): Flow<List<CommentEntity>> = repository.comments(taskId)
    fun attachments(taskId: Long): Flow<List<AttachmentEntity>> = repository.attachments(taskId)
    fun subtasks(taskId: Long): Flow<List<TaskEntity>> = repository.subtasks(taskId)

    fun addComment(taskId: Long, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch { repository.addComment(taskId, text) }
    }

    fun addAttachment(taskId: Long, name: String, uri: String, mimeType: String?) = viewModelScope.launch {
        repository.addAttachment(taskId, name, uri, mimeType)
    }

    fun addProject(title: String, color: Long = 0xFF0E9398) {
        if (title.isBlank()) return
        viewModelScope.launch { repository.addProject(title, color) }
    }

    class Factory(private val repository: PlannerRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = PlannerViewModel(repository) as T
    }
}
