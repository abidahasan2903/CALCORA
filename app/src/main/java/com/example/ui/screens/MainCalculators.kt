package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.ExpressionParserEngine
import com.example.engine.NumberFormatter
import com.example.engine.Routes
import com.example.engine.ToolRegistry
import com.example.model.AngleMode
import com.example.model.CalcOutcome
import com.example.model.ToolCategory
import com.example.model.UserPreferences
import com.example.ui.components.CalcKeyButton
import com.example.ui.components.CalcoraTopBar
import com.example.ui.components.KeypadKeyStyle
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalCalcoraPalette
import kotlin.math.abs

// ======================================================
// 1. SCIENCE WORLD — SCIENTIFIC CALCULATOR SCREEN
// ======================================================

@Composable
fun ScientificCalculatorScreen(
  preferences: UserPreferences,
  memoryValue: Double,
  reusedExpression: String?,
  onConsumeReuse: () -> Unit,
  onMemoryChange: (Double) -> Unit,
  onRecordHistory: (expr: String, result: String, calcName: String, cat: String, route: String) -> Unit,
  onNavigateToRoute: (String) -> Unit,
  onBack: () -> Unit,
) {
  var expression by rememberSaveable { mutableStateOf("") }
  var angleMode by rememberSaveable { mutableStateOf(preferences.defaultAngleMode) }
  var isSecond by rememberSaveable { mutableStateOf(false) }
  var isHyp by rememberSaveable { mutableStateOf(false) }
  var errorText by rememberSaveable { mutableStateOf<String?>(null) }
  var finalResultText by rememberSaveable { mutableStateOf("") }

  LaunchedEffect(reusedExpression) {
    if (!reusedExpression.isNullOrBlank()) {
      expression = reusedExpression
      errorText = null
      onConsumeReuse()
    }
  }

  val livePreview = remember(expression, angleMode, preferences) {
    if (expression.isBlank()) ""
    else {
      when (val res = ExpressionParserEngine.evaluate(expression, angleMode)) {
        is CalcOutcome.Success -> NumberFormatter.format(res.data, preferences)
        is CalcOutcome.Error -> ""
      }
    }
  }

  fun appendToken(token: String) {
    errorText = null
    expression += token
  }

  fun toggleSign() {
    errorText = null
    if (expression.isEmpty()) {
      expression = "-"
      return
    }
    // Toggle sign of trailing number or wrap expression
    val regex = Regex("""^(.*?)(-?\d+(?:\.\d+)?)$""")
    val match = regex.matchEntire(expression)
    expression =
      if (match != null) {
        val prefix = match.groupValues[1]
        val num = match.groupValues[2]
        if (num.startsWith("-")) "$prefix${num.substring(1)}" else "$prefix(-$num)"
      } else if (expression.startsWith("-(") && expression.endsWith(")")) {
        expression.substring(2, expression.length - 1)
      } else {
        "-($expression)"
      }
  }

  fun evaluateNow() {
    if (expression.isBlank()) return
    when (val res = ExpressionParserEngine.evaluate(expression, angleMode)) {
      is CalcOutcome.Success -> {
        val formatted = NumberFormatter.format(res.data, preferences)
        val plain = NumberFormatter.formatPlain(res.data, preferences.decimalPrecision)
        finalResultText = formatted
        errorText = null
        onRecordHistory(
          "$expression (${angleMode.label})",
          formatted,
          "Scientific Calculator",
          "Science",
          Routes.SCI_CALCULATOR,
        )
        expression = plain
      }
      is CalcOutcome.Error -> {
        errorText = res.message
      }
    }
  }

  fun currentNumericOrNull(): Double? {
    if (expression.isBlank()) return null
    return (ExpressionParserEngine.evaluate(expression, angleMode) as? CalcOutcome.Success)?.data
  }

  val scienceSubTools = remember {
    ToolRegistry.allTools.filter { it.category == ToolCategory.SCIENCE && it.route != Routes.SCI_CALCULATOR }
  }

  Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
    CalcoraTopBar(
      title = "Science Calculator",
      subtitle = "Scientific Instrument • ${angleMode.label}",
      onBack = onBack,
      actions = {
        Surface(
          onClick = {
            angleMode =
              when (angleMode) {
                AngleMode.DEG -> AngleMode.RAD
                AngleMode.RAD -> AngleMode.GRAD
                AngleMode.GRAD -> AngleMode.DEG
              }
          },
          color = MaterialTheme.colorScheme.primaryContainer,
          shape = CircleShape,
          modifier = Modifier.padding(end = 12.dp).testTag("sci_angle_mode_button"),
        ) {
          Text(
            text = angleMode.label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
          )
        }
      },
    )

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
      Column(
        modifier = Modifier.fillMaxSize().widthIn(max = 640.dp).padding(horizontal = 12.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        // Quick switcher for specialized Science tools
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          items(scienceSubTools, key = { it.id }) { tool ->
            AssistChip(
              onClick = { onNavigateToRoute(tool.route) },
              label = { Text(tool.name.removeSuffix(" Calculator"), maxLines = 1) },
              modifier = Modifier.testTag("sci_chip_${tool.id}"),
            )
          }
        }

        // Instrument LCD Display
        Surface(
          shape = MaterialTheme.shapes.large,
          color = MaterialTheme.colorScheme.surface,
          border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.End,
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = buildString {
                  append(angleMode.label)
                  if (isSecond) append(" • 2nd")
                  if (isHyp) append(" • HYP")
                  if (abs(memoryValue) > 1e-12) append(" • M=${NumberFormatter.formatPlain(memoryValue, 4)}")
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
              )
              if (finalResultText.isNotEmpty()) {
                Text(
                  text = "Ans: $finalResultText",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Box(
              modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
              contentAlignment = Alignment.CenterEnd,
            ) {
              Text(
                text = expression.ifEmpty { "0" },
                style = MaterialTheme.typography.headlineMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                modifier = Modifier.testTag("sci_display_expression"),
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = errorText ?: livePreview.ifEmpty { " " },
              style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMonoFontFamily),
              color = if (errorText != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              textAlign = TextAlign.End,
              modifier = Modifier.fillMaxWidth().testTag("sci_display_result"),
            )
          }
        }

        // Memory + Mode Row
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          CalcKeyButton("2nd", { isSecond = !isSecond }, Modifier.weight(1f), KeypadKeyStyle.FUNCTION, isToggled = isSecond, height = 40.dp, testTag = "sci_key_2nd")
          CalcKeyButton("HYP", { isHyp = !isHyp }, Modifier.weight(1f), KeypadKeyStyle.FUNCTION, isToggled = isHyp, height = 40.dp, testTag = "sci_key_hyp")
          CalcKeyButton("MC", { onMemoryChange(0.0) }, Modifier.weight(1f), KeypadKeyStyle.FUNCTION, height = 40.dp, testTag = "sci_key_mc")
          CalcKeyButton("MR", { appendToken(NumberFormatter.formatPlain(memoryValue, preferences.decimalPrecision)) }, Modifier.weight(1f), KeypadKeyStyle.FUNCTION, height = 40.dp, testTag = "sci_key_mr")
          CalcKeyButton("M+", { currentNumericOrNull()?.let { onMemoryChange(memoryValue + it) } }, Modifier.weight(1f), KeypadKeyStyle.FUNCTION, height = 40.dp, testTag = "sci_key_mplus")
          CalcKeyButton("M-", { currentNumericOrNull()?.let { onMemoryChange(memoryValue - it) } }, Modifier.weight(1f), KeypadKeyStyle.FUNCTION, height = 40.dp, testTag = "sci_key_mminus")
          CalcKeyButton("MS", { currentNumericOrNull()?.let { onMemoryChange(it) } }, Modifier.weight(1f), KeypadKeyStyle.FUNCTION, height = 40.dp, testTag = "sci_key_ms")
        }

        // Scientific Function Rows
        val trigSin = when {
          isHyp && isSecond -> "asinh(" to "sinh⁻¹"
          isHyp -> "sinh(" to "sinh"
          isSecond -> "asin(" to "sin⁻¹"
          else -> "sin(" to "sin"
        }
        val trigCos = when {
          isHyp && isSecond -> "acosh(" to "cosh⁻¹"
          isHyp -> "cosh(" to "cosh"
          isSecond -> "acos(" to "cos⁻¹"
          else -> "cos(" to "cos"
        }
        val trigTan = when {
          isHyp && isSecond -> "atanh(" to "tanh⁻¹"
          isHyp -> "tanh(" to "tanh"
          isSecond -> "atan(" to "tan⁻¹"
          else -> "tan(" to "tan"
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            CalcKeyButton(trigSin.second, { appendToken(trigSin.first) }, Modifier.weight(1f), KeypadKeyStyle.FUNCTION)
            CalcKeyButton(trigCos.second, { appendToken(trigCos.first) }, Modifier.weight(1f), KeypadKeyStyle.FUNCTION)
            CalcKeyButton(trigTan.second, { appendToken(trigTan.first) }, Modifier.weight(1f), KeypadKeyStyle.FUNCTION)
            CalcKeyButton(if (isSecond) "10^" else "log", { appendToken(if (isSecond) "10^(" else "log(") }, Modifier.weight(1f), KeypadKeyStyle.FUNCTION)
            CalcKeyButton(if (isSecond) "e^" else "ln", { appendToken(if (isSecond) "exp(" else "ln(") }, Modifier.weight(1f), KeypadKeyStyle.FUNCTION)
          }
          Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            CalcKeyButton(if (isSecond) "x³" else "x²", { appendToken(if (isSecond) "^3" else "^2") }, Modifier.weight(1f), KeypadKeyStyle.FUNCTION)
            CalcKeyButton(if (isSecond) "∛" else "√", { appendToken(if (isSecond) "cbrt(" else "sqrt(") }, Modifier.weight(1f), KeypadKeyStyle.FUNCTION)
            CalcKeyButton(if (isSecond) "ⁿ√" else "xʸ", { appendToken(if (isSecond) "root(" else "^") }, Modifier.weight(1f), KeypadKeyStyle.FUNCTION)
            CalcKeyButton("1/x", { appendToken("inv(") }, Modifier.weight(1f), KeypadKeyStyle.FUNCTION)
            CalcKeyButton(if (isSecond) "logₙ" else "n!", { appendToken(if (isSecond) "logn(" else "!") }, Modifier.weight(1f), KeypadKeyStyle.FUNCTION)
          }
          Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            CalcKeyButton("π", { appendToken("π") }, Modifier.weight(1f), KeypadKeyStyle.FUNCTION)
            CalcKeyButton("e", { appendToken("e") }, Modifier.weight(1f), KeypadKeyStyle.FUNCTION)
            CalcKeyButton("|x|", { appendToken("abs(") }, Modifier.weight(1f), KeypadKeyStyle.FUNCTION)
            CalcKeyButton("mod", { appendToken(" mod ") }, Modifier.weight(1f), KeypadKeyStyle.FUNCTION)
            CalcKeyButton(",", { appendToken(",") }, Modifier.weight(1f), KeypadKeyStyle.FUNCTION)
          }
          Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            CalcKeyButton("AC", { expression = ""; errorText = null; finalResultText = "" }, Modifier.weight(1f), KeypadKeyStyle.CLEAR, testTag = "sci_key_ac")
            CalcKeyButton("(", { appendToken("(") }, Modifier.weight(1f), KeypadKeyStyle.FUNCTION)
            CalcKeyButton(")", { appendToken(")") }, Modifier.weight(1f), KeypadKeyStyle.FUNCTION)
            CalcKeyButton("%", { appendToken("%") }, Modifier.weight(1f), KeypadKeyStyle.FUNCTION)
            CalcKeyButton("÷", { appendToken("÷") }, Modifier.weight(1f), KeypadKeyStyle.OPERATOR)
          }
          Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            CalcKeyButton("7", { appendToken("7") }, Modifier.weight(1f), KeypadKeyStyle.NUMERIC, testTag = "sci_key_7")
            CalcKeyButton("8", { appendToken("8") }, Modifier.weight(1f), KeypadKeyStyle.NUMERIC, testTag = "sci_key_8")
            CalcKeyButton("9", { appendToken("9") }, Modifier.weight(1f), KeypadKeyStyle.NUMERIC, testTag = "sci_key_9")
            CalcKeyButton("⌫", { if (expression.isNotEmpty()) expression = expression.dropLast(1); errorText = null }, Modifier.weight(1f), KeypadKeyStyle.CLEAR)
            CalcKeyButton("×", { appendToken("×") }, Modifier.weight(1f), KeypadKeyStyle.OPERATOR)
          }
          Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            CalcKeyButton("4", { appendToken("4") }, Modifier.weight(1f), KeypadKeyStyle.NUMERIC, testTag = "sci_key_4")
            CalcKeyButton("5", { appendToken("5") }, Modifier.weight(1f), KeypadKeyStyle.NUMERIC, testTag = "sci_key_5")
            CalcKeyButton("6", { appendToken("6") }, Modifier.weight(1f), KeypadKeyStyle.NUMERIC, testTag = "sci_key_6")
            CalcKeyButton("+/-", { toggleSign() }, Modifier.weight(1f), KeypadKeyStyle.FUNCTION)
            CalcKeyButton("−", { appendToken("-") }, Modifier.weight(1f), KeypadKeyStyle.OPERATOR)
          }
          Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            CalcKeyButton("1", { appendToken("1") }, Modifier.weight(1f), KeypadKeyStyle.NUMERIC, testTag = "sci_key_1")
            CalcKeyButton("2", { appendToken("2") }, Modifier.weight(1f), KeypadKeyStyle.NUMERIC, testTag = "sci_key_2")
            CalcKeyButton("3", { appendToken("3") }, Modifier.weight(1f), KeypadKeyStyle.NUMERIC, testTag = "sci_key_3")
            CalcKeyButton(".", { appendToken(".") }, Modifier.weight(1f), KeypadKeyStyle.NUMERIC)
            CalcKeyButton("+", { appendToken("+") }, Modifier.weight(1f), KeypadKeyStyle.OPERATOR)
          }
          Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            CalcKeyButton("0", { appendToken("0") }, Modifier.weight(2f), KeypadKeyStyle.NUMERIC, testTag = "sci_key_0")
            CalcKeyButton("00", { appendToken("00") }, Modifier.weight(1f), KeypadKeyStyle.NUMERIC)
            CalcKeyButton("=", { evaluateNow() }, Modifier.weight(2f), KeypadKeyStyle.ACCENT, testTag = "sci_key_equals")
          }
        }
      }
    }
  }
}

// ======================================================
// 2. COMMERCE WORLD — FINANCIAL CALCULATOR SCREEN
// ======================================================

@Composable
fun CommerceCalculatorScreen(
  preferences: UserPreferences,
  memoryValue: Double,
  reusedExpression: String?,
  onConsumeReuse: () -> Unit,
  onMemoryChange: (Double) -> Unit,
  onRecordHistory: (expr: String, result: String, calcName: String, cat: String, route: String) -> Unit,
  onNavigateToRoute: (String) -> Unit,
  onBack: () -> Unit,
) {
  var expression by rememberSaveable { mutableStateOf("") }
  var ratePercentText by rememberSaveable { mutableStateOf("15") }
  var costInputText by rememberSaveable { mutableStateOf("") }
  var showRateDialog by remember { mutableStateOf(false) }
  var statusBanner by rememberSaveable { mutableStateOf("") }
  var errorText by rememberSaveable { mutableStateOf<String?>(null) }

  LaunchedEffect(reusedExpression) {
    if (!reusedExpression.isNullOrBlank()) {
      expression = reusedExpression
      errorText = null
      onConsumeReuse()
    }
  }

  val liveValue = remember(expression) {
    if (expression.isBlank()) null
    else (ExpressionParserEngine.evaluate(expression, AngleMode.DEG) as? CalcOutcome.Success)?.data
  }

  val commerceTools = remember {
    ToolRegistry.allTools.filter { it.category == ToolCategory.COMMERCE && it.route != Routes.COM_CALCULATOR }
  }

  fun append(tok: String) {
    errorText = null
    statusBanner = ""
    expression += tok
  }

  fun evaluateEquals() {
    if (expression.isBlank()) return
    when (val res = ExpressionParserEngine.evaluate(expression, AngleMode.DEG)) {
      is CalcOutcome.Success -> {
        val formatted = NumberFormatter.formatCurrency(res.data, preferences.decimalPrecision.coerceAtMost(4), preferences.useThousandsSeparator)
        onRecordHistory(expression, formatted, "Financial Calculator", "Commerce", Routes.COM_CALCULATOR)
        expression = NumberFormatter.formatPlain(res.data, preferences.decimalPrecision)
        statusBanner = "Result: $formatted"
        errorText = null
      }
      is CalcOutcome.Error -> errorText = res.message
    }
  }

  fun applyQuickFinanceOp(op: String) {
    val amt = liveValue ?: run {
      errorText = "Enter an amount first"
      return
    }
    val rate = ratePercentText.toDoubleOrNull() ?: run {
      errorText = "Set a valid Rate% first"
      return
    }
    if (rate < 0) {
      errorText = "Rate% cannot be negative"
      return
    }
    val (resVal, label) =
      when (op) {
        "VAT+" -> (amt * (1.0 + rate / 100.0)) to "Add $rate% VAT/Tax to ${NumberFormatter.formatCurrency(amt, 2, true)}"
        "VAT-" -> (amt / (1.0 + rate / 100.0)) to "Excl. $rate% VAT/Tax from ${NumberFormatter.formatCurrency(amt, 2, true)}"
        "DISC" -> (amt * (1.0 - rate / 100.0)) to "Apply $rate% Discount to ${NumberFormatter.formatCurrency(amt, 2, true)}"
        "MARKUP" -> (amt * (1.0 + rate / 100.0)) to "Add $rate% Markup to cost ${NumberFormatter.formatCurrency(amt, 2, true)}"
        "COMM" -> (amt * (rate / 100.0)) to "$rate% Commission on ${NumberFormatter.formatCurrency(amt, 2, true)}"
        "MARGIN" -> {
          if (rate >= 100.0) {
            errorText = "Target Margin% must be < 100%"
            return
          }
          (amt / (1.0 - rate / 100.0)) to "Price for $rate% Margin on cost ${NumberFormatter.formatCurrency(amt, 2, true)}"
        }
        else -> return
      }
    val formatted = NumberFormatter.formatCurrency(resVal, 2, preferences.useThousandsSeparator)
    onRecordHistory(label, formatted, "Financial Calculator ($op)", "Commerce", Routes.COM_CALCULATOR)
    expression = NumberFormatter.formatPlain(resVal, 4)
    statusBanner = "$label = $formatted"
    errorText = null
  }

  Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
    CalcoraTopBar(
      title = "Commerce Calculator",
      subtitle = "Financial Keypad & Tools",
      onBack = onBack,
      actions = {
        AssistChip(
          onClick = { showRateDialog = true },
          label = { Text("Rate: $ratePercentText%") },
          modifier = Modifier.padding(end = 10.dp).testTag("com_rate_chip"),
        )
      },
    )

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
      Column(
        modifier = Modifier.fillMaxSize().widthIn(max = 640.dp).padding(horizontal = 12.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        // Dedicated Commerce Tools Strip
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          items(commerceTools, key = { it.id }) { tool ->
            AssistChip(
              onClick = { onNavigateToRoute(tool.route) },
              label = { Text(tool.name.removeSuffix(" Calculator"), maxLines = 1) },
              modifier = Modifier.testTag("com_chip_${tool.id}"),
            )
          }
        }

        // Financial Display
        Surface(
          shape = MaterialTheme.shapes.large,
          color = MaterialTheme.colorScheme.surface,
          border = BorderStroke(1.5.dp, LocalCalcoraPalette.current.commerceAccent.copy(alpha = 0.45f)),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.End,
          ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text(
                text = "FINANCIAL • RATE $ratePercentText%" + if (abs(memoryValue) > 1e-12) " • M=${NumberFormatter.formatPlain(memoryValue, 2)}" else "",
                style = MaterialTheme.typography.labelSmall,
                color = LocalCalcoraPalette.current.commerceAccent,
              )
              Text(
                text = if (costInputText.isNotBlank()) "Cost: $costInputText" else "",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box(
              modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
              contentAlignment = Alignment.CenterEnd,
            ) {
              Text(
                text = expression.ifEmpty { "0.00" },
                style = MaterialTheme.typography.displayMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                modifier = Modifier.testTag("com_display_expression"),
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = errorText ?: statusBanner.ifEmpty {
                liveValue?.let { NumberFormatter.formatCurrency(it, 2, preferences.useThousandsSeparator) } ?: "Ready"
              },
              style = MaterialTheme.typography.bodyLarge.copy(fontFamily = JetBrainsMonoFontFamily),
              color = if (errorText != null) MaterialTheme.colorScheme.error else LocalCalcoraPalette.current.commerceAccent,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.testTag("com_display_result"),
            )
          }
        }

        // Dedicated Financial Quick Keys
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          CalcKeyButton("COST", {
            liveValue?.let {
              costInputText = NumberFormatter.formatPlain(it, 4)
              statusBanner = "Cost set to $costInputText — enter Selling Price & tap Profit%"
            } ?: run { errorText = "Enter Cost amount first" }
          }, Modifier.weight(1f), KeypadKeyStyle.FUNCTION, height = 42.dp)
          CalcKeyButton("Profit%", {
            val sp = liveValue
            val cp = costInputText.toDoubleOrNull()
            if (sp == null || cp == null || cp <= 0.0) {
              errorText = "Set COST first, then enter Selling Price"
            } else {
              val pct = ((sp - cp) / cp) * 100.0
              val pStr = "${NumberFormatter.formatPlain(pct, 2)}%"
              statusBanner = if (pct >= 0) "Profit: $pStr (Amt: ${NumberFormatter.formatCurrency(sp - cp, 2, true)})"
              else "Loss: ${NumberFormatter.formatPlain(-pct, 2)}% (Amt: ${NumberFormatter.formatCurrency(cp - sp, 2, true)})"
              onRecordHistory("CP=$cp, SP=$sp", statusBanner, "Quick Profit/Loss", "Commerce", Routes.COM_CALCULATOR)
            }
          }, Modifier.weight(1f), KeypadKeyStyle.FUNCTION, height = 42.dp)
          CalcKeyButton("Margin%", { applyQuickFinanceOp("MARGIN") }, Modifier.weight(1f), KeypadKeyStyle.FUNCTION, height = 42.dp)
          CalcKeyButton("Disc%", { applyQuickFinanceOp("DISC") }, Modifier.weight(1f), KeypadKeyStyle.FUNCTION, height = 42.dp)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          CalcKeyButton("Markup", { applyQuickFinanceOp("MARKUP") }, Modifier.weight(1f), KeypadKeyStyle.FUNCTION, height = 42.dp)
          CalcKeyButton("VAT+", { applyQuickFinanceOp("VAT+") }, Modifier.weight(1f), KeypadKeyStyle.FUNCTION, height = 42.dp)
          CalcKeyButton("VAT-", { applyQuickFinanceOp("VAT-") }, Modifier.weight(1f), KeypadKeyStyle.FUNCTION, height = 42.dp)
          CalcKeyButton("Comm", { applyQuickFinanceOp("COMM") }, Modifier.weight(1f), KeypadKeyStyle.FUNCTION, height = 42.dp)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          CalcKeyButton("MC", { onMemoryChange(0.0) }, Modifier.weight(1f), KeypadKeyStyle.FUNCTION, height = 40.dp)
          CalcKeyButton("MR", { append(NumberFormatter.formatPlain(memoryValue, 2)) }, Modifier.weight(1f), KeypadKeyStyle.FUNCTION, height = 40.dp)
          CalcKeyButton("M+", { liveValue?.let { onMemoryChange(memoryValue + it) } }, Modifier.weight(1f), KeypadKeyStyle.FUNCTION, height = 40.dp)
          CalcKeyButton("M-", { liveValue?.let { onMemoryChange(memoryValue - it) } }, Modifier.weight(1f), KeypadKeyStyle.FUNCTION, height = 40.dp)
          CalcKeyButton("Set %", { showRateDialog = true }, Modifier.weight(1f), KeypadKeyStyle.OPERATOR, height = 40.dp)
        }

        // Standard Financial Keypad
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            CalcKeyButton("AC", { expression = ""; errorText = null; statusBanner = "" }, Modifier.weight(1f), KeypadKeyStyle.CLEAR)
            CalcKeyButton("⌫", { if (expression.isNotEmpty()) expression = expression.dropLast(1); errorText = null }, Modifier.weight(1f), KeypadKeyStyle.CLEAR)
            CalcKeyButton("%", { append("%") }, Modifier.weight(1f), KeypadKeyStyle.FUNCTION)
            CalcKeyButton("÷", { append("÷") }, Modifier.weight(1f), KeypadKeyStyle.OPERATOR)
          }
          Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            CalcKeyButton("7", { append("7") }, Modifier.weight(1f))
            CalcKeyButton("8", { append("8") }, Modifier.weight(1f))
            CalcKeyButton("9", { append("9") }, Modifier.weight(1f))
            CalcKeyButton("×", { append("×") }, Modifier.weight(1f), KeypadKeyStyle.OPERATOR)
          }
          Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            CalcKeyButton("4", { append("4") }, Modifier.weight(1f))
            CalcKeyButton("5", { append("5") }, Modifier.weight(1f))
            CalcKeyButton("6", { append("6") }, Modifier.weight(1f))
            CalcKeyButton("−", { append("-") }, Modifier.weight(1f), KeypadKeyStyle.OPERATOR)
          }
          Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            CalcKeyButton("1", { append("1") }, Modifier.weight(1f))
            CalcKeyButton("2", { append("2") }, Modifier.weight(1f))
            CalcKeyButton("3", { append("3") }, Modifier.weight(1f))
            CalcKeyButton("+", { append("+") }, Modifier.weight(1f), KeypadKeyStyle.OPERATOR)
          }
          Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            CalcKeyButton("0", { append("0") }, Modifier.weight(1f))
            CalcKeyButton("00", { append("00") }, Modifier.weight(1f))
            CalcKeyButton(".", { append(".") }, Modifier.weight(1f))
            CalcKeyButton("=", { evaluateEquals() }, Modifier.weight(1f), KeypadKeyStyle.ACCENT, testTag = "com_key_equals")
          }
        }
      }
    }
  }

  if (showRateDialog) {
    var draftRate by remember { mutableStateOf(ratePercentText) }
    AlertDialog(
      onDismissRequest = { showRateDialog = false },
      title = { Text("Set Financial Rate (%)") },
      text = {
        OutlinedTextField(
          value = draftRate,
          onValueChange = { draftRate = it },
          label = { Text("Tax / VAT / Discount / Markup / Margin %") },
          singleLine = true,
        )
      },
      confirmButton = {
        TextButton(onClick = {
          if (draftRate.toDoubleOrNull() != null && draftRate.toDouble() >= 0) {
            ratePercentText = draftRate
          }
          showRateDialog = false
        }) {
          Text("Apply")
        }
      },
      dismissButton = {
        TextButton(onClick = { showRateDialog = false }) { Text("Cancel") }
      },
    )
  }
}

// ======================================================
// 3. ARTS WORLD — GENERAL CALCULATOR SCREEN
// ======================================================

@Composable
fun ArtsCalculatorScreen(
  preferences: UserPreferences,
  memoryValue: Double,
  reusedExpression: String?,
  onConsumeReuse: () -> Unit,
  onMemoryChange: (Double) -> Unit,
  onRecordHistory: (expr: String, result: String, calcName: String, cat: String, route: String) -> Unit,
  onNavigateToRoute: (String) -> Unit,
  onBack: () -> Unit,
) {
  var expression by rememberSaveable { mutableStateOf("") }
  var errorText by rememberSaveable { mutableStateOf<String?>(null) }

  LaunchedEffect(reusedExpression) {
    if (!reusedExpression.isNullOrBlank()) {
      expression = reusedExpression
      errorText = null
      onConsumeReuse()
    }
  }

  val liveResult = remember(expression, preferences) {
    if (expression.isBlank()) ""
    else (ExpressionParserEngine.evaluate(expression, AngleMode.DEG) as? CalcOutcome.Success)?.let {
      NumberFormatter.format(it.data, preferences)
    } ?: ""
  }

  val artsTools = remember {
    ToolRegistry.allTools.filter {
      (it.category == ToolCategory.ARTS && it.route != Routes.ART_CALCULATOR) ||
        it.category == ToolCategory.STUDENT
    }
  }

  fun append(tok: String) {
    errorText = null
    expression += tok
  }

  fun toggleSign() {
    errorText = null
    if (expression.isEmpty()) {
      expression = "-"
      return
    }
    val regex = Regex("""^(.*?)(-?\d+(?:\.\d+)?)$""")
    val m = regex.matchEntire(expression)
    expression =
      if (m != null) {
        val pre = m.groupValues[1]
        val num = m.groupValues[2]
        if (num.startsWith("-")) "$pre${num.substring(1)}" else "$pre(-$num)"
      } else {
        "-($expression)"
      }
  }

  fun evaluateEquals() {
    if (expression.isBlank()) return
    when (val res = ExpressionParserEngine.evaluate(expression, AngleMode.DEG)) {
      is CalcOutcome.Success -> {
        val formatted = NumberFormatter.format(res.data, preferences)
        onRecordHistory(expression, formatted, "General Calculator", "Arts", Routes.ART_CALCULATOR)
        expression = NumberFormatter.formatPlain(res.data, preferences.decimalPrecision)
        errorText = null
      }
      is CalcOutcome.Error -> errorText = res.message
    }
  }

  Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
    CalcoraTopBar(
      title = "Arts & General Calculator",
      subtitle = "Clean Arithmetic & Academic Tools",
      onBack = onBack,
    )

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
      Column(
        modifier = Modifier.fillMaxSize().widthIn(max = 640.dp).padding(horizontal = 12.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          items(artsTools, key = { it.id }) { tool ->
            AssistChip(
              onClick = { onNavigateToRoute(tool.route) },
              label = { Text(tool.name.removeSuffix(" Calculator"), maxLines = 1) },
              modifier = Modifier.testTag("art_chip_${tool.id}"),
            )
          }
        }

        // Clean Display
        Surface(
          shape = MaterialTheme.shapes.large,
          color = MaterialTheme.colorScheme.surface,
          border = BorderStroke(1.5.dp, LocalCalcoraPalette.current.artsAccent.copy(alpha = 0.4f)),
          modifier = Modifier.fillMaxWidth().weight(0.32f),
        ) {
          Column(
            modifier = Modifier.fillMaxSize().padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.End,
          ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text(
                text = if (abs(memoryValue) > 1e-12) "M = ${NumberFormatter.formatPlain(memoryValue, 4)}" else "GENERAL",
                style = MaterialTheme.typography.labelSmall,
                color = LocalCalcoraPalette.current.artsAccent,
              )
              Text(
                text = "Parentheses & Precedence",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
            Box(
              modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
              contentAlignment = Alignment.CenterEnd,
            ) {
              Text(
                text = expression.ifEmpty { "0" },
                style = MaterialTheme.typography.displayLarge.copy(fontFamily = JetBrainsMonoFontFamily),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                modifier = Modifier.testTag("art_display_expression"),
              )
            }
            Text(
              text = errorText ?: liveResult.ifEmpty { " " },
              style = MaterialTheme.typography.titleLarge.copy(fontFamily = JetBrainsMonoFontFamily),
              color = if (errorText != null) MaterialTheme.colorScheme.error else LocalCalcoraPalette.current.artsAccent,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.testTag("art_display_result"),
            )
          }
        }

        // Memory + Parentheses Strip
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          CalcKeyButton("MC", { onMemoryChange(0.0) }, Modifier.weight(1f), KeypadKeyStyle.FUNCTION, height = 42.dp)
          CalcKeyButton("MR", { append(NumberFormatter.formatPlain(memoryValue, 4)) }, Modifier.weight(1f), KeypadKeyStyle.FUNCTION, height = 42.dp)
          CalcKeyButton("M+", {
            (ExpressionParserEngine.evaluate(expression, AngleMode.DEG) as? CalcOutcome.Success)?.data?.let {
              onMemoryChange(memoryValue + it)
            }
          }, Modifier.weight(1f), KeypadKeyStyle.FUNCTION, height = 42.dp)
          CalcKeyButton("(", { append("(") }, Modifier.weight(1f), KeypadKeyStyle.FUNCTION, height = 42.dp)
          CalcKeyButton(")", { append(")") }, Modifier.weight(1f), KeypadKeyStyle.FUNCTION, height = 42.dp)
        }

        // Main Practical Keypad
        Column(modifier = Modifier.weight(0.68f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CalcKeyButton("AC", { expression = ""; errorText = null }, Modifier.weight(1f), KeypadKeyStyle.CLEAR, testTag = "art_key_ac")
            CalcKeyButton("⌫", { if (expression.isNotEmpty()) expression = expression.dropLast(1); errorText = null }, Modifier.weight(1f), KeypadKeyStyle.CLEAR)
            CalcKeyButton("%", { append("%") }, Modifier.weight(1f), KeypadKeyStyle.FUNCTION)
            CalcKeyButton("÷", { append("÷") }, Modifier.weight(1f), KeypadKeyStyle.OPERATOR)
          }
          Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CalcKeyButton("7", { append("7") }, Modifier.weight(1f))
            CalcKeyButton("8", { append("8") }, Modifier.weight(1f))
            CalcKeyButton("9", { append("9") }, Modifier.weight(1f))
            CalcKeyButton("×", { append("×") }, Modifier.weight(1f), KeypadKeyStyle.OPERATOR)
          }
          Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CalcKeyButton("4", { append("4") }, Modifier.weight(1f))
            CalcKeyButton("5", { append("5") }, Modifier.weight(1f))
            CalcKeyButton("6", { append("6") }, Modifier.weight(1f))
            CalcKeyButton("−", { append("-") }, Modifier.weight(1f), KeypadKeyStyle.OPERATOR)
          }
          Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CalcKeyButton("1", { append("1") }, Modifier.weight(1f))
            CalcKeyButton("2", { append("2") }, Modifier.weight(1f))
            CalcKeyButton("3", { append("3") }, Modifier.weight(1f))
            CalcKeyButton("+", { append("+") }, Modifier.weight(1f), KeypadKeyStyle.OPERATOR)
          }
          Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CalcKeyButton("+/-", { toggleSign() }, Modifier.weight(1f), KeypadKeyStyle.FUNCTION)
            CalcKeyButton("0", { append("0") }, Modifier.weight(1f))
            CalcKeyButton(".", { append(".") }, Modifier.weight(1f))
            CalcKeyButton("=", { evaluateEquals() }, Modifier.weight(1f), KeypadKeyStyle.ACCENT, testTag = "art_key_equals")
          }
        }
      }
    }
  }
}

// ======================================================
// 4. UNIVERSAL WORLD HUB
// ======================================================

@Composable
fun UniversalHubScreen(
  onNavigateToRoute: (String) -> Unit,
  onBack: () -> Unit,
) {
  val universalTools = remember {
    ToolRegistry.allTools.filter { it.category == ToolCategory.UNIVERSAL || it.category == ToolCategory.STUDENT }
  }
  val accent = LocalCalcoraPalette.current.universalAccent

  Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
    CalcoraTopBar(
      title = "Universal World",
      subtitle = "Everyday & Student Calculators",
      onBack = onBack,
    )
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
      LazyColumn(
        modifier = Modifier.fillMaxSize().widthIn(max = 720.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        items(universalTools, key = { it.id }) { tool ->
          Card(
            onClick = { onNavigateToRoute(tool.route) },
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, accent.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth().testTag("uni_tool_${tool.id}"),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth().padding(14.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              if (tool.icon != null) {
                Icon(tool.icon, contentDescription = tool.name, tint = accent, modifier = Modifier.size(28.dp))
              }
              Column(modifier = Modifier.weight(1f)) {
                Text(tool.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(tool.shortDescription, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
              Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = accent)
            }
          }
        }
      }
    }
  }
}
