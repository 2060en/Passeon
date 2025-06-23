package com.ethy.passeon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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

// ✨ [正名] 將 Trips 正名為 PassHolders，並修改標籤和路徑
sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Timeline : Screen("timeline", "時間軸", Icons.Outlined.AccessTime)
    object PassHolders : Screen("pass_holders", "票夾", Icons.Outlined.Style)
    object Types : Screen("types", "類型", Icons.Outlined.ViewStream)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PasseonApp()
        }
    }
}

@Composable
fun PasseonApp() {
    PasseonTheme {
        val navController = rememberNavController()

        val fakePassHolders = listOf(
            PassHolder(1, "我的電影票", "威秀、國賓、新光"),
            PassHolder(2, "八月高雄出差", "高鐵、計程車收據")
        )
        var passHolders by remember { mutableStateOf(fakePassHolders) }

        val initialTickets = listOf(
            Ticket(1, "沙丘：第二部", "電影票", "台南威秀", "IMAX", 1720454400L, "J排12號", null, emptyMap(), passHolderId = 1),
            Ticket(2, "腦筋急轉彎2", "電影票", "台南新光", "4廳", 1720540800L, "F排8號", null, emptyMap(), passHolderId = 1),
            Ticket(3, "九龍城寨之圍城", "電影票", "國賓影城", "A廳", 1720627200L, "G排10號", null, emptyMap(), passHolderId = 1),
            Ticket(4, "芙莉歐莎", "電影票", "台南威秀", "IMAX", 1720713600L, "E排14號", null, emptyMap(), passHolderId = 1),
            Ticket(5, "哥吉拉與金剛", "電影票", "台南威秀", "4DX", 1720800000L, "D排4號", null, emptyMap(), passHolderId = 1),
            Ticket(6, "猩球崛起：王國誕生", "電影票", "台南威秀", "2廳", 1720886400L, "C排7號", null, emptyMap(), passHolderId = 1),
            Ticket(7, "台北 → 左營", "高鐵", "台北", "左營", 1720933200L, "08車 15A", null, emptyMap(), passHolderId = 2),
            Ticket(8, "左營 → 台北", "高鐵", "左營", "台北", 1721019600L, "06車 11C", null, emptyMap(), passHolderId = 2),
            Ticket(9, "公司 → 住家", "通勤", "公司", "住家", 1721106000L, "", null, emptyMap(), passHolderId = null)
        )
        var tickets by remember { mutableStateOf(initialTickets) }

        Scaffold(
            bottomBar = { AppBottomNavBar(navController = navController) }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Timeline.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Timeline.route) {
                    TimelineScreen(
                        tickets = tickets,
                        onNavigateToAddTicket = { navController.navigate("add_ticket_route") }
                    )
                }
                composable(Screen.PassHolders.route) {
                    PassHolderScreen(
                        passHolders = passHolders,
                        tickets = tickets,
                        onNavigateToAddPassHolder = { navController.navigate("add_pass_holder_route") }
                    )
                }
                composable(Screen.Types.route) {
                    TypesScreen(tickets = tickets, navController = navController)
                }
                composable("add_ticket_route") {
                    AddTicketScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onAddTicket = { newTicket -> tickets = tickets + newTicket }
                    )
                }
                composable("add_pass_holder_route") {
                    AddPassHolderScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onAddPassHolder = { newPassHolder -> passHolders = passHolders + newPassHolder }
                    )
                }
                composable(
                    route = "type_details/{typeName}",
                    arguments = listOf(navArgument("typeName") { type = NavType.StringType })
                ) { backStackEntry ->
                    val typeName = backStackEntry.arguments?.getString("typeName") ?: ""
                    TypeDetailsScreen(
                        typeName = typeName,
                        tickets = tickets,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

// 以下的程式碼都沒有變動，保持原樣即可

@Composable
fun AppBottomNavBar(navController: NavController) {
    // ✨ [正名]
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(tickets: List<Ticket>, onNavigateToAddTicket: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.ConfirmationNumber, "Passeon Logo", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Passeon", fontWeight = FontWeight.Bold)
                }},
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                actions = { IconButton(onClick = {}) { Icon(Icons.Outlined.Person, "使用者設定", modifier = Modifier.size(40.dp).border(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), CircleShape).padding(8.dp)) } }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("新增票券") },
                icon = { Icon(Icons.Default.Add, "新增") },
                onClick = onNavigateToAddTicket,
                modifier = Modifier.height(72.dp)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(tickets) { ticket ->
                TicketCard(ticket = ticket)
            }
        }
    }
}

@Composable
fun TicketCard(ticket: Ticket) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(text = ticket.type, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = ticket.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            // ✨ 把 `tripId` 改成 `passHolderId`
            Text(text = "屬於票夾 ID: ${ticket.passHolderId ?: "無"}")
        }
    }
}