package com.swim.abrechnung.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.swim.abrechnung.utils.TimeUtils
import com.swim.abrechnung.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(viewModel: MainViewModel, navController: NavController) {
    val selectedQuarter by viewModel.selectedQuarter.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()
    val entries by viewModel.filteredEntries.collectAsState(initial = emptyList())
    val userProfile by viewModel.userProfile.collectAsState(initial = null)
    
    val dateFormatter = remember { SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN) }
    val dateTimeFormatter = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMAN) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quartalsbericht") },
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
        ) {
            // Jahr-Auswahl
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.setYear(selectedYear - 1) }) {
                    Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Jahr zurück")
                }
                Text(
                    text = selectedYear.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                IconButton(onClick = { viewModel.setYear(selectedYear + 1) }) {
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Jahr vor")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quartal-Auswahl
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf(1, 2, 3, 4).forEach { q ->
                    FilterChip(
                        selected = selectedQuarter == q,
                        onClick = { viewModel.setQuarter(q) },
                        label = { Text("Q$q") }
                    )
                }
            }
            
            Divider(modifier = Modifier.padding(vertical = 16.dp))
            
            // Info über letzten versendeten Bericht
            userProfile?.let { profile ->
                if (profile.lastReportGeneratedAt > 0) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text(
                            text = "Letzter Bericht versendet am: ${dateTimeFormatter.format(Date(profile.lastReportGeneratedAt))}",
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Berechnung der Summen
            val trainerTotal = entries.filter { it.category == "Trainer" }.sumOf { it.value }
            val wettkampfEntries = entries.filter { it.category == "Wettkampf" }
            val totalWettkampfMinutes = wettkampfEntries.sumOf { 
                TimeUtils.calculateDurationMinutes(it.startTime, it.endTime) 
            }
            val kmTotal = entries.sumOf { it.kilometers }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Zusammenfassung Q$selectedQuarter $selectedYear", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Trainerstunden: $trainerTotal Std.")
                    Text("Wettkampfzeit gesamt: ${TimeUtils.formatMinutesToHours(totalWettkampfMinutes)}")
                    Text("Kilometer gesamt: $kmTotal km")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val context = androidx.compose.ui.platform.LocalContext.current
            Button(
                onClick = {
                    userProfile?.let { profile ->
                        com.swim.abrechnung.utils.PdfGenerator.generateAndSendPdf(
                            context = context,
                            profile = profile,
                            entries = entries,
                            quarter = selectedQuarter,
                            year = selectedYear,
                            onGenerated = { timestamp ->
                                viewModel.updateLastReportTimestamp(timestamp)
                            }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = entries.isNotEmpty()
            ) {
                Text("PDF erstellen & senden")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("Einträge (zum Bearbeiten anklicken):", style = MaterialTheme.typography.titleSmall)
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(entries) { entry ->
                    val title = if (entry.category == "Trainer") {
                        "Trainer: ${entry.value} Std."
                    } else {
                        val roles = mutableListOf<String>()
                        if (entry.isKampfrichter) roles.add("Kampfrichter")
                        if (entry.isBetreuer) roles.add("Betreuer")
                        val duration = TimeUtils.calculateDurationMinutes(entry.startTime, entry.endTime)
                        "${roles.joinToString("/")} (${TimeUtils.formatMinutesToHours(duration)}): ${entry.competitionName}"
                    }

                    val details = buildString {
                        append(dateFormatter.format(Date(entry.date)))
                        if (entry.category == "Wettkampf") {
                            append(" (${entry.startTime} - ${entry.endTime})")
                        }
                        append(" - ${entry.locationOrRoute}")
                        if (entry.kilometers > 0) {
                            append(" [${entry.kilometers} km]")
                        }
                    }

                    ListItem(
                        headlineContent = { Text(title) },
                        supportingContent = { Text(details) },
                        modifier = Modifier.clickable {
                            navController.navigate("entry_form/${entry.category}?entryId=${entry.id}")
                        }
                    )
                }
            }
        }
    }
}
