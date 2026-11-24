package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.HistoryEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

/** Device-local calculation history stored in SharedPreferences. */
class HistoryRepository(context: Context) {
  private val prefs: SharedPreferences =
    context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
  private val initialEntries = loadEntries()
  private val idGenerator = AtomicLong((initialEntries.maxOfOrNull { it.id } ?: 0L) + 1L)
  private val _entries = MutableStateFlow(initialEntries)
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

    val entry = HistoryEntry(
      id = idGenerator.getAndIncrement(),
      expression = cleanExpr,
      result = cleanRes,
      calculatorName = calculatorName,
      category = category,
      timestampLabel = timeFormatter.format(Date()),
      route = route,
      reusableInput = reusableInput,
    )
    _entries.value = (listOf(entry) + _entries.value).take(MAX_ENTRIES)
    persistEntries()
  }

  fun deleteEntry(id: Long) {
    _entries.value = _entries.value.filterNot { it.id == id }
    persistEntries()
  }

  fun clearAll() {
    _entries.value = emptyList()
    persistEntries()
  }

  private fun loadEntries(): List<HistoryEntry> = runCatching {
    val array = JSONArray(prefs.getString(KEY_ENTRIES, "[]"))
    (0 until array.length()).mapNotNull { index ->
      runCatching {
        val item = array.getJSONObject(index)
        HistoryEntry(
          id = item.getLong("id"),
          expression = item.getString("expression"),
          result = item.getString("result"),
          calculatorName = item.getString("calculatorName"),
          category = item.getString("category"),
          timestampLabel = item.getString("timestampLabel"),
          route = item.getString("route"),
          reusableInput = item.optString("reusableInput").takeIf { it.isNotEmpty() },
        )
      }.getOrNull()
    }.take(MAX_ENTRIES)
  }.getOrDefault(emptyList())

  private fun persistEntries() {
    val array = JSONArray()
    _entries.value.forEach { entry ->
      array.put(
        JSONObject()
          .put("id", entry.id)
          .put("expression", entry.expression)
          .put("result", entry.result)
          .put("calculatorName", entry.calculatorName)
          .put("category", entry.category)
          .put("timestampLabel", entry.timestampLabel)
          .put("route", entry.route)
          .put("reusableInput", entry.reusableInput ?: ""),
      )
    }
    prefs.edit().putString(KEY_ENTRIES, array.toString()).apply()
  }

  companion object {
    private const val PREFS_NAME = "calcora_calculation_history"
    private const val KEY_ENTRIES = "entries"
    private const val MAX_ENTRIES = 100
  }
}