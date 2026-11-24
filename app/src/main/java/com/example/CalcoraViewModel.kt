package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.data.HistoryRepository
import com.example.data.PreferencesRepository
import com.example.model.AngleMode
import com.example.model.AppearanceMode
import com.example.model.CalcoraThemeId
import com.example.model.HistoryEntry
import com.example.model.NumberFormatMode
import com.example.model.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CalcoraViewModel(application: Application) : AndroidViewModel(application) {
  val preferencesRepository = PreferencesRepository(application)
  val historyRepository = HistoryRepository(application)

  val preferences: StateFlow<UserPreferences> = preferencesRepository.preferences
  val historyEntries: StateFlow<List<HistoryEntry>> = historyRepository.entries

  // Holds reusable expression/input when user taps "Reuse" from History
  private val _pendingReuseInput = MutableStateFlow<Pair<String, String>?>(null)
  val pendingReuseInput: StateFlow<Pair<String, String>?> = _pendingReuseInput.asStateFlow()

  // Persistent session memory register shared across keypads
  private val _memoryValue = MutableStateFlow(0.0)
  val memoryValue: StateFlow<Double> = _memoryValue.asStateFlow()

  fun setTheme(themeId: CalcoraThemeId) = preferencesRepository.setTheme(themeId)
  fun setAppearanceMode(mode: AppearanceMode) = preferencesRepository.setAppearanceMode(mode)
  fun setDecimalPrecision(precision: Int) = preferencesRepository.setDecimalPrecision(precision)
  fun setDefaultAngleMode(mode: AngleMode) = preferencesRepository.setDefaultAngleMode(mode)
  fun setNumberFormatMode(mode: NumberFormatMode) = preferencesRepository.setNumberFormatMode(mode)
  fun setThousandsSeparator(enabled: Boolean) = preferencesRepository.setThousandsSeparator(enabled)
  fun setShowStepBreakdown(enabled: Boolean) = preferencesRepository.setShowStepBreakdown(enabled)

  fun recordHistory(
    expression: String,
    result: String,
    calculatorName: String,
    category: String,
    route: String,
    reusableInput: String? = null,
  ) {
    historyRepository.addEntry(expression, result, calculatorName, category, route, reusableInput)
  }

  fun deleteHistoryEntry(id: Long) = historyRepository.deleteEntry(id)
  fun clearHistory() = historyRepository.clearAll()

  fun setMemory(value: Double) {
    _memoryValue.value = value
  }

  fun addMemory(value: Double) {
    _memoryValue.value += value
  }

  fun subMemory(value: Double) {
    _memoryValue.value -= value
  }

  fun clearMemory() {
    _memoryValue.value = 0.0
  }

  fun requestReuse(route: String, input: String) {
    _pendingReuseInput.value = route to input
  }

  fun consumeReuse(route: String): String? {
    val current = _pendingReuseInput.value ?: return null
    if (current.first == route) {
      _pendingReuseInput.value = null
      return current.second
    }
    return null
  }
}
