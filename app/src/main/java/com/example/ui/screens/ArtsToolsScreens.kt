package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.engine.NumberFormatter
import com.example.engine.PercentageEngine
import com.example.engine.PercentageMode
import com.example.engine.RatioProportionEngine
import com.example.engine.Routes
import com.example.engine.StatisticsEngine
import com.example.model.CalcOutcome
import com.example.model.StepBreakdown
import com.example.ui.components.ActionButtonRow
import com.example.ui.components.CalcoraNumberField
import com.example.ui.components.CalcoraSectionCard
import com.example.ui.components.CalcoraTopBar
import com.example.ui.components.StepBreakdownCard
import com.example.ui.components.ValidationErrorCard

// ======================================================
// 1. PERCENTAGE CALCULATOR SCREEN
// ======================================================

@Composable
fun PercentageCalculatorScreen(
  showBreakdown: Boolean,
  onRecordHistory: (String, String, String, String, String) -> Unit,
  onBack: () -> Unit,
) {
  var mode by rememberSaveable { mutableStateOf(PercentageMode.PERCENT_OF) }
  var x by rememberSaveable { mutableStateOf("15") }
  var y by rememberSaveable { mutableStateOf("500") }
  var errorMsg by remember { mutableStateOf<String?>(null) }
  var breakdown by remember { mutableStateOf<StepBreakdown?>(null) }

  val labels = when (mode) {
    PercentageMode.PERCENT_OF -> "Percentage X (%)" to "Base Value Y"
    PercentageMode.WHAT_PERCENT -> "Part Value X" to "Whole Value Y"
    PercentageMode.PERCENT_INCREASE -> "Initial Value X" to "New Value Y"
    PercentageMode.PERCENT_DECREASE -> "Initial Value X" to "New Value Y"
    PercentageMode.PERCENT_DIFFERENCE -> "Value X" to "Value Y"
  }

  Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
    CalcoraTopBar(title = "Percentage Calculator", subtitle = "5 Essential Percentage Modes", onBack = onBack)
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
      LazyColumn(
        modifier = Modifier.fillMaxSize().widthIn(max = 720.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        item {
          LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(PercentageMode.entries) { m ->
              FilterChip(
                selected = mode == m,
                onClick = { mode = m; errorMsg = null; breakdown = null },
                label = { Text(m.title) },
              )
            }
          }
        }
        item {
          CalcoraSectionCard {
            CalcoraNumberField(x, { x = it }, labels.first, testTag = "pct_input_x")
            CalcoraNumberField(y, { y = it }, labels.second, testTag = "pct_input_y")
            ActionButtonRow(
              primaryLabel = "Calculate",
              onPrimaryClick = {
                val xv = x.toDoubleOrNull(); val yv = y.toDoubleOrNull()
                if (xv == null || yv == null) {
                  errorMsg = "Enter valid numbers for both fields"; breakdown = null
                } else {
                  when (val out = PercentageEngine.calculate(mode, xv, yv)) {
                    is CalcOutcome.Success -> {
                      errorMsg = null; breakdown = out.breakdown
                      out.breakdown?.let { onRecordHistory(it.inputValues, it.finalAnswer, "Percentage (${mode.title})", "Arts", Routes.ART_PERCENTAGE) }
                    }
                    is CalcOutcome.Error -> { errorMsg = out.message; breakdown = null }
                  }
                }
              },
              onResetClick = { x = ""; y = ""; errorMsg = null; breakdown = null },
            )
          }
        }
        errorMsg?.let { item { ValidationErrorCard(it) } }
        breakdown?.let { item { StepBreakdownCard(it, showDetails = showBreakdown) } }
      }
    }
  }
}

// ======================================================
// 2. AVERAGE CALCULATOR SCREEN
// ======================================================

@Composable
fun AverageCalculatorScreen(
  showBreakdown: Boolean,
  onRecordHistory: (String, String, String, String, String) -> Unit,
  onBack: () -> Unit,
) {
  var valuesText by rememberSaveable { mutableStateOf("84, 90, 76, 88, 92") }
  var errorMsg by remember { mutableStateOf<String?>(null) }
  var breakdown by remember { mutableStateOf<StepBreakdown?>(null) }

  Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
    CalcoraTopBar(title = "Average Calculator", subtitle = "Arithmetic Mean, Sum & Count", onBack = onBack)
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
      LazyColumn(
        modifier = Modifier.fillMaxSize().widthIn(max = 720.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        item {
          CalcoraSectionCard {
            OutlinedTextField(
              value = valuesText,
              onValueChange = { valuesText = it },
              label = { Text("Enter numbers separated by commas or spaces") },
              minLines = 2,
              maxLines = 4,
              modifier = Modifier.fillMaxWidth().testTag("avg_input"),
            )
            ActionButtonRow(
              primaryLabel = "Calculate Average",
              onPrimaryClick = {
                val nums = StatisticsEngine.parseNumbers(valuesText)
                if (nums == null) {
                  errorMsg = "Enter at least one valid number"; breakdown = null
                } else {
                  val sum = nums.sum()
                  val avg = sum / nums.size
                  val avgStr = NumberFormatter.formatPlain(avg, 6)
                  val bd = StepBreakdown(
                    formula = "Arithmetic Mean = Sum / Count",
                    inputValues = "${nums.size} numbers",
                    substitution = "${NumberFormatter.formatPlain(sum, 4)} / ${nums.size}",
                    calculationSteps = listOf(
                      "Count = ${nums.size}",
                      "Sum = ${NumberFormatter.formatPlain(sum, 4)}",
                      "Average = $avgStr",
                    ),
                    finalAnswer = avgStr,
                  )
                  errorMsg = null
                  breakdown = bd
                  onRecordHistory("Avg of ${nums.size} items", avgStr, "Average Calculator", "Arts", Routes.ART_AVERAGE)
                }
              },
              onResetClick = { valuesText = ""; errorMsg = null; breakdown = null },
            )
          }
        }
        errorMsg?.let { item { ValidationErrorCard(it) } }
        breakdown?.let { item { StepBreakdownCard(it, showDetails = showBreakdown) } }
      }
    }
  }
}

// ======================================================
// 3. RATIO & PROPORTION SCREEN
// ======================================================

private enum class RatioMode(val label: String) {
  SIMPLIFY("Simplify A:B"),
  COMPARE("Compare A:B & C:D"),
  PROPORTION("Solve A:B = C:x"),
  DIVIDE("Divide Amount"),
}

@Composable
fun RatioProportionScreen(
  showBreakdown: Boolean,
  onRecordHistory: (String, String, String, String, String) -> Unit,
  onBack: () -> Unit,
) {
  var mode by rememberSaveable { mutableStateOf(RatioMode.SIMPLIFY) }
  var a by rememberSaveable { mutableStateOf("24") }
  var b by rememberSaveable { mutableStateOf("36") }
  var c by rememberSaveable { mutableStateOf("4") }
  var d by rememberSaveable { mutableStateOf("6") }
  var amount by rememberSaveable { mutableStateOf("1000") }
  var ratioList by rememberSaveable { mutableStateOf("2, 3, 5") }
  var errorMsg by remember { mutableStateOf<String?>(null) }
  var breakdown by remember { mutableStateOf<StepBreakdown?>(null) }

  Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
    CalcoraTopBar(title = "Ratio & Proportion", subtitle = "Simplify, Compare, Solve & Split", onBack = onBack)
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
      LazyColumn(
        modifier = Modifier.fillMaxSize().widthIn(max = 720.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        item {
          LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(RatioMode.entries) { m ->
              FilterChip(selected = mode == m, onClick = { mode = m; errorMsg = null; breakdown = null }, label = { Text(m.label) })
            }
          }
        }
        item {
          CalcoraSectionCard {
            when (mode) {
              RatioMode.SIMPLIFY -> {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  CalcoraNumberField(a, { a = it }, "Term A", Modifier.weight(1f), allowDecimal = false)
                  CalcoraNumberField(b, { b = it }, "Term B", Modifier.weight(1f), allowDecimal = false)
                }
              }
              RatioMode.COMPARE -> {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  CalcoraNumberField(a, { a = it }, "A", Modifier.weight(1f))
                  CalcoraNumberField(b, { b = it }, "B", Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  CalcoraNumberField(c, { c = it }, "C", Modifier.weight(1f))
                  CalcoraNumberField(d, { d = it }, "D", Modifier.weight(1f))
                }
              }
              RatioMode.PROPORTION -> {
                Text("Solve A : B = C : x", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  CalcoraNumberField(a, { a = it }, "A", Modifier.weight(1f))
                  CalcoraNumberField(b, { b = it }, "B", Modifier.weight(1f))
                  CalcoraNumberField(c, { c = it }, "C", Modifier.weight(1f))
                }
              }
              RatioMode.DIVIDE -> {
                CalcoraNumberField(amount, { amount = it }, "Total Amount")
                OutlinedTextField(
                  value = ratioList,
                  onValueChange = { ratioList = it },
                  label = { Text("Ratio Parts (e.g. 2, 3, 5)") },
                  singleLine = true,
                  modifier = Modifier.fillMaxWidth(),
                )
              }
            }
            ActionButtonRow(
              primaryLabel = "Calculate",
              onPrimaryClick = {
                val out: CalcOutcome<*> =
                  when (mode) {
                    RatioMode.SIMPLIFY -> {
                      val av = a.toLongOrNull(); val bv = b.toLongOrNull()
                      if (av == null || bv == null) CalcOutcome.Error("Enter valid whole integers")
                      else RatioProportionEngine.simplifyRatio(av, bv)
                    }
                    RatioMode.COMPARE -> {
                      val av = a.toDoubleOrNull(); val bv = b.toDoubleOrNull(); val cv = c.toDoubleOrNull(); val dv = d.toDoubleOrNull()
                      if (av == null || bv == null || cv == null || dv == null) CalcOutcome.Error("Enter valid numbers")
                      else RatioProportionEngine.compareRatios(av, bv, cv, dv)
                    }
                    RatioMode.PROPORTION -> {
                      val av = a.toDoubleOrNull(); val bv = b.toDoubleOrNull(); val cv = c.toDoubleOrNull()
                      if (av == null || bv == null || cv == null) CalcOutcome.Error("Enter valid numbers")
                      else RatioProportionEngine.solveProportion(av, bv, cv)
                    }
                    RatioMode.DIVIDE -> {
                      val amt = amount.toDoubleOrNull()
                      val parts = StatisticsEngine.parseNumbers(ratioList)
                      if (amt == null || parts == null) CalcOutcome.Error("Enter a valid total amount and ratio parts")
                      else RatioProportionEngine.divideAmountByRatio(amt, parts)
                    }
                  }
                when (out) {
                  is CalcOutcome.Success -> {
                    errorMsg = null; breakdown = out.breakdown
                    out.breakdown?.let { onRecordHistory(it.inputValues, it.finalAnswer, "Ratio & Proportion", "Arts", Routes.ART_RATIO) }
                  }
                  is CalcOutcome.Error -> { errorMsg = out.message; breakdown = null }
                }
              },
              onResetClick = { a = ""; b = ""; c = ""; d = ""; amount = ""; ratioList = ""; errorMsg = null; breakdown = null },
            )
          }
        }
        errorMsg?.let { item { ValidationErrorCard(it) } }
        breakdown?.let { item { StepBreakdownCard(it, showDetails = showBreakdown) } }
      }
    }
  }
}

// ======================================================
// 4. ARTS DATE & TIME TOOLS HUB
// ======================================================

@Composable
fun ArtsDateTimeHubScreen(
  onNavigateToRoute: (String) -> Unit,
  onBack: () -> Unit,
) {
  val itemsList = remember {
    listOf(
      Triple("Date Difference / Days Between", "Exact calendar years, months, days & total days", Routes.UNI_DATE_DIFF),
      Triple("Add / Subtract Days & Weeks", "Safely add or subtract days, weeks, months, years", Routes.UNI_DATE_ADD_SUB),
      Triple("Time Duration Calculator", "Hours, minutes, seconds between start & end time", Routes.UNI_TIME_DURATION),
      Triple("Birthday & Age Calculator", "Exact age & next birthday countdown", Routes.UNI_AGE),
    )
  }

  Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
    CalcoraTopBar(title = "Date & Time Tools", subtitle = "Calendar-Aware Date Arithmetic", onBack = onBack)
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
      LazyColumn(
        modifier = Modifier.fillMaxSize().widthIn(max = 720.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        items(itemsList) { (title, desc, route) ->
          Card(
            onClick = { onNavigateToRoute(route) },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth().padding(16.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(desc, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
              Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }
          }
        }
      }
    }
  }
}
