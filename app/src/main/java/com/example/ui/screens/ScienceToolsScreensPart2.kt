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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
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
import com.example.engine.ChemistryEngine
import com.example.engine.NumberFormatter
import com.example.engine.PhysicsEngine
import com.example.engine.PhysicsToolType
import com.example.engine.ProbabilityEngine
import com.example.engine.Routes
import com.example.engine.StatisticsEngine
import com.example.engine.StatisticsResult
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
// 5. STATISTICS CALCULATOR SCREEN (SCIENCE & ARTS)
// ======================================================

@Composable
fun StatisticsCalculatorScreen(
  categoryLabel: String,
  route: String,
  showBreakdown: Boolean,
  onRecordHistory: (String, String, String, String, String) -> Unit,
  onBack: () -> Unit,
) {
  var dataInput by rememberSaveable { mutableStateOf("12, 15, 18, 22, 22, 25, 30") }
  var result by remember { mutableStateOf<StatisticsResult?>(null) }
  var breakdown by remember { mutableStateOf<StepBreakdown?>(null) }
  var errorMsg by remember { mutableStateOf<String?>(null) }

  Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
    CalcoraTopBar(title = "Statistics Calculator", subtitle = "$categoryLabel • Descriptive Stats", onBack = onBack)
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
      LazyColumn(
        modifier = Modifier.fillMaxSize().widthIn(max = 720.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        item {
          CalcoraSectionCard {
            Text("Dataset Values", style = MaterialTheme.typography.titleMedium)
            Text(
              "Separate numbers with commas or spaces",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedTextField(
              value = dataInput,
              onValueChange = { dataInput = it },
              label = { Text("Numbers (e.g. 10, 20, 30, 40)") },
              minLines = 2,
              maxLines = 4,
              modifier = Modifier.fillMaxWidth().testTag("stats_input"),
            )
            ActionButtonRow(
              primaryLabel = "Calculate Statistics",
              onPrimaryClick = {
                val parsed = StatisticsEngine.parseNumbers(dataInput)
                if (parsed == null) {
                  errorMsg = "Enter valid numbers separated by commas or spaces"
                  result = null
                  breakdown = null
                } else {
                  when (val out = StatisticsEngine.compute(parsed)) {
                    is CalcOutcome.Success -> {
                      errorMsg = null
                      result = out.data
                      breakdown = out.breakdown
                      onRecordHistory("Stats (${parsed.size} items)", "Mean=${NumberFormatter.formatPlain(out.data.mean, 4)}", "Statistics Calculator", categoryLabel, route)
                    }
                    is CalcOutcome.Error -> {
                      errorMsg = out.message
                      result = null
                      breakdown = null
                    }
                  }
                }
              },
              onResetClick = { dataInput = ""; result = null; breakdown = null; errorMsg = null },
            )
          }
        }

        errorMsg?.let { item { ValidationErrorCard(it) } }

        result?.let { r ->
          item {
            CalcoraSectionCard {
              Text("Statistical Summary", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
              StatMetricRow("Count (N)", r.count.toString())
              StatMetricRow("Sum (Σx)", NumberFormatter.formatPlain(r.sum, 4))
              StatMetricRow("Mean (μ / x̄)", NumberFormatter.formatPlain(r.mean, 4))
              StatMetricRow("Median", NumberFormatter.formatPlain(r.median, 4))
              StatMetricRow("Mode", r.modeDescription)
              StatMetricRow("Minimum", NumberFormatter.formatPlain(r.min, 4))
              StatMetricRow("Maximum", NumberFormatter.formatPlain(r.max, 4))
              StatMetricRow("Range", NumberFormatter.formatPlain(r.range, 4))
              StatMetricRow("Population Variance (σ²)", NumberFormatter.formatPlain(r.populationVariance, 6))
              StatMetricRow("Population Std Dev (σ)", NumberFormatter.formatPlain(r.populationStdDev, 6))
              StatMetricRow("Sample Variance (s²)", r.sampleVariance?.let { NumberFormatter.formatPlain(it, 6) } ?: "Requires N ≥ 2")
              StatMetricRow("Sample Std Dev (s)", r.sampleStdDev?.let { NumberFormatter.formatPlain(it, 6) } ?: "Requires N ≥ 2")
            }
          }
        }

        breakdown?.let { item { StepBreakdownCard(it, showDetails = showBreakdown) } }
      }
    }
  }
}

@Composable
private fun StatMetricRow(label: String, value: String) {
  Row(
    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Text(value, style = MaterialTheme.typography.bodyLarge.copy(fontFamily = JetBrainsMonoFontFamily), fontWeight = FontWeight.SemiBold)
  }
}

// ======================================================
// 6. PROBABILITY CALCULATOR SCREEN
// ======================================================

private enum class ProbabilityMode(val label: String) {
  BASIC("Basic P(A)"),
  PERMUTATION("nPr"),
  COMBINATION("nCr"),
}

@Composable
fun ProbabilityCalculatorScreen(
  showBreakdown: Boolean,
  onRecordHistory: (String, String, String, String, String) -> Unit,
  onBack: () -> Unit,
) {
  var mode by rememberSaveable { mutableStateOf(ProbabilityMode.BASIC) }
  var aText by rememberSaveable { mutableStateOf("3") }
  var bText by rememberSaveable { mutableStateOf("10") }
  var errorMsg by remember { mutableStateOf<String?>(null) }
  var breakdown by remember { mutableStateOf<StepBreakdown?>(null) }

  Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
    CalcoraTopBar(title = "Probability Calculator", subtitle = "Events, Permutations & Combinations", onBack = onBack)
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
      LazyColumn(
        modifier = Modifier.fillMaxSize().widthIn(max = 720.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        item {
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ProbabilityMode.entries.forEach { m ->
              FilterChip(selected = mode == m, onClick = { mode = m; errorMsg = null; breakdown = null }, label = { Text(m.label) }, modifier = Modifier.weight(1f))
            }
          }
        }
        item {
          CalcoraSectionCard {
            when (mode) {
              ProbabilityMode.BASIC -> {
                CalcoraNumberField(aText, { aText = it }, "Favorable Outcomes n(A)")
                CalcoraNumberField(bText, { bText = it }, "Total Possible Outcomes n(S)")
              }
              ProbabilityMode.PERMUTATION -> {
                CalcoraNumberField(aText, { aText = it }, "Total Items (n)", allowDecimal = false)
                CalcoraNumberField(bText, { bText = it }, "Selected Items (r)", allowDecimal = false)
              }
              ProbabilityMode.COMBINATION -> {
                CalcoraNumberField(aText, { aText = it }, "Total Items (n)", allowDecimal = false)
                CalcoraNumberField(bText, { bText = it }, "Chosen Items (r)", allowDecimal = false)
              }
            }
            ActionButtonRow(
              primaryLabel = "Calculate",
              onPrimaryClick = {
                val out =
                  when (mode) {
                    ProbabilityMode.BASIC -> {
                      val fav = aText.toDoubleOrNull()
                      val tot = bText.toDoubleOrNull()
                      if (fav == null || tot == null) CalcOutcome.Error("Enter valid numbers")
                      else ProbabilityEngine.basicProbability(fav, tot)
                    }
                    ProbabilityMode.PERMUTATION -> {
                      val n = aText.toIntOrNull()
                      val r = bText.toIntOrNull()
                      if (n == null || r == null) CalcOutcome.Error("Enter valid integers for n and r")
                      else ProbabilityEngine.permutation(n, r)
                    }
                    ProbabilityMode.COMBINATION -> {
                      val n = aText.toIntOrNull()
                      val r = bText.toIntOrNull()
                      if (n == null || r == null) CalcOutcome.Error("Enter valid integers for n and r")
                      else ProbabilityEngine.combination(n, r)
                    }
                  }
                when (out) {
                  is CalcOutcome.Success -> {
                    errorMsg = null; breakdown = out.breakdown
                    out.breakdown?.let {
                      onRecordHistory(it.inputValues, it.finalAnswer, "Probability (${mode.label})", "Science", Routes.SCI_PROBABILITY)
                    }
                  }
                  is CalcOutcome.Error -> { errorMsg = out.message; breakdown = null }
                }
              },
              onResetClick = { aText = ""; bText = ""; errorMsg = null; breakdown = null },
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
// 7. PHYSICS CALCULATOR SCREEN
// ======================================================

@Composable
fun PhysicsCalculatorScreen(
  showBreakdown: Boolean,
  onRecordHistory: (String, String, String, String, String) -> Unit,
  onBack: () -> Unit,
) {
  val groups = remember { listOf("Motion", "Force & Energy", "Electrical", "Other") }
  var selectedGroup by rememberSaveable { mutableStateOf("Motion") }
  var selectedTool by rememberSaveable { mutableStateOf(PhysicsToolType.SPEED) }
  var in1 by rememberSaveable { mutableStateOf("100") }
  var in2 by rememberSaveable { mutableStateOf("10") }
  var in3 by rememberSaveable { mutableStateOf("9.80665") }
  var errorMsg by remember { mutableStateOf<String?>(null) }
  var breakdown by remember { mutableStateOf<StepBreakdown?>(null) }

  val toolsInGroup = remember(selectedGroup) { PhysicsToolType.entries.filter { it.group == selectedGroup } }

  val labels = when (selectedTool) {
    PhysicsToolType.SPEED -> Triple("Distance (m)", "Time (s)", null)
    PhysicsToolType.VELOCITY -> Triple("Displacement (m)", "Time (s)", null)
    PhysicsToolType.DISTANCE -> Triple("Speed (m/s)", "Time (s)", null)
    PhysicsToolType.TIME -> Triple("Distance (m)", "Speed (m/s)", null)
    PhysicsToolType.ACCELERATION -> Triple("Initial Velocity u (m/s)", "Final Velocity v (m/s)", "Time (s)")
    PhysicsToolType.FORCE -> Triple("Mass (kg)", "Acceleration (m/s²)", null)
    PhysicsToolType.WORK -> Triple("Force (N)", "Distance (m)", "Angle θ (deg)")
    PhysicsToolType.POWER -> Triple("Work (J)", "Time (s)", null)
    PhysicsToolType.KINETIC_ENERGY -> Triple("Mass (kg)", "Velocity (m/s)", null)
    PhysicsToolType.POTENTIAL_ENERGY -> Triple("Mass (kg)", "Height (m)", "Gravity g (m/s²)")
    PhysicsToolType.MOMENTUM -> Triple("Mass (kg)", "Velocity (m/s)", null)
    PhysicsToolType.OHMS_VOLTAGE -> Triple("Current I (A)", "Resistance R (Ω)", null)
    PhysicsToolType.OHMS_CURRENT -> Triple("Voltage V (V)", "Resistance R (Ω)", null)
    PhysicsToolType.OHMS_RESISTANCE -> Triple("Voltage V (V)", "Current I (A)", null)
    PhysicsToolType.ELECTRICAL_POWER -> Triple("Voltage V (V)", "Current I (A)", null)
    PhysicsToolType.DENSITY -> Triple("Mass (kg)", "Volume (m³)", null)
    PhysicsToolType.PRESSURE -> Triple("Force (N)", "Area (m²)", null)
    PhysicsToolType.WAVE_SPEED -> Triple("Frequency f (Hz)", "Wavelength λ (m)", null)
    PhysicsToolType.FREQUENCY -> Triple("Wave Speed v (m/s)", "Wavelength λ (m)", null)
    PhysicsToolType.WAVELENGTH -> Triple("Wave Speed v (m/s)", "Frequency f (Hz)", null)
  }

  Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
    CalcoraTopBar(title = "Physics Calculator", subtitle = "Motion, Energy, Electricity & Waves", onBack = onBack)
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
      LazyColumn(
        modifier = Modifier.fillMaxSize().widthIn(max = 720.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        item {
          LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(groups) { grp ->
              FilterChip(
                selected = selectedGroup == grp,
                onClick = {
                  selectedGroup = grp
                  selectedTool = PhysicsToolType.entries.first { it.group == grp }
                  errorMsg = null; breakdown = null
                },
                label = { Text(grp) },
              )
            }
          }
        }
        item {
          LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(toolsInGroup) { tool ->
              FilterChip(
                selected = selectedTool == tool,
                onClick = { selectedTool = tool; errorMsg = null; breakdown = null },
                label = { Text(tool.title) },
              )
            }
          }
        }
        item {
          CalcoraSectionCard {
            Text(selectedTool.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            CalcoraNumberField(in1, { in1 = it }, labels.first)
            CalcoraNumberField(in2, { in2 = it }, labels.second)
            labels.third?.let { lbl3 -> CalcoraNumberField(in3, { in3 = it }, lbl3) }

            ActionButtonRow(
              primaryLabel = "Compute",
              onPrimaryClick = {
                val a = in1.toDoubleOrNull()
                val b = in2.toDoubleOrNull()
                val c = if (labels.third != null) in3.toDoubleOrNull() else 0.0
                if (a == null || b == null || c == null) {
                  errorMsg = "Enter valid numerical values for all required inputs"
                  breakdown = null
                } else {
                  when (val out = PhysicsEngine.calculate(selectedTool, a, b, c)) {
                    is CalcOutcome.Success -> {
                      errorMsg = null; breakdown = out.breakdown
                      out.breakdown?.let {
                        onRecordHistory(it.inputValues, it.finalAnswer, selectedTool.title, "Science", Routes.SCI_PHYSICS)
                      }
                    }
                    is CalcOutcome.Error -> { errorMsg = out.message; breakdown = null }
                  }
                }
              },
              onResetClick = { in1 = ""; in2 = ""; in3 = if (selectedTool == PhysicsToolType.POTENTIAL_ENERGY) "9.80665" else "0"; errorMsg = null; breakdown = null },
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
// 8. CHEMISTRY CALCULATOR SCREEN
// ======================================================

private enum class ChemistryTab(val label: String) {
  MOLAR_MASS("Molar Mass"),
  MOLES("Moles"),
  MOLARITY("Molarity"),
  DILUTION("Dilution"),
  CONCENTRATION("% Conc."),
  PH("pH"),
  IDEAL_GAS("Ideal Gas"),
  PERIODIC("Elements"),
}

@Composable
fun ChemistryCalculatorScreen(
  showBreakdown: Boolean,
  onRecordHistory: (String, String, String, String, String) -> Unit,
  onBack: () -> Unit,
) {
  var tab by rememberSaveable { mutableStateOf(ChemistryTab.MOLAR_MASS) }
  var formulaInput by rememberSaveable { mutableStateOf("H2O") }
  var v1 by rememberSaveable { mutableStateOf("18") }
  var v2 by rememberSaveable { mutableStateOf("18.015") }
  var v3 by rememberSaveable { mutableStateOf("") }
  var v4 by rememberSaveable { mutableStateOf("0.5") }
  var elemSearch by rememberSaveable { mutableStateOf("") }
  var errorMsg by remember { mutableStateOf<String?>(null) }
  var breakdown by remember { mutableStateOf<StepBreakdown?>(null) }

  Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
    CalcoraTopBar(title = "Chemistry Calculator", subtitle = "Offline 118-Element Periodic Dataset", onBack = onBack)
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
      LazyColumn(
        modifier = Modifier.fillMaxSize().widthIn(max = 720.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        item {
          LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(ChemistryTab.entries) { t ->
              FilterChip(
                selected = tab == t,
                onClick = {
                  tab = t; errorMsg = null; breakdown = null
                  when (t) {
                    ChemistryTab.DILUTION -> { v1 = "2.0"; v2 = "0.5"; v3 = "0.5"; v4 = "" }
                    ChemistryTab.PH -> { v1 = "0.001" }
                    ChemistryTab.IDEAL_GAS -> { v1 = ""; v2 = "22.414"; v3 = "1"; v4 = "273.15" }
                    else -> {}
                  }
                },
                label = { Text(t.label) },
              )
            }
          }
        }

        if (tab == ChemistryTab.PERIODIC) {
          item {
            OutlinedTextField(
              value = elemSearch,
              onValueChange = { elemSearch = it },
              label = { Text("Filter element by symbol, name, or number") },
              singleLine = true,
              modifier = Modifier.fillMaxWidth(),
            )
          }
          val filtered = ChemistryEngine.periodicTable.filter {
            elemSearch.isBlank() ||
              it.name.contains(elemSearch, ignoreCase = true) ||
              it.symbol.equals(elemSearch.trim(), ignoreCase = true) ||
              it.number.toString() == elemSearch.trim()
          }
          items(filtered, key = { it.number }) { elem ->
            Card(
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
              modifier = Modifier.fillMaxWidth(),
            ) {
              Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text("${elem.number}. ${elem.symbol} — ${elem.name}", style = MaterialTheme.typography.titleMedium)
                Text("${elem.atomicMass} g/mol", style = MaterialTheme.typography.bodyMedium.copy(fontFamily = JetBrainsMonoFontFamily))
              }
            }
          }
        } else {
          item {
            CalcoraSectionCard {
              when (tab) {
                ChemistryTab.MOLAR_MASS -> {
                  OutlinedTextField(
                    value = formulaInput,
                    onValueChange = { formulaInput = it },
                    label = { Text("Chemical Formula (e.g. H2O, C6H12O6, Ca(OH)2)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("chem_formula_input"),
                  )
                }
                ChemistryTab.MOLES -> {
                  CalcoraNumberField(v1, { v1 = it }, "Sample Mass (g)")
                  CalcoraNumberField(v2, { v2 = it }, "Molar Mass (g/mol)")
                }
                ChemistryTab.MOLARITY -> {
                  CalcoraNumberField(v1, { v1 = it }, "Solute Moles (mol)")
                  CalcoraNumberField(v2, { v2 = it }, "Solution Volume (L)")
                }
                ChemistryTab.DILUTION -> {
                  Text("Leave ONE field blank to solve M₁V₁ = M₂V₂", style = MaterialTheme.typography.bodyMedium)
                  CalcoraNumberField(v1, { v1 = it }, "Initial Molarity M₁ (M)")
                  CalcoraNumberField(v2, { v2 = it }, "Initial Volume V₁ (L)")
                  CalcoraNumberField(v3, { v3 = it }, "Final Molarity M₂ (M)")
                  CalcoraNumberField(v4, { v4 = it }, "Final Volume V₂ (L)")
                }
                ChemistryTab.CONCENTRATION -> {
                  CalcoraNumberField(v1, { v1 = it }, "Solute Mass (g)")
                  CalcoraNumberField(v2, { v2 = it }, "Solution Volume (mL)")
                }
                ChemistryTab.PH -> {
                  CalcoraNumberField(v1, { v1 = it }, "Hydrogen Ion Concentration [H⁺] (mol/L)")
                }
                ChemistryTab.IDEAL_GAS -> {
                  Text("Leave ONE field blank to solve PV = nRT", style = MaterialTheme.typography.bodyMedium)
                  CalcoraNumberField(v1, { v1 = it }, "Pressure P (atm)")
                  CalcoraNumberField(v2, { v2 = it }, "Volume V (L)")
                  CalcoraNumberField(v3, { v3 = it }, "Moles n (mol)")
                  CalcoraNumberField(v4, { v4 = it }, "Temperature T (K)")
                }
                ChemistryTab.PERIODIC -> {}
              }

              ActionButtonRow(
                primaryLabel = "Calculate",
                onPrimaryClick = {
                  val out: CalcOutcome<*> =
                    when (tab) {
                      ChemistryTab.MOLAR_MASS -> ChemistryEngine.calculateMolarMass(formulaInput)
                      ChemistryTab.MOLES -> {
                        val m = v1.toDoubleOrNull(); val mm = v2.toDoubleOrNull()
                        if (m == null || mm == null) CalcOutcome.Error("Enter valid numbers for mass and molar mass")
                        else ChemistryEngine.numberOfMoles(m, mm)
                      }
                      ChemistryTab.MOLARITY -> {
                        val n = v1.toDoubleOrNull(); val vol = v2.toDoubleOrNull()
                        if (n == null || vol == null) CalcOutcome.Error("Enter valid numbers for moles and volume")
                        else ChemistryEngine.molarity(n, vol)
                      }
                      ChemistryTab.DILUTION -> {
                        val p1 = if (v1.isBlank()) null else v1.toDoubleOrNull()
                        val p2 = if (v2.isBlank()) null else v2.toDoubleOrNull()
                        val p3 = if (v3.isBlank()) null else v3.toDoubleOrNull()
                        val p4 = if (v4.isBlank()) null else v4.toDoubleOrNull()
                        if ((v1.isNotBlank() && p1 == null) || (v2.isNotBlank() && p2 == null) || (v3.isNotBlank() && p3 == null) || (v4.isNotBlank() && p4 == null)) {
                          CalcOutcome.Error("Enter valid numbers in filled dilution fields")
                        } else {
                          ChemistryEngine.dilution(p1, p2, p3, p4)
                        }
                      }
                      ChemistryTab.CONCENTRATION -> {
                        val m = v1.toDoubleOrNull(); val vol = v2.toDoubleOrNull()
                        if (m == null || vol == null) CalcOutcome.Error("Enter valid numbers")
                        else ChemistryEngine.concentrationMassPercent(m, vol)
                      }
                      ChemistryTab.PH -> {
                        val h = v1.toDoubleOrNull()
                        if (h == null) CalcOutcome.Error("Enter a valid [H⁺] concentration")
                        else ChemistryEngine.calculatePh(h)
                      }
                      ChemistryTab.IDEAL_GAS -> {
                        val p = if (v1.isBlank()) null else v1.toDoubleOrNull()
                        val vol = if (v2.isBlank()) null else v2.toDoubleOrNull()
                        val n = if (v3.isBlank()) null else v3.toDoubleOrNull()
                        val t = if (v4.isBlank()) null else v4.toDoubleOrNull()
                        if ((v1.isNotBlank() && p == null) || (v2.isNotBlank() && vol == null) || (v3.isNotBlank() && n == null) || (v4.isNotBlank() && t == null)) {
                          CalcOutcome.Error("Enter valid numbers in filled gas law fields")
                        } else {
                          ChemistryEngine.idealGasLaw(p, vol, n, t)
                        }
                      }
                      ChemistryTab.PERIODIC -> CalcOutcome.Error("")
                    }

                  when (out) {
                    is CalcOutcome.Success -> {
                      errorMsg = null; breakdown = out.breakdown
                      out.breakdown?.let {
                        onRecordHistory(it.inputValues, it.finalAnswer, "Chemistry (${tab.label})", "Science", Routes.SCI_CHEMISTRY)
                      }
                    }
                    is CalcOutcome.Error -> { errorMsg = out.message; breakdown = null }
                  }
                },
                onResetClick = { formulaInput = ""; v1 = ""; v2 = ""; v3 = ""; v4 = ""; errorMsg = null; breakdown = null },
              )
            }
          }
          errorMsg?.let { item { ValidationErrorCard(it) } }
          breakdown?.let { item { StepBreakdownCard(it, showDetails = showBreakdown) } }
        }
      }
    }
  }
}
