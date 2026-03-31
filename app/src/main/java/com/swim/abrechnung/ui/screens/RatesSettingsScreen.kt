package com.swim.abrechnung.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.swim.abrechnung.data.UserProfile
import com.swim.abrechnung.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RatesSettingsScreen(viewModel: MainViewModel, navController: NavController) {
    val userProfile by viewModel.userProfile.collectAsState(initial = null)

    var rateKampfrichter by remember { mutableStateOf("") }
    var rateBetreuer by remember { mutableStateOf("") }
    var rateTrainer by remember { mutableStateOf("") }
    var rateKm by remember { mutableStateOf("") }
    var trainerStatus by remember { mutableStateOf("kein Trainerschein") }
    
    var expanded by remember { mutableStateOf(false) }
    val statusOptions = listOf("kein Trainerschein", "Trainerassistenz", "Trainer C oder höher")

    LaunchedEffect(userProfile) {
        userProfile?.let {
            rateKampfrichter = it.rateKampfrichter.toString()
            rateBetreuer = it.rateBetreuer.toString()
            rateTrainer = it.rateTrainer.toString()
            rateKm = it.rateKm.toString()
            trainerStatus = it.trainerStatus
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sätze & Status") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Abrechnungssätze (€ pro Std / km)", style = MaterialTheme.typography.titleMedium)
            
            RateField("Kampfrichter (€/Std)", rateKampfrichter) { rateKampfrichter = it }
            RateField("Betreuer (€/Std)", rateBetreuer) { rateBetreuer = it }
            RateField("Trainer (€/Std)", rateTrainer) { rateTrainer = it }
            RateField("Fahrtkosten (€/km)", rateKm) { rateKm = it }

            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            Text("Qualifikation", style = MaterialTheme.typography.titleMedium)
            
            Box {
                OutlinedTextField(
                    value = trainerStatus,
                    onValueChange = {},
                    label = { Text("Trainerstatus") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { expanded = true }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }
                )
                // Transparent layer to make the whole field clickable
                Box(modifier = Modifier.matchParentSize().clickable { expanded = true })
                
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    statusOptions.forEach { status ->
                        DropdownMenuItem(
                            text = { Text(status) },
                            onClick = {
                                trainerStatus = status
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = {
                    userProfile?.let { current ->
                        viewModel.saveProfile(
                            current.copy(
                                rateKampfrichter = rateKampfrichter.replace(",", ".").toDoubleOrNull() ?: 1.0,
                                rateBetreuer = rateBetreuer.replace(",", ".").toDoubleOrNull() ?: 1.0,
                                rateTrainer = rateTrainer.replace(",", ".").toDoubleOrNull() ?: 2.0,
                                rateKm = rateKm.replace(",", ".").toDoubleOrNull() ?: 0.20,
                                trainerStatus = trainerStatus
                            )
                        )
                    }
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Einstellungen speichern")
            }
        }
    }
}

@Composable
fun RateField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
}
