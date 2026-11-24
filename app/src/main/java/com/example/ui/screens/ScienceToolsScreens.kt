package com.example.ui.screens

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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.engine.EquationEngine
import com.example.engine.FractionEngine
import com.example.engine.FractionOp
import com.example.engine.MatrixEngine
import com.example.engine.MatrixOp
import com.example.engine.Routes
import com.example.engine.VectorEngine
import com.example.engine.VectorOp
import com.example.model.AngleMode
import com.example.model.CalcOutcome
import com.example.model.StepBreakdown
import com.example.ui.components.ActionButtonRow
import com.example.ui.components.CalcoraNumberField
import com.example.ui.components.CalcoraSectionCard
import com.example.ui.components.CalcoraTopBar
import com.example.ui.components.StepBreakdownCard
import com.example.ui.components.ValidationErrorCard

// ======================================================
// 1. FRACTION CALCULATOR SCREEN
// ======================================================

private enum class FractionTab(val label: String) {
  ARITHMETIC("a/b ± c/d"),
  DECIMAL_TO_FRAC("Decimal → Frac"),
  MIXED_TO_IMPROPER("Mixed → Improper"),
}

@Composable
fun FractionCalculatorScreen(
  showBreakdown: Boolean,
  onRecordHistory: (String, String, String, String, String) -> Unit,
  onBack: () -> Unit,
) {
  var tab by rememberSaveable { mutableStateOf(FractionTab.ARITHMETIC) }
  var op by rememberSaveable { mutableStateOf(FractionOp.ADD) }
  var n1 by rememberSaveable { mutableStateOf("1") }
  var d1 by rememberSaveable { mutableStateOf("3") }
  var n2 by rememberSaveable { mutableStateOf("1") }
  var d2 by rememberSaveable { mutableStateOf("6") }
  var decInput by rememberSaveable { mutableStateOf("0.75") }
  var mixWhole by rememberSaveable { mutableStateOf("2") }
  var mixNum by rememberSaveable { mutableStateOf("3") }
  var mixDen by rememberSaveable { mutableStateOf("4") }

  var errorMsg by remember { mutableStateOf<String?>(null) }
  var breakdown by remember { mutableStateOf<StepBreakdown?>(null) }

  Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
    CalcoraTopBar(title = "Fraction Calculator", subtitle = "Exact Rational Arithmetic", onBack = onBack)
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
      LazyColumn(
        modifier = Modifier.fillMaxSize().widthIn(max = 720.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        item {
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FractionTab.entries.forEach { t ->
              FilterChip(
                selected = tab == t,
                onClick = { tab = t; errorMsg = null; breakdown = null },
                label = { Text(t.label, maxLines = 1) },
                modifier = Modifier.weight(1f),
              )
            }
          }
        }

        item {
          CalcoraSectionCard {
            when (tab) {
              FractionTab.ARITHMETIC -> {
                Text("Fraction 1 & Fraction 2", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  CalcoraNumberField(n1, { n1 = it }, "Numerator 1", Modifier.weight(1f), allowDecimal = false)
                  CalcoraNumberField(d1, { d1 = it }, "Denominator 1", Modifier.weight(1f), allowDecimal = false)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  FractionOp.entries.forEach { fOp ->
                    FilterChip(
                      selected = op == fOp,
                      onClick = { op = fOp },
                      label = { Text(fOp.symbol) },
                      modifier = Modifier.weight(1f),
                    )
                  }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  CalcoraNumberField(n2, { n2 = it }, "Numerator 2", Modifier.weight(1f), allowDecimal = false)
                  CalcoraNumberField(d2, { d2 = it }, "Denominator 2", Modifier.weight(1f), allowDecimal = false)
                }
              }
              FractionTab.DECIMAL_TO_FRAC -> {
                CalcoraNumberField(decInput, { decInput = it }, "Decimal Number (e.g. 0.75)")
              }
              FractionTab.MIXED_TO_IMPROPER -> {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  CalcoraNumberField(mixWhole, { mixWhole = it }, "Whole", Modifier.weight(1f), allowDecimal = false)
                  CalcoraNumberField(mixNum, { mixNum = it }, "Numerator", Modifier.weight(1f), allowDecimal = false)
                  CalcoraNumberField(mixDen, { mixDen = it }, "Denominator", Modifier.weight(1f), allowDecimal = false)
                }
              }
            }

            ActionButtonRow(
              primaryLabel = "Calculate",
              onPrimaryClick = {
                val out =
                  when (tab) {
                    FractionTab.ARITHMETIC -> {
                      val a = n1.toLongOrNull()
                      val b = d1.toLongOrNull()
                      val c = n2.toLongOrNull()
                      val d = d2.toLongOrNull()
                      if (a == null || b == null || c == null || d == null) {
                        CalcOutcome.Error("Enter valid integers for numerators and denominators")
                      } else {
                        FractionEngine.operate(a, b, c, d, op)
                      }
                    }
                    FractionTab.DECIMAL_TO_FRAC -> FractionEngine.decimalToFraction(decInput)
                    FractionTab.MIXED_TO_IMPROPER -> {
                      val w = mixWhole.toLongOrNull()
                      val n = mixNum.toLongOrNull()
                      val d = mixDen.toLongOrNull()
                      if (w == null || n == null || d == null) {
                        CalcOutcome.Error("Enter valid integers for whole, numerator, and denominator")
                      } else {
                        FractionEngine.mixedToImproper(w, n, d)
                      }
                    }
                  }
                when (out) {
                  is CalcOutcome.Success -> {
                    errorMsg = null
                    breakdown = out.breakdown
                    out.breakdown?.let {
                      onRecordHistory(it.inputValues, it.finalAnswer, "Fraction Calculator", "Science", Routes.SCI_FRACTION)
                    }
                  }
                  is CalcOutcome.Error -> {
                    errorMsg = out.message
                    breakdown = null
                  }
                }
              },
              onResetClick = {
                n1 = ""; d1 = ""; n2 = ""; d2 = ""; decInput = ""; mixWhole = ""; mixNum = ""; mixDen = ""
                errorMsg = null; breakdown = null
              },
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
// 2. EQUATION SOLVER SCREEN
// ======================================================

private enum class EquationMode(val label: String) {
  LINEAR("Linear"),
  QUADRATIC("Quadratic"),
  SYS_2X2("2×2 System"),
  SYS_3X3("3×3 System"),
}

@Composable
fun EquationSolverScreen(
  showBreakdown: Boolean,
  onRecordHistory: (String, String, String, String, String) -> Unit,
  onBack: () -> Unit,
) {
  var mode by rememberSaveable { mutableStateOf(EquationMode.QUADRATIC) }
  var a by rememberSaveable { mutableStateOf("1") }
  var b by rememberSaveable { mutableStateOf("-5") }
  var c by rememberSaveable { mutableStateOf("6") }

  // 2x2
  var a1 by rememberSaveable { mutableStateOf("2") }
  var b1 by rememberSaveable { mutableStateOf("3") }
  var c1 by rememberSaveable { mutableStateOf("8") }
  var a2 by rememberSaveable { mutableStateOf("1") }
  var b2 by rememberSaveable { mutableStateOf("-1") }
  var c2 by rememberSaveable { mutableStateOf("-1") }

  // 3x3
  val grid3 = remember { mutableStateListOf("1", "1", "1", "6", "0", "2", "5", "-4", "2", "5", "-1", "27") }

  var errorMsg by remember { mutableStateOf<String?>(null) }
  var breakdown by remember { mutableStateOf<StepBreakdown?>(null) }

  Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
    CalcoraTopBar(title = "Equation Solver", subtitle = "Linear, Quadratic & Systems", onBack = onBack)
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
      LazyColumn(
        modifier = Modifier.fillMaxSize().widthIn(max = 720.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        item {
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            EquationMode.entries.forEach { m ->
              FilterChip(
                selected = mode == m,
                onClick = { mode = m; errorMsg = null; breakdown = null },
                label = { Text(m.label, maxLines = 1) },
                modifier = Modifier.weight(1f),
              )
            }
          }
        }

        item {
          CalcoraSectionCard {
            when (mode) {
              EquationMode.LINEAR -> {
                Text("ax + b = 0", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  CalcoraNumberField(a, { a = it }, "a", Modifier.weight(1f))
                  CalcoraNumberField(b, { b = it }, "b", Modifier.weight(1f))
                }
              }
              EquationMode.QUADRATIC -> {
                Text("ax² + bx + c = 0", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  CalcoraNumberField(a, { a = it }, "a", Modifier.weight(1f))
                  CalcoraNumberField(b, { b = it }, "b", Modifier.weight(1f))
                  CalcoraNumberField(c, { c = it }, "c", Modifier.weight(1f))
                }
              }
              EquationMode.SYS_2X2 -> {
                Text("a₁x + b₁y = c₁  and  a₂x + b₂y = c₂", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                  CalcoraNumberField(a1, { a1 = it }, "a₁", Modifier.weight(1f))
                  CalcoraNumberField(b1, { b1 = it }, "b₁", Modifier.weight(1f))
                  CalcoraNumberField(c1, { c1 = it }, "c₁", Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                  CalcoraNumberField(a2, { a2 = it }, "a₂", Modifier.weight(1f))
                  CalcoraNumberField(b2, { b2 = it }, "b₂", Modifier.weight(1f))
                  CalcoraNumberField(c2, { c2 = it }, "c₂", Modifier.weight(1f))
                }
              }
              EquationMode.SYS_3X3 -> {
                Text("aᵢx + bᵢy + cᵢz = dᵢ", style = MaterialTheme.typography.titleMedium)
                for (r in 0 until 3) {
                  val base = r * 4
                  Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    CalcoraNumberField(grid3[base], { grid3[base] = it }, "x${r + 1}", Modifier.weight(1f))
                    CalcoraNumberField(grid3[base + 1], { grid3[base + 1] = it }, "y${r + 1}", Modifier.weight(1f))
                    CalcoraNumberField(grid3[base + 2], { grid3[base + 2] = it }, "z${r + 1}", Modifier.weight(1f))
                    CalcoraNumberField(grid3[base + 3], { grid3[base + 3] = it }, "d${r + 1}", Modifier.weight(1f))
                  }
                }
              }
            }

            ActionButtonRow(
              primaryLabel = "Solve Equation",
              onPrimaryClick = {
                val out =
                  when (mode) {
                    EquationMode.LINEAR -> {
                      val av = a.toDoubleOrNull()
                      val bv = b.toDoubleOrNull()
                      if (av == null || bv == null) CalcOutcome.Error("Enter valid numbers for a and b")
                      else EquationEngine.solveLinear(av, bv)
                    }
                    EquationMode.QUADRATIC -> {
                      val av = a.toDoubleOrNull()
                      val bv = b.toDoubleOrNull()
                      val cv = c.toDoubleOrNull()
                      if (av == null || bv == null || cv == null) CalcOutcome.Error("Enter valid numbers for a, b, and c")
                      else EquationEngine.solveQuadratic(av, bv, cv)
                    }
                    EquationMode.SYS_2X2 -> {
                      val vals = listOf(a1, b1, c1, a2, b2, c2).map { it.toDoubleOrNull() }
                      if (vals.any { it == null }) CalcOutcome.Error("Enter valid numbers for all 6 coefficients")
                      else EquationEngine.solveSystem2x2(vals[0]!!, vals[1]!!, vals[2]!!, vals[3]!!, vals[4]!!, vals[5]!!)
                    }
                    EquationMode.SYS_3X3 -> {
                      val nums = grid3.map { it.toDoubleOrNull() }
                      if (nums.any { it == null }) CalcOutcome.Error("Enter valid numbers for all 12 entries")
                      else {
                        val coeff = Array(3) { r -> DoubleArray(3) { col -> nums[r * 4 + col]!! } }
                        val rhs = DoubleArray(3) { r -> nums[r * 4 + 3]!! }
                        EquationEngine.solveSystem3x3(coeff, rhs)
                      }
                    }
                  }
                when (out) {
                  is CalcOutcome.Success -> {
                    errorMsg = null
                    breakdown = out.breakdown
                    out.breakdown?.let {
                      onRecordHistory(it.inputValues, it.finalAnswer, "Equation Solver (${mode.label})", "Science", Routes.SCI_EQUATION)
                    }
                  }
                  is CalcOutcome.Error -> {
                    errorMsg = out.message
                    breakdown = null
                  }
                }
              },
              onResetClick = {
                a = ""; b = ""; c = ""; a1 = ""; b1 = ""; c1 = ""; a2 = ""; b2 = ""; c2 = ""
                for (i in grid3.indices) grid3[i] = ""
                errorMsg = null; breakdown = null
              },
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
// 3. MATRIX CALCULATOR SCREEN (2x2 & 3x3)
// ======================================================

@Composable
fun MatrixCalculatorScreen(
  showBreakdown: Boolean,
  onRecordHistory: (String, String, String, String, String) -> Unit,
  onBack: () -> Unit,
) {
  var size by rememberSaveable { mutableStateOf(2) }
  var op by rememberSaveable { mutableStateOf(MatrixOp.DET) }
  val matA = remember { mutableStateListOf("4", "7", "0", "2", "6", "0", "0", "0", "1") }
  val matB = remember { mutableStateListOf("1", "2", "0", "3", "4", "0", "0", "0", "1") }

  var errorMsg by remember { mutableStateOf<String?>(null) }
  var breakdown by remember { mutableStateOf<StepBreakdown?>(null) }

  val needsMatrixB = op == MatrixOp.ADD || op == MatrixOp.SUB || op == MatrixOp.MUL

  Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
    CalcoraTopBar(title = "Matrix Calculator", subtitle = "2×2 & 3×3 Linear Algebra", onBack = onBack)
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
      LazyColumn(
        modifier = Modifier.fillMaxSize().widthIn(max = 720.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        item {
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(2, 3).forEach { s ->
              FilterChip(
                selected = size == s,
                onClick = { size = s; errorMsg = null; breakdown = null },
                label = { Text("${s}×${s} Matrix") },
                modifier = Modifier.weight(1f),
              )
            }
          }
        }

        item {
          CalcoraSectionCard {
            Text("Operation", style = MaterialTheme.typography.titleMedium)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              listOf(MatrixOp.ADD, MatrixOp.SUB, MatrixOp.MUL).forEach { o ->
                FilterChip(
                  selected = op == o,
                  onClick = { op = o },
                  label = { Text(o.label) },
                  modifier = Modifier.weight(1f),
                )
              }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              listOf(MatrixOp.DET, MatrixOp.TRANSPOSE, MatrixOp.INVERSE).forEach { o ->
                FilterChip(
                  selected = op == o,
                  onClick = { op = o },
                  label = { Text(o.label) },
                  modifier = Modifier.weight(1f),
                )
              }
            }

            Text("Matrix A (${size}×${size})", style = MaterialTheme.typography.titleMedium)
            for (r in 0 until size) {
              Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                for (c in 0 until size) {
                  val idx = r * 3 + c
                  CalcoraNumberField(
                    value = matA[idx],
                    onValueChange = { matA[idx] = it },
                    label = "a${r + 1}${c + 1}",
                    modifier = Modifier.weight(1f),
                  )
                }
              }
            }

            if (needsMatrixB) {
              Text("Matrix B (${size}×${size})", style = MaterialTheme.typography.titleMedium)
              for (r in 0 until size) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                  for (c in 0 until size) {
                    val idx = r * 3 + c
                    CalcoraNumberField(
                      value = matB[idx],
                      onValueChange = { matB[idx] = it },
                      label = "b${r + 1}${c + 1}",
                      modifier = Modifier.weight(1f),
                    )
                  }
                }
              }
            }

            ActionButtonRow(
              primaryLabel = "Compute ${op.label}",
              onPrimaryClick = {
                val parsedA = mutableListOf<List<Double>>()
                var invalid = false
                for (r in 0 until size) {
                  val row = mutableListOf<Double>()
                  for (c in 0 until size) {
                    val v = matA[r * 3 + c].toDoubleOrNull()
                    if (v == null) invalid = true else row.add(v)
                  }
                  parsedA.add(row)
                }
                var parsedB: List<List<Double>>? = null
                if (needsMatrixB) {
                  val mb = mutableListOf<List<Double>>()
                  for (r in 0 until size) {
                    val row = mutableListOf<Double>()
                    for (c in 0 until size) {
                      val v = matB[r * 3 + c].toDoubleOrNull()
                      if (v == null) invalid = true else row.add(v)
                    }
                    mb.add(row)
                  }
                  parsedB = mb
                }

                if (invalid) {
                  errorMsg = "Enter valid numbers in all matrix cells"
                  breakdown = null
                } else {
                  when (val out = MatrixEngine.compute(size, parsedA, parsedB, op)) {
                    is CalcOutcome.Success -> {
                      errorMsg = null
                      breakdown = out.breakdown
                      out.breakdown?.let {
                        onRecordHistory("${op.label} (${size}×${size})", it.finalAnswer, "Matrix Calculator", "Science", Routes.SCI_MATRIX)
                      }
                    }
                    is CalcOutcome.Error -> {
                      errorMsg = out.message
                      breakdown = null
                    }
                  }
                }
              },
              onResetClick = {
                for (i in matA.indices) matA[i] = "0"
                for (i in matB.indices) matB[i] = "0"
                errorMsg = null; breakdown = null
              },
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
// 4. VECTOR CALCULATOR SCREEN (2D & 3D)
// ======================================================

@Composable
fun VectorCalculatorScreen(
  angleMode: AngleMode,
  showBreakdown: Boolean,
  onRecordHistory: (String, String, String, String, String) -> Unit,
  onBack: () -> Unit,
) {
  var is3D by rememberSaveable { mutableStateOf(true) }
  var op by rememberSaveable { mutableStateOf(VectorOp.DOT) }
  var ux by rememberSaveable { mutableStateOf("3") }
  var uy by rememberSaveable { mutableStateOf("4") }
  var uz by rememberSaveable { mutableStateOf("0") }
  var vx by rememberSaveable { mutableStateOf("1") }
  var vy by rememberSaveable { mutableStateOf("2") }
  var vz by rememberSaveable { mutableStateOf("5") }

  var errorMsg by remember { mutableStateOf<String?>(null) }
  var breakdown by remember { mutableStateOf<StepBreakdown?>(null) }

  Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
    CalcoraTopBar(title = "Vector Calculator", subtitle = "2D & 3D Vector Algebra", onBack = onBack)
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
      LazyColumn(
        modifier = Modifier.fillMaxSize().widthIn(max = 720.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        item {
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = !is3D, onClick = { is3D = false }, label = { Text("2D Vectors") }, modifier = Modifier.weight(1f))
            FilterChip(selected = is3D, onClick = { is3D = true }, label = { Text("3D Vectors") }, modifier = Modifier.weight(1f))
          }
        }

        item {
          CalcoraSectionCard {
            Text("Vector u", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              CalcoraNumberField(ux, { ux = it }, "uₓ", Modifier.weight(1f))
              CalcoraNumberField(uy, { uy = it }, "uᵧ", Modifier.weight(1f))
              if (is3D) CalcoraNumberField(uz, { uz = it }, "u_z", Modifier.weight(1f))
            }

            Text("Vector v", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              CalcoraNumberField(vx, { vx = it }, "vₓ", Modifier.weight(1f))
              CalcoraNumberField(vy, { vy = it }, "vᵧ", Modifier.weight(1f))
              if (is3D) CalcoraNumberField(vz, { vz = it }, "v_z", Modifier.weight(1f))
            }

            Text("Operation", style = MaterialTheme.typography.labelLarge)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              listOf(VectorOp.ADD, VectorOp.SUB, VectorOp.MAGNITUDE).forEach { o ->
                FilterChip(selected = op == o, onClick = { op = o }, label = { Text(o.label, maxLines = 1) }, modifier = Modifier.weight(1f))
              }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              listOf(VectorOp.DOT, VectorOp.CROSS, VectorOp.ANGLE).forEach { o ->
                FilterChip(selected = op == o, onClick = { op = o }, label = { Text(o.label, maxLines = 1) }, modifier = Modifier.weight(1f))
              }
            }

            ActionButtonRow(
              primaryLabel = "Calculate Vector",
              onPrimaryClick = {
                val uVals = listOf(ux, uy, if (is3D) uz else "0").map { it.toDoubleOrNull() }
                val vVals = listOf(vx, vy, if (is3D) vz else "0").map { it.toDoubleOrNull() }
                if (uVals.any { it == null } || vVals.any { it == null }) {
                  errorMsg = "Enter valid numbers for all vector components"
                  breakdown = null
                } else {
                  val out = VectorEngine.compute(is3D, uVals.map { it!! }.toDoubleArray(), vVals.map { it!! }.toDoubleArray(), op, angleMode)
                  when (out) {
                    is CalcOutcome.Success -> {
                      errorMsg = null
                      breakdown = out.breakdown
                      out.breakdown?.let {
                        onRecordHistory("${op.label} (${if (is3D) "3D" else "2D"})", it.finalAnswer, "Vector Calculator", "Science", Routes.SCI_VECTOR)
                      }
                    }
                    is CalcOutcome.Error -> {
                      errorMsg = out.message
                      breakdown = null
                    }
                  }
                }
              },
              onResetClick = {
                ux = ""; uy = ""; uz = ""; vx = ""; vy = ""; vz = ""
                errorMsg = null; breakdown = null
              },
            )
          }
        }

        errorMsg?.let { item { ValidationErrorCard(it) } }
        breakdown?.let { item { StepBreakdownCard(it, showDetails = showBreakdown) } }
      }
    }
  }
}
