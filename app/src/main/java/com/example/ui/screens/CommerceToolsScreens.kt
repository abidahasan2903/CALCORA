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
import com.example.engine.CommerceEngine
import com.example.engine.CompoundFrequency
import com.example.engine.DepreciationResult
import com.example.engine.NumberFormatter
import com.example.engine.PaymentFrequency
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

@Composable
fun CommerceToolScreen(
  route: String,
  showBreakdown: Boolean,
  onRecordHistory: (String, String, String, String, String) -> Unit,
  onBack: () -> Unit,
) {
  when (route) {
    Routes.COM_PROFIT_LOSS -> SimpleCommerceForm(
      title = "Profit & Loss",
      subtitle = "Cost Price vs Selling Price",
      label1 = "Cost Price (CP)",
      label2 = "Selling Price (SP)",
      default1 = "500",
      default2 = "650",
      showBreakdown = showBreakdown,
      onCalculate = { a, b, _, _ -> CommerceEngine.profitAndLoss(a, b) },
      route = route,
      onRecordHistory = onRecordHistory,
      onBack = onBack,
    )
    Routes.COM_DISCOUNT -> SimpleCommerceForm(
      title = "Discount Calculator",
      subtitle = "Original Price & Savings",
      label1 = "Original Price",
      label2 = "Discount Percentage (%)",
      default1 = "1200",
      default2 = "20",
      showBreakdown = showBreakdown,
      onCalculate = { a, b, _, _ -> CommerceEngine.discount(a, b) },
      route = route,
      onRecordHistory = onRecordHistory,
      onBack = onBack,
    )
    Routes.COM_MARKUP -> SimpleCommerceForm(
      title = "Markup Calculator",
      subtitle = "Markup vs Profit Margin Comparison",
      label1 = "Cost",
      label2 = "Markup Percentage (%)",
      default1 = "200",
      default2 = "25",
      showBreakdown = showBreakdown,
      onCalculate = { a, b, _, _ -> CommerceEngine.markup(a, b) },
      route = route,
      onRecordHistory = onRecordHistory,
      onBack = onBack,
    )
    Routes.COM_VAT_TAX -> VatTaxScreen(showBreakdown, onRecordHistory, onBack)
    Routes.COM_SIMPLE_INTEREST -> SimpleCommerceForm(
      title = "Simple Interest",
      subtitle = "SI = (P × R × T) / 100",
      label1 = "Principal Amount (P)",
      label2 = "Annual Rate R (%)",
      label3 = "Time T (Years)",
      default1 = "10000",
      default2 = "8",
      default3 = "3",
      showBreakdown = showBreakdown,
      onCalculate = { a, b, c, _ -> CommerceEngine.simpleInterest(a, b, c!!) },
      route = route,
      onRecordHistory = onRecordHistory,
      onBack = onBack,
    )
    Routes.COM_COMPOUND_INTEREST -> CompoundInterestScreen(showBreakdown, onRecordHistory, onBack)
    Routes.COM_LOAN_EMI -> LoanEmiScreen(showBreakdown, onRecordHistory, onBack)
    Routes.COM_ROI -> SimpleCommerceForm(
      title = "ROI Calculator",
      subtitle = "Return on Investment",
      label1 = "Initial Investment",
      label2 = "Final Value",
      default1 = "5000",
      default2 = "6800",
      showBreakdown = showBreakdown,
      onCalculate = { a, b, _, _ -> CommerceEngine.roi(a, b) },
      route = route,
      onRecordHistory = onRecordHistory,
      onBack = onBack,
    )
    Routes.COM_BREAK_EVEN -> SimpleCommerceForm(
      title = "Break-even Calculator",
      subtitle = "Fixed Costs, Price & Variable Cost",
      label1 = "Fixed Costs",
      label2 = "Selling Price per Unit",
      label3 = "Variable Cost per Unit",
      default1 = "25000",
      default2 = "50",
      default3 = "30",
      showBreakdown = showBreakdown,
      onCalculate = { a, b, c, _ -> CommerceEngine.breakEven(a, b, c!!) },
      route = route,
      onRecordHistory = onRecordHistory,
      onBack = onBack,
    )
    Routes.COM_COMMISSION -> SimpleCommerceForm(
      title = "Commission Calculator",
      subtitle = "Sales Commission & Earnings",
      label1 = "Sales Amount",
      label2 = "Commission Rate (%)",
      label3 = "Base Salary (Optional)",
      default1 = "15000",
      default2 = "6",
      default3 = "1000",
      showBreakdown = showBreakdown,
      onCalculate = { a, b, c, _ -> CommerceEngine.commission(a, b, c ?: 0.0) },
      route = route,
      onRecordHistory = onRecordHistory,
      onBack = onBack,
    )
    Routes.COM_SALARY -> SimpleCommerceForm(
      title = "Salary Calculator",
      subtitle = "Gross Salary, Deductions & Net Pay",
      label1 = "Basic Salary",
      label2 = "Allowances",
      label3 = "Bonuses",
      label4 = "Total Deductions",
      default1 = "4500",
      default2 = "800",
      default3 = "400",
      default4 = "650",
      showBreakdown = showBreakdown,
      onCalculate = { a, b, c, d -> CommerceEngine.salary(a, b, c!!, d!!) },
      route = route,
      onRecordHistory = onRecordHistory,
      onBack = onBack,
    )
    Routes.COM_REVENUE_EXPENSE -> RevenueExpenseScreen(showBreakdown, onRecordHistory, onBack)
    Routes.COM_DEPRECIATION -> DepreciationScreen(showBreakdown, onRecordHistory, onBack)
    Routes.COM_ACCOUNTING -> AccountingHelpersScreen(showBreakdown, onRecordHistory, onBack)
  }
}

@Composable
private fun SimpleCommerceForm(
  title: String,
  subtitle: String,
  label1: String,
  label2: String,
  label3: String? = null,
  label4: String? = null,
  default1: String,
  default2: String,
  default3: String = "",
  default4: String = "",
  showBreakdown: Boolean,
  onCalculate: (Double, Double, Double?, Double?) -> CalcOutcome<*>,
  route: String,
  onRecordHistory: (String, String, String, String, String) -> Unit,
  onBack: () -> Unit,
) {
  var v1 by rememberSaveable { mutableStateOf(default1) }
  var v2 by rememberSaveable { mutableStateOf(default2) }
  var v3 by rememberSaveable { mutableStateOf(default3) }
  var v4 by rememberSaveable { mutableStateOf(default4) }
  var errorMsg by remember { mutableStateOf<String?>(null) }
  var breakdown by remember { mutableStateOf<StepBreakdown?>(null) }

  Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
    CalcoraTopBar(title = title, subtitle = subtitle, onBack = onBack)
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
      LazyColumn(
        modifier = Modifier.fillMaxSize().widthIn(max = 720.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        item {
          CalcoraSectionCard {
            CalcoraNumberField(v1, { v1 = it }, label1, testTag = "com_input_1")
            CalcoraNumberField(v2, { v2 = it }, label2, testTag = "com_input_2")
            label3?.let { CalcoraNumberField(v3, { v3 = it }, it, testTag = "com_input_3") }
            label4?.let { CalcoraNumberField(v4, { v4 = it }, it, testTag = "com_input_4") }

            ActionButtonRow(
              primaryLabel = "Calculate",
              onPrimaryClick = {
                val n1 = v1.toDoubleOrNull()
                val n2 = v2.toDoubleOrNull()
                val n3 = if (label3 != null) v3.toDoubleOrNull() else null
                val n4 = if (label4 != null) v4.toDoubleOrNull() else null
                if (n1 == null || n2 == null || (label3 != null && n3 == null) || (label4 != null && n4 == null)) {
                  errorMsg = "Enter valid numbers for all required fields"
                  breakdown = null
                } else {
                  when (val out = onCalculate(n1, n2, n3, n4)) {
                    is CalcOutcome.Success -> {
                      errorMsg = null; breakdown = out.breakdown
                      out.breakdown?.let { onRecordHistory(it.inputValues, it.finalAnswer, title, "Commerce", route) }
                    }
                    is CalcOutcome.Error -> { errorMsg = out.message; breakdown = null }
                  }
                }
              },
              onResetClick = { v1 = ""; v2 = ""; v3 = ""; v4 = ""; errorMsg = null; breakdown = null },
            )
          }
        }
        errorMsg?.let { item { ValidationErrorCard(it) } }
        breakdown?.let { item { StepBreakdownCard(it, showDetails = showBreakdown) } }
      }
    }
  }
}

@Composable
private fun VatTaxScreen(
  showBreakdown: Boolean,
  onRecordHistory: (String, String, String, String, String) -> Unit,
  onBack: () -> Unit,
) {
  var amount by rememberSaveable { mutableStateOf("1000") }
  var rate by rememberSaveable { mutableStateOf("15") }
  var isInclusive by rememberSaveable { mutableStateOf(false) }
  var errorMsg by remember { mutableStateOf<String?>(null) }
  var breakdown by remember { mutableStateOf<StepBreakdown?>(null) }

  Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
    CalcoraTopBar(title = "VAT & Tax Calculator", subtitle = "Inclusive & Exclusive Tax", onBack = onBack)
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
      LazyColumn(
        modifier = Modifier.fillMaxSize().widthIn(max = 720.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        item {
          CalcoraSectionCard {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              FilterChip(selected = !isInclusive, onClick = { isInclusive = false }, label = { Text("Tax Exclusive (+VAT)") }, modifier = Modifier.weight(1f))
              FilterChip(selected = isInclusive, onClick = { isInclusive = true }, label = { Text("Tax Inclusive (Extract)") }, modifier = Modifier.weight(1f))
            }
            CalcoraNumberField(amount, { amount = it }, if (isInclusive) "Gross Amount (Incl. Tax)" else "Net Amount (Excl. Tax)")
            CalcoraNumberField(rate, { rate = it }, "Tax / VAT Rate (%)")
            ActionButtonRow(
              primaryLabel = "Calculate VAT/Tax",
              onPrimaryClick = {
                val a = amount.toDoubleOrNull(); val r = rate.toDoubleOrNull()
                if (a == null || r == null) {
                  errorMsg = "Enter valid numbers for amount and tax rate"; breakdown = null
                } else {
                  when (val out = CommerceEngine.vatAndTax(a, r, isInclusive)) {
                    is CalcOutcome.Success -> {
                      errorMsg = null; breakdown = out.breakdown
                      out.breakdown?.let { onRecordHistory(it.inputValues, it.finalAnswer, "VAT & Tax", "Commerce", Routes.COM_VAT_TAX) }
                    }
                    is CalcOutcome.Error -> { errorMsg = out.message; breakdown = null }
                  }
                }
              },
              onResetClick = { amount = ""; rate = ""; errorMsg = null; breakdown = null },
            )
          }
        }
        errorMsg?.let { item { ValidationErrorCard(it) } }
        breakdown?.let { item { StepBreakdownCard(it, showDetails = showBreakdown) } }
      }
    }
  }
}

@Composable
private fun CompoundInterestScreen(
  showBreakdown: Boolean,
  onRecordHistory: (String, String, String, String, String) -> Unit,
  onBack: () -> Unit,
) {
  var principal by rememberSaveable { mutableStateOf("10000") }
  var rate by rememberSaveable { mutableStateOf("8") }
  var time by rememberSaveable { mutableStateOf("5") }
  var freq by rememberSaveable { mutableStateOf(CompoundFrequency.QUARTERLY) }
  var errorMsg by remember { mutableStateOf<String?>(null) }
  var breakdown by remember { mutableStateOf<StepBreakdown?>(null) }

  Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
    CalcoraTopBar(title = "Compound Interest", subtitle = "A = P(1 + r/n)^(nt)", onBack = onBack)
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
      LazyColumn(
        modifier = Modifier.fillMaxSize().widthIn(max = 720.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        item {
          CalcoraSectionCard {
            CalcoraNumberField(principal, { principal = it }, "Principal (P)")
            CalcoraNumberField(rate, { rate = it }, "Annual Interest Rate (%)")
            CalcoraNumberField(time, { time = it }, "Time (Years)")
            Text("Compounding Frequency", style = MaterialTheme.typography.labelLarge)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              items(CompoundFrequency.entries) { f ->
                FilterChip(selected = freq == f, onClick = { freq = f }, label = { Text(f.label) })
              }
            }
            ActionButtonRow(
              primaryLabel = "Calculate Interest",
              onPrimaryClick = {
                val p = principal.toDoubleOrNull(); val r = rate.toDoubleOrNull(); val t = time.toDoubleOrNull()
                if (p == null || r == null || t == null) {
                  errorMsg = "Enter valid numbers for principal, rate, and time"; breakdown = null
                } else {
                  when (val out = CommerceEngine.compoundInterest(p, r, t, freq)) {
                    is CalcOutcome.Success -> {
                      errorMsg = null; breakdown = out.breakdown
                      out.breakdown?.let { onRecordHistory(it.inputValues, it.finalAnswer, "Compound Interest", "Commerce", Routes.COM_COMPOUND_INTEREST) }
                    }
                    is CalcOutcome.Error -> { errorMsg = out.message; breakdown = null }
                  }
                }
              },
              onResetClick = { principal = ""; rate = ""; time = ""; errorMsg = null; breakdown = null },
            )
          }
        }
        errorMsg?.let { item { ValidationErrorCard(it) } }
        breakdown?.let { item { StepBreakdownCard(it, showDetails = showBreakdown) } }
      }
    }
  }
}

@Composable
private fun LoanEmiScreen(
  showBreakdown: Boolean,
  onRecordHistory: (String, String, String, String, String) -> Unit,
  onBack: () -> Unit,
) {
  var loan by rememberSaveable { mutableStateOf("250000") }
  var rate by rememberSaveable { mutableStateOf("6.5") }
  var years by rememberSaveable { mutableStateOf("15") }
  var freq by rememberSaveable { mutableStateOf(PaymentFrequency.MONTHLY) }
  var errorMsg by remember { mutableStateOf<String?>(null) }
  var breakdown by remember { mutableStateOf<StepBreakdown?>(null) }

  Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
    CalcoraTopBar(title = "Loan & EMI Calculator", subtitle = "Amortization & Payment Schedule", onBack = onBack)
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
      LazyColumn(
        modifier = Modifier.fillMaxSize().widthIn(max = 720.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        item {
          CalcoraSectionCard {
            CalcoraNumberField(loan, { loan = it }, "Loan Amount")
            CalcoraNumberField(rate, { rate = it }, "Annual Interest Rate (%)")
            CalcoraNumberField(years, { years = it }, "Loan Duration (Years)")
            Text("Payment Frequency", style = MaterialTheme.typography.labelLarge)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              items(PaymentFrequency.entries) { f ->
                FilterChip(selected = freq == f, onClick = { freq = f }, label = { Text(f.label) })
              }
            }
            ActionButtonRow(
              primaryLabel = "Calculate EMI",
              onPrimaryClick = {
                val p = loan.toDoubleOrNull(); val r = rate.toDoubleOrNull(); val y = years.toDoubleOrNull()
                if (p == null || r == null || y == null) {
                  errorMsg = "Enter valid numbers for loan amount, rate, and duration"; breakdown = null
                } else {
                  when (val out = CommerceEngine.loanEmi(p, r, y, freq)) {
                    is CalcOutcome.Success -> {
                      errorMsg = null; breakdown = out.breakdown
                      out.breakdown?.let { onRecordHistory(it.inputValues, it.finalAnswer, "Loan & EMI", "Commerce", Routes.COM_LOAN_EMI) }
                    }
                    is CalcOutcome.Error -> { errorMsg = out.message; breakdown = null }
                  }
                }
              },
              onResetClick = { loan = ""; rate = ""; years = ""; errorMsg = null; breakdown = null },
            )
          }
        }
        errorMsg?.let { item { ValidationErrorCard(it) } }
        breakdown?.let { item { StepBreakdownCard(it, showDetails = showBreakdown) } }
      }
    }
  }
}

@Composable
private fun RevenueExpenseScreen(
  showBreakdown: Boolean,
  onRecordHistory: (String, String, String, String, String) -> Unit,
  onBack: () -> Unit,
) {
  val revenues = remember { mutableStateListOf("12000", "4500") }
  val expenses = remember { mutableStateListOf("5500", "1800") }
  var errorMsg by remember { mutableStateOf<String?>(null) }
  var breakdown by remember { mutableStateOf<StepBreakdown?>(null) }

  Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
    CalcoraTopBar(title = "Revenue & Expense", subtitle = "Session Multi-Entry Ledger", onBack = onBack)
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
      LazyColumn(
        modifier = Modifier.fillMaxSize().widthIn(max = 720.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        item {
          CalcoraSectionCard {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
              Text("Revenue Entries", style = MaterialTheme.typography.titleMedium)
              OutlinedButton(onClick = { revenues.add("") }) {
                Icon(Icons.Outlined.Add, contentDescription = null)
                Text("Add")
              }
            }
            revenues.forEachIndexed { idx, value ->
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                CalcoraNumberField(value, { revenues[idx] = it }, "Revenue #${idx + 1}", Modifier.weight(1f))
                if (revenues.size > 1) {
                  IconButton(onClick = { revenues.removeAt(idx) }) {
                    Icon(Icons.Outlined.DeleteOutline, contentDescription = "Remove")
                  }
                }
              }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
              Text("Expense Entries", style = MaterialTheme.typography.titleMedium)
              OutlinedButton(onClick = { expenses.add("") }) {
                Icon(Icons.Outlined.Add, contentDescription = null)
                Text("Add")
              }
            }
            expenses.forEachIndexed { idx, value ->
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                CalcoraNumberField(value, { expenses[idx] = it }, "Expense #${idx + 1}", Modifier.weight(1f))
                if (expenses.size > 1) {
                  IconButton(onClick = { expenses.removeAt(idx) }) {
                    Icon(Icons.Outlined.DeleteOutline, contentDescription = "Remove")
                  }
                }
              }
            }

            ActionButtonRow(
              primaryLabel = "Calculate Net Profit",
              onPrimaryClick = {
                val revNums = revenues.filter { it.isNotBlank() }.map { it.toDoubleOrNull() }
                val expNums = expenses.filter { it.isNotBlank() }.map { it.toDoubleOrNull() }
                if (revNums.any { it == null } || expNums.any { it == null }) {
                  errorMsg = "Enter valid numbers in all non-empty rows"; breakdown = null
                } else {
                  when (val out = CommerceEngine.revenueAndExpense(revNums.filterNotNull(), expNums.filterNotNull())) {
                    is CalcOutcome.Success -> {
                      errorMsg = null; breakdown = out.breakdown
                      out.breakdown?.let { onRecordHistory(it.inputValues, it.finalAnswer, "Revenue & Expense", "Commerce", Routes.COM_REVENUE_EXPENSE) }
                    }
                    is CalcOutcome.Error -> { errorMsg = out.message; breakdown = null }
                  }
                }
              },
              onResetClick = { revenues.clear(); revenues.add(""); expenses.clear(); expenses.add(""); errorMsg = null; breakdown = null },
            )
          }
        }
        errorMsg?.let { item { ValidationErrorCard(it) } }
        breakdown?.let { item { StepBreakdownCard(it, showDetails = showBreakdown) } }
      }
    }
  }
}

@Composable
private fun DepreciationScreen(
  showBreakdown: Boolean,
  onRecordHistory: (String, String, String, String, String) -> Unit,
  onBack: () -> Unit,
) {
  var isDeclining by rememberSaveable { mutableStateOf(false) }
  var cost by rememberSaveable { mutableStateOf("50000") }
  var salvage by rememberSaveable { mutableStateOf("5000") }
  var life by rememberSaveable { mutableStateOf("5") }
  var rate by rememberSaveable { mutableStateOf("30") }
  var depResult by remember { mutableStateOf<DepreciationResult?>(null) }
  var errorMsg by remember { mutableStateOf<String?>(null) }
  var breakdown by remember { mutableStateOf<StepBreakdown?>(null) }

  Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
    CalcoraTopBar(title = "Depreciation Calculator", subtitle = "Straight-Line & Declining Balance", onBack = onBack)
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
      LazyColumn(
        modifier = Modifier.fillMaxSize().widthIn(max = 720.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        item {
          CalcoraSectionCard {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              FilterChip(selected = !isDeclining, onClick = { isDeclining = false }, label = { Text("Straight-Line") }, modifier = Modifier.weight(1f))
              FilterChip(selected = isDeclining, onClick = { isDeclining = true }, label = { Text("Declining-Balance") }, modifier = Modifier.weight(1f))
            }
            CalcoraNumberField(cost, { cost = it }, "Asset Cost")
            CalcoraNumberField(salvage, { salvage = it }, "Salvage Value")
            CalcoraNumberField(life, { life = it }, "Useful Life (Years)", allowDecimal = false)
            if (isDeclining) {
              CalcoraNumberField(rate, { rate = it }, "Annual Depreciation Rate (%)")
            }
            ActionButtonRow(
              primaryLabel = "Calculate Schedule",
              onPrimaryClick = {
                val c = cost.toDoubleOrNull(); val s = salvage.toDoubleOrNull(); val l = life.toIntOrNull()
                val r = if (isDeclining) rate.toDoubleOrNull() else 0.0
                if (c == null || s == null || l == null || r == null) {
                  errorMsg = "Enter valid numbers for all fields"; depResult = null; breakdown = null
                } else {
                  val out =
                    if (isDeclining) CommerceEngine.decliningBalanceDepreciation(c, s, l, r)
                    else CommerceEngine.straightLineDepreciation(c, s, l)
                  when (out) {
                    is CalcOutcome.Success -> {
                      errorMsg = null; depResult = out.data; breakdown = out.breakdown
                      out.breakdown?.let { onRecordHistory(it.inputValues, it.finalAnswer, "Depreciation", "Commerce", Routes.COM_DEPRECIATION) }
                    }
                    is CalcOutcome.Error -> { errorMsg = out.message; depResult = null; breakdown = null }
                  }
                }
              },
              onResetClick = { cost = ""; salvage = ""; life = ""; rate = ""; depResult = null; errorMsg = null; breakdown = null },
            )
          }
        }
        errorMsg?.let { item { ValidationErrorCard(it) } }
        breakdown?.let { item { StepBreakdownCard(it, showDetails = showBreakdown) } }
        depResult?.let { res ->
          item {
            CalcoraSectionCard {
              Text("Annual Book Value Schedule", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
              res.schedule.forEach { row ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                  Text("Year ${row.year}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                  Text(
                    "Dep: ${NumberFormatter.formatCurrency(row.depreciation, 2, true)} | Book: ${NumberFormatter.formatCurrency(row.bookValue, 2, true)}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun AccountingHelpersScreen(
  showBreakdown: Boolean,
  onRecordHistory: (String, String, String, String, String) -> Unit,
  onBack: () -> Unit,
) {
  var helper by rememberSaveable { mutableStateOf(CommerceEngine.AccountingHelperType.GROSS_MARGIN) }
  var v1 by rememberSaveable { mutableStateOf("10000") }
  var v2 by rememberSaveable { mutableStateOf("6500") }
  var errorMsg by remember { mutableStateOf<String?>(null) }
  var breakdown by remember { mutableStateOf<StepBreakdown?>(null) }

  val labels = when (helper) {
    CommerceEngine.AccountingHelperType.PERCENTAGE_CHANGE -> "Old Value" to "New Value"
    CommerceEngine.AccountingHelperType.GROSS_MARGIN -> "Total Revenue" to "Cost of Goods Sold (COGS)"
    CommerceEngine.AccountingHelperType.NET_MARGIN -> "Total Revenue" to "Net Income"
    CommerceEngine.AccountingHelperType.COST_RATIO -> "Total Revenue" to "Total Costs"
    CommerceEngine.AccountingHelperType.REVENUE_COMPARISON -> "Period 1 Revenue" to "Period 2 Revenue"
  }

  Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
    CalcoraTopBar(title = "Accounting Helpers", subtitle = "Margins, Ratios & Comparisons", onBack = onBack)
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
      LazyColumn(
        modifier = Modifier.fillMaxSize().widthIn(max = 720.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        item {
          LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(CommerceEngine.AccountingHelperType.entries) { h ->
              FilterChip(selected = helper == h, onClick = { helper = h; errorMsg = null; breakdown = null }, label = { Text(h.label) })
            }
          }
        }
        item {
          CalcoraSectionCard {
            CalcoraNumberField(v1, { v1 = it }, labels.first)
            CalcoraNumberField(v2, { v2 = it }, labels.second)
            ActionButtonRow(
              primaryLabel = "Calculate",
              onPrimaryClick = {
                val a = v1.toDoubleOrNull(); val b = v2.toDoubleOrNull()
                if (a == null || b == null) {
                  errorMsg = "Enter valid numbers"; breakdown = null
                } else {
                  when (val out = CommerceEngine.accountingHelper(helper, a, b)) {
                    is CalcOutcome.Success -> {
                      errorMsg = null; breakdown = out.breakdown
                      out.breakdown?.let { onRecordHistory(it.inputValues, it.finalAnswer, helper.label, "Commerce", Routes.COM_ACCOUNTING) }
                    }
                    is CalcOutcome.Error -> { errorMsg = out.message; breakdown = null }
                  }
                }
              },
              onResetClick = { v1 = ""; v2 = ""; errorMsg = null; breakdown = null },
            )
          }
        }
        errorMsg?.let { item { ValidationErrorCard(it) } }
        breakdown?.let { item { StepBreakdownCard(it, showDetails = showBreakdown) } }
      }
    }
  }
}
