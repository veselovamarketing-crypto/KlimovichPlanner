package pro.klimovich.planner.ui

import android.app.DatePickerDialog
import android.content.Intent
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pro.klimovich.planner.PlannerViewModel
import pro.klimovich.planner.data.ProjectEntity
import pro.klimovich.planner.data.TaskEntity
import pro.klimovich.planner.data.TaskPriority
import pro.klimovich.planner.notifications.ReminderScheduler
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun TaskDetailsDialog(
    task: TaskEntity,
    projects: List<ProjectEntity>,
    vm: PlannerViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val comments by remember(task.id) { vm.comments(task.id) }.collectAsStateWithLifecycle(emptyList())
    val attachments by remember(task.id) { vm.attachments(task.id) }.collectAsStateWithLifecycle(emptyList())
    val subtasks by remember(task.id) { vm.subtasks(task.id) }.collectAsStateWithLifecycle(emptyList())
    var title by remember(task.id) { mutableStateOf(task.title) }
    var description by remember(task.id) { mutableStateOf(task.description) }
    var dueAt by remember(task.id) { mutableStateOf(task.dueAt) }
    var reminderAt by remember(task.id) { mutableStateOf(task.reminderAt) }
    var projectId by remember(task.id) { mutableStateOf(task.projectId) }
    var priority by remember(task.id) { mutableStateOf(task.priority) }
    var comment by remember { mutableStateOf("") }
    var subtask by remember { mutableStateOf("") }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        runCatching { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
        var name = "Файл"
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && index >= 0) name = cursor.getString(index)
        }
        vm.addAttachment(task.id, name, uri.toString(), context.contentResolver.getType(uri))
    }

    fun save() {
        val updated = task.copy(
            title = title.trim(), description = description.trim(), dueAt = dueAt,
            reminderAt = reminderAt, projectId = projectId, priority = priority
        )
        vm.updateTask(updated)
        ReminderScheduler.schedule(context, updated)
    }

    AlertDialog(
        onDismissRequest = { save(); onDismiss() },
        title = { Text("Карточка задачи", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    OutlinedTextField(title, { title = it }, label = { Text("Задача") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(description, { description = it }, label = { Text("Описание") }, minLines = 2, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                }
                item {
                    DetailLabel("Приоритет")
                    ChoiceRow(
                        listOf("Низкий" to TaskPriority.LOW, "Обычный" to TaskPriority.NORMAL, "Высокий" to TaskPriority.HIGH),
                        priority
                    ) { priority = it }
                }
                item {
                    DetailLabel("Срок")
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.CalendarMonth, null, tint = KlimovichTeal)
                        Text(
                            dueAt?.let { SimpleDateFormat("d MMMM yyyy, 18:00", Locale("ru")).format(Date(it)) } ?: "Выбрать дату",
                            modifier = Modifier.clickable {
                                val c = Calendar.getInstance()
                                dueAt?.let { c.timeInMillis = it }
                                DatePickerDialog(context, { _, year, month, day ->
                                    dueAt = Calendar.getInstance().apply {
                                        set(year, month, day, 18, 0, 0); set(Calendar.MILLISECOND, 0)
                                    }.timeInMillis
                                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
                            }.padding(10.dp),
                            color = if (dueAt == null) KlimovichTeal else Graphite
                        )
                    }
                }
                if (dueAt != null) item {
                    DetailLabel("Напоминание")
                    val options = listOf("Нет" to null, "За час" to dueAt!! - 3_600_000L, "За день" to dueAt!! - 86_400_000L)
                    ChoiceRow(options, reminderAt) { reminderAt = it }
                }
                if (projects.isNotEmpty()) item {
                    DetailLabel("Проект")
                    ChoiceRow(listOf("Без проекта" to null) + projects.map { it.title to it.id }, projectId) { projectId = it }
                }
                item {
                    HorizontalDivider()
                    DetailLabel("Подзадачи")
                }
                items(subtasks, key = { "sub${it.id}" }) { child ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { vm.toggleDone(child) }) {
                            Icon(if (child.status.name == "DONE") Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked, null, tint = KlimovichTeal)
                        }
                        Text(child.title)
                    }
                }
                item {
                    InlineEntry(subtask, { subtask = it }, "Новая подзадача") {
                        vm.addSubtask(task.id, subtask); subtask = ""
                    }
                }
                item {
                    HorizontalDivider()
                    DetailLabel("Комментарии")
                }
                items(comments, key = { "comment${it.id}" }) {
                    Column(Modifier.fillMaxWidth().background(Color(0xFFF1F3F0), RoundedCornerShape(10.dp)).padding(10.dp)) {
                        Text(it.text, fontSize = 14.sp)
                        Text(SimpleDateFormat("d MMM, HH:mm", Locale("ru")).format(Date(it.createdAt)), color = Muted, fontSize = 11.sp)
                    }
                }
                item {
                    InlineEntry(comment, { comment = it }, "Добавить комментарий") {
                        vm.addComment(task.id, comment); comment = ""
                    }
                }
                item {
                    HorizontalDivider()
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        DetailLabel("Файлы (${attachments.size})", Modifier.weight(1f))
                        IconButton(onClick = { filePicker.launch(arrayOf("*/*")) }) { Icon(Icons.Outlined.AttachFile, "Прикрепить файл", tint = KlimovichTeal) }
                    }
                }
                items(attachments, key = { "file${it.id}" }) { Text("• ${it.displayName}", fontSize = 14.sp, color = Graphite) }
            }
        },
        confirmButton = { TextButton(onClick = { save(); onDismiss() }, enabled = title.isNotBlank()) { Text("Сохранить") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Закрыть") } }
    )
}

@Composable
private fun DetailLabel(text: String, modifier: Modifier = Modifier) = Text(text, modifier.padding(top = 6.dp, bottom = 4.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp)

@Composable
private fun <T> ChoiceRow(options: List<Pair<String, T>>, selected: T, onSelect: (T) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        options.forEach { (label, value) ->
            Text(label, fontSize = 11.sp, color = if (selected == value) Color.White else Graphite,
                modifier = Modifier.background(if (selected == value) KlimovichTeal else Color(0xFFE9ECE8), RoundedCornerShape(18.dp)).clickable { onSelect(value) }.padding(horizontal = 9.dp, vertical = 7.dp))
        }
    }
}

@Composable
private fun InlineEntry(value: String, onValue: (String) -> Unit, label: String, onAdd: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(value, onValue, label = { Text(label) }, singleLine = true, modifier = Modifier.weight(1f))
        TextButton(onClick = onAdd, enabled = value.isNotBlank()) { Text("Добавить") }
    }
}
