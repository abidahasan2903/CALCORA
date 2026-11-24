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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import com.example.engine.CgpaSemesterInput
import com.example.engine.GpaSubjectInput
import com.example.engine.GradingScale
import com.example.engine.MarksSubjectInput
import com.example.engine.Routes
import com.example.engine.StudentEngine
import com.example.engine.WeightedItemInput
import com.example.model.CalcOutcome
import com.example.model.StepBreakdown
import com.example.ui.components.ActionButtonRow
import com.example.ui.components.CalcoraNumberField
import com.example.ui.components.CalcoraSectionCard
import com.example.ui.components.CalcoraTopBar
import com.example.ui.components.StepBreakdownCard
import com.example.ui.components.ValidationErrorCard

// ======================================================
// 1. GPA CALCULATOR SCREEN
// ======================================================

private data class MutableGpaRow(
  val id: Long,
  val subject: String,
  val gradeLetter: String,
  val gradePoint: String,
  val credits: String,
)

@Composable
fun GpaCalculatorScreen(
  showBreakdown: Boolean,
  onRecordHistory: (String, String, String, String, String) -> Unit,
  onBack: () -> Unit,
) {
  var scale by rememberSaveable { mutableStateOf(GradingScale.STANDARD_4_0) }
  var nextId by remember { mutableLongStateOf(4L) }
  val rows = remember {
    mutableStateListOf(
      MutableGpaRow(1L, "Mathematics", "A", "4.0", "3"),
      MutableGpaRow(2L, "Physics", "B", "3.0", "4"),
      MutableGpaRow(3L, "Economics", "A", "4.0", "3"),
    )
  }
  var errorMsg by remember { mutableStateOf<String?>(null) }
  var breakdown by remember { mutableStateOf<StepBreakdown?>(null) }

  Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
    CalcoraTopBar(title = "GPA Calculator", subtitle = "Credit-Weighted GPA", onBack = onBack)
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
      LazyColumn(
        modifier = Modifier.fillMaxSize().widthIn(max = 720.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        item {
          CalcoraSectionCard {
            Text("Grading Scale", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              items(GradingScale.entries) { s ->
                FilterChip(selected = scale == s, onClick = { scale = s; errorMsg = null; breakdown = null }, label = { Text(s.title) })
              }
            }
            Text(
              text = "Displayed Scale: ${scale.description}",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }

        item {
          CalcoraSectionCard {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
              Text("Subjects (${rows.size})", style = MaterialTheme.typography.titleMedium)
              OutlinedButton(
                onClick = { rows.add(MutableGpaRow(nextId++, "Subject ${rows.size + 1}", "A", "4.0", "3")) },
                modifier = Modifier.testTag("gpa_add_subject"),
              ) {
                Icon(Icons.Outlined.Add, contentDescription = null)
                Text("Add Subject")
              }
            }

            rows.forEachIndexed { idx, row ->
              Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                  OutlinedTextField(
                    value = row.subject,
                    onValueChange = { rows[idx] = row.copy(subject = it) },
                    label = { Text("Subject Name") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                  )
                  if (rows.size > 1) {
                    IconButton(onClick = { rows.removeAt(idx) }) {
                      Icon(Icons.Outlined.DeleteOutline, contentDescription = "Remove subject")
                    }
                  }
                }
                if (scale != GradingScale.CUSTOM) {
                  LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(scale.mappings) { map ->
                      FilterChip(
                        selected = row.gradeLetter == map.letter,
                        onClick = {
                          rows[idx] = row.copy(gradeLetter = map.letter, gradePoint = map.points.toString())
                        },
                        label = { Text("${map.letter} (${map.points})") },
                      )
                    }
                  }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  CalcoraNumberField(
                    value = row.gradePoint,
                    onValueChange = { rows[idx] = row.copy(gradePoint = it) },
                    label = "Grade Point",
                    modifier = Modifier.weight(1f),
                  )
                  CalcoraNumberField(
                    value = row.credits,
                    onValueChange = { rows[idx] = row.copy(credits = it) },
                    label = "Credit Hours",
                    modifier = Modifier.weight(1f),
                  )
                }
              }
            }

            ActionButtonRow(
              primaryLabel = "Calculate GPA",
              onPrimaryClick = {
                val parsed = mutableListOf<GpaSubjectInput>()
                for (r in rows) {
                  val gp = r.gradePoint.toDoubleOrNull()
                  val cr = r.credits.toDoubleOrNull()
                  if (gp == null || cr == null) {
                    errorMsg = "Enter valid numbers for grade point and credits in '${r.subject.ifBlank { "Subject" }}'"
                    breakdown = null
                    return@ActionButtonRow
                  }
                  parsed.add(GpaSubjectInput(r.id, r.subject, r.gradeLetter, gp, cr))
                }
                when (val out = StudentEngine.calculateGpa(parsed, scale)) {
                  is CalcOutcome.Success -> {
                    errorMsg = null; breakdown = out.breakdown
                    out.breakdown?.let { onRecordHistory(it.inputValues, it.finalAnswer, "GPA Calculator", "Student", Routes.STU_GPA) }
                  }
                  is CalcOutcome.Error -> { errorMsg = out.message; breakdown = null }
                }
              },
              onResetClick = {
                rows.clear()
                rows.add(MutableGpaRow(nextId++, "Subject 1", "A", "4.0", "3"))
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
// 2. CGPA CALCULATOR SCREEN
// ======================================================

private data class MutableCgpaRow(
  val id: Long,
  val name: String,
  val gpa: String,
  val credits: String,
)

@Composable
fun CgpaCalculatorScreen(
  showBreakdown: Boolean,
  onRecordHistory: (String, String, String, String, String) -> Unit,
  onBack: () -> Unit,
) {
  var maxScale by rememberSaveable { mutableStateOf(4.0) }
  var nextId by remember { mutableLongStateOf(3L) }
  val rows = remember {
    mutableStateListOf(
      MutableCgpaRow(1L, "Semester 1", "3.65", "15"),
      MutableCgpaRow(2L, "Semester 2", "3.85", "18"),
    )
  }
  var errorMsg by remember { mutableStateOf<String?>(null) }
  var breakdown by remember { mutableStateOf<StepBreakdown?>(null) }

  Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
    CalcoraTopBar(title = "CGPA Calculator", subtitle = "Cumulative Semester GPA", onBack = onBack)
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
      LazyColumn(
        modifier = Modifier.fillMaxSize().widthIn(max = 720.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        item {
          CalcoraSectionCard {
            Text("Scale Maximum", style = MaterialTheme.typography.labelLarge)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              listOf(4.0, 5.0, 10.0).forEach { s ->
                FilterChip(selected = maxScale == s, onClick = { maxScale = s }, label = { Text("${s.toInt()}.0 Scale") }, modifier = Modifier.weight(1f))
              }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
              Text("Semesters", style = MaterialTheme.typography.titleMedium)
              OutlinedButton(onClick = { rows.add(MutableCgpaRow(nextId++, "Semester ${rows.size + 1}", "", "15")) }) {
                Icon(Icons.Outlined.Add, contentDescription = null)
                Text("Add Semester")
              }
            }
            rows.forEachIndexed { idx, row ->
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(row.name, { rows[idx] = row.copy(name = it) }, label = { Text("Semester") }, singleLine = true, modifier = Modifier.weight(1.2f))
                CalcoraNumberField(row.gpa, { rows[idx] = row.copy(gpa = it) }, "GPA", Modifier.weight(0.9f))
                CalcoraNumberField(row.credits, { rows[idx] = row.copy(credits = it) }, "Credits", Modifier.weight(0.9f))
                if (rows.size > 1) {
                  IconButton(onClick = { rows.removeAt(idx) }) {
                    Icon(Icons.Outlined.DeleteOutline, contentDescription = "Remove semester")
                  }
                }
              }
            }
            ActionButtonRow(
              primaryLabel = "Calculate CGPA",
              onPrimaryClick = {
                val parsed = mutableListOf<CgpaSemesterInput>()
                for (r in rows) {
                  val g = r.gpa.toDoubleOrNull(); val c = r.credits.toDoubleOrNull()
                  if (g == null || c == null) {
                    errorMsg = "Enter valid GPA and credit hours for all semesters"; breakdown = null; return@ActionButtonRow
                  }
                  parsed.add(CgpaSemesterInput(r.id, r.name, g, c))
                }
                when (val out = StudentEngine.calculateCgpa(parsed, maxScale)) {
                  is CalcOutcome.Success -> {
                    errorMsg = null; breakdown = out.breakdown
                    out.breakdown?.let { onRecordHistory(it.inputValues, it.finalAnswer, "CGPA Calculator", "Student", Routes.STU_CGPA) }
                  }
                  is CalcOutcome.Error -> { errorMsg = out.message; breakdown = null }
                }
              },
              onResetClick = { rows.clear(); rows.add(MutableCgpaRow(nextId++, "Semester 1", "", "")); errorMsg = null; breakdown = null },
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
// 3. MARKS CALCULATOR SCREEN
// ======================================================

private data class MutableMarksRow(
  val id: Long,
  val subject: String,
  val obtained: String,
  val total: String,
)

@Composable
fun MarksCalculatorScreen(
  showBreakdown: Boolean,
  onRecordHistory: (String, String, String, String, String) -> Unit,
  onBack: () -> Unit,
) {
  var selectedScale by rememberSaveable { mutableStateOf<GradingScale?>(GradingScale.STANDARD_4_0) }
  var nextId by remember { mutableLongStateOf(4L) }
  val rows = remember {
    mutableStateListOf(
      MutableMarksRow(1L, "English", "88", "100"),
      MutableMarksRow(2L, "Math", "94", "100"),
      MutableMarksRow(3L, "Science", "82", "100"),
    )
  }
  var errorMsg by remember { mutableStateOf<String?>(null) }
  var breakdown by remember { mutableStateOf<StepBreakdown?>(null) }

  Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
    CalcoraTopBar(title = "Marks Calculator", subtitle = "Total, Percentage, Average & Grade", onBack = onBack)
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
      LazyColumn(
        modifier = Modifier.fillMaxSize().widthIn(max = 720.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        item {
          CalcoraSectionCard {
            Text("Optional Grading Scale", style = MaterialTheme.typography.labelLarge)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              item {
                FilterChip(selected = selectedScale == null, onClick = { selectedScale = null }, label = { Text("No Grade") })
              }
              items(listOf(GradingScale.STANDARD_4_0, GradingScale.PLUS_MINUS_4_0, GradingScale.SCALE_5_0)) { s ->
                FilterChip(selected = selectedScale == s, onClick = { selectedScale = s }, label = { Text(s.title) })
              }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
              Text("Subjects", style = MaterialTheme.typography.titleMedium)
              OutlinedButton(onClick = { rows.add(MutableMarksRow(nextId++, "Subject ${rows.size + 1}", "", "100")) }) {
                Icon(Icons.Outlined.Add, contentDescription = null)
                Text("Add Subject")
              }
            }
            rows.forEachIndexed { idx, row ->
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(row.subject, { rows[idx] = row.copy(subject = it) }, label = { Text("Subject") }, singleLine = true, modifier = Modifier.weight(1.2f))
                CalcoraNumberField(row.obtained, { rows[idx] = row.copy(obtained = it) }, "Obtained", Modifier.weight(0.9f))
                CalcoraNumberField(row.total, { rows[idx] = row.copy(total = it) }, "Max", Modifier.weight(0.9f))
                if (rows.size > 1) {
                  IconButton(onClick = { rows.removeAt(idx) }) {
                    Icon(Icons.Outlined.DeleteOutline, contentDescription = "Remove subject")
                  }
                }
              }
            }
            ActionButtonRow(
              primaryLabel = "Calculate Marks",
              onPrimaryClick = {
                val parsed = mutableListOf<MarksSubjectInput>()
                for (r in rows) {
                  val ob = r.obtained.toDoubleOrNull(); val tot = r.total.toDoubleOrNull()
                  if (ob == null || tot == null) {
                    errorMsg = "Enter valid marks for all subjects"; breakdown = null; return@ActionButtonRow
                  }
                  parsed.add(MarksSubjectInput(r.id, r.subject, ob, tot))
                }
                when (val out = StudentEngine.calculateMarks(parsed, selectedScale)) {
                  is CalcOutcome.Success -> {
                    errorMsg = null; breakdown = out.breakdown
                    out.breakdown?.let { onRecordHistory(it.inputValues, it.finalAnswer, "Marks Calculator", "Student", Routes.STU_MARKS) }
                  }
                  is CalcOutcome.Error -> { errorMsg = out.message; breakdown = null }
                }
              },
              onResetClick = { rows.clear(); rows.add(MutableMarksRow(nextId++, "Subject 1", "", "100")); errorMsg = null; breakdown = null },
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
// 4. WEIGHTED AVERAGE CALCULATOR SCREEN
// ======================================================

private data class MutableWeightedRow(
  val id: Long,
  val label: String,
  val value: String,
  val weight: String,
)

@Composable
fun WeightedAverageCalculatorScreen(
  showBreakdown: Boolean,
  onRecordHistory: (String, String, String, String, String) -> Unit,
  onBack: () -> Unit,
) {
  var nextId by remember { mutableLongStateOf(4L) }
  val rows = remember {
    mutableStateListOf(
      MutableWeightedRow(1L, "Assignments", "88", "30"),
      MutableWeightedRow(2L, "Midterm", "82", "30"),
      MutableWeightedRow(3L, "Final Exam", "91", "40"),
    )
  }
  var errorMsg by remember { mutableStateOf<String?>(null) }
  var breakdown by remember { mutableStateOf<StepBreakdown?>(null) }

  Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
    CalcoraTopBar(title = "Weighted Average", subtitle = "Values & Custom Weights", onBack = onBack)
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
      LazyColumn(
        modifier = Modifier.fillMaxSize().widthIn(max = 720.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        item {
          CalcoraSectionCard {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
              Text("Weighted Items", style = MaterialTheme.typography.titleMedium)
              OutlinedButton(onClick = { rows.add(MutableWeightedRow(nextId++, "Item ${rows.size + 1}", "", "")) }) {
                Icon(Icons.Outlined.Add, contentDescription = null)
                Text("Add Row")
              }
            }
            rows.forEachIndexed { idx, row ->
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(row.label, { rows[idx] = row.copy(label = it) }, label = { Text("Label") }, singleLine = true, modifier = Modifier.weight(1.2f))
                CalcoraNumberField(row.value, { rows[idx] = row.copy(value = it) }, "Value", Modifier.weight(0.9f))
                CalcoraNumberField(row.weight, { rows[idx] = row.copy(weight = it) }, "Weight", Modifier.weight(0.9f))
                if (rows.size > 1) {
                  IconButton(onClick = { rows.removeAt(idx) }) {
                    Icon(Icons.Outlined.DeleteOutline, contentDescription = "Remove item")
                  }
                }
              }
            }
            ActionButtonRow(
              primaryLabel = "Calculate Weighted Avg",
              onPrimaryClick = {
                val parsed = mutableListOf<WeightedItemInput>()
                for (r in rows) {
                  val v = r.value.toDoubleOrNull(); val w = r.weight.toDoubleOrNull()
                  if (v == null || w == null) {
                    errorMsg = "Enter valid numbers for all values and weights"; breakdown = null; return@ActionButtonRow
                  }
                  parsed.add(WeightedItemInput(r.id, r.label, v, w))
                }
                when (val out = StudentEngine.calculateWeightedAverage(parsed)) {
                  is CalcOutcome.Success -> {
                    errorMsg = null; breakdown = out.breakdown
                    out.breakdown?.let { onRecordHistory(it.inputValues, it.finalAnswer, "Weighted Average", "Student", Routes.STU_WEIGHTED_AVG) }
                  }
                  is CalcOutcome.Error -> { errorMsg = out.message; breakdown = null }
                }
              },
              onResetClick = { rows.clear(); rows.add(MutableWeightedRow(nextId++, "Item 1", "", "")); errorMsg = null; breakdown = null },
            )
          }
        }
        errorMsg?.let { item { ValidationErrorCard(it) } }
        breakdown?.let { item { StepBreakdownCard(it, showDetails = showBreakdown) } }
      }
    }
  }
}
