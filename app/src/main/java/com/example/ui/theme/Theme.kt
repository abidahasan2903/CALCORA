package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp
import com.example.model.AppearanceMode
import com.example.model.CalcoraThemeId

val LocalCalcoraPalette = staticCompositionLocalOf {
  CalcoraThemePalettes.forTheme(CalcoraThemeId.COSMO)
}

val CalcoraShapes =
  Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp),
  )

@Composable
fun CalcoraTheme(
  themeId: CalcoraThemeId = CalcoraThemeId.COSMO,
  appearanceMode: AppearanceMode = AppearanceMode.SYSTEM,
  content: @Composable () -> Unit,
) {
  val isDark =
    when (appearanceMode) {
      AppearanceMode.SYSTEM -> isSystemInDarkTheme()
      AppearanceMode.LIGHT -> false
      AppearanceMode.DARK -> true
    }
  val palette = CalcoraThemePalettes.forTheme(themeId)
  val colorScheme = if (isDark) palette.darkScheme else palette.lightScheme

  CompositionLocalProvider(LocalCalcoraPalette provides palette) {
    MaterialTheme(
      colorScheme = colorScheme,
      typography = CalcoraTypography,
      shapes = CalcoraShapes,
      content = content,
    )
  }
}
