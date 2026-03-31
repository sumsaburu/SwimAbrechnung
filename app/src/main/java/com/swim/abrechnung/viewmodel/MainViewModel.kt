package com.swim.abrechnung.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.swim.abrechnung.data.AppDatabase
import com.swim.abrechnung.data.Entry
import com.swim.abrechnung.data.UserProfile
import com.swim.abrechnung.repository.AppRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.util.Calendar

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AppRepository
    val userProfile: Flow<UserProfile?>

    private val _selectedQuarter = MutableStateFlow(getCurrentQuarter())
    val selectedQuarter: StateFlow<Int> = _selectedQuarter.asStateFlow()

    private val _selectedYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = AppRepository(database.userDao(), database.entryDao())
        userProfile = repository.userProfile
        
        viewModelScope.launch {
            fixOldEntries()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val filteredEntries: Flow<List<Entry>> = combine(_selectedQuarter, _selectedYear) { quarter, year ->
        Pair(quarter, year)
    }.flatMapLatest { (quarter, year) ->
        repository.getEntriesByQuarter(quarter, year)
    }

    private suspend fun fixOldEntries() {
        val entries = repository.getAllEntries()
        entries.forEach { entry ->
            val normalizedDate = normalizeDate(entry.date)
            val calendar = Calendar.getInstance().apply { timeInMillis = normalizedDate }
            val month = calendar.get(Calendar.MONTH)
            val year = calendar.get(Calendar.YEAR)
            val quarter = (month / 3) + 1
            
            if (entry.quarter != quarter || entry.year != year || entry.date != normalizedDate) {
                repository.updateEntry(entry.copy(date = normalizedDate, quarter = quarter, year = year))
            }
        }
    }

    fun setQuarter(quarter: Int) {
        _selectedQuarter.value = quarter
    }

    fun setYear(year: Int) {
        _selectedYear.value = year
    }

    fun saveProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.insertProfile(profile)
        }
    }

    fun upsertEntry(entry: Entry) {
        val normalizedDate = normalizeDate(entry.date)
        val calendar = Calendar.getInstance().apply { timeInMillis = normalizedDate }
        val month = calendar.get(Calendar.MONTH)
        val year = calendar.get(Calendar.YEAR)
        val quarter = (month / 3) + 1
        
        val updatedEntry = entry.copy(date = normalizedDate, quarter = quarter, year = year)
        
        viewModelScope.launch {
            if (entry.id == 0) {
                repository.insertEntry(updatedEntry)
            } else {
                repository.updateEntry(updatedEntry)
            }
        }
    }

    fun deleteEntry(id: Int) {
        viewModelScope.launch {
            repository.deleteEntry(id)
        }
    }

    suspend fun getEntryById(id: Int): Entry? {
        return repository.getEntryById(id)
    }

    suspend fun hasKmEntryOnDate(date: Long, excludeId: Int = -1): Boolean {
        return repository.hasKmEntryOnDate(normalizeDate(date), excludeId)
    }

    private fun normalizeDate(timeInMillis: Long): Long {
        return Calendar.getInstance().apply {
            this.timeInMillis = timeInMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun getCurrentQuarter(): Int {
        return (Calendar.getInstance().get(Calendar.MONTH) / 3) + 1
    }
}
