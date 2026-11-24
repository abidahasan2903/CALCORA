package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.AngleMode
import com.example.model.AppearanceMode
import com.example.model.CalcoraThemeId
import com.example.model.NumberFormatMode
import com.example.model.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferencesRepository(context: Context) {
  private val prefs: SharedPreferences =
    context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

  private val _preferences = MutableStateFlow(loadPreferences())
  val preferences: StateFlow<UserPreferences> = _preferences.asStateFlow()

  private fun loadPreferences(): UserPreferences {
    val themeId =
      CalcoraThemeId.entries.firstOrNull { it.id == prefs.getString(KEY_THEME_ID, null) }
        ?: CalcoraThemeId.COSMO
    val appearanceMode =
      AppearanceMode.entries.firstOrNull { it.name == prefs.getString(KEY_APPEARANCE_MODE, null) }
        ?: AppearanceMode.SYSTEM
    val decimalPrecision = prefs.getInt(KEY_DECIMAL_PRECISION, 6).coerceIn(0, 10)
    val angleMode =
      AngleMode.entries.firstOrNull { it.name == prefs.getString(KEY_ANGLE_MODE, null) }
        ?: AngleMode.DEG
    val numberFormatMode =
      NumberFormatMode.entries.firstOrNull { it.name == prefs.getString(KEY_NUMBER_FORMAT, null) }
        ?: NumberFormatMode.STANDARD
    val thousandsSeparator = prefs.getBoolean(KEY_THOUSANDS_SEPARATOR, true)
    val showStepBreakdown = prefs.getBoolean(KEY_SHOW_BREAKDOWN, true)

    return UserPreferences(
      themeId = themeId,
      appearanceMode = appearanceMode,
      decimalPrecision = decimalPrecision,
      defaultAngleMode = angleMode,
      numberFormatMode = numberFormatMode,
      useThousandsSeparator = thousandsSeparator,
      showStepBreakdown = showStepBreakdown,
    )
  }

  fun setTheme(themeId: CalcoraThemeId) {
    prefs.edit().putString(KEY_THEME_ID, themeId.id).apply()
    _preferences.value = _preferences.value.copy(themeId = themeId)
  }

  fun setAppearanceMode(mode: AppearanceMode) {
    prefs.edit().putString(KEY_APPEARANCE_MODE, mode.name).apply()
    _preferences.value = _preferences.value.copy(appearanceMode = mode)
  }

  fun setDecimalPrecision(precision: Int) {
    val clamped = precision.coerceIn(0, 10)
    prefs.edit().putInt(KEY_DECIMAL_PRECISION, clamped).apply()
    _preferences.value = _preferences.value.copy(decimalPrecision = clamped)
  }

  fun setDefaultAngleMode(angleMode: AngleMode) {
    prefs.edit().putString(KEY_ANGLE_MODE, angleMode.name).apply()
    _preferences.value = _preferences.value.copy(defaultAngleMode = angleMode)
  }

  fun setNumberFormatMode(formatMode: NumberFormatMode) {
    prefs.edit().putString(KEY_NUMBER_FORMAT, formatMode.name).apply()
    _preferences.value = _preferences.value.copy(numberFormatMode = formatMode)
  }

  fun setThousandsSeparator(enabled: Boolean) {
    prefs.edit().putBoolean(KEY_THOUSANDS_SEPARATOR, enabled).apply()
    _preferences.value = _preferences.value.copy(useThousandsSeparator = enabled)
  }

  fun setShowStepBreakdown(enabled: Boolean) {
    prefs.edit().putBoolean(KEY_SHOW_BREAKDOWN, enabled).apply()
    _preferences.value = _preferences.value.copy(showStepBreakdown = enabled)
  }

  companion object {
    private const val PREFS_NAME = "calcora_local_preferences"
    private const val KEY_THEME_ID = "theme_id"
    private const val KEY_APPEARANCE_MODE = "appearance_mode"
    private const val KEY_DECIMAL_PRECISION = "decimal_precision"
    private const val KEY_ANGLE_MODE = "angle_mode"
    private const val KEY_NUMBER_FORMAT = "number_format"
    private const val KEY_THOUSANDS_SEPARATOR = "thousands_separator"
    private const val KEY_SHOW_BREAKDOWN = "show_step_breakdown"
  }
}
