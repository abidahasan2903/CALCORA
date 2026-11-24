package com.example.engine

import com.example.model.AngleMode
import com.example.model.CalcOutcome
import com.example.model.StepBreakdown
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.log10
import kotlin.math.sqrt

// ==========================================
// 1. EXACT RATIONAL / FRACTION ENGINE
// ==========================================

data class Rational(val num: BigInteger, val den: BigInteger) {
  init {
    require(den != BigInteger.ZERO) { "Denominator cannot be zero" }
  }

  fun simplified(): Rational {
    if (num == BigInteger.ZERO) return Rational(BigInteger.ZERO, BigInteger.ONE)
    val sign = if (den < BigInteger.ZERO) BigInteger.valueOf(-1) else BigInteger.ONE
    val n = num * sign
    val d = den.abs()
    val g = n.abs().gcd(d)
    return Rational(n / g, d / g)
  }

  operator fun plus(other: Rational): Rational =
    Rational(num * other.den + other.num * den, den * other.den).simplified()

  operator fun minus(other: Rational): Rational =
    Rational(num * other.den - other.num * den, den * other.den).simplified()

  operator fun times(other: Rational): Rational =
    Rational(num * other.num, den * other.den).simplified()

  operator fun div(other: Rational): Rational {
    require(other.num != BigInteger.ZERO) { "Cannot divide by zero fraction" }
    return Rational(num * other.den, den * other.num).simplified()
  }

  fun toDouble(): Double = num.toDouble() / den.toDouble()

  fun toImproperString(): String = if (den == BigInteger.ONE) "$num" else "$num/$den"

  fun toMixedString(): String {
    val s = simplified()
    val absNum = s.num.abs()
    if (s.den == BigInteger.ONE || absNum < s.den) return s.toImproperString()
    val whole = s.num / s.den
    val rem = absNum % s.den
    return if (rem == BigInteger.ZERO) "$whole" else "$whole $rem/${s.den}"
  }

  companion object {
    fun fromMixed(whole: BigInteger, num: BigInteger, den: BigInteger): Rational {
      require(den != BigInteger.ZERO) { "Denominator cannot be zero" }
      val sign = if (whole < BigInteger.ZERO) BigInteger.valueOf(-1) else BigInteger.ONE
      val improperNum = whole.abs() * den.abs() + num.abs()
      val finalSign = if ((whole < BigInteger.ZERO) xor (den < BigInteger.ZERO)) -sign else sign
      return Rational(improperNum * finalSign, den.abs()).simplified()
    }

    fun fromDecimalString(decimalStr: String): Rational {
      val bd = BigDecimal(decimalStr.trim())
      val scale = bd.scale()
      return if (scale <= 0) {
        Rational(bd.toBigInteger(), BigInteger.ONE).simplified()
      } else {
        val den = BigInteger.TEN.pow(scale)
        val num = bd.unscaledValue()
        Rational(num, den).simplified()
      }
    }
  }
}

enum class FractionOp(val symbol: String) {
  ADD("+"),
  SUB("-"),
  MUL("×"),
  DIV("÷"),
}

data class FractionResult(
  val improper: String,
  val mixed: String,
  val decimal: Double,
)

object FractionEngine {
  fun operate(
    n1: Long,
    d1: Long,
    n2: Long,
    d2: Long,
    op: FractionOp,
  ): CalcOutcome<FractionResult> {
    if (d1 == 0L || d2 == 0L) return CalcOutcome.Error("Denominator cannot be zero")
    if (op == FractionOp.DIV && n2 == 0L) {
      return CalcOutcome.Error("Cannot divide by a zero fraction")
    }
    val f1 = Rational(BigInteger.valueOf(n1), BigInteger.valueOf(d1)).simplified()
    val f2 = Rational(BigInteger.valueOf(n2), BigInteger.valueOf(d2)).simplified()
    val res =
      when (op) {
        FractionOp.ADD -> f1 + f2
        FractionOp.SUB -> f1 - f2
        FractionOp.MUL -> f1 * f2
        FractionOp.DIV -> f1 / f2
      }
    val dec = res.toDouble()
    val steps =
      listOf(
        "Simplified operands: ${f1.toImproperString()} ${op.symbol} ${f2.toImproperString()}",
        "Exact simplified fraction: ${res.toImproperString()}",
        "Mixed form: ${res.toMixedString()}",
        "Decimal value: ${NumberFormatter.formatPlain(dec, 8)}",
      )
    return CalcOutcome.Success(
      FractionResult(res.toImproperString(), res.toMixedString(), dec),
      StepBreakdown(
        formula = "a/b ${op.symbol} c/d",
        inputValues = "$n1/$d1 and $n2/$d2",
        substitution = "${f1.toImproperString()} ${op.symbol} ${f2.toImproperString()}",
        calculationSteps = steps,
        finalAnswer = res.toImproperString(),
      ),
    )
  }

  fun decimalToFraction(decimalInput: String): CalcOutcome<FractionResult> {
    val clean = decimalInput.trim()
    if (clean.toBigDecimalOrNull() == null) {
      return CalcOutcome.Error("Enter a valid decimal number (e.g. 0.75)")
    }
    val rat = Rational.fromDecimalString(clean)
    val dec = rat.toDouble()
    return CalcOutcome.Success(
      FractionResult(rat.toImproperString(), rat.toMixedString(), dec),
      StepBreakdown(
        formula = "Decimal → p/q (reduced by GCD)",
        inputValues = clean,
        substitution = "Convert $clean to exact ratio and divide by GCD",
        calculationSteps =
          listOf(
            "Simplified fraction: ${rat.toImproperString()}",
            "Mixed form: ${rat.toMixedString()}",
          ),
        finalAnswer = rat.toImproperString(),
      ),
    )
  }

  fun mixedToImproper(whole: Long, num: Long, den: Long): CalcOutcome<FractionResult> {
    if (den == 0L) return CalcOutcome.Error("Denominator cannot be zero")
    val rat =
      Rational.fromMixed(
        BigInteger.valueOf(whole),
        BigInteger.valueOf(num),
        BigInteger.valueOf(den),
      )
    return CalcOutcome.Success(
      FractionResult(rat.toImproperString(), rat.toMixedString(), rat.toDouble()),
      StepBreakdown(
        formula = "w n/d = (|w|×d + n) / d",
        inputValues = "$whole $num/$den",
        substitution = "($whole × $den + $num) / $den",
        calculationSteps = listOf("Simplified improper fraction: ${rat.toImproperString()}"),
        finalAnswer = rat.toImproperString(),
      ),
    )
  }
}

// ==========================================
// 2. EQUATION SOLVER ENGINE
// ==========================================

data class EquationResult(
  val summary: String,
  val status: String,
  val roots: List<String>,
  val discriminant: Double? = null,
)

object EquationEngine {

  fun solveLinear(a: Double, b: Double): CalcOutcome<EquationResult> {
    val formula = "ax + b = 0  ⇒  x = -b / a"
    return when {
      abs(a) < 1e-12 && abs(b) < 1e-12 -> {
        val res = EquationResult("Infinitely many solutions (0 = 0)", "Dependent Identity", emptyList())
        CalcOutcome.Success(
          res,
          StepBreakdown(formula, "a = 0, b = 0", "0x + 0 = 0", listOf("Every real x satisfies 0 = 0"), "Infinitely many solutions"),
        )
      }
      abs(a) < 1e-12 -> {
        val res = EquationResult("No solution (inconsistent: ${NumberFormatter.formatPlain(b)} = 0)", "Inconsistent", emptyList())
        CalcOutcome.Success(
          res,
          StepBreakdown(formula, "a = 0, b = ${NumberFormatter.formatPlain(b)}", "0x + ${NumberFormatter.formatPlain(b)} = 0", listOf("Contradiction: constant ≠ 0"), "No solution"),
        )
      }
      else -> {
        val x = -b / a
        val xStr = NumberFormatter.formatPlain(x)
        val res = EquationResult("x = $xStr", "Unique Real Solution", listOf(xStr))
        CalcOutcome.Success(
          res,
          StepBreakdown(
            formula = formula,
            inputValues = "a = ${NumberFormatter.formatPlain(a)}, b = ${NumberFormatter.formatPlain(b)}",
            substitution = "${NumberFormatter.formatPlain(a)}x + (${NumberFormatter.formatPlain(b)}) = 0",
            calculationSteps = listOf("x = -(${NumberFormatter.formatPlain(b)}) / ${NumberFormatter.formatPlain(a)}", "x = $xStr"),
            finalAnswer = "x = $xStr",
          ),
        )
      }
    }
  }

  fun solveQuadratic(a: Double, b: Double, c: Double): CalcOutcome<EquationResult> {
    if (abs(a) < 1e-12) {
      return solveLinear(b, c)
    }
    val d = b * b - 4.0 * a * c
    val dStr = NumberFormatter.formatPlain(d)
    val aStr = NumberFormatter.formatPlain(a)
    val bStr = NumberFormatter.formatPlain(b)
    val cStr = NumberFormatter.formatPlain(c)
    val formula = "x = (-b ± √(b² - 4ac)) / (2a)"

    return if (d > 1e-12) {
      val sqrtD = sqrt(d)
      val x1 = (-b + sqrtD) / (2.0 * a)
      val x2 = (-b - sqrtD) / (2.0 * a)
      val r1 = NumberFormatter.formatPlain(x1)
      val r2 = NumberFormatter.formatPlain(x2)
      CalcOutcome.Success(
        EquationResult("x₁ = $r1,  x₂ = $r2", "Two Distinct Real Roots", listOf(r1, r2), d),
        StepBreakdown(
          formula = formula,
          inputValues = "a = $aStr, b = $bStr, c = $cStr",
          substitution = "x = (-($bStr) ± √(($bStr)² - 4($aStr)($cStr))) / (2($aStr))",
          calculationSteps =
            listOf(
              "Discriminant Δ = b² - 4ac = $dStr (> 0, real & distinct)",
              "√Δ = ${NumberFormatter.formatPlain(sqrtD)}",
              "x₁ = (-($bStr) + ${NumberFormatter.formatPlain(sqrtD)}) / ${NumberFormatter.formatPlain(2 * a)} = $r1",
              "x₂ = (-($bStr) - ${NumberFormatter.formatPlain(sqrtD)}) / ${NumberFormatter.formatPlain(2 * a)} = $r2",
            ),
          finalAnswer = "x₁ = $r1, x₂ = $r2",
        ),
      )
    } else if (abs(d) <= 1e-12) {
      val x = -b / (2.0 * a)
      val r = NumberFormatter.formatPlain(x)
      CalcOutcome.Success(
        EquationResult("x = $r (repeated)", "One Repeated Real Root", listOf(r), 0.0),
        StepBreakdown(
          formula = formula,
          inputValues = "a = $aStr, b = $bStr, c = $cStr",
          substitution = "x = -($bStr) / (2 × $aStr)",
          calculationSteps = listOf("Discriminant Δ = 0 (equal real roots)", "x = $r"),
          finalAnswer = "x = $r",
        ),
      )
    } else {
      val realPart = -b / (2.0 * a)
      val imagPart = sqrt(-d) / abs(2.0 * a)
      val rp = NumberFormatter.formatPlain(realPart)
      val ip = NumberFormatter.formatPlain(imagPart)
      val r1 = "$rp + ${ip}i"
      val r2 = "$rp - ${ip}i"
      CalcOutcome.Success(
        EquationResult("x₁ = $r1,  x₂ = $r2", "Two Complex Conjugate Roots", listOf(r1, r2), d),
        StepBreakdown(
          formula = formula,
          inputValues = "a = $aStr, b = $bStr, c = $cStr",
          substitution = "x = (-($bStr) ± i√(${NumberFormatter.formatPlain(-d)})) / (2($aStr))",
          calculationSteps =
            listOf(
              "Discriminant Δ = $dStr (< 0, complex roots)",
              "Real part = -$bStr / (2×$aStr) = $rp",
              "Imaginary part = √${NumberFormatter.formatPlain(-d)} / |2×$aStr| = $ip",
            ),
          finalAnswer = "x₁ = $r1, x₂ = $r2",
        ),
      )
    }
  }

  fun solveSystem2x2(
    a1: Double,
    b1: Double,
    c1: Double,
    a2: Double,
    b2: Double,
    c2: Double,
  ): CalcOutcome<EquationResult> {
    val det = a1 * b2 - a2 * b1
    val detX = c1 * b2 - c2 * b1
    val detY = a1 * c2 - a2 * c1
    val formula = "Cramer's Rule: x = Dₓ/D, y = Dᵧ/D"
    return if (abs(det) > 1e-12) {
      val x = detX / det
      val y = detY / det
      val xs = NumberFormatter.formatPlain(x)
      val ys = NumberFormatter.formatPlain(y)
      CalcOutcome.Success(
        EquationResult("x = $xs,  y = $ys", "Unique Solution", listOf(xs, ys)),
        StepBreakdown(
          formula = formula,
          inputValues = "${NumberFormatter.formatPlain(a1)}x + ${NumberFormatter.formatPlain(b1)}y = ${NumberFormatter.formatPlain(c1)}; ${NumberFormatter.formatPlain(a2)}x + ${NumberFormatter.formatPlain(b2)}y = ${NumberFormatter.formatPlain(c2)}",
          substitution = "D = (${NumberFormatter.formatPlain(a1)})(${NumberFormatter.formatPlain(b2)}) - (${NumberFormatter.formatPlain(a2)})(${NumberFormatter.formatPlain(b1)})",
          calculationSteps =
            listOf(
              "D = ${NumberFormatter.formatPlain(det)}",
              "Dₓ = ${NumberFormatter.formatPlain(detX)}, Dᵧ = ${NumberFormatter.formatPlain(detY)}",
              "x = Dₓ / D = $xs",
              "y = Dᵧ / D = $ys",
            ),
          finalAnswer = "x = $xs, y = $ys",
        ),
      )
    } else {
      val consistent =
        abs(detX) < 1e-10 &&
          abs(detY) < 1e-10 &&
          !(abs(a1) < 1e-12 && abs(b1) < 1e-12 && abs(c1) > 1e-12) &&
          !(abs(a2) < 1e-12 && abs(b2) < 1e-12 && abs(c2) > 1e-12)
      if (consistent) {
        CalcOutcome.Success(
          EquationResult("Infinitely many solutions (dependent lines)", "Dependent System", emptyList()),
          StepBreakdown(formula, "2×2 system", "D = 0, Dₓ = 0, Dᵧ = 0", listOf("The two equations represent the same line"), "Infinitely many solutions"),
        )
      } else {
        CalcOutcome.Success(
          EquationResult("No solution (parallel inconsistent lines)", "Inconsistent System", emptyList()),
          StepBreakdown(formula, "2×2 system", "D = 0 with non-matching constants", listOf("Lines are parallel and never intersect"), "No solution"),
        )
      }
    }
  }

  fun solveSystem3x3(
    coeff: Array<DoubleArray>, // 3x3
    rhs: DoubleArray, // length 3
  ): CalcOutcome<EquationResult> {
    if (coeff.size != 3 || coeff.any { it.size != 3 } || rhs.size != 3) {
      return CalcOutcome.Error("3×3 system requires 3 equations with 3 variables")
    }
    // Augmented matrix Gaussian elimination with partial pivoting
    val aug = Array(3) { r -> DoubleArray(4) { c -> if (c < 3) coeff[r][c] else rhs[r] } }
    var pivotRow = 0
    for (col in 0 until 3) {
      var bestRow = pivotRow
      for (r in pivotRow until 3) {
        if (abs(aug[r][col]) > abs(aug[bestRow][col])) bestRow = r
      }
      if (abs(aug[bestRow][col]) < 1e-11) continue
      val tmp = aug[pivotRow]
      aug[pivotRow] = aug[bestRow]
      aug[bestRow] = tmp

      val pivotVal = aug[pivotRow][col]
      for (c in col until 4) aug[pivotRow][c] /= pivotVal
      for (r in 0 until 3) {
        if (r != pivotRow && abs(aug[r][col]) > 1e-12) {
          val factor = aug[r][col]
          for (c in col until 4) aug[r][c] -= factor * aug[pivotRow][c]
        }
      }
      pivotRow++
    }

    // Check inconsistency
    for (r in 0 until 3) {
      val allZeroCoeff = abs(aug[r][0]) < 1e-9 && abs(aug[r][1]) < 1e-9 && abs(aug[r][2]) < 1e-9
      if (allZeroCoeff && abs(aug[r][3]) > 1e-9) {
        return CalcOutcome.Success(
          EquationResult("No solution (inconsistent system)", "Inconsistent System", emptyList()),
          StepBreakdown("Gaussian Elimination", "3×3 linear system", "Row echelon form yields 0 = ${NumberFormatter.formatPlain(aug[r][3])}", listOf("System is inconsistent"), "No solution"),
        )
      }
    }
    if (pivotRow < 3) {
      return CalcOutcome.Success(
        EquationResult("Infinitely many solutions (dependent system, rank $pivotRow)", "Dependent System", emptyList()),
        StepBreakdown("Gaussian Elimination", "3×3 linear system", "Rank = $pivotRow < 3", listOf("At least one free variable exists"), "Infinitely many solutions"),
      )
    }

    val x = NumberFormatter.formatPlain(aug[0][3])
    val y = NumberFormatter.formatPlain(aug[1][3])
    val z = NumberFormatter.formatPlain(aug[2][3])
    return CalcOutcome.Success(
      EquationResult("x = $x,  y = $y,  z = $z", "Unique Solution", listOf(x, y, z)),
      StepBreakdown(
        formula = "Gaussian Elimination (RREF)",
        inputValues = "3×3 linear system",
        substitution = "Reduced row echelon form yields identity matrix",
        calculationSteps = listOf("x = $x", "y = $y", "z = $z"),
        finalAnswer = "x = $x, y = $y, z = $z",
      ),
    )
  }
}

// ==========================================
// 3. MATRIX ENGINE (2x2 & 3x3)
// ==========================================

enum class MatrixOp(val label: String) {
  ADD("A + B"),
  SUB("A - B"),
  MUL("A × B"),
  DET("det(A)"),
  TRANSPOSE("Aᵀ"),
  INVERSE("A⁻¹"),
}

data class MatrixResult(
  val matrix: List<List<Double>>? = null,
  val scalar: Double? = null,
  val label: String,
)

object MatrixEngine {
  fun compute(
    size: Int,
    a: List<List<Double>>,
    b: List<List<Double>>?,
    op: MatrixOp,
  ): CalcOutcome<MatrixResult> {
    if (size !in 2..3 || a.size != size || a.any { it.size != size }) {
      return CalcOutcome.Error("Invalid matrix A dimensions")
    }
    if ((op == MatrixOp.ADD || op == MatrixOp.SUB || op == MatrixOp.MUL) &&
      (b == null || b.size != size || b.any { it.size != size })
    ) {
      return CalcOutcome.Error("Invalid matrix B dimensions")
    }

    return when (op) {
      MatrixOp.ADD -> {
        val m = List(size) { r -> List(size) { c -> a[r][c] + b!![r][c] } }
        CalcOutcome.Success(
          MatrixResult(matrix = m, label = "A + B"),
          StepBreakdown("C[i,j] = A[i,j] + B[i,j]", "${size}×${size} matrices", "Element-wise addition", listOf(formatMatrix(m)), formatMatrix(m)),
        )
      }
      MatrixOp.SUB -> {
        val m = List(size) { r -> List(size) { c -> a[r][c] - b!![r][c] } }
        CalcOutcome.Success(
          MatrixResult(matrix = m, label = "A - B"),
          StepBreakdown("C[i,j] = A[i,j] - B[i,j]", "${size}×${size} matrices", "Element-wise subtraction", listOf(formatMatrix(m)), formatMatrix(m)),
        )
      }
      MatrixOp.MUL -> {
        val m =
          List(size) { r ->
            List(size) { c ->
              var sum = 0.0
              for (k in 0 until size) sum += a[r][k] * b!![k][c]
              sum
            }
          }
        CalcOutcome.Success(
          MatrixResult(matrix = m, label = "A × B"),
          StepBreakdown("C[i,j] = Σ A[i,k] × B[k,j]", "${size}×${size} matrices", "Row-by-column dot products", listOf(formatMatrix(m)), formatMatrix(m)),
        )
      }
      MatrixOp.TRANSPOSE -> {
        val m = List(size) { r -> List(size) { c -> a[c][r] } }
        CalcOutcome.Success(
          MatrixResult(matrix = m, label = "Aᵀ"),
          StepBreakdown("(Aᵀ)[i,j] = A[j,i]", "${size}×${size} matrix A", "Swap rows and columns", listOf(formatMatrix(m)), formatMatrix(m)),
        )
      }
      MatrixOp.DET -> {
        val d = determinant(a)
        val ds = NumberFormatter.formatPlain(d)
        CalcOutcome.Success(
          MatrixResult(scalar = d, label = "det(A) = $ds"),
          StepBreakdown(
            formula = if (size == 2) "det(A) = ad - bc" else "Cofactor expansion along row 1",
            inputValues = formatMatrix(a),
            substitution = "Computed determinant of ${size}×${size} matrix",
            calculationSteps = listOf("det(A) = $ds"),
            finalAnswer = ds,
          ),
        )
      }
      MatrixOp.INVERSE -> {
        val d = determinant(a)
        if (abs(d) < 1e-12) {
          return CalcOutcome.Error("Matrix is singular (det = 0); inverse does not exist")
        }
        val inv = inverse(a, d)
        CalcOutcome.Success(
          MatrixResult(matrix = inv, label = "A⁻¹"),
          StepBreakdown(
            formula = "A⁻¹ = (1 / det(A)) × adj(A)",
            inputValues = formatMatrix(a),
            substitution = "det(A) = ${NumberFormatter.formatPlain(d)}",
            calculationSteps = listOf("Multiply adjugate by 1 / ${NumberFormatter.formatPlain(d)}", formatMatrix(inv)),
            finalAnswer = formatMatrix(inv),
          ),
        )
      }
    }
  }

  private fun determinant(m: List<List<Double>>): Double {
    return if (m.size == 2) {
      m[0][0] * m[1][1] - m[0][1] * m[1][0]
    } else {
      m[0][0] * (m[1][1] * m[2][2] - m[1][2] * m[2][1]) -
        m[0][1] * (m[1][0] * m[2][2] - m[1][2] * m[2][0]) +
        m[0][2] * (m[1][0] * m[2][1] - m[1][1] * m[2][0])
    }
  }

  private fun inverse(m: List<List<Double>>, det: Double): List<List<Double>> {
    return if (m.size == 2) {
      listOf(
        listOf(m[1][1] / det, -m[0][1] / det),
        listOf(-m[1][0] / det, m[0][0] / det),
      )
    } else {
      val adj = Array(3) { DoubleArray(3) }
      for (r in 0 until 3) {
        for (c in 0 until 3) {
          val sub = mutableListOf<Double>()
          for (i in 0 until 3) {
            if (i == r) continue
            for (j in 0 until 3) {
              if (j == c) continue
              sub.add(m[i][j])
            }
          }
          val minor = sub[0] * sub[3] - sub[1] * sub[2]
          val sign = if ((r + c) % 2 == 0) 1.0 else -1.0
          adj[c][r] = sign * minor / det
        }
      }
      adj.map { it.toList() }
    }
  }

  fun formatMatrix(m: List<List<Double>>): String =
    m.joinToString("\n") { row -> "[ " + row.joinToString(" , ") { NumberFormatter.formatPlain(it, 4) } + " ]" }
}

// ==========================================
// 4. VECTOR ENGINE (2D & 3D)
// ==========================================

enum class VectorOp(val label: String) {
  ADD("u + v"),
  SUB("u - v"),
  MAGNITUDE("||u|| & ||v||"),
  DOT("u · v (Dot)"),
  CROSS("u × v (Cross)"),
  ANGLE("Angle θ"),
}

data class VectorResult(val display: String)

object VectorEngine {
  fun compute(
    is3D: Boolean,
    u: DoubleArray,
    v: DoubleArray,
    op: VectorOp,
    angleMode: AngleMode = AngleMode.DEG,
  ): CalcOutcome<VectorResult> {
    val dim = if (is3D) 3 else 2
    val ux = u[0]
    val uy = u[1]
    val uz = if (is3D) u[2] else 0.0
    val vx = v[0]
    val vy = v[1]
    val vz = if (is3D) v[2] else 0.0

    fun fmtVec(x: Double, y: Double, z: Double): String =
      if (is3D) "(${NumberFormatter.formatPlain(x)}, ${NumberFormatter.formatPlain(y)}, ${NumberFormatter.formatPlain(z)})"
      else "(${NumberFormatter.formatPlain(x)}, ${NumberFormatter.formatPlain(y)})"

    val magU = sqrt(ux * ux + uy * uy + uz * uz)
    val magV = sqrt(vx * vx + vy * vy + vz * vz)

    return when (op) {
      VectorOp.ADD -> {
        val res = fmtVec(ux + vx, uy + vy, uz + vz)
        CalcOutcome.Success(
          VectorResult(res),
          StepBreakdown("u + v = (u₁+v₁, u₂+v₂${if (is3D) ", u₃+v₃" else ""})", "${dim}D vectors", "Add components", listOf("Result = $res"), res),
        )
      }
      VectorOp.SUB -> {
        val res = fmtVec(ux - vx, uy - vy, uz - vz)
        CalcOutcome.Success(
          VectorResult(res),
          StepBreakdown("u - v = (u₁-v₁, u₂-v₂${if (is3D) ", u₃-v₃" else ""})", "${dim}D vectors", "Subtract components", listOf("Result = $res"), res),
        )
      }
      VectorOp.MAGNITUDE -> {
        val mu = NumberFormatter.formatPlain(magU)
        val mv = NumberFormatter.formatPlain(magV)
        val res = "||u|| = $mu,  ||v|| = $mv"
        CalcOutcome.Success(
          VectorResult(res),
          StepBreakdown("||u|| = √(Σ uᵢ²)", "u = ${fmtVec(ux, uy, uz)}, v = ${fmtVec(vx, vy, vz)}", "Sum squares and take square root", listOf("||u|| = $mu", "||v|| = $mv"), res),
        )
      }
      VectorOp.DOT -> {
        val dot = ux * vx + uy * vy + uz * vz
        val ds = NumberFormatter.formatPlain(dot)
        CalcOutcome.Success(
          VectorResult("u · v = $ds"),
          StepBreakdown("u · v = Σ uᵢvᵢ", "u = ${fmtVec(ux, uy, uz)}, v = ${fmtVec(vx, vy, vz)}", "Multiply corresponding components and sum", listOf("u · v = $ds"), ds),
        )
      }
      VectorOp.CROSS -> {
        val cx = uy * vz - uz * vy
        val cy = uz * vx - ux * vz
        val cz = ux * vy - uy * vx
        val res =
          if (is3D) "(${NumberFormatter.formatPlain(cx)}, ${NumberFormatter.formatPlain(cy)}, ${NumberFormatter.formatPlain(cz)})"
          else "0i + 0j + ${NumberFormatter.formatPlain(cz)}k (scalar z = ${NumberFormatter.formatPlain(cz)})"
        CalcOutcome.Success(
          VectorResult("u × v = $res"),
          StepBreakdown("u × v = (u₂v₃-u₃v₂, u₃v₁-u₁v₃, u₁v₂-u₂v₁)", "u, v", "Determinant of i, j, k", listOf("u × v = $res"), res),
        )
      }
      VectorOp.ANGLE -> {
        if (magU < 1e-12 || magV < 1e-12) {
          return CalcOutcome.Error("Angle is undefined for a zero-magnitude vector")
        }
        val dot = ux * vx + uy * vy + uz * vz
        val cosTheta = (dot / (magU * magV)).coerceIn(-1.0, 1.0)
        val rad = acos(cosTheta)
        val converted =
          when (angleMode) {
            AngleMode.DEG -> rad * 180.0 / PI
            AngleMode.RAD -> rad
            AngleMode.GRAD -> rad * 200.0 / PI
          }
        val unit =
          when (angleMode) {
            AngleMode.DEG -> "°"
            AngleMode.RAD -> " rad"
            AngleMode.GRAD -> " grad"
          }
        val ans = "${NumberFormatter.formatPlain(converted)}$unit"
        CalcOutcome.Success(
          VectorResult("θ = $ans"),
          StepBreakdown(
            formula = "cos(θ) = (u · v) / (||u|| × ||v||)",
            inputValues = "u·v = ${NumberFormatter.formatPlain(dot)}, ||u|| = ${NumberFormatter.formatPlain(magU)}, ||v|| = ${NumberFormatter.formatPlain(magV)}",
            substitution = "θ = arccos(${NumberFormatter.formatPlain(cosTheta)})",
            calculationSteps = listOf("θ = ${NumberFormatter.formatPlain(rad)} rad = $ans"),
            finalAnswer = ans,
          ),
        )
      }
    }
  }
}

// ==========================================
// 5. STATISTICS ENGINE
// ==========================================

data class StatisticsResult(
  val count: Int,
  val sum: Double,
  val mean: Double,
  val median: Double,
  val modeDescription: String,
  val min: Double,
  val max: Double,
  val range: Double,
  val populationVariance: Double,
  val sampleVariance: Double?,
  val populationStdDev: Double,
  val sampleStdDev: Double?,
)

object StatisticsEngine {
  fun parseNumbers(input: String): List<Double>? {
    val parts = input.split(Regex("[,\\s;]+")).map { it.trim() }.filter { it.isNotEmpty() }
    if (parts.isEmpty()) return null
    val numbers = parts.map { it.toDoubleOrNull() ?: return null }
    return numbers
  }

  fun compute(values: List<Double>): CalcOutcome<StatisticsResult> {
    if (values.isEmpty()) return CalcOutcome.Error("Enter at least one number")
    val sorted = values.sorted()
    val count = values.size
    val sum = values.sum()
    val mean = sum / count
    val median =
      if (count % 2 == 1) sorted[count / 2]
      else (sorted[count / 2 - 1] + sorted[count / 2]) / 2.0

    val freq = values.groupingBy { it }.eachCount()
    val maxFreq = freq.values.maxOrNull() ?: 1
    val modes =
      if (maxFreq == 1 && count > 1) emptyList()
      else freq.filterValues { it == maxFreq }.keys.sorted()
    val modeDesc =
      if (modes.isEmpty()) "No mode (all values unique)"
      else modes.joinToString(", ") { NumberFormatter.formatPlain(it) } + " (freq: $maxFreq)"

    val min = sorted.first()
    val max = sorted.last()
    val range = max - min

    val sqDiffSum = values.sumOf { (it - mean) * (it - mean) }
    val popVar = sqDiffSum / count
    val popStd = sqrt(popVar)
    val sampleVar = if (count > 1) sqDiffSum / (count - 1) else null
    val sampleStd = sampleVar?.let { sqrt(it) }

    val result =
      StatisticsResult(
        count = count,
        sum = sum,
        mean = mean,
        median = median,
        modeDescription = modeDesc,
        min = min,
        max = max,
        range = range,
        populationVariance = popVar,
        sampleVariance = sampleVar,
        populationStdDev = popStd,
        sampleStdDev = sampleStd,
      )
    return CalcOutcome.Success(
      result,
      StepBreakdown(
        formula = "Mean μ = Σx / N, σ² = Σ(x-μ)² / N, s² = Σ(x-x̄)² / (N-1)",
        inputValues = "N = $count values",
        substitution = "Σx = ${NumberFormatter.formatPlain(sum)}, μ = ${NumberFormatter.formatPlain(mean)}",
        calculationSteps =
          listOf(
            "Mean = ${NumberFormatter.formatPlain(mean)}, Median = ${NumberFormatter.formatPlain(median)}",
            "Population Variance σ² = ${NumberFormatter.formatPlain(popVar)}, σ = ${NumberFormatter.formatPlain(popStd)}",
            if (sampleVar != null) "Sample Variance s² = ${NumberFormatter.formatPlain(sampleVar)}, s = ${NumberFormatter.formatPlain(sampleStd!!)}"
            else "Sample Variance requires N ≥ 2",
          ),
        finalAnswer = "Mean = ${NumberFormatter.formatPlain(mean)}",
      ),
    )
  }
}

// ==========================================
// 6. PROBABILITY ENGINE
// ==========================================

data class ProbabilityResult(
  val primaryLabel: String,
  val primaryValue: String,
  val secondaryLabel: String? = null,
  val secondaryValue: String? = null,
)

object ProbabilityEngine {
  fun basicProbability(favorable: Double, total: Double): CalcOutcome<ProbabilityResult> {
    if (total <= 0.0) return CalcOutcome.Error("Total outcomes must be greater than 0")
    if (favorable < 0.0) return CalcOutcome.Error("Favorable outcomes cannot be negative")
    if (favorable > total) return CalcOutcome.Error("Favorable outcomes cannot exceed total outcomes")
    val p = favorable / total
    val comp = 1.0 - p
    val pStr = "${NumberFormatter.formatPlain(p, 6)} (${NumberFormatter.formatPlain(p * 100, 2)}%)"
    val cStr = "${NumberFormatter.formatPlain(comp, 6)} (${NumberFormatter.formatPlain(comp * 100, 2)}%)"
    return CalcOutcome.Success(
      ProbabilityResult("P(A)", pStr, "Complement P(A')", cStr),
      StepBreakdown(
        formula = "P(A) = n(A) / n(S),  P(A') = 1 - P(A)",
        inputValues = "n(A) = ${NumberFormatter.formatPlain(favorable)}, n(S) = ${NumberFormatter.formatPlain(total)}",
        substitution = "P(A) = ${NumberFormatter.formatPlain(favorable)} / ${NumberFormatter.formatPlain(total)}",
        calculationSteps = listOf("P(A) = $pStr", "P(A') = $cStr"),
        finalAnswer = pStr,
      ),
    )
  }

  fun permutation(n: Int, r: Int): CalcOutcome<ProbabilityResult> {
    if (n < 0 || r < 0) return CalcOutcome.Error("n and r must be non-negative integers")
    if (r > n) return CalcOutcome.Error("r cannot be greater than n (requires 0 ≤ r ≤ n)")
    if (n > 500) return CalcOutcome.Error("n must be ≤ 500 to prevent excessive computation")
    var res = BigInteger.ONE
    for (i in (n - r + 1)..n) {
      res *= BigInteger.valueOf(i.toLong())
    }
    return CalcOutcome.Success(
      ProbabilityResult("nPr ($n P $r)", res.toString()),
      StepBreakdown(
        formula = "nPr = n! / (n - r)!",
        inputValues = "n = $n, r = $r",
        substitution = "$n! / ($n - $r)!",
        calculationSteps = listOf("Multiply integers from ${n - r + 1} to $n", "nPr = $res"),
        finalAnswer = res.toString(),
      ),
    )
  }

  fun combination(n: Int, r: Int): CalcOutcome<ProbabilityResult> {
    if (n < 0 || r < 0) return CalcOutcome.Error("n and r must be non-negative integers")
    if (r > n) return CalcOutcome.Error("r cannot be greater than n (requires 0 ≤ r ≤ n)")
    if (n > 500) return CalcOutcome.Error("n must be ≤ 500 to prevent excessive computation")
    val k = minOf(r, n - r)
    var num = BigInteger.ONE
    var den = BigInteger.ONE
    for (i in 1..k) {
      num *= BigInteger.valueOf((n - k + i).toLong())
      den *= BigInteger.valueOf(i.toLong())
    }
    val res = num / den
    return CalcOutcome.Success(
      ProbabilityResult("nCr ($n C $r)", res.toString()),
      StepBreakdown(
        formula = "nCr = n! / (r! × (n - r)!)",
        inputValues = "n = $n, r = $r",
        substitution = "$n! / ($r! × ${n - r}!)",
        calculationSteps = listOf("nCr = $res"),
        finalAnswer = res.toString(),
      ),
    )
  }
}

// ==========================================
// 7. PHYSICS ENGINE
// ==========================================

enum class PhysicsToolType(val title: String, val group: String) {
  SPEED("Speed (v = d / t)", "Motion"),
  VELOCITY("Velocity (v = Δs / t)", "Motion"),
  DISTANCE("Distance (d = v × t)", "Motion"),
  TIME("Time (t = d / v)", "Motion"),
  ACCELERATION("Acceleration (a = (v - u) / t)", "Motion"),
  FORCE("Force (F = m × a)", "Force & Energy"),
  WORK("Work (W = F × d × cos θ)", "Force & Energy"),
  POWER("Power (P = W / t)", "Force & Energy"),
  KINETIC_ENERGY("Kinetic Energy (KE = ½mv²)", "Force & Energy"),
  POTENTIAL_ENERGY("Potential Energy (PE = mgh)", "Force & Energy"),
  MOMENTUM("Momentum (p = m × v)", "Force & Energy"),
  OHMS_VOLTAGE("Voltage (V = I × R)", "Electrical"),
  OHMS_CURRENT("Current (I = V / R)", "Electrical"),
  OHMS_RESISTANCE("Resistance (R = V / I)", "Electrical"),
  ELECTRICAL_POWER("Electrical Power (P = V × I)", "Electrical"),
  DENSITY("Density (ρ = m / V)", "Other"),
  PRESSURE("Pressure (P = F / A)", "Other"),
  WAVE_SPEED("Wave Speed (v = f × λ)", "Other"),
  FREQUENCY("Frequency (f = v / λ)", "Other"),
  WAVELENGTH("Wavelength (λ = v / f)", "Other"),
}

object PhysicsEngine {
  fun calculate(
    tool: PhysicsToolType,
    a: Double,
    b: Double,
    c: Double = 0.0,
  ): CalcOutcome<Pair<Double, String>> {
    fun fmt(v: Double) = NumberFormatter.formatPlain(v)
    return when (tool) {
      PhysicsToolType.SPEED -> {
        if (a < 0) return CalcOutcome.Error("Distance cannot be negative")
        if (b <= 0) return CalcOutcome.Error("Time must be greater than zero")
        val v = a / b
        CalcOutcome.Success(v to "m/s", StepBreakdown("v = d / t", "d = ${fmt(a)} m, t = ${fmt(b)} s", "v = ${fmt(a)} / ${fmt(b)}", listOf("v = ${fmt(v)} m/s"), "${fmt(v)} m/s", "m/s"))
      }
      PhysicsToolType.VELOCITY -> {
        if (b <= 0) return CalcOutcome.Error("Time must be greater than zero")
        val v = a / b
        CalcOutcome.Success(v to "m/s", StepBreakdown("v = Δs / t", "Δs = ${fmt(a)} m, t = ${fmt(b)} s", "v = ${fmt(a)} / ${fmt(b)}", listOf("v = ${fmt(v)} m/s"), "${fmt(v)} m/s", "m/s"))
      }
      PhysicsToolType.DISTANCE -> {
        if (b < 0) return CalcOutcome.Error("Time cannot be negative")
        val d = a * b
        CalcOutcome.Success(d to "m", StepBreakdown("d = v × t", "v = ${fmt(a)} m/s, t = ${fmt(b)} s", "d = ${fmt(a)} × ${fmt(b)}", listOf("d = ${fmt(d)} m"), "${fmt(d)} m", "m"))
      }
      PhysicsToolType.TIME -> {
        if (a < 0) return CalcOutcome.Error("Distance cannot be negative")
        if (b <= 0) return CalcOutcome.Error("Speed must be greater than zero")
        val t = a / b
        CalcOutcome.Success(t to "s", StepBreakdown("t = d / v", "d = ${fmt(a)} m, v = ${fmt(b)} m/s", "t = ${fmt(a)} / ${fmt(b)}", listOf("t = ${fmt(t)} s"), "${fmt(t)} s", "s"))
      }
      PhysicsToolType.ACCELERATION -> {
        if (c <= 0) return CalcOutcome.Error("Time interval must be greater than zero")
        val acc = (b - a) / c
        CalcOutcome.Success(acc to "m/s²", StepBreakdown("a = (v - u) / t", "u = ${fmt(a)} m/s, v = ${fmt(b)} m/s, t = ${fmt(c)} s", "a = (${fmt(b)} - ${fmt(a)}) / ${fmt(c)}", listOf("a = ${fmt(acc)} m/s²"), "${fmt(acc)} m/s²", "m/s²"))
      }
      PhysicsToolType.FORCE -> {
        if (a < 0) return CalcOutcome.Error("Mass cannot be negative")
        val f = a * b
        CalcOutcome.Success(f to "N", StepBreakdown("F = m × a", "m = ${fmt(a)} kg, a = ${fmt(b)} m/s²", "F = ${fmt(a)} × ${fmt(b)}", listOf("F = ${fmt(f)} N"), "${fmt(f)} N", "N"))
      }
      PhysicsToolType.WORK -> {
        val w = a * b * cos(c * PI / 180.0)
        CalcOutcome.Success(w to "J", StepBreakdown("W = F × d × cos(θ)", "F = ${fmt(a)} N, d = ${fmt(b)} m, θ = ${fmt(c)}°", "W = ${fmt(a)} × ${fmt(b)} × cos(${fmt(c)}°)", listOf("W = ${fmt(w)} J"), "${fmt(w)} J", "J"))
      }
      PhysicsToolType.POWER -> {
        if (b <= 0) return CalcOutcome.Error("Time must be greater than zero")
        val p = a / b
        CalcOutcome.Success(p to "W", StepBreakdown("P = W / t", "W = ${fmt(a)} J, t = ${fmt(b)} s", "P = ${fmt(a)} / ${fmt(b)}", listOf("P = ${fmt(p)} W"), "${fmt(p)} W", "W"))
      }
      PhysicsToolType.KINETIC_ENERGY -> {
        if (a < 0) return CalcOutcome.Error("Mass cannot be negative")
        val ke = 0.5 * a * b * b
        CalcOutcome.Success(ke to "J", StepBreakdown("KE = ½ × m × v²", "m = ${fmt(a)} kg, v = ${fmt(b)} m/s", "KE = 0.5 × ${fmt(a)} × (${fmt(b)})²", listOf("KE = ${fmt(ke)} J"), "${fmt(ke)} J", "J"))
      }
      PhysicsToolType.POTENTIAL_ENERGY -> {
        if (a < 0) return CalcOutcome.Error("Mass cannot be negative")
        val g = if (c > 0) c else 9.80665
        val pe = a * g * b
        CalcOutcome.Success(pe to "J", StepBreakdown("PE = m × g × h", "m = ${fmt(a)} kg, h = ${fmt(b)} m, g = ${fmt(g)} m/s²", "PE = ${fmt(a)} × ${fmt(g)} × ${fmt(b)}", listOf("PE = ${fmt(pe)} J"), "${fmt(pe)} J", "J"))
      }
      PhysicsToolType.MOMENTUM -> {
        if (a < 0) return CalcOutcome.Error("Mass cannot be negative")
        val p = a * b
        CalcOutcome.Success(p to "kg·m/s", StepBreakdown("p = m × v", "m = ${fmt(a)} kg, v = ${fmt(b)} m/s", "p = ${fmt(a)} × ${fmt(b)}", listOf("p = ${fmt(p)} kg·m/s"), "${fmt(p)} kg·m/s", "kg·m/s"))
      }
      PhysicsToolType.OHMS_VOLTAGE -> {
        if (b < 0) return CalcOutcome.Error("Resistance cannot be negative")
        val v = a * b
        CalcOutcome.Success(v to "V", StepBreakdown("V = I × R", "I = ${fmt(a)} A, R = ${fmt(b)} Ω", "V = ${fmt(a)} × ${fmt(b)}", listOf("V = ${fmt(v)} V"), "${fmt(v)} V", "V"))
      }
      PhysicsToolType.OHMS_CURRENT -> {
        if (b <= 0) return CalcOutcome.Error("Resistance must be greater than zero")
        val i = a / b
        CalcOutcome.Success(i to "A", StepBreakdown("I = V / R", "V = ${fmt(a)} V, R = ${fmt(b)} Ω", "I = ${fmt(a)} / ${fmt(b)}", listOf("I = ${fmt(i)} A"), "${fmt(i)} A", "A"))
      }
      PhysicsToolType.OHMS_RESISTANCE -> {
        if (b == 0.0) return CalcOutcome.Error("Current cannot be zero")
        val r = a / b
        if (r < 0) return CalcOutcome.Error("Resistance cannot be negative; ensure V and I share sign")
        CalcOutcome.Success(r to "Ω", StepBreakdown("R = V / I", "V = ${fmt(a)} V, I = ${fmt(b)} A", "R = ${fmt(a)} / ${fmt(b)}", listOf("R = ${fmt(r)} Ω"), "${fmt(r)} Ω", "Ω"))
      }
      PhysicsToolType.ELECTRICAL_POWER -> {
        val p = a * b
        CalcOutcome.Success(p to "W", StepBreakdown("P = V × I", "V = ${fmt(a)} V, I = ${fmt(b)} A", "P = ${fmt(a)} × ${fmt(b)}", listOf("P = ${fmt(p)} W"), "${fmt(p)} W", "W"))
      }
      PhysicsToolType.DENSITY -> {
        if (a < 0) return CalcOutcome.Error("Mass cannot be negative")
        if (b <= 0) return CalcOutcome.Error("Volume must be greater than zero")
        val rho = a / b
        CalcOutcome.Success(rho to "kg/m³", StepBreakdown("ρ = m / V", "m = ${fmt(a)} kg, V = ${fmt(b)} m³", "ρ = ${fmt(a)} / ${fmt(b)}", listOf("ρ = ${fmt(rho)} kg/m³"), "${fmt(rho)} kg/m³", "kg/m³"))
      }
      PhysicsToolType.PRESSURE -> {
        if (b <= 0) return CalcOutcome.Error("Area must be greater than zero")
        val p = a / b
        CalcOutcome.Success(p to "Pa", StepBreakdown("P = F / A", "F = ${fmt(a)} N, A = ${fmt(b)} m²", "P = ${fmt(a)} / ${fmt(b)}", listOf("P = ${fmt(p)} Pa"), "${fmt(p)} Pa", "Pa"))
      }
      PhysicsToolType.WAVE_SPEED -> {
        if (a < 0 || b < 0) return CalcOutcome.Error("Frequency and wavelength must be non-negative")
        val v = a * b
        CalcOutcome.Success(v to "m/s", StepBreakdown("v = f × λ", "f = ${fmt(a)} Hz, λ = ${fmt(b)} m", "v = ${fmt(a)} × ${fmt(b)}", listOf("v = ${fmt(v)} m/s"), "${fmt(v)} m/s", "m/s"))
      }
      PhysicsToolType.FREQUENCY -> {
        if (a < 0) return CalcOutcome.Error("Wave speed cannot be negative")
        if (b <= 0) return CalcOutcome.Error("Wavelength must be greater than zero")
        val f = a / b
        CalcOutcome.Success(f to "Hz", StepBreakdown("f = v / λ", "v = ${fmt(a)} m/s, λ = ${fmt(b)} m", "f = ${fmt(a)} / ${fmt(b)}", listOf("f = ${fmt(f)} Hz"), "${fmt(f)} Hz", "Hz"))
      }
      PhysicsToolType.WAVELENGTH -> {
        if (a < 0) return CalcOutcome.Error("Wave speed cannot be negative")
        if (b <= 0) return CalcOutcome.Error("Frequency must be greater than zero")
        val l = a / b
        CalcOutcome.Success(l to "m", StepBreakdown("λ = v / f", "v = ${fmt(a)} m/s, f = ${fmt(b)} Hz", "λ = ${fmt(a)} / ${fmt(b)}", listOf("λ = ${fmt(l)} m"), "${fmt(l)} m", "m"))
      }
    }
  }
}

// ==========================================
// 8. CHEMISTRY ENGINE + 118-ELEMENT PERIODIC TABLE
// ==========================================

data class ChemicalElement(val number: Int, val symbol: String, val name: String, val atomicMass: Double)

object ChemistryEngine {
  val periodicTable: List<ChemicalElement> = listOf(
    ChemicalElement(1, "H", "Hydrogen", 1.008),
    ChemicalElement(2, "He", "Helium", 4.0026),
    ChemicalElement(3, "Li", "Lithium", 6.94),
    ChemicalElement(4, "Be", "Beryllium", 9.0122),
    ChemicalElement(5, "B", "Boron", 10.81),
    ChemicalElement(6, "C", "Carbon", 12.011),
    ChemicalElement(7, "N", "Nitrogen", 14.007),
    ChemicalElement(8, "O", "Oxygen", 15.999),
    ChemicalElement(9, "F", "Fluorine", 18.998),
    ChemicalElement(10, "Ne", "Neon", 20.180),
    ChemicalElement(11, "Na", "Sodium", 22.990),
    ChemicalElement(12, "Mg", "Magnesium", 24.305),
    ChemicalElement(13, "Al", "Aluminium", 26.982),
    ChemicalElement(14, "Si", "Silicon", 28.085),
    ChemicalElement(15, "P", "Phosphorus", 30.974),
    ChemicalElement(16, "S", "Sulfur", 32.06),
    ChemicalElement(17, "Cl", "Chlorine", 35.45),
    ChemicalElement(18, "Ar", "Argon", 39.948),
    ChemicalElement(19, "K", "Potassium", 39.098),
    ChemicalElement(20, "Ca", "Calcium", 40.078),
    ChemicalElement(21, "Sc", "Scandium", 44.956),
    ChemicalElement(22, "Ti", "Titanium", 47.867),
    ChemicalElement(23, "V", "Vanadium", 50.942),
    ChemicalElement(24, "Cr", "Chromium", 51.996),
    ChemicalElement(25, "Mn", "Manganese", 54.938),
    ChemicalElement(26, "Fe", "Iron", 55.845),
    ChemicalElement(27, "Co", "Cobalt", 58.933),
    ChemicalElement(28, "Ni", "Nickel", 58.693),
    ChemicalElement(29, "Cu", "Copper", 63.546),
    ChemicalElement(30, "Zn", "Zinc", 65.38),
    ChemicalElement(31, "Ga", "Gallium", 69.723),
    ChemicalElement(32, "Ge", "Germanium", 72.630),
    ChemicalElement(33, "As", "Arsenic", 74.922),
    ChemicalElement(34, "Se", "Selenium", 78.971),
    ChemicalElement(35, "Br", "Bromine", 79.904),
    ChemicalElement(36, "Kr", "Krypton", 83.798),
    ChemicalElement(37, "Rb", "Rubidium", 85.468),
    ChemicalElement(38, "Sr", "Strontium", 87.62),
    ChemicalElement(39, "Y", "Yttrium", 88.906),
    ChemicalElement(40, "Zr", "Zirconium", 91.224),
    ChemicalElement(41, "Nb", "Niobium", 92.906),
    ChemicalElement(42, "Mo", "Molybdenum", 95.95),
    ChemicalElement(43, "Tc", "Technetium", 98.0),
    ChemicalElement(44, "Ru", "Ruthenium", 101.07),
    ChemicalElement(45, "Rh", "Rhodium", 102.91),
    ChemicalElement(46, "Pd", "Palladium", 106.42),
    ChemicalElement(47, "Ag", "Silver", 107.87),
    ChemicalElement(48, "Cd", "Cadmium", 112.41),
    ChemicalElement(49, "In", "Indium", 114.82),
    ChemicalElement(50, "Sn", "Tin", 118.71),
    ChemicalElement(51, "Sb", "Antimony", 121.76),
    ChemicalElement(52, "Te", "Tellurium", 127.60),
    ChemicalElement(53, "I", "Iodine", 126.90),
    ChemicalElement(54, "Xe", "Xenon", 131.29),
    ChemicalElement(55, "Cs", "Cesium", 132.91),
    ChemicalElement(56, "Ba", "Barium", 137.33),
    ChemicalElement(57, "La", "Lanthanum", 138.91),
    ChemicalElement(58, "Ce", "Cerium", 140.12),
    ChemicalElement(59, "Pr", "Praseodymium", 140.91),
    ChemicalElement(60, "Nd", "Neodymium", 144.24),
    ChemicalElement(61, "Pm", "Promethium", 145.0),
    ChemicalElement(62, "Sm", "Samarium", 150.36),
    ChemicalElement(63, "Eu", "Europium", 151.96),
    ChemicalElement(64, "Gd", "Gadolinium", 157.25),
    ChemicalElement(65, "Tb", "Terbium", 158.93),
    ChemicalElement(66, "Dy", "Dysprosium", 162.50),
    ChemicalElement(67, "Ho", "Holmium", 164.93),
    ChemicalElement(68, "Er", "Erbium", 167.26),
    ChemicalElement(69, "Tm", "Thulium", 168.93),
    ChemicalElement(70, "Yb", "Ytterbium", 173.05),
    ChemicalElement(71, "Lu", "Lutetium", 174.97),
    ChemicalElement(72, "Hf", "Hafnium", 178.49),
    ChemicalElement(73, "Ta", "Tantalum", 180.95),
    ChemicalElement(74, "W", "Tungsten", 183.84),
    ChemicalElement(75, "Re", "Rhenium", 186.21),
    ChemicalElement(76, "Os", "Osmium", 190.23),
    ChemicalElement(77, "Ir", "Iridium", 192.22),
    ChemicalElement(78, "Pt", "Platinum", 195.08),
    ChemicalElement(79, "Au", "Gold", 196.97),
    ChemicalElement(80, "Hg", "Mercury", 200.59),
    ChemicalElement(81, "Tl", "Thallium", 204.38),
    ChemicalElement(82, "Pb", "Lead", 207.2),
    ChemicalElement(83, "Bi", "Bismuth", 208.98),
    ChemicalElement(84, "Po", "Polonium", 209.0),
    ChemicalElement(85, "At", "Astatine", 210.0),
    ChemicalElement(86, "Rn", "Radon", 222.0),
    ChemicalElement(87, "Fr", "Francium", 223.0),
    ChemicalElement(88, "Ra", "Radium", 226.0),
    ChemicalElement(89, "Ac", "Actinium", 227.0),
    ChemicalElement(90, "Th", "Thorium", 232.04),
    ChemicalElement(91, "Pa", "Protactinium", 231.04),
    ChemicalElement(92, "U", "Uranium", 238.03),
    ChemicalElement(93, "Np", "Neptunium", 237.0),
    ChemicalElement(94, "Pu", "Plutonium", 244.0),
    ChemicalElement(95, "Am", "Americium", 243.0),
    ChemicalElement(96, "Cm", "Curium", 247.0),
    ChemicalElement(97, "Bk", "Berkelium", 247.0),
    ChemicalElement(98, "Cf", "Californium", 251.0),
    ChemicalElement(99, "Es", "Einsteinium", 252.0),
    ChemicalElement(100, "Fm", "Fermium", 257.0),
    ChemicalElement(101, "Md", "Mendelevium", 258.0),
    ChemicalElement(102, "No", "Nobelium", 259.0),
    ChemicalElement(103, "Lr", "Lawrencium", 266.0),
    ChemicalElement(104, "Rf", "Rutherfordium", 267.0),
    ChemicalElement(105, "Db", "Dubnium", 268.0),
    ChemicalElement(106, "Sg", "Seaborgium", 269.0),
    ChemicalElement(107, "Bh", "Bohrium", 270.0),
    ChemicalElement(108, "Hs", "Hassium", 277.0),
    ChemicalElement(109, "Mt", "Meitnerium", 278.0),
    ChemicalElement(110, "Ds", "Darmstadtium", 281.0),
    ChemicalElement(111, "Rg", "Roentgenium", 282.0),
    ChemicalElement(112, "Cn", "Copernicium", 285.0),
    ChemicalElement(113, "Nh", "Nihonium", 286.0),
    ChemicalElement(114, "Fl", "Flerovium", 289.0),
    ChemicalElement(115, "Mc", "Moscovium", 290.0),
    ChemicalElement(116, "Lv", "Livermorium", 293.0),
    ChemicalElement(117, "Ts", "Tennessine", 294.0),
    ChemicalElement(118, "Og", "Oganesson", 294.0),
  )

  private val elementBySymbol = periodicTable.associateBy { it.symbol }

  fun calculateMolarMass(formulaRaw: String): CalcOutcome<Double> {
    val formula = formulaRaw.trim()
    if (formula.isEmpty()) return CalcOutcome.Error("Enter a chemical formula (e.g. H2O, Ca(OH)2)")
    if (formula.any { it.isWhitespace() || it == '·' || it == '.' }) {
      return CalcOutcome.Error("Unsupported hydrate dot or whitespace notation; enter single formula e.g. Ca(OH)2")
    }
    return try {
      val counts = parseFormulaCounts(formula)
      if (counts.isEmpty()) return CalcOutcome.Error("No valid elements found in formula")
      var total = 0.0
      val steps = mutableListOf<String>()
      for ((sym, count) in counts) {
        val elem = elementBySymbol[sym] ?: return CalcOutcome.Error("Unknown chemical element '$sym'")
        val contrib = elem.atomicMass * count
        total += contrib
        steps.add("${elem.symbol} (${elem.name}): $count × ${elem.atomicMass} = ${NumberFormatter.formatPlain(contrib, 4)} g/mol")
      }
      val totalStr = "${NumberFormatter.formatPlain(total, 4)} g/mol"
      CalcOutcome.Success(
        total,
        StepBreakdown(
          formula = "M = Σ (countᵢ × atomic_massᵢ)",
          inputValues = formula,
          substitution = counts.entries.joinToString(" + ") { "${it.value}×M(${it.key})" },
          calculationSteps = steps,
          finalAnswer = totalStr,
          unit = "g/mol",
        ),
      )
    } catch (e: IllegalArgumentException) {
      CalcOutcome.Error(e.message ?: "Invalid chemical formula")
    }
  }

  private fun parseFormulaCounts(formula: String): Map<String, Int> {
    var idx = 0
    val n = formula.length

    fun readCount(): Int {
      val start = idx
      while (idx < n && formula[idx].isDigit()) idx++
      return if (idx == start) 1 else formula.substring(start, idx).toIntOrNull()
        ?: throw IllegalArgumentException("Invalid atom count")
    }

    fun parseGroup(inParens: Boolean): MutableMap<String, Int> {
      val map = linkedMapOf<String, Int>()
      while (idx < n) {
        val ch = formula[idx]
        when {
          ch == '(' -> {
            idx++
            val inner = parseGroup(inParens = true)
            if (inner.isEmpty()) throw IllegalArgumentException("Empty parentheses in formula")
            val mult = readCount()
            for ((k, v) in inner) {
              map[k] = (map[k] ?: 0) + v * mult
            }
          }
          ch == ')' -> {
            if (!inParens) throw IllegalArgumentException("Unmatched closing parenthesis ')'")
            idx++
            return map
          }
          ch.isUpperCase() -> {
            val start = idx
            idx++
            if (idx < n && formula[idx].isLowerCase()) idx++
            val sym = formula.substring(start, idx)
            if (!elementBySymbol.containsKey(sym)) {
              throw IllegalArgumentException("Unknown chemical element '$sym'")
            }
            val cnt = readCount()
            map[sym] = (map[sym] ?: 0) + cnt
          }
          else -> throw IllegalArgumentException("Unsupported character '$ch' in chemical formula")
        }
      }
      if (inParens) throw IllegalArgumentException("Missing closing parenthesis ')'")
      return map
    }

    return parseGroup(inParens = false)
  }

  fun numberOfMoles(massGrams: Double, molarMass: Double): CalcOutcome<Double> {
    if (massGrams < 0) return CalcOutcome.Error("Mass cannot be negative")
    if (molarMass <= 0) return CalcOutcome.Error("Molar mass must be greater than zero")
    val n = massGrams / molarMass
    val ans = "${NumberFormatter.formatPlain(n, 6)} mol"
    return CalcOutcome.Success(
      n,
      StepBreakdown("n = m / M", "m = $massGrams g, M = $molarMass g/mol", "n = $massGrams / $molarMass", listOf("n = $ans"), ans, "mol"),
    )
  }

  fun molarity(moles: Double, volumeLiters: Double): CalcOutcome<Double> {
    if (moles < 0) return CalcOutcome.Error("Moles cannot be negative")
    if (volumeLiters <= 0) return CalcOutcome.Error("Solution volume must be greater than zero")
    val m = moles / volumeLiters
    val ans = "${NumberFormatter.formatPlain(m, 6)} mol/L (M)"
    return CalcOutcome.Success(
      m,
      StepBreakdown("M = n / V", "n = $moles mol, V = $volumeLiters L", "M = $moles / $volumeLiters", listOf("M = $ans"), ans, "M"),
    )
  }

  fun dilution(m1: Double?, v1: Double?, m2: Double?, v2: Double?): CalcOutcome<Pair<String, Double>> {
    val nulls = listOf(m1, v1, m2, v2).count { it == null }
    if (nulls != 1) return CalcOutcome.Error("Leave exactly ONE field blank to solve M₁V₁ = M₂V₂")
    if (listOfNotNull(m1, v1, m2, v2).any { it <= 0.0 }) {
      return CalcOutcome.Error("All known concentrations and volumes must be positive")
    }
    return when {
      m1 == null -> {
        val ans = (m2!! * v2!!) / v1!!
        CalcOutcome.Success("M₁" to ans, StepBreakdown("M₁ = (M₂ × V₂) / V₁", "V₁=$v1, M₂=$m2, V₂=$v2", "M₁ = ($m2 × $v2) / $v1", listOf("M₁ = ${NumberFormatter.formatPlain(ans)} M"), "${NumberFormatter.formatPlain(ans)} M", "M"))
      }
      v1 == null -> {
        val ans = (m2!! * v2!!) / m1
        CalcOutcome.Success("V₁" to ans, StepBreakdown("V₁ = (M₂ × V₂) / M₁", "M₁=$m1, M₂=$m2, V₂=$v2", "V₁ = ($m2 × $v2) / $m1", listOf("V₁ = ${NumberFormatter.formatPlain(ans)} L"), "${NumberFormatter.formatPlain(ans)} L", "L"))
      }
      m2 == null -> {
        val ans = (m1 * v1) / v2!!
        CalcOutcome.Success("M₂" to ans, StepBreakdown("M₂ = (M₁ × V₁) / V₂", "M₁=$m1, V₁=$v1, V₂=$v2", "M₂ = ($m1 × $v1) / $v2", listOf("M₂ = ${NumberFormatter.formatPlain(ans)} M"), "${NumberFormatter.formatPlain(ans)} M", "M"))
      }
      else -> {
        val ans = (m1 * v1) / m2
        CalcOutcome.Success("V₂" to ans, StepBreakdown("V₂ = (M₁ × V₁) / M₂", "M₁=$m1, V₁=$v1, M₂=$m2", "V₂ = ($m1 × $v1) / $m2", listOf("V₂ = ${NumberFormatter.formatPlain(ans)} L"), "${NumberFormatter.formatPlain(ans)} L", "L"))
      }
    }
  }

  fun concentrationMassPercent(soluteGrams: Double, volumeMl: Double): CalcOutcome<Double> {
    if (soluteGrams < 0) return CalcOutcome.Error("Solute mass cannot be negative")
    if (volumeMl <= 0) return CalcOutcome.Error("Solution volume must be greater than zero")
    val pct = (soluteGrams / volumeMl) * 100.0
    val gPerL = (soluteGrams / volumeMl) * 1000.0
    val ans = "${NumberFormatter.formatPlain(pct, 4)}% w/v"
    return CalcOutcome.Success(
      pct,
      StepBreakdown("C(% w/v) = (m_solute(g) / V_solution(mL)) × 100", "m = $soluteGrams g, V = $volumeMl mL", "($soluteGrams / $volumeMl) × 100", listOf("Concentration = $ans", "Equivalent = ${NumberFormatter.formatPlain(gPerL, 4)} g/L"), ans, "% w/v"),
    )
  }

  fun calculatePh(hydrogenIonConc: Double): CalcOutcome<Pair<Double, Double>> {
    if (hydrogenIonConc <= 0.0) return CalcOutcome.Error("[H⁺] concentration must be greater than zero")
    val ph = -log10(hydrogenIonConc)
    val poh = 14.0 - ph
    val status =
      when {
        abs(ph - 7.0) < 1e-6 -> "Neutral"
        ph < 7.0 -> "Acidic"
        else -> "Basic / Alkaline"
      }
    val phStr = NumberFormatter.formatPlain(ph, 4)
    val pohStr = NumberFormatter.formatPlain(poh, 4)
    return CalcOutcome.Success(
      ph to poh,
      StepBreakdown("pH = -log₁₀[H⁺], pOH = 14 - pH", "[H⁺] = $hydrogenIonConc mol/L", "pH = -log₁₀($hydrogenIonConc)", listOf("pH = $phStr ($status)", "pOH = $pohStr"), "pH $phStr"),
    )
  }

  fun idealGasLaw(pAtm: Double?, vLiters: Double?, nMoles: Double?, tKelvin: Double?): CalcOutcome<Pair<String, Double>> {
    val r = 0.082057366
    val nulls = listOf(pAtm, vLiters, nMoles, tKelvin).count { it == null }
    if (nulls != 1) return CalcOutcome.Error("Leave exactly ONE variable blank to solve PV = nRT")
    if (listOfNotNull(pAtm, vLiters, nMoles, tKelvin).any { it <= 0.0 }) {
      return CalcOutcome.Error("Known values (P, V, n, T in Kelvin) must all be positive")
    }
    return when {
      pAtm == null -> {
        val ans = (nMoles!! * r * tKelvin!!) / vLiters!!
        CalcOutcome.Success("Pressure (P)" to ans, StepBreakdown("P = nRT / V", "n=$nMoles mol, T=$tKelvin K, V=$vLiters L, R=0.082057", "P = ($nMoles × 0.082057 × $tKelvin) / $vLiters", listOf("P = ${NumberFormatter.formatPlain(ans, 4)} atm"), "${NumberFormatter.formatPlain(ans, 4)} atm", "atm"))
      }
      vLiters == null -> {
        val ans = (nMoles!! * r * tKelvin!!) / pAtm
        CalcOutcome.Success("Volume (V)" to ans, StepBreakdown("V = nRT / P", "n=$nMoles mol, T=$tKelvin K, P=$pAtm atm, R=0.082057", "V = ($nMoles × 0.082057 × $tKelvin) / $pAtm", listOf("V = ${NumberFormatter.formatPlain(ans, 4)} L"), "${NumberFormatter.formatPlain(ans, 4)} L", "L"))
      }
      nMoles == null -> {
        val ans = (pAtm * vLiters) / (r * tKelvin!!)
        CalcOutcome.Success("Moles (n)" to ans, StepBreakdown("n = PV / RT", "P=$pAtm atm, V=$vLiters L, T=$tKelvin K, R=0.082057", "n = ($pAtm × $vLiters) / (0.082057 × $tKelvin)", listOf("n = ${NumberFormatter.formatPlain(ans, 4)} mol"), "${NumberFormatter.formatPlain(ans, 4)} mol", "mol"))
      }
      else -> {
        val ans = (pAtm * vLiters) / (nMoles * r)
        CalcOutcome.Success("Temperature (T)" to ans, StepBreakdown("T = PV / nR", "P=$pAtm atm, V=$vLiters L, n=$nMoles mol, R=0.082057", "T = ($pAtm × $vLiters) / ($nMoles × 0.082057)", listOf("T = ${NumberFormatter.formatPlain(ans, 4)} K"), "${NumberFormatter.formatPlain(ans, 4)} K", "K"))
      }
    }
  }
}
