package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "currency_rates")
data class CurrencyRateEntity(
    @PrimaryKey val code: String,
    val rate: Double,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "favorite_currencies")
data class FavoriteEntity(
    @PrimaryKey val code: String,
    val timestamp: Long = System.currentTimeMillis()
)
