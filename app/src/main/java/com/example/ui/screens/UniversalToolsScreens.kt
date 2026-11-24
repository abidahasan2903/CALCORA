package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cake
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import com.example.engine.AgeResult
import com.example.engine.CalcoraDate
import com.example.engine.DateDiffResult
import com.example.engine.DateTimeEngine
import com.example.engine.Routes
import com.example.model.CalcOutcome
import com.example.model.StepBreakdown
import com.example.ui.components.ActionButtonRow
import com.example.ui.components.CalcoraNumberField
import com.example.ui.components.CalcoraSectionCard
import com.example.ui.components.CalcoraTopBar
import com.example.ui.components.StepBreakdownCard
import com.example.ui.components.ValidationErrorCard
import com.example.ui.theme.JetBrainsMonoFontFamily
import java.util.Calendar
import java.util.TimeZone

private fun CalcoraDate.toUtcMillis(): Long {
  val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
  cal.clear()
  cal.set(year, month - 1, day, 12, 0, 0)
  return cal.timeInMillis
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalcoraDatePickerModal(
  initialDate: CalcoraDate,
  onDateSelected: (CalcoraDate) -> Unit,
  onDismiss: () -> Unit,
) {
  val state = rememberDatePickerState(initialSelectedDateMillis = initialDate.toUtcMillis())
  DatePickerDialog(
    onDismissRequest = onDismiss,
    confirmButton = {
      TextButton(onClick = {
        state.selectedDateMillis?.let { millis ->
          onDateSelected(CalcoraDate.fromEpochMillisUtc(millis))
        }
        onDismiss()
      }) {
        Text("Select")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel") }
    },
  ) {
    DatePicker(state = state)
  }
}

@Composable
private fun DateSelectorRow(
  label: String,
  date: CalcoraDate,
  onPickClick: () -> Unit,
  testTag: String,
  trailingAction: (@Composable () -> Unit)? = null,
) {
  Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
    Text(label, style = MaterialTheme.typography.labelLarge)
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      OutlinedButton(
        onClick = onPickClick,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.weight(1f).heightIn(min = 50.dp).testTag(testTag),
      ) {
        Icon(Icons.Outlined.CalendarToday, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("${date.formatDisplay()} (${date.formatIso()})")
      }
      trailingAction?.invoke()
    }
  }
}

// ======================================================
// 1. BIRTHDAY & AGE CALCULATOR SCREEN
// ======================================================

@Composable
fun BirthdayAgeCalculatorScreen(
  showBreakdown: Boolean,
  onRecordHistory: (String, String, String, String, String) -> Unit,
  onBack: () -> Unit,
) {
  val today = remember { CalcoraDate.today() }
  var birthYear by rememberSaveable { mutableStateOf(2002) }
  var birthMonth by rememberSaveable { mutableStateOf(5) }
  var birthDay by rememberSaveable { mutableStateOf(15) }

  var presentYear by rememberSaveable { mutableStateOf(today.year) }
  var presentMonth by rememberSaveable { mutableStateOf(today.month) }
  var presentDay by rememberSaveable { mutableStateOf(today.day) }

  val birthDate = remember(birthYear, birthMonth, birthDay) { CalcoraDate(birthYear, birthMonth, birthDay) }
  val presentDate = remember(presentYear, presentMonth, presentDay) { CalcoraDate(presentYear, presentMonth, presentDay) }

  var showBirthPicker by remember { mutableStateOf(false) }
  var showPresentPicker by remember { mutableStateOf(false) }
  var ageResult by remember { mutableStateOf<AgeResult?>(null) }
  var breakdown by remember { mutableStateOf<StepBreakdown?>(null) }
  var errorMsg by remember { mutableStateOf<String?>(null) }

  Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
    CalcoraTopBar(title = "Birthday & Age Calculator", subtitle = "Calendar-Aware Exact Age", onBack = onBack)
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
      LazyColumn(
        modifier = Modifier.fillMaxSize().widthIn(max = 720.dp).testTag("age_lazy_column"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        item {
          CalcoraSectionCard {
            DateSelectorRow(
              label = "Birthday / Date of Birth",
              date = birthDate,
              onPickClick = { showBirthPicker = true },
              testTag = "age_pick_birthday",
            )
            DateSelectorRow(
              label = "Present Date",
              date = presentDate,
              onPickClick = { showPresentPicker = true },
              testTag = "age_pick_present",
              trailingAction = {
                OutlinedButton(
                  onClick = {
                    val t = CalcoraDate.today()
                    presentYear = t.year; presentMonth = t.month; presentDay = t.day
                  },
                  shape = MaterialTheme.shapes.small,
                  modifier = Modifier.heightIn(min = 50.dp).testTag("age_today_button"),
                ) {
                  Icon(Icons.Outlined.Today, contentDescription = null)
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Today")
                }
              },
            )
            Button(
              onClick = {
                when (val out = DateTimeEngine.calculateAge(birthDate, presentDate)) {
                  is CalcOutcome.Success -> {
                    errorMsg = null
                    ageResult = out.data
                    breakdown = out.breakdown
                    onRecordHistory(
                      "DOB ${birthDate.formatIso()} → ${presentDate.formatIso()}",
                      out.data.formattedAge,
                      "Birthday & Age Calculator",
                      "Universal",
                      Routes.UNI_AGE,
                    )
                  }
                  is CalcOutcome.Error -> {
                    errorMsg = out.message
                    ageResult = null
                    breakdown = null
                  }
                }
              },
              shape = MaterialTheme.shapes.medium,
              modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("age_calculate_button"),
            ) {
              Icon(Icons.Outlined.Cake, contentDescription = null)
              Spacer(modifier = Modifier.width(8.dp))
              Text("Calculate Age")
            }
          }
        }

        errorMsg?.let { item { ValidationErrorCard(it) } }

        ageResult?.let { res ->
          item {
            Card(
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
              shape = MaterialTheme.shapes.large,
              modifier = Modifier.fillMaxWidth().testTag("age_result_card"),
            ) {
              Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("EXACT AGE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text(
                  text = res.formattedAge,
                  style = MaterialTheme.typography.headlineMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                  color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                AgeDetailRow("Completed Years / Months / Days", "${res.years} yrs, ${res.months} mos, ${res.days} days")
                AgeDetailRow("Birthday", res.birthDate.formatDisplay())
                AgeDetailRow("Present Date", res.presentDate.formatDisplay())
                AgeDetailRow("Total Days Lived", "${res.totalDaysLived} days")
                AgeDetailRow("Total Weeks", "${res.totalWeeks} weeks, ${res.remainingWeekDays} days")
                AgeDetailRow("Next Birthday", res.nextBirthday.formatDisplay())
                AgeDetailRow("Days Until Next Birthday", "${res.daysUntilNextBirthday} days remaining")
              }
            }
          }
        }

        breakdown?.let { item { StepBreakdownCard(it, showDetails = showBreakdown) } }
      }
    }
  }

  if (showBirthPicker) {
    CalcoraDatePickerModal(
      initialDate = birthDate,
      onDateSelected = { d -> birthYear = d.year; birthMonth = d.month; birthDay = d.day },
      onDismiss = { showBirthPicker = false },
    )
  }
  if (showPresentPicker) {
    CalcoraDatePickerModal(
      initialDate = presentDate,
      onDateSelected = { d -> presentYear = d.year; presentMonth = d.month; presentDay = d.day },
      onDismiss = { showPresentPicker = false },
    )
  }
}

@Composable
private fun AgeDetailRow(label: String, value: String) {
  Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
    Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
    Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
  }
}

// ======================================================
// 2. DATE DIFFERENCE CALCULATOR SCREEN
// ======================================================

@Composable
fun DateDifferenceCalculatorScreen(
  showBreakdown: Boolean,
  onRecordHistory: (String, String, String, String, String) -> Unit,
  onBack: () -> Unit,
) {
  val today = remember { CalcoraDate.today() }
  var startY by rememberSaveable { mutableStateOf(today.year) }
  var startM by rememberSaveable { mutableStateOf(1) }
  var startD by rememberSaveable { mutableStateOf(1) }
  var endY by rememberSaveable { mutableStateOf(today.year) }
  var endM by rememberSaveable { mutableStateOf(today.month) }
  var endD by rememberSaveable { mutableStateOf(today.day) }

  val startDate = remember(startY, startM, startD) { CalcoraDate(startY, startM, startD) }
  val endDate = remember(endY, endM, endD) { CalcoraDate(endY, endM, endD) }
  var pickStart by remember { mutableStateOf(false) }
  var pickEnd by remember { mutableStateOf(false) }
  var diffResult by remember { mutableStateOf<DateDiffResult?>(null) }
  var breakdown by remember { mutableStateOf<StepBreakdown?>(null) }

  Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
    CalcoraTopBar(title = "Date Difference", subtitle = "Calendar Interval & Total Days", onBack = onBack)
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
      LazyColumn(
        modifier = Modifier.fillMaxSize().widthIn(max = 720.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        item {
          CalcoraSectionCard {
            DateSelectorRow("Start Date", startDate, { pickStart = true }, "datediff_start")
            DateSelectorRow("End Date", endDate, { pickEnd = true }, "datediff_end")
            Button(
              onClick = {
                val out = DateTimeEngine.dateDifference(startDate, endDate)
                if (out is CalcOutcome.Success) {
                  diffResult = out.data
                  breakdown = out.breakdown
                  out.breakdown?.let { onRecordHistory(it.inputValues, it.finalAnswer, "Date Difference", "Universal", Routes.UNI_DATE_DIFF) }
                }
              },
              modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp).testTag("datediff_calculate"),
            ) {
              Text("Calculate Date Difference")
            }
          }
        }
        diffResult?.let { r ->
          item {
            CalcoraSectionCard {
              Text("${r.years} Years, ${r.months} Months, ${r.days} Days", style = MaterialTheme.typography.headlineMedium)
              Text("Total Exact Days: ${r.totalDays} days", style = MaterialTheme.typography.bodyLarge)
              Text("Weeks & Days: ${r.totalWeeks} weeks, ${r.remainingWeekDays} days", style = MaterialTheme.typography.bodyMedium)
            }
          }
        }
        breakdown?.let { item { StepBreakdownCard(it, showDetails = showBreakdown) } }
      }
    }
  }

  if (pickStart) {
    CalcoraDatePickerModal(startDate, { d -> startY = d.year; startM = d.month; startD = d.day }, { pickStart = false })
  }
  if (pickEnd) {
    CalcoraDatePickerModal(endDate, { d -> endY = d.year; endM = d.month; endD = d.day }, { pickEnd = false })
  }
}

// ======================================================
// 3. DATE ADD & SUBTRACT SCREEN
// ======================================================

@Composable
fun DateAddSubtractCalculatorScreen(
  showBreakdown: Boolean,
  onRecordHistory: (String, String, String, String, String) -> Unit,
  onBack: () -> Unit,
) {
  val today = remember { CalcoraDate.today() }
  var baseY by rememberSaveable { mutableStateOf(today.year) }
  var baseM by rememberSaveable { mutableStateOf(today.month) }
  var baseD by rememberSaveable { mutableStateOf(today.day) }
  val baseDate = remember(baseY, baseM, baseD) { CalcoraDate(baseY, baseM, baseD) }

  var isSubtract by rememberSaveable { mutableStateOf(false) }
  var unit by rememberSaveable { mutableStateOf(DateTimeEngine.DateUnit.DAYS) }
  var amountText by rememberSaveable { mutableStateOf("30") }
  var pickBase by remember { mutableStateOf(false) }
  var errorMsg by remember { mutableStateOf<String?>(null) }
  var breakdown by remember { mutableStateOf<StepBreakdown?>(null) }

  Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
    CalcoraTopBar(title = "Date Add & Subtract", subtitle = "Safe Month-End & Leap Handling", onBack = onBack)
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
      LazyColumn(
        modifier = Modifier.fillMaxSize().widthIn(max = 720.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        item {
          CalcoraSectionCard {
            DateSelectorRow("Starting Date", baseDate, { pickBase = true }, "dateadd_pick")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              FilterChip(selected = !isSubtract, onClick = { isSubtract = false }, label = { Text("Add (+)") }, modifier = Modifier.weight(1f))
              FilterChip(selected = isSubtract, onClick = { isSubtract = true }, label = { Text("Subtract (-)") }, modifier = Modifier.weight(1f))
            }
            CalcoraNumberField(amountText, { amountText = it }, "Amount", allowDecimal = false, allowNegative = false)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              DateTimeEngine.DateUnit.entries.forEach { u ->
                FilterChip(selected = unit == u, onClick = { unit = u }, label = { Text(u.label) }, modifier = Modifier.weight(1f))
              }
            }
            ActionButtonRow(
              primaryLabel = "Compute Date",
              onPrimaryClick = {
                val amt = amountText.toIntOrNull()
                if (amt == null) {
                  errorMsg = "Enter a valid non-negative integer amount"; breakdown = null
                } else {
                  when (val out = DateTimeEngine.addOrSubtract(baseDate, amt, unit, isSubtract)) {
                    is CalcOutcome.Success -> {
                      errorMsg = null; breakdown = out.breakdown
                      out.breakdown?.let { onRecordHistory(it.inputValues, it.finalAnswer, "Date Add/Subtract", "Universal", Routes.UNI_DATE_ADD_SUB) }
                    }
                    is CalcOutcome.Error -> { errorMsg = out.message; breakdown = null }
                  }
                }
              },
              onResetClick = { amountText = ""; errorMsg = null; breakdown = null },
            )
          }
        }
        errorMsg?.let { item { ValidationErrorCard(it) } }
        breakdown?.let { item { StepBreakdownCard(it, showDetails = showBreakdown) } }
      }
    }
  }
  if (pickBase) {
    CalcoraDatePickerModal(baseDate, { d -> baseY = d.year; baseM = d.month; baseD = d.day }, { pickBase = false })
  }
}

// ======================================================
// 4. TIME DURATION CALCULATOR SCREEN
// ======================================================

@Composable
fun TimeDurationCalculatorScreen(
  showBreakdown: Boolean,
  onRecordHistory: (String, String, String, String, String) -> Unit,
  onBack: () -> Unit,
) {
  var sh by rememberSaveable { mutableStateOf("09") }
  var sm by rememberSaveable { mutableStateOf("15") }
  var ss by rememberSaveable { mutableStateOf("00") }
  var eh by rememberSaveable { mutableStateOf("17") }
  var em by rememberSaveable { mutableStateOf("45") }
  var es by rememberSaveable { mutableStateOf("30") }
  var allowOvernight by rememberSaveable { mutableStateOf(true) }
  var errorMsg by remember { mutableStateOf<String?>(null) }
  var breakdown by remember { mutableStateOf<StepBreakdown?>(null) }

  Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
    CalcoraTopBar(title = "Time Duration", subtitle = "Hours, Minutes & Seconds", onBack = onBack)
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
      LazyColumn(
        modifier = Modifier.fillMaxSize().widthIn(max = 720.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        item {
          CalcoraSectionCard {
            Text("Start Time (24h)", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              CalcoraNumberField(sh, { sh = it }, "Hr", Modifier.weight(1f), allowDecimal = false)
              CalcoraNumberField(sm, { sm = it }, "Min", Modifier.weight(1f), allowDecimal = false)
              CalcoraNumberField(ss, { ss = it }, "Sec", Modifier.weight(1f), allowDecimal = false)
            }
            Text("End Time (24h)", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              CalcoraNumberField(eh, { eh = it }, "Hr", Modifier.weight(1f), allowDecimal = false)
              CalcoraNumberField(em, { em = it }, "Min", Modifier.weight(1f), allowDecimal = false)
              CalcoraNumberField(es, { es = it }, "Sec", Modifier.weight(1f), allowDecimal = false)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
              Text("Allow Overnight Crossing Midnight", style = MaterialTheme.typography.bodyLarge)
              Switch(checked = allowOvernight, onCheckedChange = { allowOvernight = it })
            }
            ActionButtonRow(
              primaryLabel = "Calculate Duration",
              onPrimaryClick = {
                val vals = listOf(sh, sm, ss, eh, em, es).map { it.toIntOrNull() }
                if (vals.any { it == null }) {
                  errorMsg = "Enter valid integer hours, minutes, and seconds"; breakdown = null
                } else {
                  when (val out = DateTimeEngine.timeDuration(vals[0]!!, vals[1]!!, vals[2]!!, vals[3]!!, vals[4]!!, vals[5]!!, allowOvernight)) {
                    is CalcOutcome.Success -> {
                      errorMsg = null; breakdown = out.breakdown
                      out.breakdown?.let { onRecordHistory(it.inputValues, it.finalAnswer, "Time Duration", "Universal", Routes.UNI_TIME_DURATION) }
                    }
                    is CalcOutcome.Error -> { errorMsg = out.message; breakdown = null }
                  }
                }
              },
              onResetClick = { sh = "0"; sm = "0"; ss = "0"; eh = "0"; em = "0"; es = "0"; errorMsg = null; breakdown = null },
            )
          }
        }
        errorMsg?.let { item { ValidationErrorCard(it) } }
        breakdown?.let { item { StepBreakdownCard(it, showDetails = showBreakdown) } }
      }
    }
  }
}
