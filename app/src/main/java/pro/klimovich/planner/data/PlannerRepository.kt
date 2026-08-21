package pro.klimovich.planner.data

class PlannerRepository(private val dao: PlannerDao) {
    val tasks = dao.observeTasks()
    val projects = dao.observeProjects()

    suspend fun addTask(title: String, dueAt: Long?, projectId: Long?, priority: TaskPriority) =
        dao.insertTask(TaskEntity(title = title.trim(), dueAt = dueAt, projectId = projectId, priority = priority))

    suspend fun addDetailedTask(
        title: String,
        description: String,
        dueAt: Long?,
        reminderAt: Long?,
        projectId: Long?,
        parentTaskId: Long?,
        priority: TaskPriority
    ) = dao.insertTask(TaskEntity(
        title = title.trim(), description = description.trim(), dueAt = dueAt,
        reminderAt = reminderAt, projectId = projectId, parentTaskId = parentTaskId,
        priority = priority
    ))

    suspend fun toggleDone(task: TaskEntity) = dao.updateTask(
        task.copy(
            status = if (task.status == TaskStatus.DONE) TaskStatus.TODO else TaskStatus.DONE,
            updatedAt = System.currentTimeMillis()
        )
    )

    suspend fun addProject(title: String, color: Long) =
        dao.insertProject(ProjectEntity(title = title.trim(), color = color))

    suspend fun updateTask(task: TaskEntity) = dao.updateTask(task.copy(updatedAt = System.currentTimeMillis()))
    fun comments(taskId: Long) = dao.observeComments(taskId)
    fun attachments(taskId: Long) = dao.observeAttachments(taskId)
    fun subtasks(taskId: Long) = dao.observeSubtasks(taskId)
    suspend fun addComment(taskId: Long, text: String) = dao.insertComment(CommentEntity(taskId = taskId, text = text.trim()))
    suspend fun addAttachment(taskId: Long, name: String, uri: String, mimeType: String?) =
        dao.insertAttachment(AttachmentEntity(taskId = taskId, displayName = name, uri = uri, mimeType = mimeType))
}
