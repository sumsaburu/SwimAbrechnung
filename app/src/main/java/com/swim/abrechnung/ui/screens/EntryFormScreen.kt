package com.swim.abrechnung.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.swim.abrechnung.data.Entry
import com.swim.abrechnung.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryFormScreen(
    viewModel: MainViewModel, 
    navController: NavController, 
    type: String, // "Trainer" oder "Wettkampf"
    entryId: Int = -1
) {
    val context = LocalContext.current
    val isEditing = entryId != -1
    
    // States
    var date by remember { mutableStateOf(System.currentTimeMillis()) }
    var startTime by remember { mutableStateOf("08:00") }
    var endTime by remember { mutableStateOf("12:00") }
    var competitionName by remember { mutableStateOf("") }
    var isKampfrichter by remember { mutableStateOf(false) }
    var isBetreuer by remember { mutableStateOf(false) }
    var hours by remember { mutableStateOf("") } // Nur für Trainer
    var kilometers by remember { mutableStateOf("") }
    var locationOrRoute by remember { mutableStateOf("") }
    
    var kmEnabled by remember { mutableStateOf(true) }
    var existingEntry by remember { mutableStateOf<Entry?>(null) }

    val dateFormatter = remember { SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN) }

    // Initialisierung bei Bearbeitung
    LaunchedEffect(entryId) {
        if (isEditing) {
            val entry = viewModel.getEntryById(entryId)
            if (entry != null) {
                existingEntry = entry
                date = entry.date
                startTime = entry.startTime
                endTime = entry.endTime
                competitionName = entry.competitionName
                isKampfrichter = entry.isKampfrichter
                isBetreuer = entry.isBetreuer
                hours = entry.value.toString()
                kilometers = if (entry.kilometers > 0) entry.kilometers.toString() else ""
                locationOrRoute = entry.locationOrRoute
            }
        }
    }

    // Kilometer-Logik: Prüfen ob an diesem Tag schon km erfasst wurden
    LaunchedEffect(date) {
        val alreadyHasKm = viewModel.hasKmEntryOnDate(date, entryId)
        kmEnabled = !alreadyHasKm
        if (!kmEnabled && kilometers.isEmpty()) {
            kilometers = ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Eintrag bearbeiten" else if (type == "Trainer") "Trainer-Stunden" else "Wettkampf-Einsatz") },
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
            // Datumswahl
            OutlinedCard(
                onClick = {
                    val calendar = Calendar.getInstance().apply { timeInMillis = date }
                    DatePickerDialog(
                        context,
                        { _, y, m, d ->
                            val newCal = Calendar.getInstance()
                            newCal.set(y, m, d, 0, 0, 0)
                            newCal.set(Calendar.MILLISECOND, 0)
                            date = newCal.timeInMillis
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Datum", style = MaterialTheme.typography.labelMedium)
                        Text(dateFormatter.format(Date(date)), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            if (type == "Wettkampf") {
                // Zeitauswahl Von - Bis
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            val parts = startTime.split(":")
                            TimePickerDialog(context, { _, h, m -> startTime = String.format("%02d:%02d", h, m) }, parts[0].toInt(), parts[1].toInt(), true).show()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Von: $startTime")
                    }
                    OutlinedButton(
                        onClick = {
                            val parts = endTime.split(":")
                            TimePickerDialog(context, { _, h, m -> endTime = String.format("%02d:%02d", h, m) }, parts[0].toInt(), parts[1].toInt(), true).show()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Bis: $endTime")
                    }
                }

                OutlinedTextField(
                    value = competitionName,
                    onValueChange = { competitionName = it },
                    label = { Text("Name des Wettkampfs") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isKampfrichter, onCheckedChange = { isKampfrichter = it })
                    Text("Kampfrichter")
                    Spacer(modifier = Modifier.width(24.dp))
                    Checkbox(checked = isBetreuer, onCheckedChange = { isBetreuer = it })
                    Text("Betreuer")
                }
            } else {
                // Trainer-spezifische Felder
                OutlinedTextField(
                    value = hours,
                    onValueChange = { hours = it },
                    label = { Text("Anzahl Stunden") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = locationOrRoute,
                    onValueChange = { locationOrRoute = it },
                    label = { Text("Ort / Trainingseinheit") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (type == "Wettkampf") {
                // Kilometer-Feld (nur für Wettkampf relevant)
                OutlinedTextField(
                    value = kilometers,
                    onValueChange = { kilometers = it },
                    label = { Text("Gefahrene Kilometer") },
                    enabled = kmEnabled || (isEditing && existingEntry?.kilometers ?: 0.0 > 0.0),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = {
                        if (!kmEnabled && !(isEditing && existingEntry?.kilometers ?: 0.0 > 0.0)) {
                            Text("Für diesen Tag wurden bereits Kilometer erfasst.", color = MaterialTheme.colorScheme.error)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val entryValue = hours.replace(",", ".").toDoubleOrNull() ?: 0.0
                    val kmValue = kilometers.replace(",", ".").toDoubleOrNull() ?: 0.0
                    
                    val newEntry = Entry(
                        id = if (isEditing) entryId else 0,
                        category = type,
                        date = date,
                        startTime = if (type == "Wettkampf") startTime else "",
                        endTime = if (type == "Wettkampf") endTime else "",
                        competitionName = if (type == "Wettkampf") competitionName else "",
                        isKampfrichter = isKampfrichter,
                        isBetreuer = isBetreuer,
                        value = entryValue,
                        kilometers = if (type == "Wettkampf") kmValue else 0.0,
                        locationOrRoute = locationOrRoute,
                        quarter = 0, // Wird im ViewModel berechnet
                        year = 0    // Wird im ViewModel berechnet
                    )
                    
                    viewModel.upsertEntry(newEntry)
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = if (type == "Wettkampf") (isKampfrichter || isBetreuer) && competitionName.isNotEmpty() else hours.isNotEmpty()
            ) {
                Text(if (isEditing) "Änderungen speichern" else "Eintrag speichern")
            }

            if (isEditing) {
                TextButton(
                    onClick = {
                        viewModel.deleteEntry(entryId)
                        navController.popBackStack()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eintrag löschen")
                }
            }
        }
    }
}
