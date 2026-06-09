package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CurrencyDao {
    @Query("SELECT * FROM currency_rates ORDER BY code ASC")
    fun getAllRates(): Flow<List<CurrencyRateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRates(rates: List<CurrencyRateEntity>)

    @Query("DELETE FROM currency_rates")
    suspend fun clearAllRates()

    @Transaction
    suspend fun refreshRates(rates: List<CurrencyRateEntity>) {
        clearAllRates()
        insertRates(rates)
    }

    @Query("SELECT * FROM favorite_currencies ORDER BY timestamp ASC")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorite_currencies WHERE code = :code")
    suspend fun deleteFavorite(code: String)
}
