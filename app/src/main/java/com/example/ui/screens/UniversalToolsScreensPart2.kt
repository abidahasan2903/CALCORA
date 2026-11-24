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
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.engine.BiologicalSex
import com.example.engine.BmiResult
import com.example.engine.BmrResult
import com.example.engine.GeometryEngine
import com.example.engine.GeometryShape
import com.example.engine.HealthEngine
import com.example.engine.NumberBase
import com.example.engine.NumberFormatter
import com.example.engine.NumberSystemEngine
import com.example.engine.NumberSystemResult
import com.example.engine.Routes
import com.example.engine.UnitCategory
import com.example.engine.UnitConverterEngine
import com.example.model.CalcOutcome
import com.example.model.StepBreakdown
import com.example.ui.components.ActionButtonRow
import com.example.ui.components.CalcoraNumberField
import com.example.ui.components.CalcoraSectionCard
import com.example.ui.components.CalcoraTopBar
import com.example.ui.components.StepBreakdownCard
import com.example.ui.components.ValidationErrorCard
import com.example.ui.theme.JetBrainsMonoFontFamily

// ======================================================
// 5. GEOMETRY CALCULATOR SCREEN (2D & 3D)
// ======================================================

@Composable
fun GeometryCalculatorScreen(
  showBreakdown: Boolean,
  onRecordHistory: (String, String, String, String, String) -> Unit,
  onBack: () -> Unit,
) {
  var show3D by rememberSaveable { mutableStateOf(false) }
  var selectedShape by rememberSaveable { mutableStateOf(GeometryShape.CIRCLE) }
  val fieldValues = remember { mutableStateListOf("5", "4", "4", "4", "4") }
  var errorMsg by remember { mutableStateOf<String?>(null) }
  var breakdown by remember { mutableStateOf<StepBreakdown?>(null) }

  val shapes = remember(show3D) { GeometryShape.entries.filter { it.is3D == show3D } }

  Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
    CalcoraTopBar(title = "Geometry Calculator", subtitle = "2D Area/Perimeter & 3D Volume/Surface", onBack = onBack)
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
      LazyColumn(
        modifier = Modifier.fillMaxSize().widthIn(max = 720.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        item {
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
              selected = !show3D,
              onClick = { show3D = false; selectedShape = GeometryShape.CIRCLE; errorMsg = null; breakdown = null },
              label = { Text("2D Shapes") },
              modifier = Modifier.weight(1f),
            )
            FilterChip(
              selected = show3D,
              onClick = { show3D = true; selectedShape = GeometryShape.CYLINDER; errorMsg = null; breakdown = null },
              label = { Text("3D Shapes") },
              modifier = Modifier.weight(1f),
            )
          }
        }
        item {
          LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(shapes) { sh ->
              FilterChip(selected = selectedShape == sh, onClick = { selectedShape = sh; errorMsg = null; breakdown = null }, label = { Text(sh.label) })
            }
          }
        }
        item {
          CalcoraSectionCard {
            Text(selectedShape.label, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            selectedShape.inputs.forEachIndexed { idx, lbl ->
              CalcoraNumberField(fieldValues[idx], { fieldValues[idx] = it }, lbl)
            }
            ActionButtonRow(
              primaryLabel = "Calculate ${selectedShape.label}",
              onPrimaryClick = {
                val params = (0 until selectedShape.inputs.size).map { fieldValues[it].toDoubleOrNull() }
                if (params.any { it == null }) {
                  errorMsg = "Enter valid numbers for all shape dimensions"; breakdown = null
                } else {
                  when (val out = GeometryEngine.calculate(selectedShape, params.filterNotNull())) {
                    is CalcOutcome.Success -> {
                      errorMsg = null; breakdown = out.breakdown
                      out.breakdown?.let { onRecordHistory("${selectedShape.label} (${it.inputValues})", it.finalAnswer, "Geometry Calculator", "Universal", Routes.UNI_GEOMETRY) }
                    }
                    is CalcOutcome.Error -> { errorMsg = out.message; breakdown = null }
                  }
                }
              },
              onResetClick = { for (i in fieldValues.indices) fieldValues[i] = ""; errorMsg = null; breakdown = null },
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
// 6. UNIT CONVERTER SCREEN (12 CATEGORIES)
// ======================================================

@Composable
fun UnitConverterScreen(
  showBreakdown: Boolean,
  onRecordHistory: (String, String, String, String, String) -> Unit,
  onBack: () -> Unit,
) {
  var category by rememberSaveable { mutableStateOf(UnitCategory.LENGTH) }
  val units = remember(category) { UnitConverterEngine.unitsFor(category) }
  var fromIndex by rememberSaveable(category) { mutableStateOf(0) }
  var toIndex by rememberSaveable(category) { mutableStateOf(if (units.size > 2) 2 else 1) }
  var inputValue by rememberSaveable { mutableStateOf("1") }
  var errorMsg by remember { mutableStateOf<String?>(null) }
  var breakdown by remember { mutableStateOf<StepBreakdown?>(null) }

  val fromUnit = units.getOrElse(fromIndex) { units.first() }
  val toUnit = units.getOrElse(toIndex) { units.last() }

  Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
    CalcoraTopBar(title = "Unit Converter", subtitle = "12 Categories • SI & IEC Data Units", onBack = onBack)
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
      LazyColumn(
        modifier = Modifier.fillMaxSize().widthIn(max = 720.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        item {
          LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(UnitCategory.entries) { cat ->
              FilterChip(
                selected = category == cat,
                onClick = {
                  category = cat
                  val newUnits = UnitConverterEngine.unitsFor(cat)
                  fromIndex = 0
                  toIndex = if (newUnits.size > 1) 1 else 0
                  errorMsg = null; breakdown = null
                },
                label = { Text(cat.title) },
              )
            }
          }
        }
        item {
          CalcoraSectionCard {
            CalcoraNumberField(inputValue, { inputValue = it }, "Value in ${fromUnit.label}", testTag = "unit_input")

            Text("From Unit", style = MaterialTheme.typography.labelLarge)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              items(units.indices.toList()) { idx ->
                FilterChip(selected = fromIndex == idx, onClick = { fromIndex = idx }, label = { Text("${units[idx].label} (${units[idx].symbol})") })
              }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
              IconButton(onClick = { val tmp = fromIndex; fromIndex = toIndex; toIndex = tmp }) {
                Icon(Icons.Outlined.SwapVert, contentDescription = "Swap units")
              }
            }

            Text("To Unit", style = MaterialTheme.typography.labelLarge)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              items(units.indices.toList()) { idx ->
                FilterChip(selected = toIndex == idx, onClick = { toIndex = idx }, label = { Text("${units[idx].label} (${units[idx].symbol})") })
              }
            }

            ActionButtonRow(
              primaryLabel = "Convert",
              onPrimaryClick = {
                val v = inputValue.toDoubleOrNull()
                if (v == null) {
                  errorMsg = "Enter a valid number to convert"; breakdown = null
                } else {
                  when (val out = UnitConverterEngine.convert(category, v, fromUnit, toUnit)) {
                    is CalcOutcome.Success -> {
                      errorMsg = null; breakdown = out.breakdown
                      out.breakdown?.let { onRecordHistory("${it.inputValues} → ${toUnit.symbol}", it.finalAnswer, "Unit Converter (${category.title})", "Universal", Routes.UNI_UNIT_CONVERTER) }
                    }
                    is CalcOutcome.Error -> { errorMsg = out.message; breakdown = null }
                  }
                }
              },
              onResetClick = { inputValue = ""; errorMsg = null; breakdown = null },
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
// 7. NUMBER SYSTEM CONVERTER SCREEN
// ======================================================

@Composable
fun NumberSystemConverterScreen(
  showBreakdown: Boolean,
  onRecordHistory: (String, String, String, String, String) -> Unit,
  onBack: () -> Unit,
) {
  var fromBase by rememberSaveable { mutableStateOf(NumberBase.DECIMAL) }
  var rawInput by rememberSaveable { mutableStateOf("255") }
  var result by remember { mutableStateOf<NumberSystemResult?>(null) }
  var errorMsg by remember { mutableStateOf<String?>(null) }
  var breakdown by remember { mutableStateOf<StepBreakdown?>(null) }

  Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
    CalcoraTopBar(title = "Number System Converter", subtitle = "Binary • Octal • Decimal • Hex", onBack = onBack)
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
      LazyColumn(
        modifier = Modifier.fillMaxSize().widthIn(max = 720.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        item {
          CalcoraSectionCard {
            Text("Input Base", style = MaterialTheme.typography.labelLarge)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              items(NumberBase.entries) { base ->
                FilterChip(selected = fromBase == base, onClick = { fromBase = base }, label = { Text(base.label) })
              }
            }
            OutlinedTextField(
              value = rawInput,
              onValueChange = { rawInput = it },
              label = { Text("Enter ${fromBase.label} value") },
              singleLine = true,
              modifier = Modifier.fillMaxWidth().testTag("numsys_input"),
            )
            ActionButtonRow(
              primaryLabel = "Convert Base",
              onPrimaryClick = {
                when (val out = NumberSystemEngine.convert(rawInput, fromBase)) {
                  is CalcOutcome.Success -> {
                    errorMsg = null; result = out.data; breakdown = out.breakdown
                    onRecordHistory("$rawInput (${fromBase.label})", "Dec=${out.data.decimal}, Hex=${out.data.hex}", "Number System Converter", "Universal", Routes.UNI_NUMBER_SYSTEM)
                  }
                  is CalcOutcome.Error -> { errorMsg = out.message; result = null; breakdown = null }
                }
              },
              onResetClick = { rawInput = ""; result = null; errorMsg = null; breakdown = null },
            )
          }
        }
        errorMsg?.let { item { ValidationErrorCard(it) } }
        result?.let { r ->
          item {
            CalcoraSectionCard {
              Text("Converted Values", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
              BaseRow("Binary (Base 2)", r.binary)
              BaseRow("Octal (Base 8)", r.octal)
              BaseRow("Decimal (Base 10)", r.decimal)
              BaseRow("Hexadecimal (Base 16)", r.hex)
            }
          }
        }
        breakdown?.let { item { StepBreakdownCard(it, showDetails = showBreakdown) } }
      }
    }
  }
}

@Composable
private fun BaseRow(label: String, value: String) {
  Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
    Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Text(value, style = MaterialTheme.typography.bodyLarge.copy(fontFamily = JetBrainsMonoFontFamily), fontWeight = FontWeight.Bold)
  }
}

// ======================================================
// 8. BMI CALCULATOR SCREEN
// ======================================================

@Composable
fun BmiCalculatorScreen(
  showBreakdown: Boolean,
  onRecordHistory: (String, String, String, String, String) -> Unit,
  onBack: () -> Unit,
) {
  var weight by rememberSaveable { mutableStateOf("70") }
  var height by rememberSaveable { mutableStateOf("175") }
  var bmiResult by remember { mutableStateOf<BmiResult?>(null) }
  var errorMsg by remember { mutableStateOf<String?>(null) }
  var breakdown by remember { mutableStateOf<StepBreakdown?>(null) }

  Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
    CalcoraTopBar(title = "BMI Calculator", subtitle = "Body Mass Index Screening", onBack = onBack)
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
      LazyColumn(
        modifier = Modifier.fillMaxSize().widthIn(max = 720.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        item {
          CalcoraSectionCard {
            CalcoraNumberField(weight, { weight = it }, "Weight (kg)", testTag = "bmi_weight")
            CalcoraNumberField(height, { height = it }, "Height (cm)", testTag = "bmi_height")
            ActionButtonRow(
              primaryLabel = "Calculate BMI",
              onPrimaryClick = {
                val w = weight.toDoubleOrNull(); val h = height.toDoubleOrNull()
                if (w == null || h == null) {
                  errorMsg = "Enter valid numbers for weight and height"; bmiResult = null; breakdown = null
                } else {
                  when (val out = HealthEngine.calculateBmi(w, h)) {
                    is CalcOutcome.Success -> {
                      errorMsg = null; bmiResult = out.data; breakdown = out.breakdown
                      onRecordHistory("W=$w kg, H=$h cm", "${NumberFormatter.formatPlain(out.data.bmi, 2)} (${out.data.category})", "BMI Calculator", "Universal", Routes.UNI_BMI)
                    }
                    is CalcOutcome.Error -> { errorMsg = out.message; bmiResult = null; breakdown = null }
                  }
                }
              },
              onResetClick = { weight = ""; height = ""; bmiResult = null; errorMsg = null; breakdown = null },
            )
          }
        }
        errorMsg?.let { item { ValidationErrorCard(it) } }
        bmiResult?.let { res ->
          item {
            Card(
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
              modifier = Modifier.fillMaxWidth(),
            ) {
              Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("BMI = ${NumberFormatter.formatPlain(res.bmi, 2)}", style = MaterialTheme.typography.headlineMedium)
                Text(res.category, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Text(res.explanation, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
            }
          }
        }
        breakdown?.let { item { StepBreakdownCard(it, showDetails = showBreakdown) } }
      }
    }
  }
}

// ======================================================
// 9. BMR CALCULATOR SCREEN
// ======================================================

@Composable
fun BmrCalculatorScreen(
  showBreakdown: Boolean,
  onRecordHistory: (String, String, String, String, String) -> Unit,
  onBack: () -> Unit,
) {
  var sex by rememberSaveable { mutableStateOf(BiologicalSex.MALE) }
  var age by rememberSaveable { mutableStateOf("25") }
  var height by rememberSaveable { mutableStateOf("175") }
  var weight by rememberSaveable { mutableStateOf("70") }
  var bmrResult by remember { mutableStateOf<BmrResult?>(null) }
  var errorMsg by remember { mutableStateOf<String?>(null) }
  var breakdown by remember { mutableStateOf<StepBreakdown?>(null) }

  Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
    CalcoraTopBar(title = "BMR Calculator", subtitle = "Mifflin-St Jeor Equation", onBack = onBack)
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
      LazyColumn(
        modifier = Modifier.fillMaxSize().widthIn(max = 720.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        item {
          CalcoraSectionCard {
            Text("Equation Sex Parameter", style = MaterialTheme.typography.labelLarge)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              BiologicalSex.entries.forEach { s ->
                FilterChip(selected = sex == s, onClick = { sex = s }, label = { Text(s.label) }, modifier = Modifier.weight(1f))
              }
            }
            CalcoraNumberField(age, { age = it }, "Age (Years)", allowDecimal = false)
            CalcoraNumberField(height, { height = it }, "Height (cm)")
            CalcoraNumberField(weight, { weight = it }, "Weight (kg)")
            ActionButtonRow(
              primaryLabel = "Estimate BMR",
              onPrimaryClick = {
                val a = age.toIntOrNull(); val h = height.toDoubleOrNull(); val w = weight.toDoubleOrNull()
                if (a == null || h == null || w == null) {
                  errorMsg = "Enter valid numbers for age, height, and weight"; bmrResult = null; breakdown = null
                } else {
                  when (val out = HealthEngine.calculateBmr(sex, a, h, w)) {
                    is CalcOutcome.Success -> {
                      errorMsg = null; bmrResult = out.data; breakdown = out.breakdown
                      out.breakdown?.let { onRecordHistory(it.inputValues, it.finalAnswer, "BMR Calculator", "Universal", Routes.UNI_BMR) }
                    }
                    is CalcOutcome.Error -> { errorMsg = out.message; bmrResult = null; breakdown = null }
                  }
                }
              },
              onResetClick = { age = ""; height = ""; weight = ""; bmrResult = null; errorMsg = null; breakdown = null },
            )
          }
        }
        errorMsg?.let { item { ValidationErrorCard(it) } }
        bmrResult?.let { res ->
          item {
            CalcoraSectionCard {
              Text("${NumberFormatter.formatPlain(res.bmrCalories, 0)} kcal / day", style = MaterialTheme.typography.headlineMedium)
              Text("Formula: ${res.formulaUsed}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
              Text(res.explanation, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
        }
        breakdown?.let { item { StepBreakdownCard(it, showDetails = showBreakdown) } }
      }
    }
  }
}
