package com.swim.abrechnung

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
import com.swim.abrechnung.ui.theme.SwimAbrechnungTheme
import com.swim.abrechnung.viewmodel.MainViewModel
import com.swim.abrechnung.ui.screens.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SwimAbrechnungTheme {
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
            SettingsScreen(viewModel, navController)
        }
        composable("profile_edit") {
            ProfileSetupScreen(viewModel, navController, isEditing = true)
        }
        composable("rates_edit") {
            RatesSettingsScreen(viewModel, navController)
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
