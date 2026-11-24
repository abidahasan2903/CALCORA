package com.example.engine

import com.example.model.CalcOutcome
import com.example.model.StepBreakdown
import java.math.BigDecimal
import java.math.BigInteger
import java.util.Calendar
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.sqrt

// ==========================================
// 1. PERCENTAGE ENGINE (ARTS)
// ==========================================

enum class PercentageMode(val title: String) {
  PERCENT_OF("What is X% of Y?"),
  WHAT_PERCENT("X is what % of Y?"),
  PERCENT_INCREASE("% Increase from X to Y"),
  PERCENT_DECREASE("% Decrease from X to Y"),
  PERCENT_DIFFERENCE("% Difference between X and Y"),
}

object PercentageEngine {
  fun calculate(mode: PercentageMode, x: Double, y: Double): CalcOutcome<Double> {
    fun fmt(v: Double) = NumberFormatter.formatPlain(v, 6)
    return when (mode) {
      PercentageMode.PERCENT_OF -> {
        val ans = (x / 100.0) * y
        CalcOutcome.Success(
          ans,
          StepBreakdown("Result = (X / 100) × Y", "X = ${fmt(x)}%, Y = ${fmt(y)}", "(${fmt(x)} / 100) × ${fmt(y)}", listOf("Result = ${fmt(ans)}"), fmt(ans)),
        )
      }
      PercentageMode.WHAT_PERCENT -> {
        if (y == 0.0) return CalcOutcome.Error("Base value Y cannot be zero")
        val ans = (x / y) * 100.0
        CalcOutcome.Success(
          ans,
          StepBreakdown("Percentage = (X / Y) × 100", "X = ${fmt(x)}, Y = ${fmt(y)}", "(${fmt(x)} / ${fmt(y)}) × 100", listOf("Result = ${fmt(ans)}%"), "${fmt(ans)}%", "%"),
        )
      }
      PercentageMode.PERCENT_INCREASE -> {
        if (x == 0.0) return CalcOutcome.Error("Starting value X cannot be zero")
        val ans = ((y - x) / abs(x)) * 100.0
        CalcOutcome.Success(
          ans,
          StepBreakdown("% Increase = ((Y - X) / |X|) × 100", "From ${fmt(x)} to ${fmt(y)}", "((${fmt(y)} - ${fmt(x)}) / |${fmt(x)}|) × 100", listOf("Increase = ${fmt(ans)}%"), "${fmt(ans)}%", "%"),
        )
      }
      PercentageMode.PERCENT_DECREASE -> {
        if (x == 0.0) return CalcOutcome.Error("Starting value X cannot be zero")
        val ans = ((x - y) / abs(x)) * 100.0
        CalcOutcome.Success(
          ans,
          StepBreakdown("% Decrease = ((X - Y) / |X|) × 100", "From ${fmt(x)} to ${fmt(y)}", "((${fmt(x)} - ${fmt(y)}) / |${fmt(x)}|) × 100", listOf("Decrease = ${fmt(ans)}%"), "${fmt(ans)}%", "%"),
        )
      }
      PercentageMode.PERCENT_DIFFERENCE -> {
        val avg = (abs(x) + abs(y)) / 2.0
        if (avg == 0.0) return CalcOutcome.Error("Both values cannot be zero for percentage difference")
        val ans = (abs(x - y) / avg) * 100.0
        CalcOutcome.Success(
          ans,
          StepBreakdown("% Diff = (|X - Y| / ((|X| + |Y|) / 2)) × 100", "X = ${fmt(x)}, Y = ${fmt(y)}", "(|${fmt(x)} - ${fmt(y)}| / ${fmt(avg)}) × 100", listOf("Difference = ${fmt(ans)}%"), "${fmt(ans)}%", "%"),
        )
      }
    }
  }
}

// ==========================================
// 2. RATIO & PROPORTION ENGINE (ARTS)
// ==========================================

object RatioProportionEngine {
  private fun gcd(a: Long, b: Long): Long {
    var x = abs(a)
    var y = abs(b)
    while (y != 0L) {
      val t = x % y
      x = y
      y = t
    }
    return if (x == 0L) 1L else x
  }

  fun simplifyRatio(a: Long, b: Long): CalcOutcome<String> {
    if (a == 0L && b == 0L) return CalcOutcome.Error("Ratio 0:0 cannot be simplified")
    val g = gcd(a, b)
    val sa = a / g
    val sb = b / g
    val ans = "$sa : $sb"
    return CalcOutcome.Success(
      ans,
      StepBreakdown("Simplified = (A ÷ GCD) : (B ÷ GCD)", "$a : $b", "GCD($a, $b) = $g", listOf("($a ÷ $g) : ($b ÷ $g) = $ans"), ans),
    )
  }

  fun compareRatios(a: Double, b: Double, c: Double, d: Double): CalcOutcome<String> {
    if (b == 0.0 || d == 0.0) return CalcOutcome.Error("Ratio denominators cannot be zero")
    val r1 = a / b
    val r2 = c / d
    val relation =
      when {
        abs(r1 - r2) < 1e-10 -> "Equal ($a:$b = $c:$d)"
        r1 > r2 -> "First ratio is greater ($a:$b > $c:$d)"
        else -> "Second ratio is greater ($a:$b < $c:$d)"
      }
    return CalcOutcome.Success(
      relation,
      StepBreakdown("Compare A/B vs C/D", "$a:$b and $c:$d", "${NumberFormatter.formatPlain(r1, 5)} vs ${NumberFormatter.formatPlain(r2, 5)}", listOf(relation), relation),
    )
  }

  fun solveProportion(a: Double, b: Double, c: Double): CalcOutcome<Double> {
    if (a == 0.0) return CalcOutcome.Error("First term A cannot be zero in A:B = C:x")
    val x = (b * c) / a
    val xs = NumberFormatter.formatPlain(x)
    return CalcOutcome.Success(
      x,
      StepBreakdown("A : B = C : x  ⇒  x = (B × C) / A", "A=$a, B=$b, C=$c", "x = ($b × $c) / $a", listOf("x = $xs"), xs),
    )
  }

  fun divideAmountByRatio(totalAmount: Double, parts: List<Double>): CalcOutcome<List<Double>> {
    if (parts.isEmpty() || parts.any { it < 0.0 }) return CalcOutcome.Error("Enter non-negative ratio parts")
    val sum = parts.sum()
    if (sum <= 0.0) return CalcOutcome.Error("Sum of ratio parts must be greater than zero")
    val shares = parts.map { totalAmount * (it / sum) }
    val steps = shares.mapIndexed { idx, s -> "Part ${idx + 1} (${NumberFormatter.formatPlain(parts[idx])}/${NumberFormatter.formatPlain(sum)}) = ${NumberFormatter.formatPlain(s, 4)}" }
    return CalcOutcome.Success(
      shares,
      StepBreakdown("Shareᵢ = Total × (Partᵢ / ΣParts)", "Total=$totalAmount, Ratio=${parts.joinToString(":") { NumberFormatter.formatPlain(it) }}", "Sum of parts = ${NumberFormatter.formatPlain(sum)}", steps, shares.joinToString(" , ") { NumberFormatter.formatPlain(it, 2) }),
    )
  }
}

// ==========================================
// 3. CALENDAR-AWARE DATE & TIME ENGINE
// ==========================================

data class CalcoraDate(val year: Int, val month: Int, val day: Int) : Comparable<CalcoraDate> {
  init {
    require(month in 1..12) { "Month must be 1..12" }
    require(day in 1..daysInMonth(year, month)) { "Invalid day $day for $year-$month" }
  }

  override fun compareTo(other: CalcoraDate): Int =
    compareValuesBy(this, other, { it.year }, { it.month }, { it.day })

  fun formatIso(): String = String.format(java.util.Locale.US, "%04d-%02d-%02d", year, month, day)

  fun formatDisplay(): String {
    val monthNames = arrayOf("", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    return "${monthNames[month]} $day, $year"
  }

  /** Exact Julian Day Number (proleptic Gregorian calendar). */
  fun toEpochDays(): Long {
    val a = (14 - month) / 12
    val y = year + 4800 - a
    val m = month + 12 * a - 3
    return day.toLong() + (153 * m + 2) / 5 + 365L * y + y / 4 - y / 100 + y / 400 - 32045
  }

  companion object {
    fun isLeapYear(year: Int): Boolean =
      (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)

    fun daysInMonth(year: Int, month: Int): Int =
      when (month) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        2 -> if (isLeapYear(year)) 29 else 28
        else -> throw IllegalArgumentException("Invalid month $month")
      }

    fun today(): CalcoraDate {
      val cal = Calendar.getInstance()
      return CalcoraDate(
        year = cal.get(Calendar.YEAR),
        month = cal.get(Calendar.MONTH) + 1,
        day = cal.get(Calendar.DAY_OF_MONTH),
      )
    }

    fun fromEpochDays(jdnZeroBased: Long): CalcoraDate {
      val j = jdnZeroBased + 32044
      val g = j / 146097
      val dg = j % 146097
      val c = (dg / 36524 + 1) * 3 / 4
      val dc = dg - c * 36524
      val b = dc / 1461
      val db = dc % 1461
      val a = (db / 365 + 1) * 3 / 4
      val da = db - a * 365
      val y = g * 400 + c * 100 + b * 4 + a
      val m = (da * 5 + 308) / 153 - 2
      val d = da - (m + 4) * 153 / 5 + 122
      val year = (y - 4800 + (m + 2) / 12).toInt()
      val month = ((m + 2) % 12 + 1).toInt()
      val day = (d + 1).toInt()
      return CalcoraDate(year, month, day)
    }

    fun fromEpochMillisUtc(millis: Long): CalcoraDate {
      val cal = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
      cal.timeInMillis = millis
      return CalcoraDate(
        year = cal.get(Calendar.YEAR),
        month = cal.get(Calendar.MONTH) + 1,
        day = cal.get(Calendar.DAY_OF_MONTH),
      )
    }
  }
}

data class AgeResult(
  val years: Int,
  val months: Int,
  val days: Int,
  val totalDaysLived: Long,
  val totalWeeks: Long,
  val remainingWeekDays: Long,
  val birthDate: CalcoraDate,
  val presentDate: CalcoraDate,
  val nextBirthday: CalcoraDate,
  val daysUntilNextBirthday: Long,
) {
  val formattedAge: String
    get() = "$years ${if (years == 1) "Year" else "Years"}, $months ${if (months == 1) "Month" else "Months"}, $days ${if (days == 1) "Day" else "Days"}"
}

data class DateDiffResult(
  val years: Int,
  val months: Int,
  val days: Int,
  val totalDays: Long,
  val totalWeeks: Long,
  val remainingWeekDays: Long,
)

data class TimeDurationResult(
  val hours: Int,
  val minutes: Int,
  val seconds: Int,
  val totalSeconds: Long,
)

object DateTimeEngine {

  /**
   * Exact calendar-aware difference between startDate <= endDate.
   * Borrows days from the actual preceding month of endDate (handling 28/29/30/31-day months).
   */
  fun calendarDifference(start: CalcoraDate, end: CalcoraDate): Triple<Int, Int, Int> {
    var years = end.year - start.year
    var months = end.month - start.month
    var days = end.day - start.day

    if (days < 0) {
      months -= 1
      val prevMonth = if (end.month == 1) 12 else end.month - 1
      val prevYear = if (end.month == 1) end.year - 1 else end.year
      days += CalcoraDate.daysInMonth(prevYear, prevMonth)
    }
    if (months < 0) {
      years -= 1
      months += 12
    }
    return Triple(years, months, days)
  }

  fun calculateAge(
    birthDate: CalcoraDate,
    presentDate: CalcoraDate,
    todayDate: CalcoraDate = CalcoraDate.today(),
    allowFuturePresentDate: Boolean = true,
  ): CalcOutcome<AgeResult> {
    if (presentDate < birthDate) {
      return CalcOutcome.Error("Present date cannot be earlier than date of birth")
    }
    if (!allowFuturePresentDate && birthDate > todayDate) {
      return CalcOutcome.Error("Birth date cannot be in the future")
    }
    val (y, m, d) = calendarDifference(birthDate, presentDate)
    val totalDays = presentDate.toEpochDays() - birthDate.toEpochDays()
    val totalWeeks = totalDays / 7L
    val remWeekDays = totalDays % 7L

    // Compute exact next birthday strictly after presentDate
    val nextBday = computeNextBirthday(birthDate, presentDate)
    val daysUntilNext = nextBday.toEpochDays() - presentDate.toEpochDays()

    val res =
      AgeResult(
        years = y,
        months = m,
        days = d,
        totalDaysLived = totalDays,
        totalWeeks = totalWeeks,
        remainingWeekDays = remWeekDays,
        birthDate = birthDate,
        presentDate = presentDate,
        nextBirthday = nextBday,
        daysUntilNextBirthday = daysUntilNext,
      )
    return CalcOutcome.Success(
      res,
      StepBreakdown(
        formula = "Exact Gregorian calendar difference with month-length & leap-year borrowing",
        inputValues = "DOB = ${birthDate.formatDisplay()}, Present = ${presentDate.formatDisplay()}",
        substitution = "${presentDate.formatIso()} - ${birthDate.formatIso()}",
        calculationSteps = listOf(
          "Completed Age: ${res.formattedAge}",
          "Total Days Lived: $totalDays days ($totalWeeks weeks, $remWeekDays days)",
          "Next Birthday: ${nextBday.formatDisplay()} ($daysUntilNext days remaining)",
        ),
        finalAnswer = res.formattedAge,
      ),
    )
  }

  private fun computeNextBirthday(birth: CalcoraDate, present: CalcoraDate): CalcoraDate {
    fun birthdayInYear(targetYear: Int): CalcoraDate {
      return if (birth.month == 2 && birth.day == 29 && !CalcoraDate.isLeapYear(targetYear)) {
        // Non-leap year handling for Feb 29 birthdays: March 1 (or Feb 28 — March 1 follows calendar rollover)
        CalcoraDate(targetYear, 3, 1)
      } else {
        CalcoraDate(targetYear, birth.month, birth.day)
      }
    }
    val candidate = birthdayInYear(present.year)
    return if (candidate > present) candidate else birthdayInYear(present.year + 1)
  }

  fun dateDifference(start: CalcoraDate, end: CalcoraDate): CalcOutcome<DateDiffResult> {
    val (earlier, later) = if (start <= end) start to end else end to start
    val (y, m, d) = calendarDifference(earlier, later)
    val totalDays = later.toEpochDays() - earlier.toEpochDays()
    val weeks = totalDays / 7L
    val rem = totalDays % 7L
    val res = DateDiffResult(y, m, d, totalDays, weeks, rem)
    val label = "$y yrs, $m mos, $d days"
    return CalcOutcome.Success(
      res,
      StepBreakdown(
        formula = "Calendar interval + exact Julian Day Number total days",
        inputValues = "${earlier.formatDisplay()} to ${later.formatDisplay()}" + if (start > end) " (swapped order)" else "",
        substitution = "${later.formatIso()} - ${earlier.formatIso()}",
        calculationSteps = listOf(
          "Calendar Difference: $label",
          "Total Exact Days: $totalDays days",
          "Weeks + Days: $weeks weeks, $rem days",
        ),
        finalAnswer = "$label ($totalDays days)",
      ),
    )
  }

  enum class DateUnit(val label: String) {
    DAYS("Days"),
    WEEKS("Weeks"),
    MONTHS("Months"),
    YEARS("Years"),
  }

  fun addOrSubtract(
    base: CalcoraDate,
    amount: Int,
    unit: DateUnit,
    subtract: Boolean,
  ): CalcOutcome<CalcoraDate> {
    if (amount < 0) return CalcOutcome.Error("Amount must be non-negative")
    val signed = if (subtract) -amount else amount
    val resultDate =
      when (unit) {
        DateUnit.DAYS -> CalcoraDate.fromEpochDays(base.toEpochDays() + signed.toLong())
        DateUnit.WEEKS -> CalcoraDate.fromEpochDays(base.toEpochDays() + signed.toLong() * 7L)
        DateUnit.MONTHS -> addMonthsClamped(base, signed)
        DateUnit.YEARS -> addMonthsClamped(base, signed * 12)
      }
    val opWord = if (subtract) "Subtract" else "Add"
    return CalcOutcome.Success(
      resultDate,
      StepBreakdown(
        formula = "Calendar-safe date arithmetic (clamping month-end & leap-day boundaries)",
        inputValues = "${base.formatDisplay()}, $opWord $amount ${unit.label}",
        substitution = "${base.formatIso()} ${if (subtract) "-" else "+"} $amount ${unit.label.lowercase()}",
        calculationSteps = listOf("Result Date: ${resultDate.formatDisplay()} (${resultDate.formatIso()})"),
        finalAnswer = resultDate.formatDisplay(),
      ),
    )
  }

  private fun addMonthsClamped(date: CalcoraDate, deltaMonths: Int): CalcoraDate {
    val totalMonths = (date.year * 12 + (date.month - 1)) + deltaMonths
    val newYear = Math.floorDiv(totalMonths, 12)
    val newMonth = Math.floorMod(totalMonths, 12) + 1
    val maxDay = CalcoraDate.daysInMonth(newYear, newMonth)
    val clampedDay = minOf(date.day, maxDay)
    return CalcoraDate(newYear, newMonth, clampedDay)
  }

  fun timeDuration(
    startH: Int,
    startM: Int,
    startS: Int,
    endH: Int,
    endM: Int,
    endS: Int,
    allowOvernight: Boolean,
  ): CalcOutcome<TimeDurationResult> {
    if (startH !in 0..23 || endH !in 0..23 || startM !in 0..59 || endM !in 0..59 || startS !in 0..59 || endS !in 0..59) {
      return CalcOutcome.Error("Hours must be 0–23; minutes and seconds must be 0–59")
    }
    val startTotal = startH * 3600L + startM * 60L + startS
    val endTotal = endH * 3600L + endM * 60L + endS
    var diff = endTotal - startTotal
    if (diff < 0) {
      if (allowOvernight) diff += 86400L
      else return CalcOutcome.Error("End time is earlier than start time (enable Overnight mode if crossing midnight)")
    }
    val h = (diff / 3600L).toInt()
    val m = ((diff % 3600L) / 60L).toInt()
    val s = (diff % 60L).toInt()
    val formatted = "${h}h ${m}m ${s}s"
    return CalcOutcome.Success(
      TimeDurationResult(h, m, s, diff),
      StepBreakdown(
        formula = "Δt = End - Start (+ 24h if overnight)",
        inputValues = String.format(java.util.Locale.US, "%02d:%02d:%02d → %02d:%02d:%02d", startH, startM, startS, endH, endM, endS),
        substitution = "Total seconds = $diff s",
        calculationSteps = listOf("Duration = $formatted", "Total Minutes = ${NumberFormatter.formatPlain(diff / 60.0, 2)} min"),
        finalAnswer = formatted,
      ),
    )
  }
}

// ==========================================
// 4. GEOMETRY ENGINE (2D & 3D)
// ==========================================

enum class GeometryShape(val label: String, val is3D: Boolean, val inputs: List<String>) {
  SQUARE("Square", false, listOf("Side (a)")),
  RECTANGLE("Rectangle", false, listOf("Length (l)", "Width (w)")),
  TRIANGLE("Triangle (Base & Height)", false, listOf("Base (b)", "Height (h)", "Side a", "Side c")),
  CIRCLE("Circle", false, listOf("Radius (r)")),
  PARALLELOGRAM("Parallelogram", false, listOf("Base (b)", "Height (h)", "Side (a)")),
  TRAPEZIUM("Trapezium", false, listOf("Base a", "Base b", "Height (h)", "Side c", "Side d")),
  RHOMBUS("Rhombus", false, listOf("Diagonal 1 (d₁)", "Diagonal 2 (d₂)")),
  CUBE("Cube", true, listOf("Side (a)")),
  CUBOID("Cuboid", true, listOf("Length (l)", "Width (w)", "Height (h)")),
  CYLINDER("Cylinder", true, listOf("Radius (r)", "Height (h)")),
  CONE("Cone", true, listOf("Radius (r)", "Height (h)")),
  SPHERE("Sphere", true, listOf("Radius (r)")),
  HEMISPHERE("Hemisphere", true, listOf("Radius (r)")),
  PRISM("Prism (Rectangular/General)", true, listOf("Base Area (B)", "Base Perimeter (P)", "Height (h)")),
}

data class GeometryResult(
  val primaryLabel: String,
  val primaryValue: Double,
  val secondaryLabel: String,
  val secondaryValue: Double,
  val tertiaryLabel: String? = null,
  val tertiaryValue: Double? = null,
)

object GeometryEngine {
  fun calculate(shape: GeometryShape, p: List<Double>): CalcOutcome<GeometryResult> {
    if (p.any { it <= 0.0 }) return CalcOutcome.Error("All dimensions must be positive numbers")
    fun f(v: Double) = NumberFormatter.formatPlain(v, 4)

    return when (shape) {
      GeometryShape.SQUARE -> {
        val a = p[0]
        val area = a * a
        val perim = 4 * a
        CalcOutcome.Success(GeometryResult("Area", area, "Perimeter", perim), StepBreakdown("A = a², P = 4a", "a = ${f(a)}", "A = ${f(a)}², P = 4×${f(a)}", listOf("Area = ${f(area)}", "Perimeter = ${f(perim)}"), "Area = ${f(area)}"))
      }
      GeometryShape.RECTANGLE -> {
        val (l, w) = p[0] to p[1]
        val area = l * w
        val perim = 2 * (l + w)
        CalcOutcome.Success(GeometryResult("Area", area, "Perimeter", perim), StepBreakdown("A = l×w, P = 2(l+w)", "l=${f(l)}, w=${f(w)}", "A=${f(l)}×${f(w)}", listOf("Area = ${f(area)}", "Perimeter = ${f(perim)}"), "Area = ${f(area)}"))
      }
      GeometryShape.TRIANGLE -> {
        val (b, h, sa, sc) = listOf(p[0], p[1], p[2], p[3])
        if (b + sa <= sc || b + sc <= sa || sa + sc <= b) {
          return CalcOutcome.Error("Triangle inequality violated: sum of any two sides must exceed third side")
        }
        val area = 0.5 * b * h
        val perim = b + sa + sc
        CalcOutcome.Success(GeometryResult("Area", area, "Perimeter", perim), StepBreakdown("A = ½bh, P = a + b + c", "b=${f(b)}, h=${f(h)}, sides=${f(sa)}, ${f(sc)}", "A = 0.5×${f(b)}×${f(h)}", listOf("Area = ${f(area)}", "Perimeter = ${f(perim)}"), "Area = ${f(area)}"))
      }
      GeometryShape.CIRCLE -> {
        val r = p[0]
        val area = PI * r * r
        val circ = 2 * PI * r
        CalcOutcome.Success(GeometryResult("Area", area, "Circumference", circ), StepBreakdown("A = πr², C = 2πr", "r = ${f(r)}", "A = π×${f(r)}²", listOf("Area = ${f(area)}", "Circumference = ${f(circ)}"), "Area = ${f(area)}"))
      }
      GeometryShape.PARALLELOGRAM -> {
        val (b, h, a) = Triple(p[0], p[1], p[2])
        val area = b * h
        val perim = 2 * (a + b)
        CalcOutcome.Success(GeometryResult("Area", area, "Perimeter", perim), StepBreakdown("A = b×h, P = 2(a+b)", "b=${f(b)}, h=${f(h)}, side=${f(a)}", "A = ${f(b)}×${f(h)}", listOf("Area = ${f(area)}", "Perimeter = ${f(perim)}"), "Area = ${f(area)}"))
      }
      GeometryShape.TRAPEZIUM -> {
        val (a, b, h, c, d) = p
        val area = 0.5 * (a + b) * h
        val perim = a + b + c + d
        CalcOutcome.Success(GeometryResult("Area", area, "Perimeter", perim), StepBreakdown("A = ½(a+b)h, P = a+b+c+d", "a=${f(a)}, b=${f(b)}, h=${f(h)}", "A = 0.5×(${f(a)}+${f(b)})×${f(h)}", listOf("Area = ${f(area)}", "Perimeter = ${f(perim)}"), "Area = ${f(area)}"))
      }
      GeometryShape.RHOMBUS -> {
        val (d1, d2) = p[0] to p[1]
        val area = 0.5 * d1 * d2
        val side = sqrt((d1 / 2) * (d1 / 2) + (d2 / 2) * (d2 / 2))
        val perim = 4 * side
        CalcOutcome.Success(GeometryResult("Area", area, "Perimeter", perim), StepBreakdown("A = ½d₁d₂, P = 4√((d₁/2)²+(d₂/2)²)", "d₁=${f(d1)}, d₂=${f(d2)}", "A = 0.5×${f(d1)}×${f(d2)}", listOf("Area = ${f(area)}", "Side = ${f(side)}, Perimeter = ${f(perim)}"), "Area = ${f(area)}"))
      }
      GeometryShape.CUBE -> {
        val a = p[0]
        val vol = a * a * a
        val tsa = 6 * a * a
        val lsa = 4 * a * a
        CalcOutcome.Success(GeometryResult("Volume", vol, "Total Surface Area", tsa, "Lateral Surface Area", lsa), StepBreakdown("V = a³, TSA = 6a², LSA = 4a²", "a=${f(a)}", "V = ${f(a)}³", listOf("Volume = ${f(vol)}", "TSA = ${f(tsa)}", "LSA = ${f(lsa)}"), "V = ${f(vol)}"))
      }
      GeometryShape.CUBOID -> {
        val (l, w, h) = Triple(p[0], p[1], p[2])
        val vol = l * w * h
        val tsa = 2 * (l * w + w * h + h * l)
        val lsa = 2 * h * (l + w)
        CalcOutcome.Success(GeometryResult("Volume", vol, "Total Surface Area", tsa, "Lateral Surface Area", lsa), StepBreakdown("V = lwh, TSA = 2(lw+wh+hl)", "l=${f(l)}, w=${f(w)}, h=${f(h)}", "V = ${f(l)}×${f(w)}×${f(h)}", listOf("Volume = ${f(vol)}", "TSA = ${f(tsa)}", "LSA = ${f(lsa)}"), "V = ${f(vol)}"))
      }
      GeometryShape.CYLINDER -> {
        val (r, h) = p[0] to p[1]
        val vol = PI * r * r * h
        val csa = 2 * PI * r * h
        val tsa = 2 * PI * r * (r + h)
        CalcOutcome.Success(GeometryResult("Volume", vol, "Total Surface Area", tsa, "Curved Surface Area", csa), StepBreakdown("V = πr²h, CSA = 2πrh, TSA = 2πr(r+h)", "r=${f(r)}, h=${f(h)}", "V = π×${f(r)}²×${f(h)}", listOf("Volume = ${f(vol)}", "TSA = ${f(tsa)}", "CSA = ${f(csa)}"), "V = ${f(vol)}"))
      }
      GeometryShape.CONE -> {
        val (r, h) = p[0] to p[1]
        val slant = sqrt(r * r + h * h)
        val vol = (1.0 / 3.0) * PI * r * r * h
        val csa = PI * r * slant
        val tsa = PI * r * (r + slant)
        CalcOutcome.Success(GeometryResult("Volume", vol, "Total Surface Area", tsa, "Curved Surface Area", csa), StepBreakdown("V = ⅓πr²h, l = √(r²+h²), CSA = πrl", "r=${f(r)}, h=${f(h)}", "Slant l = ${f(slant)}", listOf("Volume = ${f(vol)}", "TSA = ${f(tsa)}", "CSA = ${f(csa)}"), "V = ${f(vol)}"))
      }
      GeometryShape.SPHERE -> {
        val r = p[0]
        val vol = (4.0 / 3.0) * PI * r * r * r
        val sa = 4 * PI * r * r
        CalcOutcome.Success(GeometryResult("Volume", vol, "Surface Area", sa), StepBreakdown("V = ⁴⁄₃πr³, SA = 4πr²", "r=${f(r)}", "V = (4/3)π×${f(r)}³", listOf("Volume = ${f(vol)}", "Surface Area = ${f(sa)}"), "V = ${f(vol)}"))
      }
      GeometryShape.HEMISPHERE -> {
        val r = p[0]
        val vol = (2.0 / 3.0) * PI * r * r * r
        val csa = 2 * PI * r * r
        val tsa = 3 * PI * r * r
        CalcOutcome.Success(GeometryResult("Volume", vol, "Total Surface Area", tsa, "Curved Surface Area", csa), StepBreakdown("V = ⅔πr³, CSA = 2πr², TSA = 3πr²", "r=${f(r)}", "V = (2/3)π×${f(r)}³", listOf("Volume = ${f(vol)}", "TSA = ${f(tsa)}", "CSA = ${f(csa)}"), "V = ${f(vol)}"))
      }
      GeometryShape.PRISM -> {
        val (baseArea, basePerim, h) = Triple(p[0], p[1], p[2])
        val vol = baseArea * h
        val lsa = basePerim * h
        val tsa = lsa + 2 * baseArea
        CalcOutcome.Success(GeometryResult("Volume", vol, "Total Surface Area", tsa, "Lateral Surface Area", lsa), StepBreakdown("V = B×h, LSA = P×h, TSA = LSA + 2B", "B=${f(baseArea)}, P=${f(basePerim)}, h=${f(h)}", "V = ${f(baseArea)}×${f(h)}", listOf("Volume = ${f(vol)}", "TSA = ${f(tsa)}", "LSA = ${f(lsa)}"), "V = ${f(vol)}"))
      }
    }
  }
}

// ==========================================
// 5. UNIT CONVERTER ENGINE (12 CATEGORIES)
// ==========================================

enum class UnitCategory(val title: String) {
  LENGTH("Length"),
  MASS("Weight / Mass"),
  TEMPERATURE("Temperature"),
  AREA("Area"),
  VOLUME("Volume"),
  SPEED("Speed"),
  TIME("Time"),
  DATA("Data (SI & Binary)"),
  PRESSURE("Pressure"),
  ENERGY("Energy"),
  POWER("Power"),
  ANGLE("Angle"),
}

data class UnitDef(val id: String, val label: String, val symbol: String, val toBaseFactor: Double)

object UnitConverterEngine {

  fun unitsFor(category: UnitCategory): List<UnitDef> =
    when (category) {
      UnitCategory.LENGTH -> listOf(
        UnitDef("mm", "Millimeter", "mm", 0.001),
        UnitDef("cm", "Centimeter", "cm", 0.01),
        UnitDef("m", "Meter", "m", 1.0),
        UnitDef("km", "Kilometer", "km", 1000.0),
        UnitDef("in", "Inch", "in", 0.0254),
        UnitDef("ft", "Foot", "ft", 0.3048),
        UnitDef("yd", "Yard", "yd", 0.9144),
        UnitDef("mi", "Mile", "mi", 1609.344),
      )
      UnitCategory.MASS -> listOf(
        UnitDef("mg", "Milligram", "mg", 1e-6),
        UnitDef("g", "Gram", "g", 0.001),
        UnitDef("kg", "Kilogram", "kg", 1.0),
        UnitDef("t", "Tonne (Metric)", "t", 1000.0),
        UnitDef("oz", "Ounce", "oz", 0.028349523125),
        UnitDef("lb", "Pound", "lb", 0.45359237),
      )
      UnitCategory.TEMPERATURE -> listOf(
        UnitDef("C", "Celsius", "°C", 1.0),
        UnitDef("F", "Fahrenheit", "°F", 1.0),
        UnitDef("K", "Kelvin", "K", 1.0),
      )
      UnitCategory.AREA -> listOf(
        UnitDef("mm2", "Square Millimeter", "mm²", 1e-6),
        UnitDef("cm2", "Square Centimeter", "cm²", 1e-4),
        UnitDef("m2", "Square Meter", "m²", 1.0),
        UnitDef("km2", "Square Kilometer", "km²", 1e6),
        UnitDef("ha", "Hectare", "ha", 10000.0),
        UnitDef("acre", "Acre", "ac", 4046.8564224),
      )
      UnitCategory.VOLUME -> listOf(
        UnitDef("mL", "Milliliter", "mL", 0.001),
        UnitDef("L", "Liter", "L", 1.0),
        UnitDef("cm3", "Cubic Centimeter", "cm³", 0.001),
        UnitDef("m3", "Cubic Meter", "m³", 1000.0),
        UnitDef("gal", "US Gallon", "gal", 3.785411784),
      )
      UnitCategory.SPEED -> listOf(
        UnitDef("ms", "Meters / second", "m/s", 1.0),
        UnitDef("kmh", "Kilometers / hour", "km/h", 1.0 / 3.6),
        UnitDef("mph", "Miles / hour", "mph", 0.44704),
      )
      UnitCategory.TIME -> listOf(
        UnitDef("ms", "Millisecond", "ms", 0.001),
        UnitDef("s", "Second", "s", 1.0),
        UnitDef("min", "Minute", "min", 60.0),
        UnitDef("hr", "Hour", "hr", 3600.0),
        UnitDef("day", "Day", "day", 86400.0),
        UnitDef("wk", "Week", "wk", 604800.0),
      )
      UnitCategory.DATA -> listOf(
        UnitDef("bit", "Bit", "bit", 1.0),
        UnitDef("B", "Byte (8 bits)", "B", 8.0),
        UnitDef("KB", "Kilobyte (SI 10³ B)", "KB", 8.0 * 1_000.0),
        UnitDef("MB", "Megabyte (SI 10⁶ B)", "MB", 8.0 * 1_000_000.0),
        UnitDef("GB", "Gigabyte (SI 10⁹ B)", "GB", 8.0 * 1_000_000_000.0),
        UnitDef("TB", "Terabyte (SI 10¹² B)", "TB", 8.0 * 1_000_000_000_000.0),
        UnitDef("KiB", "Kibibyte (Binary 2¹⁰ B)", "KiB", 8.0 * 1024.0),
        UnitDef("MiB", "Mebibyte (Binary 2²⁰ B)", "MiB", 8.0 * 1048576.0),
        UnitDef("GiB", "Gibibyte (Binary 2³⁰ B)", "GiB", 8.0 * 1073741824.0),
        UnitDef("TiB", "Tebibyte (Binary 2⁴⁰ B)", "TiB", 8.0 * 1099511627776.0),
      )
      UnitCategory.PRESSURE -> listOf(
        UnitDef("Pa", "Pascal", "Pa", 1.0),
        UnitDef("kPa", "Kilopascal", "kPa", 1000.0),
        UnitDef("bar", "Bar", "bar", 100000.0),
        UnitDef("atm", "Atmosphere", "atm", 101325.0),
        UnitDef("psi", "PSI", "psi", 6894.757293168),
      )
      UnitCategory.ENERGY -> listOf(
        UnitDef("J", "Joule", "J", 1.0),
        UnitDef("kJ", "Kilojoule", "kJ", 1000.0),
        UnitDef("cal", "Calorie", "cal", 4.184),
        UnitDef("kcal", "Kilocalorie", "kcal", 4184.0),
        UnitDef("Wh", "Watt-hour", "Wh", 3600.0),
        UnitDef("kWh", "Kilowatt-hour", "kWh", 3_600_000.0),
      )
      UnitCategory.POWER -> listOf(
        UnitDef("W", "Watt", "W", 1.0),
        UnitDef("kW", "Kilowatt", "kW", 1000.0),
        UnitDef("MW", "Megawatt", "MW", 1_000_000.0),
        UnitDef("hp", "Mechanical Horsepower", "hp", 745.699871582),
      )
      UnitCategory.ANGLE -> listOf(
        UnitDef("deg", "Degree", "°", PI / 180.0),
        UnitDef("rad", "Radian", "rad", 1.0),
        UnitDef("grad", "Gradian", "grad", PI / 200.0),
      )
    }

  fun convert(category: UnitCategory, value: Double, from: UnitDef, to: UnitDef): CalcOutcome<Double> {
    if (category != UnitCategory.TEMPERATURE && value < 0.0 && category in setOf(UnitCategory.LENGTH, UnitCategory.MASS, UnitCategory.AREA, UnitCategory.VOLUME, UnitCategory.TIME, UnitCategory.DATA, UnitCategory.PRESSURE)) {
      return CalcOutcome.Error("Value cannot be negative for ${category.title}")
    }
    if (category == UnitCategory.TEMPERATURE) {
      val celsius =
        when (from.id) {
          "C" -> value
          "F" -> (value - 32.0) * 5.0 / 9.0
          "K" -> value - 273.15
          else -> value
        }
      if (celsius < -273.15 - 1e-9) {
        return CalcOutcome.Error("Temperature cannot be below absolute zero (-273.15°C / 0 K)")
      }
      val result =
        when (to.id) {
          "C" -> celsius
          "F" -> celsius * 9.0 / 5.0 + 32.0
          "K" -> celsius + 273.15
          else -> celsius
        }
      val formula =
        when ("${from.id}->${to.id}") {
          "C->F" -> "°F = (°C × 9/5) + 32"
          "F->C" -> "°C = (°F - 32) × 5/9"
          "C->K" -> "K = °C + 273.15"
          "K->C" -> "°C = K - 273.15"
          "F->K" -> "K = (°F - 32) × 5/9 + 273.15"
          "K->F" -> "°F = (K - 273.15) × 9/5 + 32"
          else -> "Identity"
        }
      val ans = "${NumberFormatter.formatPlain(result, 6)} ${to.symbol}"
      return CalcOutcome.Success(
        result,
        StepBreakdown(formula, "${NumberFormatter.formatPlain(value)} ${from.symbol}", "Convert via Celsius (${NumberFormatter.formatPlain(celsius, 4)} °C)", listOf("Result = $ans"), ans, to.symbol),
      )
    }

    val base = value * from.toBaseFactor
    val result = base / to.toBaseFactor
    val ans = "${NumberFormatter.formatPlain(result, 8)} ${to.symbol}"
    return CalcOutcome.Success(
      result,
      StepBreakdown(
        formula = "Target = Value × (${from.symbol}_to_base / ${to.symbol}_to_base)",
        inputValues = "${NumberFormatter.formatPlain(value)} ${from.label}",
        substitution = "${NumberFormatter.formatPlain(value)} × ${NumberFormatter.formatPlain(from.toBaseFactor / to.toBaseFactor, 8)}",
        calculationSteps = listOf("Result = $ans"),
        finalAnswer = ans,
        unit = to.symbol,
      ),
    )
  }
}

// ==========================================
// 6. NUMBER SYSTEM CONVERTER ENGINE
// ==========================================

enum class NumberBase(val label: String, val radix: Int) {
  BINARY("Binary (Base 2)", 2),
  OCTAL("Octal (Base 8)", 8),
  DECIMAL("Decimal (Base 10)", 10),
  HEXADECIMAL("Hexadecimal (Base 16)", 16),
}

data class NumberSystemResult(
  val binary: String,
  val octal: String,
  val decimal: String,
  val hex: String,
)

object NumberSystemEngine {
  fun convert(rawInput: String, fromBase: NumberBase): CalcOutcome<NumberSystemResult> {
    val clean = rawInput.trim().uppercase()
    if (clean.isEmpty()) return CalcOutcome.Error("Enter a number in ${fromBase.label}")

    val negative = clean.startsWith("-")
    val unsigned = if (negative || clean.startsWith("+")) clean.substring(1) else clean
    if (unsigned.isEmpty()) return CalcOutcome.Error("Incomplete number")

    val parts = unsigned.split(".")
    if (parts.size > 2) return CalcOutcome.Error("Multiple decimal points are invalid")

    val intStr = parts[0].ifEmpty { "0" }
    val fracStr = if (parts.size == 2) parts[1] else ""
    if (parts.size == 2 && fracStr.isEmpty()) return CalcOutcome.Error("Enter digits after the radix point")

    val validDigits = "0123456789ABCDEF".substring(0, fromBase.radix)
    if (intStr.any { it !in validDigits } || fracStr.any { it !in validDigits }) {
      return CalcOutcome.Error("Invalid digit for ${fromBase.label}. Allowed: $validDigits")
    }

    val intBig = BigInteger(intStr, fromBase.radix)
    val signPrefix = if (negative && (intBig > BigInteger.ZERO || fracStr.any { it != '0' })) "-" else ""

    if (fracStr.isEmpty()) {
      val res =
        NumberSystemResult(
          binary = signPrefix + intBig.toString(2),
          octal = signPrefix + intBig.toString(8),
          decimal = signPrefix + intBig.toString(10),
          hex = signPrefix + intBig.toString(16).uppercase(),
        )
      return CalcOutcome.Success(
        res,
        StepBreakdown(
          formula = "Positional Base-${fromBase.radix} Conversion",
          inputValues = "$clean (${fromBase.label})",
          substitution = "Decimal value = ${res.decimal}",
          calculationSteps = listOf("Binary: ${res.binary}", "Octal: ${res.octal}", "Decimal: ${res.decimal}", "Hex: ${res.hex}"),
          finalAnswer = "Dec: ${res.decimal} | Hex: ${res.hex}",
        ),
      )
    }

    // Exact fractional conversion using BigDecimal
    var fracValue = BigDecimal.ZERO
    val baseBd = BigDecimal.valueOf(fromBase.radix.toLong())
    var denom = baseBd
    for (ch in fracStr) {
      val digit = ch.digitToInt(fromBase.radix)
      fracValue = fracValue.add(BigDecimal.valueOf(digit.toLong()).divide(denom, 24, java.math.RoundingMode.HALF_UP))
      denom = denom.multiply(baseBd)
    }

    fun formatInBase(targetRadix: Int): String {
      val intOut = intBig.toString(targetRadix).uppercase()
      var rem = fracValue
      val sb = StringBuilder()
      val radixBd = BigDecimal.valueOf(targetRadix.toLong())
      repeat(12) {
        if (rem.compareTo(BigDecimal.ZERO) == 0) return@repeat
        rem = rem.multiply(radixBd)
        val whole = rem.toInt().coerceIn(0, targetRadix - 1)
        sb.append("0123456789ABCDEF"[whole])
        rem = rem.subtract(BigDecimal.valueOf(whole.toLong()))
      }
      val fracOut = sb.toString().trimEnd('0')
      return if (fracOut.isEmpty()) "$signPrefix$intOut" else "$signPrefix$intOut.$fracOut"
    }

    val res =
      NumberSystemResult(
        binary = formatInBase(2),
        octal = formatInBase(8),
        decimal = formatInBase(10),
        hex = formatInBase(16),
      )
    return CalcOutcome.Success(
      res,
      StepBreakdown(
        formula = "Integer + Positional Fractional Conversion (radix ${fromBase.radix})",
        inputValues = "$clean (${fromBase.label})",
        substitution = "Integer = $signPrefix$intStr, Fractional digits = $fracStr",
        calculationSteps = listOf("Binary: ${res.binary}", "Octal: ${res.octal}", "Decimal: ${res.decimal}", "Hex: ${res.hex}"),
        finalAnswer = "Dec: ${res.decimal}",
      ),
    )
  }
}

// ==========================================
// 7. HEALTH ENGINE (BMI & BMR)
// ==========================================

data class BmiResult(
  val bmi: Double,
  val category: String,
  val explanation: String,
)

enum class BiologicalSex(val label: String) {
  MALE("Male"),
  FEMALE("Female"),
}

data class BmrResult(
  val bmrCalories: Double,
  val formulaUsed: String,
  val explanation: String,
)

object HealthEngine {
  fun calculateBmi(weightKg: Double, heightCm: Double): CalcOutcome<BmiResult> {
    if (weightKg <= 0.0) return CalcOutcome.Error("Weight must be greater than zero")
    if (heightCm <= 0.0) return CalcOutcome.Error("Height must be greater than zero")
    val heightM = heightCm / 100.0
    val bmi = weightKg / (heightM * heightM)
    val category =
      when {
        bmi < 18.5 -> "Underweight (< 18.5)"
        bmi < 25.0 -> "Normal Weight (18.5 – 24.9)"
        bmi < 30.0 -> "Overweight (25.0 – 29.9)"
        else -> "Obesity (≥ 30.0)"
      }
    val explanation =
      "Based on WHO adult BMI classification. Note: BMI is a general screening measure and not a medical diagnosis."
    val bmiStr = NumberFormatter.formatPlain(bmi, 2)
    return CalcOutcome.Success(
      BmiResult(bmi, category, explanation),
      StepBreakdown(
        formula = "BMI = Weight(kg) / (Height(m))²",
        inputValues = "Weight = $weightKg kg, Height = $heightCm cm (${NumberFormatter.formatPlain(heightM, 3)} m)",
        substitution = "BMI = $weightKg / (${NumberFormatter.formatPlain(heightM, 3)})²",
        calculationSteps = listOf("BMI = $bmiStr kg/m²", "Category: $category", explanation),
        finalAnswer = "$bmiStr kg/m²",
        unit = "kg/m²",
      ),
    )
  }

  fun calculateBmr(
    sex: BiologicalSex,
    ageYears: Int,
    heightCm: Double,
    weightKg: Double,
  ): CalcOutcome<BmrResult> {
    if (ageYears !in 1..120) return CalcOutcome.Error("Enter a valid age between 1 and 120 years")
    if (heightCm <= 0.0) return CalcOutcome.Error("Height must be greater than zero")
    if (weightKg <= 0.0) return CalcOutcome.Error("Weight must be greater than zero")

    val sexOffset = if (sex == BiologicalSex.MALE) 5.0 else -161.0
    val bmr = 10.0 * weightKg + 6.25 * heightCm - 5.0 * ageYears + sexOffset
    val formulaName = "Mifflin-St Jeor Equation"
    val formulaExpr =
      if (sex == BiologicalSex.MALE) "BMR = 10W + 6.25H - 5A + 5"
      else "BMR = 10W + 6.25H - 5A - 161"
    val explanation =
      "Estimated basal metabolic rate (resting energy expenditure) using Mifflin-St Jeor. This is a mathematical estimate, not medical advice."
    val bmrStr = "${NumberFormatter.formatPlain(bmr, 0)} kcal/day"

    return CalcOutcome.Success(
      BmrResult(bmr, formulaName, explanation),
      StepBreakdown(
        formula = formulaExpr,
        inputValues = "${sex.label}, Age=$ageYears yrs, Height=$heightCm cm, Weight=$weightKg kg",
        substitution = "10($weightKg) + 6.25($heightCm) - 5($ageYears) ${if (sex == BiologicalSex.MALE) "+ 5" else "- 161"}",
        calculationSteps = listOf("Estimated BMR = $bmrStr", explanation),
        finalAnswer = bmrStr,
        unit = "kcal/day",
      ),
    )
  }
}
