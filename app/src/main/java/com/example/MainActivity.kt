package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.data.CurrencyApiService
import com.example.data.CurrencyRepository
import com.example.ui.CurrencyScreen
import com.example.ui.CurrencyViewModel
import com.example.ui.CurrencyViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Initialize local Room Database
        val database = AppDatabase.getDatabase(applicationContext)

        // 2. Initialize Retrofit API Network Client
        val apiService = CurrencyApiService.create()

        // 3. Initialize Unified Repository
        val repository = CurrencyRepository(apiService, database.currencyDao())

        // 4. Instantiate ViewModel with factory pattern
        val factory = CurrencyViewModelFactory(repository)
        val viewModel = ViewModelProvider(this, factory)[CurrencyViewModel::class.java]

        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    CurrencyScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
