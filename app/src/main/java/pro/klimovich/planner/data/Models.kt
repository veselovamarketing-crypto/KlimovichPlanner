package pro.klimovich.planner.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class TaskStatus { TODO, IN_PROGRESS, DONE }
enum class TaskPriority { LOW, NORMAL, HIGH }

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val color: Long = 0xFF14A5A5,
    val archived: Boolean = false
)

@Entity(
    tableName = "tasks",
    foreignKeys = [ForeignKey(
        entity = ProjectEntity::class,
        parentColumns = ["id"],
        childColumns = ["projectId"],
        onDelete = ForeignKey.SET_NULL
    )],
    indices = [Index("projectId"), Index("parentTaskId")]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val projectId: Long? = null,
    val parentTaskId: Long? = null,
    val dueAt: Long? = null,
    val reminderAt: Long? = null,
    val priority: TaskPriority = TaskPriority.NORMAL,
    val status: TaskStatus = TaskStatus.TODO,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncState: String = "LOCAL_ONLY"
)

@Entity(tableName = "comments", indices = [Index("taskId")])
data class CommentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long,
    val text: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "attachments", indices = [Index("taskId")])
data class AttachmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long,
    val displayName: String,
    val uri: String,
    val mimeType: String? = null
)
