package com.example.engine

import com.example.model.AngleMode
import com.example.model.CalcOutcome
import kotlin.math.E
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.acosh
import kotlin.math.asin
import kotlin.math.asinh
import kotlin.math.atan
import kotlin.math.atanh
import kotlin.math.cbrt
import kotlin.math.cos
import kotlin.math.cosh
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sinh
import kotlin.math.sqrt
import kotlin.math.tan
import kotlin.math.tanh

/**
 * Safe recursive-descent mathematical expression parser and evaluator.
 * Supports arithmetic, operator precedence, parentheses, implicit multiplication,
 * trig/inverse trig (respecting DEG/RAD/GRAD), hyperbolic functions, logarithms,
 * roots, factorials, modulo, percentage postfix, and mathematical constants.
 */
object ExpressionParserEngine {

  private sealed interface Token {
    data class Number(val value: Double) : Token
    data class Ident(val name: String) : Token
    data class Op(val symbol: String) : Token
    data object LParen : Token
    data object RParen : Token
    data object Comma : Token
  }

  fun evaluate(
    rawExpression: String,
    angleMode: AngleMode = AngleMode.DEG,
  ): CalcOutcome<Double> {
    val trimmed = rawExpression.trim()
    if (trimmed.isEmpty()) {
      return CalcOutcome.Error("Enter an expression to calculate")
    }

    return try {
      val tokens = tokenize(trimmed)
      val expanded = insertImplicitMultiplication(tokens)
      val parser = Parser(expanded, angleMode)
      val result = parser.parseExpression()
      if (parser.hasMore()) {
        return CalcOutcome.Error("Unexpected token near '${parser.peekDisplay()}'")
      }
      if (result.isNaN() || result.isInfinite()) {
        return CalcOutcome.Error("Result is undefined or out of numerical range")
      }
      val cleaned = if (abs(result) < 1e-13) 0.0 else result
      CalcOutcome.Success(cleaned)
    } catch (e: MathEvalException) {
      CalcOutcome.Error(e.message ?: "Invalid expression")
    } catch (_: Exception) {
      CalcOutcome.Error("Invalid mathematical expression")
    }
  }

  private class MathEvalException(message: String) : RuntimeException(message)

  private fun tokenize(input: String): List<Token> {
    val normalized =
      input
        .replace('×', '*')
        .replace('·', '*')
        .replace('÷', '/')
        .replace('−', '-')
        .replace("π", "pi")
        .replace("√", "sqrt")
        .replace("⁻¹", "inv_trig")
        .replace("sininv_trig", "asin")
        .replace("cosinv_trig", "acos")
        .replace("taninv_trig", "atan")
        .replace("sinh⁻¹", "asinh")
        .replace("cosh⁻¹", "acosh")
        .replace("tanh⁻¹", "atanh")
        .replace("²", "^2")
        .replace("³", "^3")

    val tokens = mutableListOf<Token>()
    var i = 0
    val n = normalized.length

    while (i < n) {
      val ch = normalized[i]
      when {
        ch.isWhitespace() -> i++
        ch.isDigit() || ch == '.' -> {
          val start = i
          var dotCount = if (ch == '.') 1 else 0
          i++
          while (i < n) {
            val c = normalized[i]
            if (c.isDigit()) {
              i++
            } else if (c == '.') {
              dotCount++
              if (dotCount > 1) throw MathEvalException("Invalid number format")
              i++
            } else if ((c == 'E' || c == 'e') && i + 1 < n && (normalized[i + 1].isDigit() || normalized[i + 1] == '+' || normalized[i + 1] == '-')) {
              // Scientific notation like 1.5E-4
              val nextIsSign = normalized[i + 1] == '+' || normalized[i + 1] == '-'
              if (nextIsSign && (i + 2 >= n || !normalized[i + 2].isDigit())) break
              i += if (nextIsSign) 2 else 1
              while (i < n && normalized[i].isDigit()) i++
              break
            } else {
              break
            }
          }
          val numStr = normalized.substring(start, i)
          if (numStr == ".") throw MathEvalException("Invalid decimal point")
          val num = numStr.toDoubleOrNull() ?: throw MathEvalException("Invalid number '$numStr'")
          tokens.add(Token.Number(num))
        }
        ch.isLetter() || ch == '_' -> {
          val start = i
          i++
          while (i < n && (normalized[i].isLetterOrDigit() || normalized[i] == '_')) {
            i++
          }
          val ident = normalized.substring(start, i).lowercase()
          if (ident == "mod") {
            tokens.add(Token.Op("mod"))
          } else {
            tokens.add(Token.Ident(ident))
          }
        }
        ch == '(' -> {
          tokens.add(Token.LParen)
          i++
        }
        ch == ')' -> {
          tokens.add(Token.RParen)
          i++
        }
        ch == ',' -> {
          tokens.add(Token.Comma)
          i++
        }
        ch in listOf('+', '-', '*', '/', '^', '%', '!') -> {
          tokens.add(Token.Op(ch.toString()))
          i++
        }
        ch == 'φ' -> {
          tokens.add(Token.Ident("phi"))
          i++
        }
        else -> throw MathEvalException("Unsupported symbol '$ch'")
      }
    }
    return tokens
  }

  private fun isConstantOrVariable(name: String): Boolean {
    return name in setOf("pi", "e", "phi", "c", "g")
  }

  private fun insertImplicitMultiplication(tokens: List<Token>): List<Token> {
    if (tokens.isEmpty()) return tokens
    val out = mutableListOf<Token>()
    for (i in tokens.indices) {
      val curr = tokens[i]
      if (i > 0) {
        val prev = tokens[i - 1]
        val prevEndsValue =
          prev is Token.Number ||
            prev is Token.RParen ||
            (prev is Token.Ident && isConstantOrVariable(prev.name)) ||
            (prev is Token.Op && (prev.symbol == "%" || prev.symbol == "!"))
        val currStartsValue =
          curr is Token.Number || curr is Token.LParen || curr is Token.Ident
        if (prevEndsValue && currStartsValue) {
          out.add(Token.Op("*"))
        }
      }
      out.add(curr)
    }
    return out
  }

  private class Parser(
    private val tokens: List<Token>,
    private val angleMode: AngleMode,
  ) {
    private var pos = 0

    fun hasMore(): Boolean = pos < tokens.size

    fun peekDisplay(): String = tokens.getOrNull(pos)?.toString() ?: "end"

    private fun peek(): Token? = tokens.getOrNull(pos)

    private fun consume(): Token = tokens[pos++]

    fun parseExpression(): Double {
      var value = parseTerm()
      while (true) {
        val t = peek()
        if (t is Token.Op && (t.symbol == "+" || t.symbol == "-")) {
          consume()
          val rhs = parseTerm()
          value = if (t.symbol == "+") value + rhs else value - rhs
        } else {
          break
        }
      }
      return value
    }

    private fun parseTerm(): Double {
      var value = parsePower()
      while (true) {
        val t = peek()
        if (t is Token.Op && (t.symbol == "*" || t.symbol == "/" || t.symbol == "mod")) {
          consume()
          val rhs = parsePower()
          value =
            when (t.symbol) {
              "*" -> value * rhs
              "/" -> {
                if (rhs == 0.0) throw MathEvalException("Cannot divide by zero")
                value / rhs
              }
              "mod" -> {
                if (rhs == 0.0) throw MathEvalException("Modulo by zero is undefined")
                value % rhs
              }
              else -> value
            }
        } else {
          break
        }
      }
      return value
    }

    private fun parsePower(): Double {
      val base = parseUnary()
      val t = peek()
      if (t is Token.Op && t.symbol == "^") {
        consume()
        val exponent = parsePower() // Right-associative
        if (base == 0.0 && exponent < 0.0) {
          throw MathEvalException("0 raised to a negative power is undefined")
        }
        val res =
          if (base < 0.0 && abs(exponent - exponent.roundToInt()) < 1e-12) {
            val intExp = exponent.roundToInt()
            val mag = abs(base).pow(intExp)
            if (intExp % 2 == 0) mag else -mag
          } else {
            base.pow(exponent)
          }
        if (res.isNaN() || res.isInfinite()) {
          throw MathEvalException("Power calculation out of real domain")
        }
        return res
      }
      return base
    }

    private fun parseUnary(): Double {
      val t = peek()
      if (t is Token.Op && (t.symbol == "+" || t.symbol == "-")) {
        consume()
        val value = parseUnary()
        return if (t.symbol == "-") -value else value
      }
      return parsePostfix()
    }

    private fun parsePostfix(): Double {
      var value = parsePrimary()
      while (true) {
        val t = peek()
        if (t is Token.Op && t.symbol == "!") {
          consume()
          value = factorial(value)
        } else if (t is Token.Op && t.symbol == "%") {
          consume()
          value /= 100.0
        } else {
          break
        }
      }
      return value
    }

    private fun parsePrimary(): Double {
      val t = peek() ?: throw MathEvalException("Incomplete expression")
      return when (t) {
        is Token.Number -> {
          consume()
          t.value
        }
        is Token.LParen -> {
          consume()
          val inside = parseExpression()
          val close = peek()
          if (close !is Token.RParen) throw MathEvalException("Missing closing parenthesis ')'")
          consume()
          inside
        }
        is Token.Ident -> {
          consume()
          when (t.name) {
            "pi" -> PI
            "e" -> E
            "phi" -> (1.0 + sqrt(5.0)) / 2.0
            "c" -> 299792458.0
            "g" -> 9.80665
            else -> {
              // Function call: requires parentheses or single primary argument
              val args = parseFunctionArguments()
              evaluateFunction(t.name, args)
            }
          }
        }
        else -> throw MathEvalException("Unexpected operator or symbol")
      }
    }

    private fun parseFunctionArguments(): List<Double> {
      val next = peek()
      if (next is Token.LParen) {
        consume()
        if (peek() is Token.RParen) {
          throw MathEvalException("Function requires an argument")
        }
        val args = mutableListOf<Double>()
        args.add(parseExpression())
        while (peek() is Token.Comma) {
          consume()
          args.add(parseExpression())
        }
        if (peek() !is Token.RParen) {
          throw MathEvalException("Missing closing parenthesis ')'")
        }
        consume()
        return args
      }
      return listOf(parseUnary())
    }

    private fun evaluateFunction(name: String, args: List<Double>): Double {
      fun requireOne(): Double {
        if (args.size != 1) throw MathEvalException("$name expects 1 argument")
        return args[0]
      }
      fun requireTwo(): Pair<Double, Double> {
        if (args.size != 2) throw MathEvalException("$name expects 2 arguments")
        return args[0] to args[1]
      }

      return when (name) {
        "sin" -> cleanZero(sin(toRadians(requireOne())))
        "cos" -> cleanZero(cos(toRadians(requireOne())))
        "tan" -> {
          val rad = toRadians(requireOne())
          val c = cos(rad)
          if (abs(c) < 1e-12) throw MathEvalException("Tangent is undefined at this angle")
          cleanZero(sin(rad) / c)
        }
        "asin" -> {
          val x = requireOne()
          if (x < -1.0 || x > 1.0) throw MathEvalException("asin domain is [-1, 1]")
          fromRadians(asin(x))
        }
        "acos" -> {
          val x = requireOne()
          if (x < -1.0 || x > 1.0) throw MathEvalException("acos domain is [-1, 1]")
          fromRadians(acos(x))
        }
        "atan" -> fromRadians(atan(requireOne()))
        "sinh" -> checkFinite(sinh(requireOne()), "sinh overflow")
        "cosh" -> checkFinite(cosh(requireOne()), "cosh overflow")
        "tanh" -> tanh(requireOne())
        "asinh" -> asinh(requireOne())
        "acosh" -> {
          val x = requireOne()
          if (x < 1.0) throw MathEvalException("acosh domain is [1, ∞)")
          acosh(x)
        }
        "atanh" -> {
          val x = requireOne()
          if (x <= -1.0 || x >= 1.0) throw MathEvalException("atanh domain is (-1, 1)")
          atanh(x)
        }
        "log", "log10" -> {
          val x = requireOne()
          if (x <= 0.0) throw MathEvalException("Logarithm is only defined for positive numbers")
          log10(x)
        }
        "ln" -> {
          val x = requireOne()
          if (x <= 0.0) throw MathEvalException("Natural log is only defined for positive numbers")
          ln(x)
        }
        "logn", "logbase" -> {
          val (base, x) = requireTwo()
          if (base <= 0.0 || base == 1.0) {
            throw MathEvalException("Log base must be positive and not equal to 1")
          }
          if (x <= 0.0) throw MathEvalException("Logarithm argument must be positive")
          ln(x) / ln(base)
        }
        "sqrt" -> {
          val x = requireOne()
          if (x < 0.0) throw MathEvalException("Square root of a negative number is not real")
          sqrt(x)
        }
        "cbrt" -> cbrt(requireOne())
        "root" -> {
          val (n, x) = requireTwo()
          if (n == 0.0) throw MathEvalException("0th root is undefined")
          val nInt = n.roundToInt()
          val isInt = abs(n - nInt) < 1e-12
          if (x < 0.0) {
            if (isInt && abs(nInt % 2) == 1) {
              -abs(x).pow(1.0 / n)
            } else {
              throw MathEvalException("Even root of a negative number is not real")
            }
          } else if (x == 0.0) {
            if (n < 0.0) throw MathEvalException("Cannot take negative root of 0")
            0.0
          } else {
            x.pow(1.0 / n)
          }
        }
        "abs" -> abs(requireOne())
        "fact" -> factorial(requireOne())
        "inv" -> {
          val x = requireOne()
          if (x == 0.0) throw MathEvalException("Reciprocal of zero is undefined")
          1.0 / x
        }
        "sq" -> {
          val x = requireOne()
          x * x
        }
        "cube" -> {
          val x = requireOne()
          x * x * x
        }
        "exp" -> checkFinite(exp(requireOne()), "Exponential overflow")
        else -> throw MathEvalException("Unknown function '$name'")
      }
    }

    private fun toRadians(angle: Double): Double =
      when (angleMode) {
        AngleMode.DEG -> angle * PI / 180.0
        AngleMode.RAD -> angle
        AngleMode.GRAD -> angle * PI / 200.0
      }

    private fun fromRadians(rad: Double): Double =
      when (angleMode) {
        AngleMode.DEG -> rad * 180.0 / PI
        AngleMode.RAD -> rad
        AngleMode.GRAD -> rad * 200.0 / PI
      }

    private fun cleanZero(v: Double): Double = if (abs(v) < 1e-12) 0.0 else v

    private fun checkFinite(v: Double, msg: String): Double {
      if (v.isNaN() || v.isInfinite()) throw MathEvalException(msg)
      return v
    }

    private fun factorial(x: Double): Double {
      if (x < 0.0) throw MathEvalException("Factorial is undefined for negative numbers")
      val n = floor(x + 1e-9)
      if (abs(x - n) > 1e-9) throw MathEvalException("Factorial requires a non-negative integer")
      val intN = n.toInt()
      if (intN > 170) throw MathEvalException("Factorial result exceeds maximum precision (>170!)")
      var acc = 1.0
      for (i in 2..intN) acc *= i
      return acc
    }
  }
}
