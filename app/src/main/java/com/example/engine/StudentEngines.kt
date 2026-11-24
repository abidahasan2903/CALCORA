package com.example.engine

import com.example.model.CalcOutcome
import com.example.model.StepBreakdown

data class GradeMapping(val letter: String, val points: Double, val minPercent: Double = 0.0)

enum class GradingScale(
  val title: String,
  val maxPoints: Double,
  val description: String,
  val mappings: List<GradeMapping>,
) {
  STANDARD_4_0(
    title = "4.0 Standard Scale",
    maxPoints = 4.0,
    description = "A=4.0, B=3.0, C=2.0, D=1.0, F=0.0",
    mappings = listOf(
      GradeMapping("A", 4.0, 90.0),
      GradeMapping("B", 3.0, 80.0),
      GradeMapping("C", 2.0, 70.0),
      GradeMapping("D", 1.0, 60.0),
      GradeMapping("F", 0.0, 0.0),
    ),
  ),
  PLUS_MINUS_4_0(
    title = "4.0 (+/-) Scale",
    maxPoints = 4.0,
    description = "A=4.0, A-=3.7, B+=3.3, B=3.0, C=2.0, D=1.0, F=0.0",
    mappings = listOf(
      GradeMapping("A", 4.0, 93.0),
      GradeMapping("A-", 3.7, 90.0),
      GradeMapping("B+", 3.3, 87.0),
      GradeMapping("B", 3.0, 83.0),
      GradeMapping("B-", 2.7, 80.0),
      GradeMapping("C+", 2.3, 77.0),
      GradeMapping("C", 2.0, 73.0),
      GradeMapping("C-", 1.7, 70.0),
      GradeMapping("D", 1.0, 60.0),
      GradeMapping("F", 0.0, 0.0),
    ),
  ),
  SCALE_5_0(
    title = "5.0 Scale",
    maxPoints = 5.0,
    description = "A+=5.0, A=4.0, A-=3.5, B=3.0, C=2.0, D=1.0, F=0.0",
    mappings = listOf(
      GradeMapping("A+", 5.0, 80.0),
      GradeMapping("A", 4.0, 70.0),
      GradeMapping("A-", 3.5, 60.0),
      GradeMapping("B", 3.0, 50.0),
      GradeMapping("C", 2.0, 40.0),
      GradeMapping("D", 1.0, 33.0),
      GradeMapping("F", 0.0, 0.0),
    ),
  ),
  CUSTOM(
    title = "Custom Manual Points",
    maxPoints = 10.0,
    description = "Enter custom grade points directly per subject",
    mappings = emptyList(),
  ),
}

data class GpaSubjectInput(
  val id: Long,
  val name: String,
  val gradeLetter: String,
  val gradePoint: Double,
  val creditHours: Double,
)

data class GpaResult(
  val gpa: Double,
  val totalCredits: Double,
  val totalQualityPoints: Double,
)

data class CgpaSemesterInput(
  val id: Long,
  val semesterName: String,
  val gpa: Double,
  val creditHours: Double,
)

data class MarksSubjectInput(
  val id: Long,
  val subjectName: String,
  val obtainedMarks: Double,
  val totalMarks: Double,
)

data class MarksResult(
  val totalObtained: Double,
  val totalMaximum: Double,
  val percentage: Double,
  val averageObtained: Double,
  val averagePercentage: Double,
  val assignedGrade: String?,
)

data class WeightedItemInput(
  val id: Long,
  val label: String,
  val value: Double,
  val weight: Double,
)

data class WeightedAverageResult(
  val weightedAverage: Double,
  val totalWeight: Double,
  val weightedSum: Double,
)

object StudentEngine {

  fun calculateGpa(subjects: List<GpaSubjectInput>, scale: GradingScale): CalcOutcome<GpaResult> {
    if (subjects.isEmpty()) return CalcOutcome.Error("Add at least one subject")
    for (s in subjects) {
      if (s.creditHours <= 0.0) return CalcOutcome.Error("Credit hours must be positive for '${s.name.ifBlank { "Subject" }}'")
      if (s.gradePoint < 0.0) return CalcOutcome.Error("Grade point cannot be negative")
      if (scale != GradingScale.CUSTOM && s.gradePoint > scale.maxPoints + 1e-6) {
        return CalcOutcome.Error("Grade point ${s.gradePoint} exceeds ${scale.title} max (${scale.maxPoints})")
      }
    }
    val totalCredits = subjects.sumOf { it.creditHours }
    val qualityPoints = subjects.sumOf { it.creditHours * it.gradePoint }
    if (totalCredits <= 0.0) return CalcOutcome.Error("Total credits must be greater than zero")
    val gpa = qualityPoints / totalCredits
    val gpaStr = NumberFormatter.formatPlain(gpa, 3)
    return CalcOutcome.Success(
      GpaResult(gpa, totalCredits, qualityPoints),
      StepBreakdown(
        formula = "GPA = Σ(Credit × Grade Point) / Σ(Credit)",
        inputValues = "${subjects.size} subject(s) • Scale: ${scale.title}",
        substitution = "GPA = ${NumberFormatter.formatPlain(qualityPoints, 2)} / ${NumberFormatter.formatPlain(totalCredits, 2)}",
        calculationSteps = listOf(
          "Total Quality Points = ${NumberFormatter.formatPlain(qualityPoints, 2)}",
          "Total Credit Hours = ${NumberFormatter.formatPlain(totalCredits, 2)}",
          "Weighted GPA = $gpaStr",
        ),
        finalAnswer = gpaStr,
      ),
    )
  }

  fun calculateCgpa(semesters: List<CgpaSemesterInput>, maxScale: Double = 4.0): CalcOutcome<GpaResult> {
    if (semesters.isEmpty()) return CalcOutcome.Error("Add at least one semester")
    for (sem in semesters) {
      if (sem.creditHours <= 0.0) return CalcOutcome.Error("Credit hours must be positive for '${sem.semesterName.ifBlank { "Semester" }}'")
      if (sem.gpa < 0.0 || sem.gpa > maxScale + 1e-6) {
        return CalcOutcome.Error("GPA for '${sem.semesterName.ifBlank { "Semester" }}' must be between 0 and $maxScale")
      }
    }
    val totalCredits = semesters.sumOf { it.creditHours }
    val qualityPoints = semesters.sumOf { it.gpa * it.creditHours }
    val cgpa = qualityPoints / totalCredits
    val cgpaStr = NumberFormatter.formatPlain(cgpa, 3)
    return CalcOutcome.Success(
      GpaResult(cgpa, totalCredits, qualityPoints),
      StepBreakdown(
        formula = "CGPA = Σ(Semester GPA × Semester Credits) / Σ(Credits)",
        inputValues = "${semesters.size} semester(s) • Max scale $maxScale",
        substitution = "CGPA = ${NumberFormatter.formatPlain(qualityPoints, 2)} / ${NumberFormatter.formatPlain(totalCredits, 2)}",
        calculationSteps = listOf(
          "Cumulative Points = ${NumberFormatter.formatPlain(qualityPoints, 2)}",
          "Total Credits = ${NumberFormatter.formatPlain(totalCredits, 2)}",
          "Cumulative GPA = $cgpaStr",
        ),
        finalAnswer = cgpaStr,
      ),
    )
  }

  fun calculateMarks(
    subjects: List<MarksSubjectInput>,
    scale: GradingScale?,
  ): CalcOutcome<MarksResult> {
    if (subjects.isEmpty()) return CalcOutcome.Error("Add at least one subject")
    for (s in subjects) {
      val name = s.subjectName.ifBlank { "Subject" }
      if (s.totalMarks <= 0.0) return CalcOutcome.Error("Total marks for '$name' must be greater than zero")
      if (s.obtainedMarks < 0.0) return CalcOutcome.Error("Obtained marks for '$name' cannot be negative")
      if (s.obtainedMarks > s.totalMarks) {
        return CalcOutcome.Error("Obtained marks (${s.obtainedMarks}) cannot exceed total marks (${s.totalMarks}) for '$name'")
      }
    }
    val totalObtained = subjects.sumOf { it.obtainedMarks }
    val totalMax = subjects.sumOf { it.totalMarks }
    val percentage = (totalObtained / totalMax) * 100.0
    val avgObtained = totalObtained / subjects.size
    val avgPct = subjects.map { (it.obtainedMarks / it.totalMarks) * 100.0 }.average()

    val grade =
      if (scale != null && scale != GradingScale.CUSTOM) {
        scale.mappings.firstOrNull { percentage >= it.minPercent }?.letter ?: scale.mappings.last().letter
      } else {
        null
      }

    val pctStr = "${NumberFormatter.formatPlain(percentage, 2)}%"
    return CalcOutcome.Success(
      MarksResult(totalObtained, totalMax, percentage, avgObtained, avgPct, grade),
      StepBreakdown(
        formula = "Percentage = (Σ Obtained / Σ Maximum) × 100",
        inputValues = "${subjects.size} subject(s)",
        substitution = "(${NumberFormatter.formatPlain(totalObtained, 2)} / ${NumberFormatter.formatPlain(totalMax, 2)}) × 100",
        calculationSteps = listOfNotNull(
          "Total Obtained = ${NumberFormatter.formatPlain(totalObtained, 2)} / ${NumberFormatter.formatPlain(totalMax, 2)}",
          "Overall Percentage = $pctStr",
          "Average Marks per Subject = ${NumberFormatter.formatPlain(avgObtained, 2)}",
          grade?.let { "Grade (${scale?.title}): $it" },
        ),
        finalAnswer = if (grade != null) "$pctStr ($grade)" else pctStr,
      ),
    )
  }

  fun calculateWeightedAverage(items: List<WeightedItemInput>): CalcOutcome<WeightedAverageResult> {
    if (items.isEmpty()) return CalcOutcome.Error("Add at least one value and weight")
    if (items.any { it.weight < 0.0 }) return CalcOutcome.Error("Weights cannot be negative")
    val totalWeight = items.sumOf { it.weight }
    if (totalWeight <= 0.0) return CalcOutcome.Error("Sum of weights must be greater than zero")
    val weightedSum = items.sumOf { it.value * it.weight }
    val avg = weightedSum / totalWeight
    val avgStr = NumberFormatter.formatPlain(avg, 4)
    return CalcOutcome.Success(
      WeightedAverageResult(avg, totalWeight, weightedSum),
      StepBreakdown(
        formula = "Weighted Avg = Σ(Valueᵢ × Weightᵢ) / Σ(Weightᵢ)",
        inputValues = "${items.size} items, Total Weight = ${NumberFormatter.formatPlain(totalWeight, 2)}",
        substitution = "${NumberFormatter.formatPlain(weightedSum, 4)} / ${NumberFormatter.formatPlain(totalWeight, 4)}",
        calculationSteps = listOf("Weighted Sum = ${NumberFormatter.formatPlain(weightedSum, 4)}", "Weighted Average = $avgStr"),
        finalAnswer = avgStr,
      ),
    )
  }
}
