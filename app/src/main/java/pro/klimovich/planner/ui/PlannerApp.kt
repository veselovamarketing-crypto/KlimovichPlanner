package pro.klimovich.planner.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pro.klimovich.planner.PlannerViewModel
import pro.klimovich.planner.data.TaskEntity
import pro.klimovich.planner.data.TaskPriority
import pro.klimovich.planner.data.TaskStatus
import pro.klimovich.planner.data.ProjectEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private data class NavItem(val label: String, val icon: ImageVector)

@Composable
fun KlimovichPlannerApp(vm: PlannerViewModel) {
    val tasks by vm.tasks.collectAsStateWithLifecycle()
    val projects by vm.projects.collectAsStateWithLifecycle()
    val nav = listOf(
        NavItem("Сегодня", Icons.Outlined.Home),
        NavItem("Календарь", Icons.Outlined.CalendarMonth),
        NavItem("Проекты", Icons.Outlined.FolderOpen),
        NavItem("Задачи", Icons.Outlined.TaskAlt)
    )
    var selected by remember { mutableIntStateOf(0) }
    var showTaskDialog by remember { mutableStateOf(false) }
    var selectedTask by remember { mutableStateOf<TaskEntity?>(null) }

    Scaffold(
        containerColor = WarmBackground,
        bottomBar = {
            NavigationBar(containerColor = CardBackground) {
                nav.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selected == index,
                        onClick = { selected = index },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label, fontSize = 11.sp) }
                    )
                }
            }
        },
        floatingActionButton = {
            if (selected != 2) FloatingActionButton(
                onClick = { showTaskDialog = true },
                containerColor = KlimovichTeal,
                contentColor = Color.White,
                shape = CircleShape
            ) { Icon(Icons.Outlined.Add, contentDescription = "Новая задача") }
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (selected) {
                0 -> TodayScreen(tasks, vm::toggleDone) { selectedTask = it }
                1 -> CalendarScreen(tasks, vm::toggleDone) { selectedTask = it }
                2 -> ProjectsScreen(projects, vm::addProject)
                else -> AllTasksScreen(tasks, vm::toggleDone) { selectedTask = it }
            }
        }
    }

    if (showTaskDialog) AddTaskDialog(
        onDismiss = { showTaskDialog = false },
        onAdd = { title, priority -> vm.addTask(title, priority = priority); showTaskDialog = false }
    )
    selectedTask?.let { task ->
        TaskDetailsDialog(task, projects, vm) { selectedTask = null }
    }
}

@Composable
private fun ScreenHeader(kicker: String, title: String, count: Int? = null) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 20.dp)) {
        Text(kicker.uppercase(), color = KlimovichTeal, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp)
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(title, fontSize = 30.sp, lineHeight = 34.sp, fontWeight = FontWeight.Bold, color = Graphite, modifier = Modifier.weight(1f))
            count?.let { Text("$it", color = Muted, fontSize = 15.sp, modifier = Modifier.padding(bottom = 4.dp)) }
        }
    }
}

@Composable
private fun TodayScreen(tasks: List<TaskEntity>, onToggle: (TaskEntity) -> Unit, onOpen: (TaskEntity) -> Unit) {
    val today = remember { SimpleDateFormat("d MMMM, EEEE", Locale("ru")).format(Date()) }
    val active = tasks.filter { it.parentTaskId == null && it.status != TaskStatus.DONE }
    LazyColumn(Modifier.fillMaxSize()) {
        item { ScreenHeader(today, "Фокус на сегодня", active.size) }
        if (active.isEmpty()) item { EmptyState("На сегодня всё спокойно", "Добавьте первую задачу кнопкой +") }
        items(active, key = { it.id }) { TaskCard(it, onToggle, onOpen) }
        item { Spacer(Modifier.height(100.dp)) }
    }
}

@Composable
private fun AllTasksScreen(tasks: List<TaskEntity>, onToggle: (TaskEntity) -> Unit, onOpen: (TaskEntity) -> Unit) {
    LazyColumn(Modifier.fillMaxSize()) {
        item { ScreenHeader("Рабочее пространство", "Все задачи", tasks.size) }
        if (tasks.isEmpty()) item { EmptyState("Задач пока нет", "Создайте задачу и назначьте срок") }
        items(tasks.filter { it.parentTaskId == null }, key = { it.id }) { TaskCard(it, onToggle, onOpen) }
        item { Spacer(Modifier.height(100.dp)) }
    }
}

@Composable
private fun TaskCard(task: TaskEntity, onToggle: (TaskEntity) -> Unit, onOpen: (TaskEntity) -> Unit) {
    Card(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 5.dp).fillMaxWidth().clickable { onOpen(task) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onToggle(task) }, modifier = Modifier.size(36.dp)) {
                Icon(
                    if (task.status == TaskStatus.DONE) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                    contentDescription = "Статус",
                    tint = if (task.status == TaskStatus.DONE) KlimovichTeal else Muted
                )
            }
            Column(Modifier.weight(1f).padding(start = 8.dp)) {
                Text(task.title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Graphite)
                val meta = buildList {
                    task.dueAt?.let { add(SimpleDateFormat("d MMM, HH:mm", Locale("ru")).format(Date(it))) }
                    if (task.priority == TaskPriority.HIGH) add("Высокий приоритет")
                }.joinToString(" • ")
                if (meta.isNotEmpty()) Text(meta, color = if (task.priority == TaskPriority.HIGH) Color(0xFFD85F52) else Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 5.dp))
            }
        }
    }
}

@Composable
private fun CalendarScreen(tasks: List<TaskEntity>, onToggle: (TaskEntity) -> Unit, onOpen: (TaskEntity) -> Unit) {
    val calendar = remember { Calendar.getInstance() }
    val month = remember { SimpleDateFormat("LLLL yyyy", Locale("ru")).format(calendar.time) }
    LazyColumn(Modifier.fillMaxSize()) {
        item { ScreenHeader("Календарь", month.replaceFirstChar { it.uppercase() }, tasks.count { it.dueAt != null }) }
        item {
            Card(Modifier.padding(horizontal = 20.dp, vertical = 4.dp).fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(CardBackground)) {
                val days = listOf("ПН", "ВТ", "СР", "ЧТ", "ПТ", "СБ", "ВС")
                Column(Modifier.padding(14.dp)) {
                    Row(Modifier.fillMaxWidth()) { days.forEach { Text(it, Modifier.weight(1f), color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold) } }
                    Spacer(Modifier.height(12.dp))
                    val firstDay = (calendar.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }
                    val mondayOffset = (firstDay.get(Calendar.DAY_OF_WEEK) + 5) % 7
                    val cells = List(mondayOffset) { null } + (1..calendar.getActualMaximum(Calendar.DAY_OF_MONTH)).map { it }
                    cells.chunked(7).forEach { week ->
                        Row(Modifier.fillMaxWidth()) {
                            week.forEach { day ->
                                val today = day != null && day == calendar.get(Calendar.DAY_OF_MONTH)
                                Box(Modifier.weight(1f).height(42.dp), contentAlignment = Alignment.Center) {
                                    if (today) Box(Modifier.size(34.dp).background(KlimovichTeal, CircleShape))
                                    if (day != null) Text("$day", color = if (today) Color.White else Graphite, fontWeight = if (today) FontWeight.Bold else FontWeight.Normal)
                                }
                            }
                            repeat(7 - week.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                }
            }
        }
        item { Text("Задачи со сроком", Modifier.padding(start = 20.dp, top = 22.dp, bottom = 8.dp), fontWeight = FontWeight.Bold, fontSize = 18.sp) }
        items(tasks.filter { it.parentTaskId == null && it.dueAt != null }, key = { it.id }) { TaskCard(it, onToggle, onOpen) }
        item { Spacer(Modifier.height(100.dp)) }
    }
}

@Composable
private fun ProjectsScreen(projects: List<ProjectEntity>, onAdd: (String, Long) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    LazyColumn(Modifier.fillMaxSize()) {
        item { ScreenHeader("Организация работы", "Проекты", projects.size) }
        items(projects, key = { it.id }) { project ->
            Card(Modifier.padding(horizontal = 20.dp, vertical = 5.dp).fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(CardBackground)) {
                Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(12.dp).background(KlimovichTeal, CircleShape))
                    Text(project.title, Modifier.padding(start = 12.dp), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }
            }
        }
        item {
            TextButton(onClick = { showDialog = true }, modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                Icon(Icons.Outlined.Add, contentDescription = null)
                Text("Добавить проект", Modifier.padding(start = 6.dp))
            }
        }
    }
    if (showDialog) SimpleNameDialog("Новый проект", "Название проекта", { showDialog = false }) { onAdd(it, 0xFF0E9398); showDialog = false }
}

@Composable
private fun EmptyState(title: String, subtitle: String) {
    Column(Modifier.fillMaxWidth().padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(subtitle, color = Muted, fontSize = 14.sp, modifier = Modifier.padding(top = 6.dp))
    }
}

@Composable
private fun AddTaskDialog(onDismiss: () -> Unit, onAdd: (String, TaskPriority) -> Unit) {
    var title by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(TaskPriority.NORMAL) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новая задача", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(title, { title = it }, label = { Text("Что нужно сделать?") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Row(Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(TaskPriority.LOW to "Низкий", TaskPriority.NORMAL to "Обычный", TaskPriority.HIGH to "Высокий").forEach { (value, label) ->
                        Text(label, color = if (priority == value) Color.White else Graphite, fontSize = 12.sp, modifier = Modifier.background(if (priority == value) KlimovichTeal else Color(0xFFE9ECE8), RoundedCornerShape(20.dp)).clickable { priority = value }.padding(horizontal = 11.dp, vertical = 8.dp))
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { onAdd(title, priority) }, enabled = title.isNotBlank()) { Text("Создать") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}

@Composable
private fun SimpleNameDialog(title: String, label: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var value by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { OutlinedTextField(value, { value = it }, label = { Text(label) }, singleLine = true) },
        confirmButton = { TextButton(onClick = { onSave(value) }, enabled = value.isNotBlank()) { Text("Сохранить") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}
