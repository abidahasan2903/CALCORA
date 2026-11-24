package com.example.model

import androidx.compose.ui.graphics.vector.ImageVector

enum class CalculationWorld(
  val id: String,
  val title: String,
  val subtitle: String,
  val examples: String,
) {
  SCIENCE(
    id = "science",
    title = "SCIENCE",
    subtitle = "Scientific, math, engineering, physics & chemistry",
    examples = "Trig, Matrices, Equations, Physics, Molar Mass",
  ),
  COMMERCE(
    id = "commerce",
    title = "COMMERCE",
    subtitle = "Business, accounting, financial & economic tools",
    examples = "Profit & Loss, EMI, Compound Interest, VAT, ROI",
  ),
  ARTS(
    id = "arts",
    title = "ARTS",
    subtitle = "General math, percentage, ratio, stats & dates",
    examples = "Percentage, Average, Ratio, Statistics, Dates",
  ),
  UNIVERSAL(
    id = "universal",
    title = "UNIVERSAL",
    subtitle = "Everyday calculators & converters for everyone",
    examples = "Birthday & Age, Unit Converter, Geometry, BMI",
  ),
}

enum class ToolCategory(val label: String) {
  ALL("All"),
  SCIENCE("Science"),
  COMMERCE("Commerce"),
  ARTS("Arts"),
  UNIVERSAL("Universal"),
  STUDENT("Student"),
}

enum class AngleMode(val label: String) {
  DEG("DEG"),
  RAD("RAD"),
  GRAD("GRAD"),
}

enum class AppearanceMode(val label: String) {
  SYSTEM("System Default"),
  LIGHT("Light Mode"),
  DARK("Dark Mode"),
}

enum class NumberFormatMode(val label: String) {
  STANDARD("Standard"),
  SCIENTIFIC("Scientific"),
}

enum class CalcoraThemeId(
  val id: String,
  val displayName: String,
  val characterTag: String,
  val description: String,
) {
  COSMO(
    id = "cosmo",
    displayName = "COSMO",
    characterTag = "Space & Astronaut",
    description = "Deep cosmic indigo with nebula cyan accents and orbiting starlight.",
  ),
  BYTE(
    id = "byte",
    displayName = "BYTE",
    characterTag = "Futuristic Robot",
    description = "Precision cyber teal and electric mint inspired by intelligent robotics.",
  ),
  LABBY(
    id = "labby",
    displayName = "LABBY",
    characterTag = "Scientist with Goggles",
    description = "Crisp laboratory emerald and cobalt chemistry accents for discovery.",
  ),
  PANDA_MATH(
    id = "panda_math",
    displayName = "PANDA MATH",
    characterTag = "Panda with Books & Calculator",
    description = "Calm bamboo green and warm study paper tones for focused learning.",
  ),
  FOX_FINANCE(
    id = "fox_finance",
    displayName = "FOX FINANCE",
    characterTag = "Fox with Tie",
    description = "Sharp terracotta amber and executive slate crafted for finance pros.",
  ),
  DOODLE(
    id = "doodle",
    displayName = "DOODLE",
    characterTag = "Hand-drawn Creative Student",
    description = "Playful coral sketchbook warmth and expressive ink contrast.",
  ),
  DREAMY(
    id = "dreamy",
    displayName = "DREAMY",
    characterTag = "Moon & Star Calculator",
    description = "Twilight lavender and soft stardust gold for calm late-night study.",
  ),
}

data class UserPreferences(
  val themeId: CalcoraThemeId = CalcoraThemeId.COSMO,
  val appearanceMode: AppearanceMode = AppearanceMode.SYSTEM,
  val decimalPrecision: Int = 6,
  val defaultAngleMode: AngleMode = AngleMode.DEG,
  val numberFormatMode: NumberFormatMode = NumberFormatMode.STANDARD,
  val useThousandsSeparator: Boolean = true,
  val showStepBreakdown: Boolean = true,
)

data class HistoryEntry(
  val id: Long,
  val expression: String,
  val result: String,
  val calculatorName: String,
  val category: String,
  val timestampLabel: String,
  val route: String,
  val reusableInput: String? = null,
)

data class StepBreakdown(
  val formula: String,
  val inputValues: String,
  val substitution: String,
  val calculationSteps: List<String>,
  val finalAnswer: String,
  val unit: String = "",
)

sealed class CalcOutcome<out T> {
  data class Success<T>(val data: T, val breakdown: StepBreakdown? = null) : CalcOutcome<T>()
  data class Error(val message: String) : CalcOutcome<Nothing>()
}

data class CalculatorTool(
  val id: String,
  val name: String,
  val shortDescription: String,
  val category: ToolCategory,
  val route: String,
  val keywords: List<String>,
  val icon: ImageVector? = null,
)
