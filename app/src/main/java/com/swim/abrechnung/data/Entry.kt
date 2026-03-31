package com.swim.abrechnung.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "entries")
data class Entry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String, // "Trainer" oder "Wettkampf"
    val date: Long,
    val startTime: String = "",
    val endTime: String = "",
    val competitionName: String = "",
    val isKampfrichter: Boolean = false,
    val isBetreuer: Boolean = false,
    val value: Double = 0.0, // Stunden für Trainer
    val kilometers: Double = 0.0,
    val locationOrRoute: String = "",
    val quarter: Int,
    val year: Int
)
