package com.example.data

import com.example.model.HistoryEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Session-only in-memory calculation history repository.
 * No database or disk persistence is used, per CALCORA offline session architecture.
 */
class HistoryRepository {
  private val idGenerator = AtomicLong(1L)
  private val _entries = MutableStateFlow<List<HistoryEntry>>(emptyList())
  val entries: StateFlow<List<HistoryEntry>> = _entries.asStateFlow()

  private val timeFormatter = SimpleDateFormat("hh:mm:ss a", Locale.US)

  fun addEntry(
    expression: String,
    result: String,
    calculatorName: String,
    category: String,
    route: String,
    reusableInput: String? = null,
  ) {
    val cleanExpr = expression.trim()
    val cleanRes = result.trim()
    if (cleanExpr.isEmpty() || cleanRes.isEmpty()) return

    val entry =
      HistoryEntry(
        id = idGenerator.getAndIncrement(),
        expression = cleanExpr,
        result = cleanRes,
        calculatorName = calculatorName,
        category = category,
        timestampLabel = timeFormatter.format(Date()),
        route = route,
        reusableInput = reusableInput,
      )
    _entries.value = listOf(entry) + _entries.value
  }

  fun deleteEntry(id: Long) {
    _entries.value = _entries.value.filterNot { it.id == id }
  }

  fun clearAll() {
    _entries.value = emptyList()
  }
}
