package pro.klimovich.planner.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

class PlannerConverters {
    @TypeConverter fun taskStatus(value: String) = TaskStatus.valueOf(value)
    @TypeConverter fun taskStatus(value: TaskStatus) = value.name
    @TypeConverter fun priority(value: String) = TaskPriority.valueOf(value)
    @TypeConverter fun priority(value: TaskPriority) = value.name
}

@Database(
    entities = [TaskEntity::class, ProjectEntity::class, CommentEntity::class, AttachmentEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(PlannerConverters::class)
abstract class PlannerDatabase : RoomDatabase() {
    abstract fun plannerDao(): PlannerDao

    companion object {
        fun create(context: Context): PlannerDatabase = Room.databaseBuilder(
            context,
            PlannerDatabase::class.java,
            "klimovich_planner.db"
        ).build()
    }
}
