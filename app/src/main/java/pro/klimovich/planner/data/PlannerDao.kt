package pro.klimovich.planner.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PlannerDao {
    @Query("SELECT * FROM tasks ORDER BY status = 'DONE', COALESCE(dueAt, 9223372036854775807), createdAt DESC")
    fun observeTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM projects WHERE archived = 0 ORDER BY title")
    fun observeProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM comments WHERE taskId = :taskId ORDER BY createdAt")
    fun observeComments(taskId: Long): Flow<List<CommentEntity>>

    @Query("SELECT * FROM attachments WHERE taskId = :taskId ORDER BY id")
    fun observeAttachments(taskId: Long): Flow<List<AttachmentEntity>>

    @Query("SELECT * FROM tasks WHERE parentTaskId = :taskId ORDER BY status = 'DONE', createdAt")
    fun observeSubtasks(taskId: Long): Flow<List<TaskEntity>>

    @Insert suspend fun insertTask(task: TaskEntity): Long
    @Update suspend fun updateTask(task: TaskEntity)
    @Delete suspend fun deleteTask(task: TaskEntity)
    @Insert suspend fun insertProject(project: ProjectEntity): Long
    @Insert suspend fun insertComment(comment: CommentEntity): Long
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttachment(attachment: AttachmentEntity): Long

    @Delete suspend fun deleteAttachment(attachment: AttachmentEntity)
}
