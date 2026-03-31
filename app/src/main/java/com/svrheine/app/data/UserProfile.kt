package com.svrheine.app.data

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
    val signatureBase64: String? = null
)
