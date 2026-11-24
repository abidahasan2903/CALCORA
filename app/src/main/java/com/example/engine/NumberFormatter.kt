package com.example.engine

import com.example.model.NumberFormatMode
import com.example.model.UserPreferences
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs

object NumberFormatter {

  fun format(
    value: Double,
    preferences: UserPreferences = UserPreferences(),
  ): String {
    if (value.isNaN() || value.isInfinite()) return "Error"
    val cleanValue = if (abs(value) < 1e-13) 0.0 else value
    val precision = preferences.decimalPrecision.coerceIn(0, 10)

    if (
      preferences.numberFormatMode == NumberFormatMode.SCIENTIFIC ||
        (abs(cleanValue) >= 1e12 && cleanValue != 0.0) ||
        (abs(cleanValue) > 0.0 && abs(cleanValue) < 1e-7 && precision < 8)
    ) {
      val pattern =
        if (precision == 0) "0E0" else "0." + "#".repeat(precision) + "E0"
      val df = DecimalFormat(pattern, DecimalFormatSymbols(Locale.US))
      df.roundingMode = RoundingMode.HALF_UP
      return df.format(cleanValue)
    }

    val bd = BigDecimal.valueOf(cleanValue).setScale(precision, RoundingMode.HALF_UP).stripTrailingZeros()
    val plain = bd.toPlainString()
    if (!preferences.useThousandsSeparator) return plain

    val parts = plain.split(".")
    val intPart = parts[0]
    val negative = intPart.startsWith("-")
    val digits = if (negative) intPart.substring(1) else intPart
    val grouped =
      digits.reversed().chunked(3).joinToString(",").reversed()
    val signedInt = if (negative) "-$grouped" else grouped
    return if (parts.size > 1) "$signedInt.${parts[1]}" else signedInt
  }

  fun formatPlain(value: Double, precision: Int = 6): String {
    if (value.isNaN() || value.isInfinite()) return "Error"
    val cleanValue = if (abs(value) < 1e-13) 0.0 else value
    if (abs(cleanValue) >= 1e13) {
      return DecimalFormat("0.######E0", DecimalFormatSymbols(Locale.US)).format(cleanValue)
    }
    return BigDecimal.valueOf(cleanValue)
      .setScale(precision.coerceIn(0, 10), RoundingMode.HALF_UP)
      .stripTrailingZeros()
      .toPlainString()
  }

  fun formatCurrency(value: Double, precision: Int = 2, thousandsSeparator: Boolean = true): String {
    if (value.isNaN() || value.isInfinite()) return "Error"
    val cleanValue = if (abs(value) < 1e-12) 0.0 else value
    val bd = BigDecimal.valueOf(cleanValue).setScale(precision.coerceIn(0, 6), RoundingMode.HALF_UP)
    val plain = bd.toPlainString()
    if (!thousandsSeparator) return plain
    val parts = plain.split(".")
    val intPart = parts[0]
    val negative = intPart.startsWith("-")
    val digits = if (negative) intPart.substring(1) else intPart
    val grouped = digits.reversed().chunked(3).joinToString(",").reversed()
    val signedInt = if (negative) "-$grouped" else grouped
    return if (parts.size > 1) "$signedInt.${parts[1]}" else signedInt
  }
}
