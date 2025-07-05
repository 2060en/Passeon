package com.ethy.passeon

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ethy.passeon.ui.theme.PasseonTheme


sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Timeline : Screen("timeline", "時間軸", Icons.Outlined.AccessTime)
    object PassHolders : Screen("pass_holders", "票夾", Icons.Outlined.Style)
    object Types : Screen("types", "類型", Icons.Outlined.ViewStream)
}

class MainActivity : ComponentActivity() {

    private val passeonViewModel: PasseonViewModel by viewModels {
        PasseonViewModelFactory((application as PasseonApplication).database.passeonDao())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PasseonApp(viewModel = passeonViewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasseonApp(
    viewModel: PasseonViewModel

) {
    PasseonTheme {
        val navController = rememberNavController()

        val passHolders by viewModel.allPassHolders.collectAsState()
        val tickets by viewModel.allTickets.collectAsState()

        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        when {
                            currentRoute == Screen.Timeline.route -> Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.ConfirmationNumber, "Passeon Logo", tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Passeon", fontWeight = FontWeight.Bold)
                            }
                            currentRoute == Screen.PassHolders.route -> Text("我的票夾")
                            currentRoute == Screen.Types.route -> Text("依類型顯示")
                            currentRoute == "add_ticket_route" -> Text("新增票券")
                            currentRoute == "add_pass_holder_route" -> Text("新增票夾")
                            currentRoute?.startsWith("pass_holder_details/") == true -> {
                                val passHolderId = navBackStackEntry?.arguments?.getInt("passHolderId")
                                val title = passHolders.find { it.id == passHolderId }?.name ?: "票夾內容"
                                Text(title)
                            }
                            currentRoute?.startsWith("type_details/") == true -> {
                                val title = navBackStackEntry?.arguments?.getString("typeName") ?: "票券"
                                Text(title)
                            }
                        }
                    },
                    navigationIcon = {
                        val isMainScreen = currentRoute in listOf(Screen.Timeline.route, Screen.PassHolders.route, Screen.Types.route)
                        if (!isMainScreen) {
                            IconButton(onClick = { navController.navigateUp() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                            }
                        }
                    },
                    actions = {
                        if (currentRoute == Screen.PassHolders.route) {
                            IconButton(onClick = { navController.navigate("add_pass_holder_route") }) {
                                Icon(Icons.Default.Add, "新增票夾")
                            }
                        }
                        if (currentRoute in listOf(Screen.Timeline.route, Screen.PassHolders.route, Screen.Types.route)) {
                            IconButton(onClick = { /* TODO: 設定頁 */ }) {
                                Icon(Icons.Outlined.Person, "使用者設定", modifier = Modifier.size(40.dp).border(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), CircleShape).padding(8.dp))
                            }
                        }
                    }
                )
            },
            bottomBar = { AppBottomNavBar(navController = navController) },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    text = { Text("新增票券") },
                    icon = { Icon(Icons.Default.Add, "新增票券") },
                    onClick = {
                        // ✨ 監聽點 A: 確認按鈕點擊事件被觸發
                        Log.d("PasseonDebug", "FAB clicked. Navigating to add_ticket_route...")
                        navController.navigate("add_ticket_route")
                    }
                )
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Timeline.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Timeline.route) {
                    // ✨ [修正] 把 viewModel 也一起傳下去
                    TimelineScreen(
                        tickets = tickets,
                        viewModel = viewModel
                    )
                }
                composable(Screen.PassHolders.route) {
                    PassHolderScreen(
                        passHolders = passHolders,
                        tickets = tickets,
                        viewModel = viewModel,
                        onPassHolderClick = { id -> navController.navigate("pass_holder_details/$id") }
                    )
                }
                composable(Screen.Types.route) { TypesScreen(tickets = tickets,
                    viewModel = viewModel,navController = navController) }

                composable("add_ticket_route") {
                    Log.d("PasseonDebug", "NavHost: Composable for add_ticket_route is being built.")
                    AddTicketScreen(

                        passHolders = passHolders,
                        viewModel = viewModel,
                        onAddTicket = { newTicket ->
                            viewModel.insertTicket(newTicket)
                            navController.popBackStack()
                        }
                    )
                }
                composable("add_pass_holder_route") {

                    AddPassHolderScreen(
                        onNavigateBack = { navController.popBackStack() },
                        viewModel = viewModel,
                        onAddPassHolder = { newPassHolder ->
                            viewModel.insertPassHolder(newPassHolder)
                            navController.navigate(Screen.PassHolders.route) {
                                popUpTo("add_pass_holder_route") { inclusive = true }
                            }
                        }
                    )
                }

                composable(
                    route = "type_details/{typeName}",
                    arguments = listOf(navArgument("typeName") { type = NavType.StringType })
                ) { backStackEntry ->
                    val typeName = backStackEntry?.arguments?.getString("typeName") ?: ""
                    // ✨ [修正] 使用簡化後的呼叫方式
                    TypeDetailsScreen(
                        typeName = typeName, // ✨ 將解析出的 typeName 傳入
                        tickets = tickets.filter { it.type == typeName },
                        viewModel = viewModel
                    )
                }
                composable(
                    route = "pass_holder_details/{passHolderId}",
                    arguments = listOf(navArgument("passHolderId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val passHolderId = backStackEntry?.arguments?.getInt("passHolderId")
                    val filteredTickets = tickets.filter { it.passHolderId == passHolderId }
                    PassHolderDetailScreen(
                        tickets = filteredTickets,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

// ✨ [修正] 把 AppBottomNavBar 的定義也放回到檔案的頂層
@Composable
fun AppBottomNavBar(navController: NavController) {
    val items = listOf(Screen.Timeline, Screen.PassHolders, Screen.Types)
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        items.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.label) },
                label = { Text(screen.label) },
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

// =======================================================
// ✨ 全域共享 UI 元件庫 ✨
// =======================================================




@Composable
fun DeleteConfirmationDialog(
    itemType: String,
    itemName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "確認刪除") },
        text = { Text("您確定要刪除這${if (itemType == "票夾") "個" else "張"}$itemType「$itemName」嗎？\n此動作無法復原。") },
        confirmButton = { TextButton(onClick = onConfirm) { Text("刪除") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}