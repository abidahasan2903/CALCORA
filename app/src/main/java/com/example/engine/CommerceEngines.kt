package com.example.engine

import com.example.model.CalcOutcome
import com.example.model.StepBreakdown
import kotlin.math.abs
import kotlin.math.pow

enum class CompoundFrequency(val label: String, val periodsPerYear: Int) {
  ANNUALLY("Annually (1/yr)", 1),
  SEMI_ANNUALLY("Semi-Annually (2/yr)", 2),
  QUARTERLY("Quarterly (4/yr)", 4),
  MONTHLY("Monthly (12/yr)", 12),
  DAILY("Daily (365/yr)", 365),
}

enum class PaymentFrequency(val label: String, val periodsPerYear: Int) {
  MONTHLY("Monthly (12/yr)", 12),
  QUARTERLY("Quarterly (4/yr)", 4),
  BI_WEEKLY("Bi-Weekly (26/yr)", 26),
  WEEKLY("Weekly (52/yr)", 52),
  ANNUALLY("Annually (1/yr)", 1),
}

data class ProfitLossResult(
  val isProfit: Boolean,
  val isBreakEven: Boolean,
  val amount: Double,
  val percentage: Double,
)

data class DiscountResult(
  val discountAmount: Double,
  val finalPrice: Double,
  val savingsPercentage: Double,
)

data class MarkupResult(
  val markupAmount: Double,
  val sellingPrice: Double,
  val markupPercentage: Double,
  val profitMarginPercentage: Double,
)

data class VatTaxResult(
  val taxAmount: Double,
  val amountBeforeTax: Double,
  val amountAfterTax: Double,
)

data class InterestResult(
  val interest: Double,
  val totalAmount: Double,
)

data class LoanEmiResult(
  val periodicPayment: Double,
  val totalPayment: Double,
  val totalInterest: Double,
  val totalInstallments: Int,
)

data class RoiResult(
  val netProfit: Double,
  val roiPercentage: Double,
)

data class BreakEvenResult(
  val contributionPerUnit: Double,
  val breakEvenUnits: Double,
  val breakEvenRevenue: Double,
)

data class CommissionResult(
  val commissionAmount: Double,
  val totalEarnings: Double,
)

data class SalaryResult(
  val grossSalary: Double,
  val totalDeductions: Double,
  val netSalary: Double,
)

data class RevenueExpenseResult(
  val totalRevenue: Double,
  val totalExpenses: Double,
  val netProfit: Double,
  val profitMarginPercent: Double,
)

data class DepreciationRow(
  val year: Int,
  val depreciation: Double,
  val accumulated: Double,
  val bookValue: Double,
)

data class DepreciationResult(
  val annualDepreciationFirstYear: Double,
  val totalDepreciation: Double,
  val finalBookValue: Double,
  val schedule: List<DepreciationRow>,
)

object CommerceEngine {
  private fun fmt(v: Double) = NumberFormatter.formatCurrency(v, 2, true)
  private fun fmtPct(v: Double) = "${NumberFormatter.formatPlain(v, 2)}%"

  fun profitAndLoss(costPrice: Double, sellingPrice: Double): CalcOutcome<ProfitLossResult> {
    if (costPrice <= 0.0) return CalcOutcome.Error("Cost price must be greater than zero")
    if (sellingPrice < 0.0) return CalcOutcome.Error("Selling price cannot be negative")
    val diff = sellingPrice - costPrice
    val isBreakEven = abs(diff) < 1e-9
    val isProfit = diff > 0
    val amount = abs(diff)
    val pct = (amount / costPrice) * 100.0
    val status = when {
      isBreakEven -> "Break-Even (No Profit, No Loss)"
      isProfit -> "Profit"
      else -> "Loss"
    }
    return CalcOutcome.Success(
      ProfitLossResult(isProfit, isBreakEven, amount, pct),
      StepBreakdown(
        formula = if (isProfit) "Profit = SP - CP,  Profit% = (Profit / CP) × 100" else "Loss = CP - SP,  Loss% = (Loss / CP) × 100",
        inputValues = "CP = ${fmt(costPrice)}, SP = ${fmt(sellingPrice)}",
        substitution = "|${fmt(sellingPrice)} - ${fmt(costPrice)}| = ${fmt(amount)}",
        calculationSteps = listOf("Status: $status", "Amount = ${fmt(amount)}", "Percentage (on CP) = ${fmtPct(pct)}"),
        finalAnswer = "$status: ${fmt(amount)} (${fmtPct(pct)})",
      ),
    )
  }

  fun discount(originalPrice: Double, discountPct: Double): CalcOutcome<DiscountResult> {
    if (originalPrice < 0.0) return CalcOutcome.Error("Original price cannot be negative")
    if (discountPct < 0.0 || discountPct > 100.0) return CalcOutcome.Error("Discount percentage must be between 0% and 100%")
    val discAmt = originalPrice * (discountPct / 100.0)
    val finalPrice = originalPrice - discAmt
    return CalcOutcome.Success(
      DiscountResult(discAmt, finalPrice, discountPct),
      StepBreakdown(
        formula = "Discount = Original × (Rate / 100),  Final = Original - Discount",
        inputValues = "Original = ${fmt(originalPrice)}, Discount = ${fmtPct(discountPct)}",
        substitution = "Discount = ${fmt(originalPrice)} × (${discountPct} / 100)",
        calculationSteps = listOf("Discount Amount = ${fmt(discAmt)}", "Final Price = ${fmt(finalPrice)}", "Savings = ${fmtPct(discountPct)}"),
        finalAnswer = fmt(finalPrice),
      ),
    )
  }

  fun markup(cost: Double, markupPct: Double): CalcOutcome<MarkupResult> {
    if (cost <= 0.0) return CalcOutcome.Error("Cost must be greater than zero")
    if (markupPct < 0.0) return CalcOutcome.Error("Markup percentage cannot be negative")
    val markupAmt = cost * (markupPct / 100.0)
    val sellingPrice = cost + markupAmt
    val marginPct = if (sellingPrice > 0.0) (markupAmt / sellingPrice) * 100.0 else 0.0
    return CalcOutcome.Success(
      MarkupResult(markupAmt, sellingPrice, markupPct, marginPct),
      StepBreakdown(
        formula = "Markup = Cost × (Markup% / 100),  Margin% = (Markup / Selling Price) × 100",
        inputValues = "Cost = ${fmt(cost)}, Markup% = ${fmtPct(markupPct)}",
        substitution = "Selling Price = ${fmt(cost)} + (${fmt(cost)} × ${markupPct}%)",
        calculationSteps = listOf(
          "Markup Amount = ${fmt(markupAmt)}",
          "Selling Price = ${fmt(sellingPrice)}",
          "Markup% (on Cost) = ${fmtPct(markupPct)} vs Profit Margin% (on Selling Price) = ${fmtPct(marginPct)}",
        ),
        finalAnswer = "SP = ${fmt(sellingPrice)}",
      ),
    )
  }

  fun vatAndTax(amount: Double, taxRatePct: Double, isInclusive: Boolean): CalcOutcome<VatTaxResult> {
    if (amount < 0.0) return CalcOutcome.Error("Amount cannot be negative")
    if (taxRatePct < 0.0) return CalcOutcome.Error("Tax / VAT rate cannot be negative")
    val rate = taxRatePct / 100.0
    return if (!isInclusive) {
      val tax = amount * rate
      val after = amount + tax
      CalcOutcome.Success(
        VatTaxResult(tax, amount, after),
        StepBreakdown(
          formula = "Tax = Net × Rate,  Gross = Net + Tax (Tax Exclusive)",
          inputValues = "Net = ${fmt(amount)}, Rate = ${fmtPct(taxRatePct)}",
          substitution = "Tax = ${fmt(amount)} × ${NumberFormatter.formatPlain(rate, 4)}",
          calculationSteps = listOf("Amount Before Tax = ${fmt(amount)}", "Tax Amount = ${fmt(tax)}", "Amount After Tax = ${fmt(after)}"),
          finalAnswer = fmt(after),
        ),
      )
    } else {
      val before = amount / (1.0 + rate)
      val tax = amount - before
      CalcOutcome.Success(
        VatTaxResult(tax, before, amount),
        StepBreakdown(
          formula = "Net = Gross / (1 + Rate),  Tax = Gross - Net (Tax Inclusive)",
          inputValues = "Gross = ${fmt(amount)}, Rate = ${fmtPct(taxRatePct)}",
          substitution = "Net = ${fmt(amount)} / (1 + ${NumberFormatter.formatPlain(rate, 4)})",
          calculationSteps = listOf("Amount Before Tax = ${fmt(before)}", "Tax Amount = ${fmt(tax)}", "Amount After Tax = ${fmt(amount)}"),
          finalAnswer = "Net = ${fmt(before)}, Tax = ${fmt(tax)}",
        ),
      )
    }
  }

  fun simpleInterest(principal: Double, annualRatePct: Double, timeYears: Double): CalcOutcome<InterestResult> {
    if (principal < 0.0) return CalcOutcome.Error("Principal cannot be negative")
    if (annualRatePct < 0.0) return CalcOutcome.Error("Interest rate cannot be negative")
    if (timeYears < 0.0) return CalcOutcome.Error("Time cannot be negative")
    val si = (principal * annualRatePct * timeYears) / 100.0
    val total = principal + si
    return CalcOutcome.Success(
      InterestResult(si, total),
      StepBreakdown(
        formula = "SI = (P × R × T) / 100,  Total = P + SI",
        inputValues = "P = ${fmt(principal)}, R = ${fmtPct(annualRatePct)}, T = ${NumberFormatter.formatPlain(timeYears)} yrs",
        substitution = "SI = (${fmt(principal)} × $annualRatePct × $timeYears) / 100",
        calculationSteps = listOf("Simple Interest = ${fmt(si)}", "Total Amount = ${fmt(total)}"),
        finalAnswer = fmt(total),
      ),
    )
  }

  fun compoundInterest(
    principal: Double,
    annualRatePct: Double,
    timeYears: Double,
    frequency: CompoundFrequency,
  ): CalcOutcome<InterestResult> {
    if (principal < 0.0) return CalcOutcome.Error("Principal cannot be negative")
    if (annualRatePct < 0.0) return CalcOutcome.Error("Interest rate cannot be negative")
    if (timeYears < 0.0) return CalcOutcome.Error("Time cannot be negative")
    val r = annualRatePct / 100.0
    val n = frequency.periodsPerYear.toDouble()
    val amount = principal * (1.0 + r / n).pow(n * timeYears)
    if (amount.isNaN() || amount.isInfinite()) return CalcOutcome.Error("Result exceeds numerical bounds")
    val interest = amount - principal
    return CalcOutcome.Success(
      InterestResult(interest, amount),
      StepBreakdown(
        formula = "A = P(1 + r/n)^(nt),  CI = A - P",
        inputValues = "P = ${fmt(principal)}, r = ${fmtPct(annualRatePct)}, t = ${NumberFormatter.formatPlain(timeYears)} yrs, n = ${frequency.periodsPerYear} (${frequency.label})",
        substitution = "A = ${fmt(principal)} × (1 + ${NumberFormatter.formatPlain(r, 6)}/${frequency.periodsPerYear})^(${frequency.periodsPerYear} × $timeYears)",
        calculationSteps = listOf("Compound Amount A = ${fmt(amount)}", "Compound Interest CI = ${fmt(interest)}"),
        finalAnswer = fmt(amount),
      ),
    )
  }

  fun loanEmi(
    loanAmount: Double,
    annualRatePct: Double,
    durationYears: Double,
    frequency: PaymentFrequency,
  ): CalcOutcome<LoanEmiResult> {
    if (loanAmount <= 0.0) return CalcOutcome.Error("Loan amount must be greater than zero")
    if (annualRatePct < 0.0) return CalcOutcome.Error("Interest rate cannot be negative")
    if (durationYears <= 0.0) return CalcOutcome.Error("Loan duration must be greater than zero")
    val periodsPerYear = frequency.periodsPerYear
    val totalInstallments = kotlin.math.round(durationYears * periodsPerYear).toInt().coerceAtLeast(1)
    val periodicRate = (annualRatePct / 100.0) / periodsPerYear

    val periodicPayment: Double =
      if (abs(periodicRate) < 1e-12) {
        loanAmount / totalInstallments
      } else {
        val factor = (1.0 + periodicRate).pow(totalInstallments)
        if (factor.isInfinite()) return CalcOutcome.Error("Amortization term exceeds numerical bounds")
        loanAmount * periodicRate * factor / (factor - 1.0)
      }
    val totalPayment = periodicPayment * totalInstallments
    val totalInterest = totalPayment - loanAmount

    return CalcOutcome.Success(
      LoanEmiResult(periodicPayment, totalPayment, totalInterest, totalInstallments),
      StepBreakdown(
        formula = if (abs(periodicRate) < 1e-12) "Payment = P / N (0% interest)" else "EMI = P × r × (1+r)^N / ((1+r)^N - 1)",
        inputValues = "P = ${fmt(loanAmount)}, Rate = ${fmtPct(annualRatePct)}/yr, N = $totalInstallments (${frequency.label})",
        substitution = "r_period = ${NumberFormatter.formatPlain(periodicRate * 100, 5)}%, N = $totalInstallments",
        calculationSteps = listOf(
          "Periodic Payment = ${fmt(periodicPayment)}",
          "Total Payment ($totalInstallments periods) = ${fmt(totalPayment)}",
          "Total Interest = ${fmt(totalInterest)}",
        ),
        finalAnswer = fmt(periodicPayment),
      ),
    )
  }

  fun roi(initialInvestment: Double, finalValue: Double): CalcOutcome<RoiResult> {
    if (initialInvestment <= 0.0) return CalcOutcome.Error("Initial investment must be greater than zero")
    if (finalValue < 0.0) return CalcOutcome.Error("Final value cannot be negative")
    val netProfit = finalValue - initialInvestment
    val roiPct = (netProfit / initialInvestment) * 100.0
    return CalcOutcome.Success(
      RoiResult(netProfit, roiPct),
      StepBreakdown(
        formula = "Net Profit = Final - Initial,  ROI% = (Net Profit / Initial) × 100",
        inputValues = "Initial = ${fmt(initialInvestment)}, Final = ${fmt(finalValue)}",
        substitution = "ROI% = ((${fmt(finalValue)} - ${fmt(initialInvestment)}) / ${fmt(initialInvestment)}) × 100",
        calculationSteps = listOf("Net Profit = ${fmt(netProfit)}", "ROI = ${fmtPct(roiPct)}"),
        finalAnswer = fmtPct(roiPct),
      ),
    )
  }

  fun breakEven(fixedCosts: Double, sellingPricePerUnit: Double, variableCostPerUnit: Double): CalcOutcome<BreakEvenResult> {
    if (fixedCosts < 0.0) return CalcOutcome.Error("Fixed costs cannot be negative")
    if (sellingPricePerUnit < 0.0 || variableCostPerUnit < 0.0) {
      return CalcOutcome.Error("Prices and variable costs cannot be negative")
    }
    val contribution = sellingPricePerUnit - variableCostPerUnit
    if (contribution <= 0.0) {
      return CalcOutcome.Error("Selling price per unit must exceed variable cost per unit (Contribution ≤ 0)")
    }
    val units = fixedCosts / contribution
    val revenue = units * sellingPricePerUnit
    return CalcOutcome.Success(
      BreakEvenResult(contribution, units, revenue),
      StepBreakdown(
        formula = "Contribution = P - V,  BEP(units) = Fixed Costs / Contribution,  BEP(rev) = BEP(units) × P",
        inputValues = "FC = ${fmt(fixedCosts)}, P = ${fmt(sellingPricePerUnit)}, V = ${fmt(variableCostPerUnit)}",
        substitution = "BEP = ${fmt(fixedCosts)} / (${fmt(sellingPricePerUnit)} - ${fmt(variableCostPerUnit)})",
        calculationSteps = listOf(
          "Contribution per Unit = ${fmt(contribution)}",
          "Break-even Quantity = ${NumberFormatter.formatPlain(units, 2)} units",
          "Break-even Revenue = ${fmt(revenue)}",
        ),
        finalAnswer = "${NumberFormatter.formatPlain(units, 2)} units",
      ),
    )
  }

  fun commission(salesAmount: Double, commissionRatePct: Double, baseSalary: Double = 0.0): CalcOutcome<CommissionResult> {
    if (salesAmount < 0.0) return CalcOutcome.Error("Sales amount cannot be negative")
    if (commissionRatePct < 0.0) return CalcOutcome.Error("Commission rate cannot be negative")
    if (baseSalary < 0.0) return CalcOutcome.Error("Base salary cannot be negative")
    val comm = salesAmount * (commissionRatePct / 100.0)
    val total = baseSalary + comm
    return CalcOutcome.Success(
      CommissionResult(comm, total),
      StepBreakdown(
        formula = "Commission = Sales × (Rate / 100),  Total = Base + Commission",
        inputValues = "Sales = ${fmt(salesAmount)}, Rate = ${fmtPct(commissionRatePct)}, Base = ${fmt(baseSalary)}",
        substitution = "Commission = ${fmt(salesAmount)} × ${commissionRatePct}%",
        calculationSteps = listOf("Commission = ${fmt(comm)}", "Total Earnings = ${fmt(total)}"),
        finalAnswer = fmt(comm),
      ),
    )
  }

  fun salary(basic: Double, allowances: Double, bonuses: Double, deductions: Double): CalcOutcome<SalaryResult> {
    if (basic < 0.0 || allowances < 0.0 || bonuses < 0.0 || deductions < 0.0) {
      return CalcOutcome.Error("Salary components and deductions cannot be negative")
    }
    val gross = basic + allowances + bonuses
    val net = gross - deductions
    return CalcOutcome.Success(
      SalaryResult(gross, deductions, net),
      StepBreakdown(
        formula = "Gross = Basic + Allowances + Bonuses,  Net = Gross - Deductions",
        inputValues = "Basic=${fmt(basic)}, Allowances=${fmt(allowances)}, Bonuses=${fmt(bonuses)}, Deductions=${fmt(deductions)}",
        substitution = "Net = (${fmt(basic)} + ${fmt(allowances)} + ${fmt(bonuses)}) - ${fmt(deductions)}",
        calculationSteps = listOf("Gross Salary = ${fmt(gross)}", "Total Deductions = ${fmt(deductions)}", "Net Salary = ${fmt(net)}"),
        finalAnswer = fmt(net),
      ),
    )
  }

  fun revenueAndExpense(revenues: List<Double>, expenses: List<Double>): CalcOutcome<RevenueExpenseResult> {
    if (revenues.isEmpty() && expenses.isEmpty()) {
      return CalcOutcome.Error("Add at least one revenue or expense entry")
    }
    if (revenues.any { it < 0.0 } || expenses.any { it < 0.0 }) {
      return CalcOutcome.Error("Revenue and expense entries must be non-negative")
    }
    val totRev = revenues.sum()
    val totExp = expenses.sum()
    val net = totRev - totExp
    val margin = if (totRev > 0.0) (net / totRev) * 100.0 else 0.0
    return CalcOutcome.Success(
      RevenueExpenseResult(totRev, totExp, net, margin),
      StepBreakdown(
        formula = "Net Profit = Total Revenue - Total Expenses,  Margin% = (Net / Revenue) × 100",
        inputValues = "${revenues.size} revenue item(s), ${expenses.size} expense item(s)",
        substitution = "Net = ${fmt(totRev)} - ${fmt(totExp)}",
        calculationSteps = listOf(
          "Total Revenue = ${fmt(totRev)}",
          "Total Expenses = ${fmt(totExp)}",
          "Net Profit = ${fmt(net)}",
          "Profit Margin = ${fmtPct(margin)}",
        ),
        finalAnswer = fmt(net),
      ),
    )
  }

  fun straightLineDepreciation(assetCost: Double, salvageValue: Double, usefulLifeYears: Int): CalcOutcome<DepreciationResult> {
    if (assetCost <= 0.0) return CalcOutcome.Error("Asset cost must be greater than zero")
    if (salvageValue < 0.0) return CalcOutcome.Error("Salvage value cannot be negative")
    if (salvageValue > assetCost) return CalcOutcome.Error("Salvage value cannot exceed asset cost")
    if (usefulLifeYears <= 0 || usefulLifeYears > 100) return CalcOutcome.Error("Useful life must be between 1 and 100 years")
    val annual = (assetCost - salvageValue) / usefulLifeYears
    val rows = mutableListOf<DepreciationRow>()
    var acc = 0.0
    var book = assetCost
    for (y in 1..usefulLifeYears) {
      acc += annual
      book -= annual
      rows.add(DepreciationRow(y, annual, acc, book.coerceAtLeast(salvageValue)))
    }
    return CalcOutcome.Success(
      DepreciationResult(annual, assetCost - salvageValue, salvageValue, rows),
      StepBreakdown(
        formula = "Annual Depreciation = (Cost - Salvage) / Useful Life",
        inputValues = "Cost=${fmt(assetCost)}, Salvage=${fmt(salvageValue)}, Life=$usefulLifeYears yrs",
        substitution = "(${fmt(assetCost)} - ${fmt(salvageValue)}) / $usefulLifeYears",
        calculationSteps = listOf("Annual Depreciation = ${fmt(annual)}", "Total Depreciable Base = ${fmt(assetCost - salvageValue)}"),
        finalAnswer = "${fmt(annual)} / yr",
      ),
    )
  }

  fun decliningBalanceDepreciation(
    assetCost: Double,
    salvageValue: Double,
    usefulLifeYears: Int,
    annualRatePct: Double,
  ): CalcOutcome<DepreciationResult> {
    if (assetCost <= 0.0) return CalcOutcome.Error("Asset cost must be greater than zero")
    if (salvageValue < 0.0) return CalcOutcome.Error("Salvage value cannot be negative")
    if (salvageValue > assetCost) return CalcOutcome.Error("Salvage value cannot exceed asset cost")
    if (usefulLifeYears <= 0 || usefulLifeYears > 100) return CalcOutcome.Error("Useful life must be between 1 and 100 years")
    if (annualRatePct <= 0.0 || annualRatePct > 100.0) return CalcOutcome.Error("Depreciation rate must be between 0% and 100%")

    val rate = annualRatePct / 100.0
    val rows = mutableListOf<DepreciationRow>()
    var book = assetCost
    var acc = 0.0
    for (y in 1..usefulLifeYears) {
      val rawDep = book * rate
      val cappedDep = (book - salvageValue).coerceAtLeast(0.0).let { maxDep -> minOf(rawDep, maxDep) }
      acc += cappedDep
      book -= cappedDep
      rows.add(DepreciationRow(y, cappedDep, acc, book))
    }
    return CalcOutcome.Success(
      DepreciationResult(rows.firstOrNull()?.depreciation ?: 0.0, acc, book, rows),
      StepBreakdown(
        formula = "Year k Dep = Book Value_(k-1) × Rate (floored at Salvage Value)",
        inputValues = "Cost=${fmt(assetCost)}, Salvage=${fmt(salvageValue)}, Life=$usefulLifeYears yrs, Rate=${fmtPct(annualRatePct)}",
        substitution = "Year 1 = ${fmt(assetCost)} × ${annualRatePct}%",
        calculationSteps = listOf(
          "Year 1 Depreciation = ${fmt(rows.first().depreciation)}",
          "Total Depreciation = ${fmt(acc)}",
          "Final Book Value = ${fmt(book)}",
        ),
        finalAnswer = "Year 1: ${fmt(rows.first().depreciation)}",
      ),
    )
  }

  enum class AccountingHelperType(val label: String) {
    PERCENTAGE_CHANGE("Percentage Change"),
    GROSS_MARGIN("Gross Margin %"),
    NET_MARGIN("Net Margin %"),
    COST_RATIO("Cost Ratio %"),
    REVENUE_COMPARISON("Revenue Comparison"),
  }

  fun accountingHelper(type: AccountingHelperType, a: Double, b: Double): CalcOutcome<Pair<String, Double>> {
    return when (type) {
      AccountingHelperType.PERCENTAGE_CHANGE -> {
        if (a == 0.0) return CalcOutcome.Error("Initial value cannot be zero for percentage change")
        val pct = ((b - a) / abs(a)) * 100.0
        CalcOutcome.Success(
          "Percentage Change" to pct,
          StepBreakdown("% Change = ((New - Old) / |Old|) × 100", "Old=${fmt(a)}, New=${fmt(b)}", "((${fmt(b)} - ${fmt(a)}) / |${fmt(a)}|) × 100", listOf("Change = ${fmtPct(pct)}"), fmtPct(pct), "%"),
        )
      }
      AccountingHelperType.GROSS_MARGIN -> {
        if (a <= 0.0) return CalcOutcome.Error("Revenue must be greater than zero")
        if (b < 0.0) return CalcOutcome.Error("COGS cannot be negative")
        val grossProfit = a - b
        val margin = (grossProfit / a) * 100.0
        CalcOutcome.Success(
          "Gross Margin" to margin,
          StepBreakdown("Gross Margin% = ((Revenue - COGS) / Revenue) × 100", "Revenue=${fmt(a)}, COGS=${fmt(b)}", "Gross Profit = ${fmt(grossProfit)}", listOf("Gross Margin = ${fmtPct(margin)}"), fmtPct(margin), "%"),
        )
      }
      AccountingHelperType.NET_MARGIN -> {
        if (a <= 0.0) return CalcOutcome.Error("Revenue must be greater than zero")
        val margin = (b / a) * 100.0
        CalcOutcome.Success(
          "Net Margin" to margin,
          StepBreakdown("Net Margin% = (Net Income / Revenue) × 100", "Revenue=${fmt(a)}, Net Income=${fmt(b)}", "(${fmt(b)} / ${fmt(a)}) × 100", listOf("Net Margin = ${fmtPct(margin)}"), fmtPct(margin), "%"),
        )
      }
      AccountingHelperType.COST_RATIO -> {
        if (a <= 0.0) return CalcOutcome.Error("Revenue must be greater than zero")
        if (b < 0.0) return CalcOutcome.Error("Total costs cannot be negative")
        val ratio = (b / a) * 100.0
        CalcOutcome.Success(
          "Cost Ratio" to ratio,
          StepBreakdown("Cost Ratio% = (Total Costs / Revenue) × 100", "Revenue=${fmt(a)}, Costs=${fmt(b)}", "(${fmt(b)} / ${fmt(a)}) × 100", listOf("Cost Ratio = ${fmtPct(ratio)}"), fmtPct(ratio), "%"),
        )
      }
      AccountingHelperType.REVENUE_COMPARISON -> {
        if (a <= 0.0) return CalcOutcome.Error("Baseline revenue must be greater than zero")
        val diff = b - a
        val pct = (diff / a) * 100.0
        CalcOutcome.Success(
          "Variance (${fmt(diff)})" to pct,
          StepBreakdown("Variance = Period 2 - Period 1,  Growth% = (Variance / Period 1) × 100", "Period 1=${fmt(a)}, Period 2=${fmt(b)}", "Diff = ${fmt(diff)}", listOf("Growth = ${fmtPct(pct)}"), "${fmt(diff)} (${fmtPct(pct)})", "%"),
        )
      }
    }
  }
}
