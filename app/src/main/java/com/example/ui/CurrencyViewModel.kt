package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.CurrencyRateEntity
import com.example.data.CurrencyRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface CurrencyUiState {
    object Loading : CurrencyUiState
    data class Success(
        val rates: List<CurrencyRateItem>,
        val lastUpdated: String
    ) : CurrencyUiState
    data class Error(val message: String) : CurrencyUiState
}

data class CurrencyRateItem(
    val code: String,
    val rate: Double, // 1 Base = X target
    val inverseRate: Double, // 1 Target = X base
    val name: String,
    val flag: String,
    val isFavorite: Boolean
)

class CurrencyViewModel(private val repository: CurrencyRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _baseCurrency = MutableStateFlow("AZN")
    val baseCurrency = _baseCurrency.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    // Calculator states
    private val _calcSourceAmount = MutableStateFlow("100")
    val calcSourceAmount = _calcSourceAmount.asStateFlow()

    private val _calcSourceCurrency = MutableStateFlow("USD")
    val calcSourceCurrency = _calcSourceCurrency.asStateFlow()

    private val _calcTargetCurrency = MutableStateFlow("AZN")
    val calcTargetCurrency = _calcTargetCurrency.asStateFlow()

    val calcResultAmount: Flow<Double> = combine(
        _calcSourceAmount,
        _calcSourceCurrency,
        _calcTargetCurrency,
        repository.allRates
    ) { amountText, source, target, dbRates ->
        val amount = amountText.toDoubleOrNull() ?: 0.0
        val ratesMap = dbRates.associate { it.code to it.rate }
        val sourceRateInUSD = ratesMap[source] ?: 1.0
        val targetRateInUSD = ratesMap[target] ?: 1.0

        if (sourceRateInUSD == 0.0) return@combine 0.0
        // Formula: Amount in USD * targetRateInUSD
        // Amount of source -> convert to USD first (rate is how many source per USD)
        val amountInUSD = amount / sourceRateInUSD
        amountInUSD * targetRateInUSD
    }

    // Main uiState containing filtered and favorite mapped items
    val uiState: StateFlow<CurrencyUiState> = combine(
        repository.allRates,
        repository.favorites,
        _baseCurrency,
        _searchQuery
    ) { dbRates, favorites, base, search ->
        if (dbRates.isEmpty()) {
            CurrencyUiState.Loading
        } else {
            val ratesMap = dbRates.associate { it.code to it.rate }
            val baseRateInUSD = ratesMap[base] ?: 1.0

            if (baseRateInUSD == 0.0) {
                CurrencyUiState.Error("Baza valyuta mezənnəsi tapılmadı.")
            } else {
                val favoriteCodes = favorites.map { it.code }.toSet()
                
                val rateItems = dbRates.map { entity ->
                    val targetRateInUSD = entity.rate
                    
                    // 1 Base = X target
                    // 1 USD = targetRateInUSD, 1 USD = baseRateInUSD
                    // 1 Base = targetRateInUSD / baseRateInUSD
                    val baseToTarget = targetRateInUSD / baseRateInUSD
                    
                    // 1 Target = X base
                    val targetToBase = if (targetRateInUSD == 0.0) 0.0 else baseRateInUSD / targetRateInUSD

                    CurrencyRateItem(
                        code = entity.code,
                        rate = baseToTarget,
                        inverseRate = targetToBase,
                        name = CurrencyUtils.getCurrencyName(entity.code),
                        flag = CurrencyUtils.getFlagEmoji(entity.code),
                        isFavorite = favoriteCodes.contains(entity.code)
                    )
                }

                // Filter by search query (code or name)
                val filteredItems = if (search.isBlank()) {
                    rateItems
                } else {
                    rateItems.filter {
                        it.code.contains(search, ignoreCase = true) ||
                        it.name.contains(search, ignoreCase = true)
                    }
                }

                // Sorting: Favorites first, then Alphabetically
                val sortedItems = filteredItems.sortedWith(
                    compareByDescending<CurrencyRateItem> { it.isFavorite }
                        .thenBy { it.code }
                )

                val lastUpdatedTime = dbRates.firstOrNull()?.timestamp?.let {
                    val sdf = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault())
                    sdf.format(java.util.Date(it))
                } ?: "Yenilənməyib"

                CurrencyUiState.Success(
                    rates = sortedItems,
                    lastUpdated = lastUpdatedTime
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CurrencyUiState.Loading
    )

    init {
        viewModelScope.launch {
            repository.prepopulateFavoritesIfEmpty()
            refreshRatesSilently()
        }
    }

    fun refreshRates() {
        viewModelScope.launch {
            _isRefreshing.value = true
            repository.refreshRates()
            _isRefreshing.value = false
        }
    }

    private suspend fun refreshRatesSilently() {
        repository.refreshRates()
    }

    fun toggleFavorite(code: String, isFav: Boolean) {
        viewModelScope.launch {
            if (isFav) {
                repository.removeFavorite(code)
            } else {
                repository.addFavorite(code)
            }
        }
    }

    fun setBaseCurrency(code: String) {
        _baseCurrency.value = code
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateCalcSourceAmount(amount: String) {
        _calcSourceAmount.value = amount
    }

    fun setCalcSourceCurrency(code: String) {
        _calcSourceCurrency.value = code
    }

    fun setCalcTargetCurrency(code: String) {
        _calcTargetCurrency.value = code
    }

    fun swapCalculatorCurrencies() {
        val oldSource = _calcSourceCurrency.value
        _calcSourceCurrency.value = _calcTargetCurrency.value
        _calcTargetCurrency.value = oldSource
    }
}

class CurrencyViewModelFactory(private val repository: CurrencyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CurrencyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CurrencyViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
