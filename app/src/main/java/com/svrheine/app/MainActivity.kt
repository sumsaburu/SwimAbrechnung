package com.svrheine.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.svrheine.app.ui.theme.SVRheineTheme
import com.svrheine.app.viewmodel.MainViewModel
import com.svrheine.app.ui.screens.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SVRheineTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val viewModel: MainViewModel = viewModel()
    val userProfile by viewModel.userProfile.collectAsState(initial = null)

    // Logik angepasst: Wenn bereits Daten im Profil vorhanden sind (z.B. der Name nicht leer ist), 
    // dann starte direkt auf dem HomeScreen ("home").
    val startDestination = if (userProfile == null || userProfile?.fullName?.isEmpty() == true) {
        "profile_setup"
    } else {
        "home"
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable("profile_setup") {
            ProfileSetupScreen(viewModel, navController)
        }
        composable("home") {
            HomeScreen(viewModel, navController)
        }
        composable("settings") {
            ProfileSetupScreen(viewModel, navController, isEditing = true)
        }
        composable(
            "entry_form/{type}?entryId={entryId}",
            arguments = listOf(
                navArgument("type") { type = NavType.StringType },
                navArgument("entryId") { 
                    type = NavType.IntType
                    defaultValue = -1 
                }
            )
        ) { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type") ?: "Trainer"
            val entryId = backStackEntry.arguments?.getInt("entryId") ?: -1
            EntryFormScreen(viewModel, navController, type, entryId)
        }
        composable("report") {
            ReportScreen(viewModel, navController)
        }
    }
}
