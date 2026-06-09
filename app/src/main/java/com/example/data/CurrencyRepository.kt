package com.example.data

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow

class CurrencyRepository(
    private val apiService: CurrencyApiService,
    private val currencyDao: CurrencyDao
) {
    val allRates: Flow<List<CurrencyRateEntity>> = currencyDao.getAllRates()
    val favorites: Flow<List<FavoriteEntity>> = currencyDao.getAllFavorites()

    suspend fun refreshRates(): Result<Unit> {
        return try {
            val response = apiService.getLatestRates("USD")
            if (response.result == "success") {
                val now = System.currentTimeMillis()
                val rateEntities = response.rates.map { (code, rate) ->
                    CurrencyRateEntity(
                        code = code,
                        rate = rate,
                        timestamp = now
                    )
                }
                currencyDao.refreshRates(rateEntities)
                Log.d("CurrencyRepository", "Exchange rates refreshed successfully: ${rateEntities.size} currencies.")
                Result.success(Unit)
            } else {
                Log.e("CurrencyRepository", "Failed to refresh rates: result was not success (${response.result})")
                Result.failure(Exception("API returned unsuccessful status: ${response.result}"))
            }
        } catch (e: Exception) {
            Log.e("CurrencyRepository", "Error refreshing rates", e)
            Result.failure(e)
        }
    }

    suspend fun addFavorite(code: String) {
        currencyDao.insertFavorite(FavoriteEntity(code = code))
    }

    suspend fun removeFavorite(code: String) {
        currencyDao.deleteFavorite(code)
    }

    suspend fun prepopulateFavoritesIfEmpty() {
        val currentFavs = currencyDao.getAllFavorites().firstOrNull()
        if (currentFavs.isNullOrEmpty()) {
            val initialFavs = listOf("AZN", "USD", "EUR", "TRY", "RUB", "GBP")
            initialFavs.forEach { code ->
                currencyDao.insertFavorite(FavoriteEntity(code = code))
            }
        }
    }
}
