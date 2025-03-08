package com.example.nexustodo

import android.content.Context
import androidx.compose.material3.TextFieldDefaults
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.nexustodo.ui.theme.NexusTODoTheme
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.compose.ui.text.font.FontWeight
import java.text.SimpleDateFormat
import androidx.compose.runtime.LaunchedEffect


import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NexusTODoTheme {
                MainScreen(context = this)
            }
        }
    }
}

@Composable
fun MainScreen(context: Context) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentScreen by remember { mutableStateOf("Home") }
    val completedTasks = remember { mutableStateListOf<Pair<String, Long>>() }
    val tasks = remember { mutableStateListOf<String>() }

    // Initialize TaskManager
    val taskManager = remember { TaskManager(context) }

    // Load tasks when the app starts
    LaunchedEffect(Unit) {
        tasks.addAll(taskManager.loadTasks())
        completedTasks.addAll(taskManager.loadCompletedTasks())
    }

    // Save tasks when they change
    LaunchedEffect(tasks, completedTasks) {
        taskManager.saveTasks(tasks)
        taskManager.saveCompletedTasks(completedTasks)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent { screen ->
                scope.launch { drawerState.close() }
                currentScreen = screen
            }
        }
    ) {
        when (currentScreen) {
            "Home" -> AppContent(
                onMenuClick = { scope.launch { drawerState.open() } },
                tasks = tasks,
                completedTasks = completedTasks
            )
            "Profile" -> ProfileScreen(
                completedTasks = completedTasks,
                onMenuClick = { scope.launch { drawerState.open() } }
            )
            "Settings" -> SettingsScreen(
                onMenuClick = { scope.launch { drawerState.open() } },
                onClearCompletedTasks = { completedTasks.clear() },
                onResetApp = {
                    tasks.clear()
                    completedTasks.clear()
                }
            )
            else -> AppContent(
                onMenuClick = { scope.launch { drawerState.open() } },
                tasks = tasks,
                completedTasks = completedTasks
            )
        }
    }
}

@Composable
fun AppContent(
    onMenuClick: () -> Unit,
    tasks: MutableList<String>,
    completedTasks: MutableList<Pair<String, Long>>
) {
    var text by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Top Row (Menu & Search Button)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MenuButton(onClick = onMenuClick)
                    Spacer(modifier = Modifier.weight(1f))
                    SearchButton { }
                }

                // TextField & Add Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(top = 40.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        label = { Text("Add Task") },
                        shape = RoundedCornerShape(24.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            disabledContainerColor = Color.LightGray,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(onClick = {
                        if (text.isNotBlank()) {
                            if (tasks.size < 10) {
                                tasks.add(text)
                                text = ""
                            } else {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Too many tasks at hand!")
                                }
                            }
                        }
                    }) {
                        Text("+")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Divider(color = Color.Gray, thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))

                // Task List with Scrollable LazyColumn
                LazyColumn {
                    items(tasks) { task ->
                        TaskItem(
                            task,
                            onComplete = {
                                tasks.remove(task)
                                completedTasks.add(task to System.currentTimeMillis())
                            },
                            onDelete = { tasks.remove(task) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TaskItem(task: String, onComplete: () -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .background(Color.LightGray, RoundedCornerShape(18.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = task,
            fontSize = 18.sp,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onComplete) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Complete Task",
                tint = Color.Green
            )
        }
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete Task",
                tint = Color.Red
            )
        }
    }
}

@Composable
fun DrawerContent(onItemClick: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 80.dp, start = 24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Button(onClick = { onItemClick("Home") }, modifier = Modifier.padding(vertical = 4.dp)) {
            Text("Home", fontSize = 18.sp)
        }

        Button(onClick = { onItemClick("Profile") }, modifier = Modifier.padding(vertical = 4.dp)) {
            Text("Profile", fontSize = 18.sp)
        }

        Button(onClick = { onItemClick("Settings") }, modifier = Modifier.padding(vertical = 4.dp)) {
            Text("Settings", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun MenuButton(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = Icons.Default.Menu,
            contentDescription = "Menu",
            tint = Color.Black
        )
    }
}

@Composable
fun SearchButton(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search",
            tint = Color.Black
        )
    }
}

@Composable
fun ProfileScreen(
    completedTasks: List<Pair<String, Long>>,
    onMenuClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        // Top Row (Menu Button)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MenuButton(onClick = onMenuClick)
            Spacer(modifier = Modifier.weight(1f))
        }

        Spacer(Modifier.padding(24.dp))
        Text("Completed Tasks", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        LazyColumn {
            items(completedTasks) { (task, timestamp) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(task, modifier = Modifier.weight(1f))
                    Text(SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(timestamp)))
                }
            }
        }
    }
}
@Composable
fun SettingsScreen(
    onMenuClick: () -> Unit,
    onClearCompletedTasks: () -> Unit,
    onResetApp: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        // Top Row (Menu Button)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MenuButton(onClick = onMenuClick)
            Spacer(modifier = Modifier.weight(1f))
        }

        Spacer(Modifier.padding(24.dp))
        Text("Settings", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Button(
            onClick = onClearCompletedTasks,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Clear Completed Tasks")
        }

        // Reset App Button
        Button(
            onClick = onResetApp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Reset App")
        }
    }
}