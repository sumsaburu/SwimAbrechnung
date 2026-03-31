package com.swim.abrechnung.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val fullName: String,
    val street: String,
    val houseNumber: String,
    val zip: String,
    val city: String,
    val iban: String,
    val bankName: String,
    val signatureBase64: String? = null,
    // Abrechnungssätze
    val rateKampfrichter: Double = 1.0,
    val rateBetreuer: Double = 1.0,
    val rateTrainer: Double = 2.0,
    val rateKm: Double = 0.20,
    // Trainerstatus
    val trainerStatus: String = "kein Trainerschein",
    // Tracking für Berichte (Timestamp der letzten Erstellung)
    val lastReportGeneratedAt: Long = 0L
)
