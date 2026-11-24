package com.example

import com.example.engine.BiologicalSex
import com.example.engine.CalcoraDate
import com.example.engine.ChemistryEngine
import com.example.engine.CommerceEngine
import com.example.engine.CompoundFrequency
import com.example.engine.DateTimeEngine
import com.example.engine.EquationEngine
import com.example.engine.ExpressionParserEngine
import com.example.engine.FractionEngine
import com.example.engine.FractionOp
import com.example.engine.GeometryEngine
import com.example.engine.GeometryShape
import com.example.engine.GpaSubjectInput
import com.example.engine.GradingScale
import com.example.engine.HealthEngine
import com.example.engine.MarksSubjectInput
import com.example.engine.MatrixEngine
import com.example.engine.MatrixOp
import com.example.engine.NumberBase
import com.example.engine.NumberSystemEngine
import com.example.engine.PaymentFrequency
import com.example.engine.PercentageEngine
import com.example.engine.PercentageMode
import com.example.engine.PhysicsEngine
import com.example.engine.PhysicsToolType
import com.example.engine.ProbabilityEngine
import com.example.engine.RatioProportionEngine
import com.example.engine.SmartSearchEngine
import com.example.engine.StatisticsEngine
import com.example.engine.StudentEngine
import com.example.engine.UnitCategory
import com.example.engine.UnitConverterEngine
import com.example.engine.VectorEngine
import com.example.engine.VectorOp
import com.example.engine.WeightedItemInput
import com.example.engine.CgpaSemesterInput
import com.example.model.AngleMode
import com.example.model.CalcOutcome
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI

class CalcoraEngineUnitTest {

  private fun assertSuccessDouble(expected: Double, outcome: CalcOutcome<Double>, delta: Double = 1e-6) {
    assertTrue("Expected Success but got $outcome", outcome is CalcOutcome.Success)
    assertEquals(expected, (outcome as CalcOutcome.Success).data, delta)
  }

  // ==========================================
  // BASIC & SCIENTIFIC EXPRESSION PARSER TESTS
  // ==========================================

  @Test
  fun `expression parser handles precedence, parentheses, decimals, negatives, percentage`() {
    assertSuccessDouble(14.0, ExpressionParserEngine.evaluate("2 + 3 * 4"))
    assertSuccessDouble(20.0, ExpressionParserEngine.evaluate("(2 + 3) * 4"))
    assertSuccessDouble(-6.5, ExpressionParserEngine.evaluate("-10 + 3.5"))
    assertSuccessDouble(75.0, ExpressionParserEngine.evaluate("500 * 15%"))
    assertSuccessDouble(1.0, ExpressionParserEngine.evaluate("10 mod 3"))
    assertSuccessDouble(120.0, ExpressionParserEngine.evaluate("5!"))
    assertTrue(ExpressionParserEngine.evaluate("10 / 0") is CalcOutcome.Error)
  }

  @Test
  fun `trigonometry and inverse trig respect DEG, RAD, GRAD`() {
    assertSuccessDouble(0.5, ExpressionParserEngine.evaluate("sin(30)", AngleMode.DEG))
    assertSuccessDouble(1.0, ExpressionParserEngine.evaluate("sin(pi / 2)", AngleMode.RAD))
    assertSuccessDouble(1.0, ExpressionParserEngine.evaluate("sin(100)", AngleMode.GRAD))
    assertSuccessDouble(30.0, ExpressionParserEngine.evaluate("asin(0.5)", AngleMode.DEG))
    assertSuccessDouble(PI / 6.0, ExpressionParserEngine.evaluate("asin(0.5)", AngleMode.RAD))
    assertSuccessDouble(33.333333, ExpressionParserEngine.evaluate("acos(sqrt(3)/2)", AngleMode.GRAD), 1e-4)
    assertTrue(ExpressionParserEngine.evaluate("tan(90)", AngleMode.DEG) is CalcOutcome.Error)
  }

  @Test
  fun `logarithms, powers, and roots evaluate accurately with domain checks`() {
    assertSuccessDouble(2.0, ExpressionParserEngine.evaluate("log(100)"))
    assertSuccessDouble(1.0, ExpressionParserEngine.evaluate("ln(e)"))
    assertSuccessDouble(3.0, ExpressionParserEngine.evaluate("logn(2, 8)"))
    assertSuccessDouble(8.0, ExpressionParserEngine.evaluate("2^3"))
    assertSuccessDouble(3.0, ExpressionParserEngine.evaluate("root(3, 27)"))
    assertSuccessDouble(-2.0, ExpressionParserEngine.evaluate("root(3, -8)"))
    assertTrue(ExpressionParserEngine.evaluate("sqrt(-4)") is CalcOutcome.Error)
    assertTrue(ExpressionParserEngine.evaluate("log(0)") is CalcOutcome.Error)
  }

  // ==========================================
  // SCIENCE ENGINES TESTS
  // ==========================================

  @Test
  fun `exact fraction arithmetic avoids floating point error`() {
    val res = FractionEngine.operate(1, 3, 1, 6, FractionOp.ADD) as CalcOutcome.Success
    assertEquals("1/2", res.data.improper)
    val decRes = FractionEngine.decimalToFraction("0.75") as CalcOutcome.Success
    assertEquals("3/4", decRes.data.improper)
    val mixRes = FractionEngine.mixedToImproper(2, 3, 4) as CalcOutcome.Success
    assertEquals("11/4", mixRes.data.improper)
  }

  @Test
  fun `equation solver handles linear, quadratic real and complex, and 2x2 and 3x3 systems`() {
    val lin = EquationEngine.solveLinear(2.0, -8.0) as CalcOutcome.Success
    assertEquals(listOf("4"), lin.data.roots)

    val quadReal = EquationEngine.solveQuadratic(1.0, -5.0, 6.0) as CalcOutcome.Success
    assertEquals(listOf("3", "2"), quadReal.data.roots)
    assertEquals(1.0, quadReal.data.discriminant!!, 1e-9)

    val quadComplex = EquationEngine.solveQuadratic(1.0, 0.0, 4.0) as CalcOutcome.Success
    assertTrue(quadComplex.data.status.contains("Complex"))

    val sys2 = EquationEngine.solveSystem2x2(2.0, 1.0, 5.0, 1.0, -1.0, 1.0) as CalcOutcome.Success
    assertEquals(listOf("2", "1"), sys2.data.roots)

    val sys3 = EquationEngine.solveSystem3x3(
      arrayOf(
        doubleArrayOf(1.0, 1.0, 1.0),
        doubleArrayOf(0.0, 2.0, 5.0),
        doubleArrayOf(2.0, 5.0, -1.0),
      ),
      doubleArrayOf(6.0, -4.0, 27.0),
    ) as CalcOutcome.Success
    assertEquals(listOf("5", "3", "-2"), sys3.data.roots)
  }

  @Test
  fun `matrix, vector, statistics, probability, physics, and chemistry engines compute accurately`() {
    val det = MatrixEngine.compute(2, listOf(listOf(4.0, 7.0), listOf(2.0, 6.0)), null, MatrixOp.DET) as CalcOutcome.Success
    assertEquals(10.0, det.data.scalar!!, 1e-9)
    val singular = MatrixEngine.compute(2, listOf(listOf(1.0, 2.0), listOf(2.0, 4.0)), null, MatrixOp.INVERSE)
    assertTrue(singular is CalcOutcome.Error)

    val dot = VectorEngine.compute(true, doubleArrayOf(1.0, 2.0, 3.0), doubleArrayOf(4.0, -5.0, 6.0), VectorOp.DOT) as CalcOutcome.Success
    assertTrue(dot.data.display.contains("12"))

    val stats = StatisticsEngine.compute(listOf(2.0, 4.0, 4.0, 4.0, 5.0, 5.0, 7.0, 9.0)) as CalcOutcome.Success
    assertEquals(5.0, stats.data.mean, 1e-9)
    assertEquals(4.0, stats.data.populationVariance, 1e-9)
    assertEquals(2.0, stats.data.populationStdDev, 1e-9)

    val comb = ProbabilityEngine.combination(10, 3) as CalcOutcome.Success
    assertEquals("120", comb.data.primaryValue)

    val ke = PhysicsEngine.calculate(PhysicsToolType.KINETIC_ENERGY, 4.0, 5.0) as CalcOutcome.Success
    assertEquals(50.0, ke.data.first, 1e-9)

    val h2o = ChemistryEngine.calculateMolarMass("H2O") as CalcOutcome.Success
    assertEquals(18.015, h2o.data, 1e-2)
    assertTrue(ChemistryEngine.calculateMolarMass("Xx2O") is CalcOutcome.Error)
  }

  // ==========================================
  // COMMERCE ENGINES TESTS
  // ==========================================

  @Test
  fun `commerce engines calculate profit, discount, markup, vat, interests, emi, roi, breakeven, salary, depreciation`() {
    val pl = CommerceEngine.profitAndLoss(200.0, 250.0) as CalcOutcome.Success
    assertTrue(pl.data.isProfit)
    assertEquals(50.0, pl.data.amount, 1e-6)
    assertEquals(25.0, pl.data.percentage, 1e-6)

    val disc = CommerceEngine.discount(1000.0, 15.0) as CalcOutcome.Success
    assertEquals(850.0, disc.data.finalPrice, 1e-6)

    val markup = CommerceEngine.markup(100.0, 25.0) as CalcOutcome.Success
    assertEquals(125.0, markup.data.sellingPrice, 1e-6)
    assertEquals(20.0, markup.data.profitMarginPercentage, 1e-6)

    val vatIncl = CommerceEngine.vatAndTax(115.0, 15.0, isInclusive = true) as CalcOutcome.Success
    assertEquals(100.0, vatIncl.data.amountBeforeTax, 1e-6)
    assertEquals(15.0, vatIncl.data.taxAmount, 1e-6)

    val si = CommerceEngine.simpleInterest(1000.0, 5.0, 2.0) as CalcOutcome.Success
    assertEquals(100.0, si.data.interest, 1e-6)

    val ci = CommerceEngine.compoundInterest(1000.0, 10.0, 2.0, CompoundFrequency.ANNUALLY) as CalcOutcome.Success
    assertEquals(1210.0, ci.data.totalAmount, 1e-6)

    val emiZero = CommerceEngine.loanEmi(12000.0, 0.0, 1.0, PaymentFrequency.MONTHLY) as CalcOutcome.Success
    assertEquals(1000.0, emiZero.data.periodicPayment, 1e-6)

    val roi = CommerceEngine.roi(5000.0, 6500.0) as CalcOutcome.Success
    assertEquals(30.0, roi.data.roiPercentage, 1e-6)

    val bep = CommerceEngine.breakEven(10000.0, 50.0, 30.0) as CalcOutcome.Success
    assertEquals(500.0, bep.data.breakEvenUnits, 1e-6)

    val sal = CommerceEngine.salary(4000.0, 500.0, 500.0, 750.0) as CalcOutcome.Success
    assertEquals(4250.0, sal.data.netSalary, 1e-6)

    val dep = CommerceEngine.straightLineDepreciation(10000.0, 2000.0, 4) as CalcOutcome.Success
    assertEquals(2000.0, dep.data.annualDepreciationFirstYear, 1e-6)
  }

  // ==========================================
  // ARTS, UNIVERSAL, STUDENT & DATE BOUNDARY TESTS
  // ==========================================

  @Test
  fun `arts percentage and ratio engines calculate correctly`() {
    assertSuccessDouble(75.0, PercentageEngine.calculate(PercentageMode.PERCENT_OF, 15.0, 500.0))
    val ratio = RatioProportionEngine.simplifyRatio(24, 36) as CalcOutcome.Success
    assertEquals("2 : 3", ratio.data)
    val prop = RatioProportionEngine.solveProportion(2.0, 5.0, 8.0) as CalcOutcome.Success
    assertEquals(20.0, prop.data, 1e-6)
  }

  @Test
  fun `date boundary testing for Feb 28, Feb 29, leap years, month ends, same day, and invalid order`() {
    // Feb 29 leap year to next leap year
    val leapExact = DateTimeEngine.calculateAge(CalcoraDate(2000, 2, 29), CalcoraDate(2024, 2, 29)) as CalcOutcome.Success
    assertEquals(24, leapExact.data.years)
    assertEquals(0, leapExact.data.months)
    assertEquals(0, leapExact.data.days)

    // Feb 29 birth on non-leap year Feb 28
    val feb28NonLeap = DateTimeEngine.calculateAge(CalcoraDate(2000, 2, 29), CalcoraDate(2023, 2, 28)) as CalcOutcome.Success
    assertEquals(22, feb28NonLeap.data.years)
    assertEquals(11, feb28NonLeap.data.months)
    assertEquals(30, feb28NonLeap.data.days)

    // Jan 31 to Feb 29 (leap year) -> 29 days
    val jan31ToFeb29 = DateTimeEngine.dateDifference(CalcoraDate(2020, 1, 31), CalcoraDate(2020, 2, 29)) as CalcOutcome.Success
    assertEquals(29L, jan31ToFeb29.data.totalDays)

    // April 30 & Dec 31 boundaries
    val apr30 = DateTimeEngine.addOrSubtract(CalcoraDate(2023, 1, 31), 3, DateTimeEngine.DateUnit.MONTHS, false) as CalcOutcome.Success
    assertEquals(CalcoraDate(2023, 4, 30), apr30.data)

    val dec31 = DateTimeEngine.dateDifference(CalcoraDate(2023, 12, 31), CalcoraDate(2024, 1, 1)) as CalcOutcome.Success
    assertEquals(1L, dec31.data.totalDays)

    // Same birth and present date
    val sameDay = DateTimeEngine.calculateAge(CalcoraDate(2024, 5, 10), CalcoraDate(2024, 5, 10)) as CalcOutcome.Success
    assertEquals(0, sameDay.data.years)
    assertEquals(0L, sameDay.data.totalDaysLived)

    // Present date before birth date -> Error
    val invalidPresent = DateTimeEngine.calculateAge(CalcoraDate(2025, 1, 1), CalcoraDate(2024, 12, 31))
    assertTrue(invalidPresent is CalcOutcome.Error)

    // Future birth date relative to today when disallowed -> Error
    val futureBirth = DateTimeEngine.calculateAge(
      birthDate = CalcoraDate(2030, 1, 1),
      presentDate = CalcoraDate(2030, 6, 1),
      todayDate = CalcoraDate(2026, 11, 24),
      allowFuturePresentDate = false,
    )
    assertTrue(futureBirth is CalcOutcome.Error)
  }

  @Test
  fun `geometry, unit converter, number systems, bmi, bmr, and student gpa cgpa marks engines`() {
    val circle = GeometryEngine.calculate(GeometryShape.CIRCLE, listOf(10.0)) as CalcOutcome.Success
    assertEquals(100.0 * PI, circle.data.primaryValue, 1e-6)

    val kgToLb = UnitConverterEngine.convert(
      UnitCategory.MASS,
      10.0,
      UnitConverterEngine.unitsFor(UnitCategory.MASS).first { it.id == "kg" },
      UnitConverterEngine.unitsFor(UnitCategory.MASS).first { it.id == "lb" },
    ) as CalcOutcome.Success
    assertEquals(22.0462262, kgToLb.data, 1e-4)

    val cToF = UnitConverterEngine.convert(
      UnitCategory.TEMPERATURE,
      100.0,
      UnitConverterEngine.unitsFor(UnitCategory.TEMPERATURE).first { it.id == "C" },
      UnitConverterEngine.unitsFor(UnitCategory.TEMPERATURE).first { it.id == "F" },
    ) as CalcOutcome.Success
    assertEquals(212.0, cToF.data, 1e-6)

    val numSys = NumberSystemEngine.convert("255", NumberBase.DECIMAL) as CalcOutcome.Success
    assertEquals("11111111", numSys.data.binary)
    assertEquals("377", numSys.data.octal)
    assertEquals("FF", numSys.data.hex)

    val bmi = HealthEngine.calculateBmi(70.0, 175.0) as CalcOutcome.Success
    assertEquals(22.8571, bmi.data.bmi, 1e-3)

    val bmr = HealthEngine.calculateBmr(BiologicalSex.MALE, 25, 175.0, 70.0) as CalcOutcome.Success
    assertEquals(1673.75, bmr.data.bmrCalories, 1e-4)

    val gpa = StudentEngine.calculateGpa(
      listOf(
        GpaSubjectInput(1, "Math", "A", 4.0, 3.0),
        GpaSubjectInput(2, "Bio", "B", 3.0, 3.0),
      ),
      GradingScale.STANDARD_4_0,
    ) as CalcOutcome.Success
    assertEquals(3.5, gpa.data.gpa, 1e-6)

    val cgpa = StudentEngine.calculateCgpa(
      listOf(
        CgpaSemesterInput(1, "Sem 1", 3.0, 15.0),
        CgpaSemesterInput(2, "Sem 2", 4.0, 15.0),
      ),
    ) as CalcOutcome.Success
    assertEquals(3.5, cgpa.data.gpa, 1e-6)

    val marks = StudentEngine.calculateMarks(
      listOf(MarksSubjectInput(1, "Math", 90.0, 100.0)),
      GradingScale.STANDARD_4_0,
    ) as CalcOutcome.Success
    assertEquals(90.0, marks.data.percentage, 1e-6)
    assertEquals("A", marks.data.assignedGrade)

    val wAvg = StudentEngine.calculateWeightedAverage(
      listOf(
        WeightedItemInput(1, "Mid", 80.0, 40.0),
        WeightedItemInput(2, "Final", 90.0, 60.0),
      ),
    ) as CalcOutcome.Success
    assertEquals(86.0, wAvg.data.weightedAverage, 1e-6)

    // Smart search natural parser
    val smart = SmartSearchEngine.search("Calculate 15% of 500")
    assertEquals("75", smart.quickAnswer?.answer)
  }
}
