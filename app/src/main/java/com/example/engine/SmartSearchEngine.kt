package com.example.engine

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.Balance
import androidx.compose.material.icons.outlined.Biotech
import androidx.compose.material.icons.outlined.Cake
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Casino
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.DeveloperBoard
import androidx.compose.material.icons.outlined.Discount
import androidx.compose.material.icons.outlined.Equalizer
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Functions
import androidx.compose.material.icons.outlined.Grading
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.NorthEast
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Percent
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.SquareFoot
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.TrendingDown
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material.icons.outlined.WorkOutline
import com.example.model.AngleMode
import com.example.model.CalcOutcome
import com.example.model.CalculatorTool
import com.example.model.ToolCategory

object Routes {
  const val SPLASH = "splash"
  const val HOME = "home"
  const val TOOLS = "tools"
  const val HISTORY = "history"
  const val SETTINGS = "settings"
  const val THEMES = "themes"

  // Science
  const val SCI_CALCULATOR = "sci_calculator"
  const val SCI_FRACTION = "sci_fraction"
  const val SCI_EQUATION = "sci_equation"
  const val SCI_MATRIX = "sci_matrix"
  const val SCI_VECTOR = "sci_vector"
  const val SCI_STATISTICS = "sci_statistics"
  const val SCI_PROBABILITY = "sci_probability"
  const val SCI_PHYSICS = "sci_physics"
  const val SCI_CHEMISTRY = "sci_chemistry"

  // Commerce
  const val COM_CALCULATOR = "com_calculator"
  const val COM_PROFIT_LOSS = "com_profit_loss"
  const val COM_DISCOUNT = "com_discount"
  const val COM_MARKUP = "com_markup"
  const val COM_VAT_TAX = "com_vat_tax"
  const val COM_SIMPLE_INTEREST = "com_simple_interest"
  const val COM_COMPOUND_INTEREST = "com_compound_interest"
  const val COM_LOAN_EMI = "com_loan_emi"
  const val COM_ROI = "com_roi"
  const val COM_BREAK_EVEN = "com_break_even"
  const val COM_COMMISSION = "com_commission"
  const val COM_SALARY = "com_salary"
  const val COM_REVENUE_EXPENSE = "com_revenue_expense"
  const val COM_DEPRECIATION = "com_depreciation"
  const val COM_ACCOUNTING = "com_accounting"

  // Arts
  const val ART_CALCULATOR = "art_calculator"
  const val ART_PERCENTAGE = "art_percentage"
  const val ART_AVERAGE = "art_average"
  const val ART_RATIO = "art_ratio"
  const val ART_STATISTICS = "art_statistics"
  const val ART_DATETIME = "art_datetime"

  // Universal
  const val UNI_HUB = "uni_hub"
  const val UNI_AGE = "uni_age"
  const val UNI_DATE_DIFF = "uni_date_diff"
  const val UNI_DATE_ADD_SUB = "uni_date_add_sub"
  const val UNI_TIME_DURATION = "uni_time_duration"
  const val UNI_GEOMETRY = "uni_geometry"
  const val UNI_UNIT_CONVERTER = "uni_unit_converter"
  const val UNI_NUMBER_SYSTEM = "uni_number_system"
  const val UNI_BMI = "uni_bmi"
  const val UNI_BMR = "uni_bmr"

  // Student
  const val STU_GPA = "stu_gpa"
  const val STU_CGPA = "stu_cgpa"
  const val STU_MARKS = "stu_marks"
  const val STU_WEIGHTED_AVG = "stu_weighted_avg"
}

object ToolRegistry {
  val allTools: List<CalculatorTool> = listOf(
    // SCIENCE
    CalculatorTool(
      id = "sci_calc",
      name = "Scientific Calculator",
      shortDescription = "Trig, hyperbolic, logs, roots, powers, factorial, memory & DEG/RAD/GRAD",
      category = ToolCategory.SCIENCE,
      route = Routes.SCI_CALCULATOR,
      keywords = listOf("scientific", "sin", "cos", "tan", "log", "ln", "factorial", "root", "rad", "deg"),
      icon = Icons.Outlined.Functions,
    ),
    CalculatorTool(
      id = "sci_fraction",
      name = "Fraction Calculator",
      shortDescription = "Exact rational arithmetic, mixed & improper fractions, decimal conversion",
      category = ToolCategory.SCIENCE,
      route = Routes.SCI_FRACTION,
      keywords = listOf("fraction", "rational", "mixed", "improper", "decimal", "numerator", "denominator"),
      icon = Icons.Outlined.PieChart,
    ),
    CalculatorTool(
      id = "sci_equation",
      name = "Equation Solver",
      shortDescription = "Linear, Quadratic (real & complex roots), and 2×2 / 3×3 systems",
      category = ToolCategory.SCIENCE,
      route = Routes.SCI_EQUATION,
      keywords = listOf("equation", "quadratic", "linear", "simultaneous", "discriminant", "roots", "algebra"),
      icon = Icons.Outlined.ShowChart,
    ),
    CalculatorTool(
      id = "sci_matrix",
      name = "Matrix Calculator",
      shortDescription = "2×2 & 3×3 addition, subtraction, multiplication, determinant, transpose & inverse",
      category = ToolCategory.SCIENCE,
      route = Routes.SCI_MATRIX,
      keywords = listOf("matrix", "matrices", "determinant", "inverse", "transpose", "2x2", "3x3"),
      icon = Icons.Outlined.GridOn,
    ),
    CalculatorTool(
      id = "sci_vector",
      name = "Vector Calculator",
      shortDescription = "2D & 3D vector addition, magnitude, dot product, cross product & angle",
      category = ToolCategory.SCIENCE,
      route = Routes.SCI_VECTOR,
      keywords = listOf("vector", "dot product", "cross product", "magnitude", "angle", "2d", "3d"),
      icon = Icons.Outlined.NorthEast,
    ),
    CalculatorTool(
      id = "sci_stats",
      name = "Statistics Calculator",
      shortDescription = "Mean, median, mode, range, population & sample variance and std dev",
      category = ToolCategory.SCIENCE,
      route = Routes.SCI_STATISTICS,
      keywords = listOf("statistics", "mean", "median", "mode", "variance", "standard deviation", "range"),
      icon = Icons.Outlined.Equalizer,
    ),
    CalculatorTool(
      id = "sci_prob",
      name = "Probability Calculator",
      shortDescription = "Basic probability, complement, permutations (nPr) & combinations (nCr)",
      category = ToolCategory.SCIENCE,
      route = Routes.SCI_PROBABILITY,
      keywords = listOf("probability", "permutation", "combination", "npr", "ncr", "complement"),
      icon = Icons.Outlined.Casino,
    ),
    CalculatorTool(
      id = "sci_physics",
      name = "Physics Calculator",
      shortDescription = "Motion, Force & Energy, Ohm's Law, Electrical Power, Density, Pressure & Waves",
      category = ToolCategory.SCIENCE,
      route = Routes.SCI_PHYSICS,
      keywords = listOf("physics", "speed", "velocity", "acceleration", "force", "work", "energy", "ohms", "density", "pressure", "wave"),
      icon = Icons.Outlined.Speed,
    ),
    CalculatorTool(
      id = "sci_chemistry",
      name = "Chemistry Calculator",
      shortDescription = "Molar mass (118 elements), moles, molarity, dilution, concentration, pH & gas law",
      category = ToolCategory.SCIENCE,
      route = Routes.SCI_CHEMISTRY,
      keywords = listOf("chemistry", "molar mass", "moles", "molarity", "dilution", "ph", "ideal gas", "periodic table"),
      icon = Icons.Outlined.Biotech,
    ),

    // COMMERCE
    CalculatorTool(
      id = "com_calc",
      name = "Financial Calculator",
      shortDescription = "Commerce calculator keypad with quick Profit, Margin, Discount, VAT & Tax keys",
      category = ToolCategory.COMMERCE,
      route = Routes.COM_CALCULATOR,
      keywords = listOf("commerce", "financial", "business", "calculator", "vat", "margin", "profit"),
      icon = Icons.Outlined.AttachMoney,
    ),
    CalculatorTool(
      id = "com_pl",
      name = "Profit & Loss",
      shortDescription = "Compute profit/loss amount and percentage based on cost price",
      category = ToolCategory.COMMERCE,
      route = Routes.COM_PROFIT_LOSS,
      keywords = listOf("profit", "loss", "cost price", "selling price", "commerce"),
      icon = Icons.Outlined.TrendingUp,
    ),
    CalculatorTool(
      id = "com_disc",
      name = "Discount Calculator",
      shortDescription = "Original price, discount percentage, discount amount & savings",
      category = ToolCategory.COMMERCE,
      route = Routes.COM_DISCOUNT,
      keywords = listOf("discount", "sale", "savings", "final price"),
      icon = Icons.Outlined.Discount,
    ),
    CalculatorTool(
      id = "com_markup",
      name = "Markup Calculator",
      shortDescription = "Markup amount, selling price & clear comparison with profit margin",
      category = ToolCategory.COMMERCE,
      route = Routes.COM_MARKUP,
      keywords = listOf("markup", "margin", "selling price", "cost"),
      icon = Icons.Outlined.TrendingUp,
    ),
    CalculatorTool(
      id = "com_vat",
      name = "VAT & Tax Calculator",
      shortDescription = "Tax-inclusive and tax-exclusive calculations with custom rate input",
      category = ToolCategory.COMMERCE,
      route = Routes.COM_VAT_TAX,
      keywords = listOf("vat", "tax", "gst", "inclusive", "exclusive"),
      icon = Icons.Outlined.ReceiptLong,
    ),
    CalculatorTool(
      id = "com_si",
      name = "Simple Interest",
      shortDescription = "Calculate simple interest SI = (P × R × T) / 100 and total amount",
      category = ToolCategory.COMMERCE,
      route = Routes.COM_SIMPLE_INTEREST,
      keywords = listOf("simple interest", "si", "principal", "rate", "interest"),
      icon = Icons.Outlined.Savings,
    ),
    CalculatorTool(
      id = "com_ci",
      name = "Compound Interest",
      shortDescription = "Compounded annually, semi-annually, quarterly, monthly or daily",
      category = ToolCategory.COMMERCE,
      route = Routes.COM_COMPOUND_INTEREST,
      keywords = listOf("compound interest", "compounding", "investment", "ci"),
      icon = Icons.Outlined.AccountBalance,
    ),
    CalculatorTool(
      id = "com_emi",
      name = "Loan & EMI Calculator",
      shortDescription = "Periodic EMI payment, total payment & total interest with zero-rate support",
      category = ToolCategory.COMMERCE,
      route = Routes.COM_LOAN_EMI,
      keywords = listOf("loan", "emi", "mortgage", "amortization", "installment"),
      icon = Icons.Outlined.Payments,
    ),
    CalculatorTool(
      id = "com_roi",
      name = "ROI Calculator",
      shortDescription = "Net profit and Return on Investment percentage",
      category = ToolCategory.COMMERCE,
      route = Routes.COM_ROI,
      keywords = listOf("roi", "return on investment", "net profit"),
      icon = Icons.Outlined.Analytics,
    ),
    CalculatorTool(
      id = "com_bep",
      name = "Break-even Calculator",
      shortDescription = "Contribution per unit, break-even quantity & break-even revenue",
      category = ToolCategory.COMMERCE,
      route = Routes.COM_BREAK_EVEN,
      keywords = listOf("break even", "breakeven", "fixed cost", "variable cost", "contribution"),
      icon = Icons.Outlined.Balance,
    ),
    CalculatorTool(
      id = "com_comm",
      name = "Commission Calculator",
      shortDescription = "Sales commission and total earnings with optional base pay",
      category = ToolCategory.COMMERCE,
      route = Routes.COM_COMMISSION,
      keywords = listOf("commission", "sales", "earnings"),
      icon = Icons.Outlined.Payments,
    ),
    CalculatorTool(
      id = "com_salary",
      name = "Salary Calculator",
      shortDescription = "Gross salary, total deductions and net take-home pay",
      category = ToolCategory.COMMERCE,
      route = Routes.COM_SALARY,
      keywords = listOf("salary", "gross", "net", "payroll", "deductions", "allowances"),
      icon = Icons.Outlined.WorkOutline,
    ),
    CalculatorTool(
      id = "com_revexp",
      name = "Revenue & Expense",
      shortDescription = "Multi-entry session ledger for total revenue, expenses, net profit & margin",
      category = ToolCategory.COMMERCE,
      route = Routes.COM_REVENUE_EXPENSE,
      keywords = listOf("revenue", "expense", "ledger", "profit margin", "cashflow"),
      icon = Icons.Outlined.ReceiptLong,
    ),
    CalculatorTool(
      id = "com_dep",
      name = "Depreciation Calculator",
      shortDescription = "Straight-line & declining-balance depreciation schedules and book value",
      category = ToolCategory.COMMERCE,
      route = Routes.COM_DEPRECIATION,
      keywords = listOf("depreciation", "straight line", "declining balance", "salvage", "book value"),
      icon = Icons.Outlined.TrendingDown,
    ),
    CalculatorTool(
      id = "com_acct",
      name = "Accounting Helpers",
      shortDescription = "Percentage change, gross margin, net margin, cost ratio & revenue comparison",
      category = ToolCategory.COMMERCE,
      route = Routes.COM_ACCOUNTING,
      keywords = listOf("accounting", "gross margin", "net margin", "cost ratio", "revenue comparison"),
      icon = Icons.Outlined.Analytics,
    ),

    // ARTS
    CalculatorTool(
      id = "art_calc",
      name = "Basic Calculator",
      shortDescription = "Clean everyday arithmetic calculator with parentheses, % and memory",
      category = ToolCategory.ARTS,
      route = Routes.ART_CALCULATOR,
      keywords = listOf("basic", "general", "arithmetic", "calculator", "add", "subtract"),
      icon = Icons.Outlined.Calculate,
    ),
    CalculatorTool(
      id = "art_pct",
      name = "Percentage Calculator",
      shortDescription = "X% of Y, X is what % of Y, % increase, % decrease & % difference",
      category = ToolCategory.ARTS,
      route = Routes.ART_PERCENTAGE,
      keywords = listOf("percentage", "percent", "increase", "decrease", "difference", "%"),
      icon = Icons.Outlined.Percent,
    ),
    CalculatorTool(
      id = "art_avg",
      name = "Average Calculator",
      shortDescription = "Arithmetic mean, sum and count for multiple numbers",
      category = ToolCategory.ARTS,
      route = Routes.ART_AVERAGE,
      keywords = listOf("average", "mean", "sum", "count"),
      icon = Icons.Outlined.Equalizer,
    ),
    CalculatorTool(
      id = "art_ratio",
      name = "Ratio & Proportion",
      shortDescription = "Simplify ratios, compare ratios, solve proportions & split amounts",
      category = ToolCategory.ARTS,
      route = Routes.ART_RATIO,
      keywords = listOf("ratio", "proportion", "simplify", "divide amount"),
      icon = Icons.Outlined.Balance,
    ),
    CalculatorTool(
      id = "art_stats",
      name = "General Statistics",
      shortDescription = "Mean, median, mode, range, variance & standard deviation",
      category = ToolCategory.ARTS,
      route = Routes.ART_STATISTICS,
      keywords = listOf("general statistics", "median", "mode", "std dev"),
      icon = Icons.Outlined.Equalizer,
    ),
    CalculatorTool(
      id = "art_dt",
      name = "Date & Time Tools",
      shortDescription = "Calendar date difference, add/subtract days & weeks, and time duration",
      category = ToolCategory.ARTS,
      route = Routes.ART_DATETIME,
      keywords = listOf("date", "time", "days between", "calendar", "weeks"),
      icon = Icons.Outlined.DateRange,
    ),

    // UNIVERSAL
    CalculatorTool(
      id = "uni_age",
      name = "Birthday & Age Calculator",
      shortDescription = "Exact calendar age (years, months, days), days lived & next birthday countdown",
      category = ToolCategory.UNIVERSAL,
      route = Routes.UNI_AGE,
      keywords = listOf("age", "birthday", "dob", "birth date", "leap year", "next birthday"),
      icon = Icons.Outlined.Cake,
    ),
    CalculatorTool(
      id = "uni_date_diff",
      name = "Date Difference",
      shortDescription = "Exact years, months, days, weeks and total days between any two dates",
      category = ToolCategory.UNIVERSAL,
      route = Routes.UNI_DATE_DIFF,
      keywords = listOf("date difference", "days between", "interval", "calendar"),
      icon = Icons.Outlined.DateRange,
    ),
    CalculatorTool(
      id = "uni_date_add",
      name = "Date Add & Subtract",
      shortDescription = "Safely add or subtract days, weeks, months or years across leap boundaries",
      category = ToolCategory.UNIVERSAL,
      route = Routes.UNI_DATE_ADD_SUB,
      keywords = listOf("add days", "subtract days", "add months", "add years", "calendar"),
      icon = Icons.Outlined.CalendarMonth,
    ),
    CalculatorTool(
      id = "uni_time_dur",
      name = "Time Duration",
      shortDescription = "Hours, minutes and seconds between start and end times (supports overnight)",
      category = ToolCategory.UNIVERSAL,
      route = Routes.UNI_TIME_DURATION,
      keywords = listOf("time duration", "hours", "minutes", "seconds", "overnight"),
      icon = Icons.Outlined.Schedule,
    ),
    CalculatorTool(
      id = "uni_geom",
      name = "Geometry Calculator",
      shortDescription = "2D area/perimeter (triangle, circle, etc.) & 3D volume/surface area",
      category = ToolCategory.UNIVERSAL,
      route = Routes.UNI_GEOMETRY,
      keywords = listOf("geometry", "triangle area", "circle", "sphere", "cylinder", "cone", "volume", "area", "perimeter"),
      icon = Icons.Outlined.SquareFoot,
    ),
    CalculatorTool(
      id = "uni_unit",
      name = "Unit Converter",
      shortDescription = "Length, mass, temperature, area, volume, speed, time, SI/IEC data, pressure & more",
      category = ToolCategory.UNIVERSAL,
      route = Routes.UNI_UNIT_CONVERTER,
      keywords = listOf("unit converter", "convert", "kg", "pound", "celsius", "fahrenheit", "meter", "mile", "gb", "gib"),
      icon = Icons.Outlined.SwapHoriz,
    ),
    CalculatorTool(
      id = "uni_numsys",
      name = "Number System Converter",
      shortDescription = "Convert between Binary, Octal, Decimal and Hexadecimal (with negatives)",
      category = ToolCategory.UNIVERSAL,
      route = Routes.UNI_NUMBER_SYSTEM,
      keywords = listOf("binary", "decimal", "octal", "hexadecimal", "hex", "base converter"),
      icon = Icons.Outlined.DeveloperBoard,
    ),
    CalculatorTool(
      id = "uni_bmi",
      name = "BMI Calculator",
      shortDescription = "Body Mass Index screening value and WHO reference category",
      category = ToolCategory.UNIVERSAL,
      route = Routes.UNI_BMI,
      keywords = listOf("bmi", "body mass index", "weight", "height", "health"),
      icon = Icons.Outlined.FitnessCenter,
    ),
    CalculatorTool(
      id = "uni_bmr",
      name = "BMR Calculator",
      shortDescription = "Estimated Basal Metabolic Rate using the Mifflin-St Jeor equation",
      category = ToolCategory.UNIVERSAL,
      route = Routes.UNI_BMR,
      keywords = listOf("bmr", "calories", "mifflin", "metabolism"),
      icon = Icons.Outlined.LocalFireDepartment,
    ),

    // STUDENT
    CalculatorTool(
      id = "stu_gpa",
      name = "GPA Calculator",
      shortDescription = "Credit-weighted semester GPA with selectable & custom grading scales",
      category = ToolCategory.STUDENT,
      route = Routes.STU_GPA,
      keywords = listOf("gpa", "grade point", "credits", "semester", "student"),
      icon = Icons.Outlined.School,
    ),
    CalculatorTool(
      id = "stu_cgpa",
      name = "CGPA Calculator",
      shortDescription = "Cumulative credit-weighted GPA across multiple semesters",
      category = ToolCategory.STUDENT,
      route = Routes.STU_CGPA,
      keywords = listOf("cgpa", "cumulative gpa", "semesters", "student"),
      icon = Icons.Outlined.School,
    ),
    CalculatorTool(
      id = "stu_marks",
      name = "Marks Calculator",
      shortDescription = "Total obtained marks, max marks, percentage, average & grade",
      category = ToolCategory.STUDENT,
      route = Routes.STU_MARKS,
      keywords = listOf("marks", "exam", "score", "percentage", "grade"),
      icon = Icons.Outlined.Grading,
    ),
    CalculatorTool(
      id = "stu_wavg",
      name = "Weighted Average",
      shortDescription = "Calculate weighted average across values and custom weights",
      category = ToolCategory.STUDENT,
      route = Routes.STU_WEIGHTED_AVG,
      keywords = listOf("weighted average", "weights", "assignment", "student"),
      icon = Icons.Outlined.Category,
    ),
  )

  fun findByRoute(route: String): CalculatorTool? = allTools.firstOrNull { it.route == route }
}

data class QuickNaturalResult(
  val title: String,
  val answer: String,
  val subtitle: String,
  val targetRoute: String,
)

data class SmartSearchOutput(
  val quickAnswer: QuickNaturalResult?,
  val matchingTools: List<CalculatorTool>,
)

/**
 * 100% offline local search + deterministic natural-language parser.
 */
object SmartSearchEngine {

  fun search(queryRaw: String, angleMode: AngleMode = AngleMode.DEG): SmartSearchOutput {
    val query = queryRaw.trim()
    if (query.isEmpty()) {
      return SmartSearchOutput(null, ToolRegistry.allTools)
    }

    val quick = tryParseNaturalCalculation(query, angleMode)
    val qLower = query.lowercase()
    val tokens = qLower.split(Regex("\\s+")).filter { it.isNotBlank() }

    val scored =
      ToolRegistry.allTools
        .mapNotNull { tool ->
          val name = tool.name.lowercase()
          val desc = tool.shortDescription.lowercase()
          val cat = tool.category.label.lowercase()
          val kws = tool.keywords.map { it.lowercase() }

          var score = 0
          if (name == qLower) score += 100
          if (name.contains(qLower)) score += 60
          if (cat.contains(qLower)) score += 25
          if (kws.any { it == qLower }) score += 55
          if (kws.any { it.contains(qLower) }) score += 35
          if (desc.contains(qLower)) score += 20

          for (tok in tokens) {
            if (tok.length < 2) continue
            if (name.contains(tok)) score += 18
            if (kws.any { it.contains(tok) }) score += 14
            if (desc.contains(tok)) score += 8
            if (cat.contains(tok)) score += 6
          }

          if (score > 0) tool to score else null
        }
        .sortedByDescending { it.second }
        .map { it.first }

    // If natural parser matched unit conversion or percentage, ensure its target tool appears at top
    val finalTools =
      if (quick != null) {
        val target = ToolRegistry.findByRoute(quick.targetRoute)
        if (target != null) (listOf(target) + scored).distinctBy { it.id } else scored
      } else {
        scored
      }

    return SmartSearchOutput(quick, finalTools)
  }

  private fun tryParseNaturalCalculation(query: String, angleMode: AngleMode): QuickNaturalResult? {
    val lower = query.lowercase().trim()

    // 1. "Calculate 15% of 500" or "15% of 500"
    val pctRegex = Regex("""^(?:calculate\s+)?(-?\d+(?:\.\d+)?)\s*%\s*of\s*(-?\d+(?:\.\d+)?)$""")
    pctRegex.matchEntire(lower)?.let { m ->
      val pct = m.groupValues[1].toDoubleOrNull()
      val base = m.groupValues[2].toDoubleOrNull()
      if (pct != null && base != null) {
        val res = (pct / 100.0) * base
        return QuickNaturalResult(
          title = "${NumberFormatter.formatPlain(pct)}% of ${NumberFormatter.formatPlain(base)}",
          answer = NumberFormatter.formatPlain(res, 6),
          subtitle = "Tap to open Percentage Calculator",
          targetRoute = Routes.ART_PERCENTAGE,
        )
      }
    }

    // 2. "Convert 10 kg to pound" or "convert kg to pound"
    val convRegex = Regex("""^(?:convert\s+)?(?:(-?\d+(?:\.\d+)?)\s+)?([a-zA-Z°/²³]+)\s+(?:to|in|into)\s+([a-zA-Z°/²³]+)$""")
    convRegex.matchEntire(lower)?.let { m ->
      val num = m.groupValues[1].toDoubleOrNull() ?: 1.0
      val fromStr = m.groupValues[2]
      val toStr = m.groupValues[3]
      val match = findUnitPair(fromStr, toStr)
      if (match != null) {
        val (cat, fromUnit, toUnit) = match
        val out = UnitConverterEngine.convert(cat, num, fromUnit, toUnit)
        if (out is CalcOutcome.Success) {
          return QuickNaturalResult(
            title = "${NumberFormatter.formatPlain(num)} ${fromUnit.symbol} → ${toUnit.symbol}",
            answer = "${NumberFormatter.formatPlain(out.data, 6)} ${toUnit.symbol}",
            subtitle = "${cat.title} • Tap to open Unit Converter",
            targetRoute = Routes.UNI_UNIT_CONVERTER,
          )
        }
      }
    }

    // 3. Direct math expression if it contains operators or math functions
    if (lower.any { it in listOf('+', '-', '*', '/', '^', '×', '÷', '(') } ||
      lower.startsWith("sin") || lower.startsWith("cos") || lower.startsWith("sqrt") || lower.startsWith("log")
    ) {
      val res = ExpressionParserEngine.evaluate(query, angleMode)
      if (res is CalcOutcome.Success) {
        return QuickNaturalResult(
          title = query,
          answer = NumberFormatter.formatPlain(res.data, 6),
          subtitle = "Evaluated locally (${angleMode.label}) • Open Scientific Calculator",
          targetRoute = Routes.SCI_CALCULATOR,
        )
      }
    }

    return null
  }

  private fun findUnitPair(fromAlias: String, toAlias: String): Triple<UnitCategory, UnitDef, UnitDef>? {
    val aliases = mapOf(
      "mm" to "mm", "millimeter" to "mm", "millimeters" to "mm",
      "cm" to "cm", "centimeter" to "cm", "centimeters" to "cm",
      "m" to "m", "meter" to "m", "meters" to "m",
      "km" to "km", "kilometer" to "km", "kilometers" to "km",
      "in" to "in", "inch" to "in", "inches" to "in",
      "ft" to "ft", "foot" to "ft", "feet" to "ft",
      "yd" to "yd", "yard" to "yd", "yards" to "yd",
      "mi" to "mi", "mile" to "mi", "miles" to "mi",
      "mg" to "mg", "milligram" to "mg",
      "g" to "g", "gram" to "g", "grams" to "g",
      "kg" to "kg", "kilogram" to "kg", "kilograms" to "kg",
      "t" to "t", "tonne" to "t", "tonnes" to "t",
      "oz" to "oz", "ounce" to "oz", "ounces" to "oz",
      "lb" to "lb", "lbs" to "lb", "pound" to "lb", "pounds" to "lb",
      "c" to "C", "celsius" to "C", "°c" to "C",
      "f" to "F", "fahrenheit" to "F", "°f" to "F",
      "k" to "K", "kelvin" to "K",
      "l" to "L", "liter" to "L", "liters" to "L",
      "ml" to "mL", "milliliter" to "mL",
      "gal" to "gal", "gallon" to "gal", "gallons" to "gal",
      "kb" to "KB", "mb" to "MB", "gb" to "GB", "tb" to "TB",
      "kib" to "KiB", "mib" to "MiB", "gib" to "GiB", "tib" to "TiB",
      "mph" to "mph", "kmh" to "kmh", "km/h" to "kmh", "m/s" to "ms",
      "s" to "s", "sec" to "s", "min" to "min", "hr" to "hr", "hour" to "hr", "day" to "day",
    )
    val fId = aliases[fromAlias.lowercase()] ?: return null
    val tId = aliases[toAlias.lowercase()] ?: return null
    for (cat in UnitCategory.entries) {
      val list = UnitConverterEngine.unitsFor(cat)
      val fUnit = list.firstOrNull { it.id == fId }
      val tUnit = list.firstOrNull { it.id == tId }
      if (fUnit != null && tUnit != null) return Triple(cat, fUnit, tUnit)
    }
    return null
  }
}
