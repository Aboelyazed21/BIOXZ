package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.BioViewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                BioinformaticsApp()
            }
        }
    }
}

@Composable
fun BioinformaticsApp() {
    val navController = rememberNavController()
    val viewModel: BioViewModel = viewModel()
    
    var currentRoute by remember { mutableStateOf("dashboard") }

    // Navigation items representing modules requested in prompt
    val navItems = listOf(
        NavItem("dashboard", "Dashboard", Icons.Default.Home, "🏠"),
        NavItem("chat", "AI Assistant", Icons.Default.Send, "🤖"),
        NavItem("dna", "DNA Analysis", Icons.Default.Build, "🧬"),
        NavItem("rna", "RNA Secondary", Icons.Default.Settings, "🧪"),
        NavItem("protein", "Protein folding", Icons.Default.Star, "🔮"),
        NavItem("databases", "Sci DB Search", Icons.Default.Search, "🔎"),
        NavItem("research", "Indexed Papers", Icons.Default.List, "📚"),
        NavItem("notebook", "Notebook log", Icons.Default.Edit, "📝")
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        val isWideScreen = maxWidth >= 600.dp

        Row(modifier = Modifier.fillMaxSize()) {
            // Adaptive Side Navigation Rail for wide layouts (Tablets/Foldables/DeX)
            if (isWideScreen) {
                Surface(
                    modifier = Modifier
                        .width(220.dp)
                        .fillMaxHeight()
                        .border(1.dp, Color(0xFFE2E8F0)),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Brand Logo Card
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🧬", color = Color.White, fontSize = 18.sp)
                            }
                            Column {
                                Text("BIOXZ", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary)
                                Text("INTELLIGENCE PLATFORM", fontSize = 8.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            }
                        }

                        Divider(color = Color(0xFFE2E8F0))

                        // Nav list items
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            navItems.forEach { item ->
                                val isSelected = currentRoute == item.route
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else Color.Transparent)
                                        .clickable {
                                            currentRoute = item.route
                                            navController.navigate(item.route) {
                                                popUpTo("dashboard") { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                        .testTag("nav_rail_${item.route}")
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(item.emoji, fontSize = 16.sp)
                                    Text(
                                        text = item.label,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }

                        // Developer animated signature at bottom of Navigation Rail
                        EngZezoSignature(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        )
                    }
                }
            }

            // Central Area
            Scaffold(
                modifier = Modifier.weight(1f),
                contentWindowInsets = WindowInsets.safeDrawing,
                bottomBar = {
                    // Responsive standard Bottom Navigation Bar for compact phones
                    if (!isWideScreen) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 8.dp,
                            modifier = Modifier.border(1.dp, Color(0xFFE2E8F0))
                        ) {
                            // Take first 5 main items for compact bottom bar so it doesn't crowd!
                            navItems.take(5).forEach { item ->
                                val isSelected = currentRoute == item.route
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = {
                                        currentRoute = item.route
                                        navController.navigate(item.route) {
                                            popUpTo("dashboard") { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = item.label,
                                            tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
                                        )
                                    },
                                    label = { Text(item.label, fontSize = 10.sp, maxLines = 1) },
                                    modifier = Modifier.testTag("nav_bottom_${item.route}")
                                )
                            }
                        }
                    }
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = "dashboard",
                        modifier = Modifier.fillMaxSize()
                    ) {
                        composable("dashboard") {
                            DashboardScreen(viewModel = viewModel, onNavigateTo = { route ->
                                currentRoute = route
                                navController.navigate(route) {
                                    popUpTo("dashboard") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            })
                        }
                        composable("chat") {
                            AiChatScreen(viewModel = viewModel)
                        }
                        composable("dna") {
                            DnaScreen(viewModel = viewModel)
                        }
                        composable("rna") {
                            RnaScreen(viewModel = viewModel)
                        }
                        composable("protein") {
                            ProteinScreen(viewModel = viewModel)
                        }
                        composable("databases") {
                            DatabaseScreen(viewModel = viewModel)
                        }
                        composable("research") {
                            ResearchScreen(viewModel = viewModel)
                        }
                        composable("notebook") {
                            NotebookScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}

data class NavItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val emoji: String
)
