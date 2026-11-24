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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.AngleMode
import com.example.model.AppearanceMode
import com.example.model.CalcoraThemeId
import com.example.model.NumberFormatMode
import com.example.model.UserPreferences
import com.example.ui.components.CalcoraSectionCard
import com.example.ui.components.CalcoraTopBar
import com.example.ui.components.DeveloperCreditFooter
import com.example.ui.components.OfficialCalcoraIcon
import com.example.ui.theme.CalcoraThemePalettes
import com.example.ui.theme.ThemeMascotArtwork

@Composable
fun SettingsScreen(
  preferences: UserPreferences,
  historyCount: Int,
  onOpenThemes: () -> Unit,
  onAppearanceChange: (AppearanceMode) -> Unit,
  onPrecisionChange: (Int) -> Unit,
  onAngleModeChange: (AngleMode) -> Unit,
  onNumberFormatChange: (NumberFormatMode) -> Unit,
  onThousandsSeparatorChange: (Boolean) -> Unit,
  onShowBreakdownChange: (Boolean) -> Unit,
  onClearHistory: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var clearedNotice by remember { mutableStateOf(false) }

  Box(
    modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
    contentAlignment = Alignment.TopCenter,
  ) {
    LazyColumn(
      modifier = Modifier.fillMaxSize().widthIn(max = 720.dp),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      item {
        Text(
          text = "Settings",
          style = MaterialTheme.typography.headlineMedium,
          fontWeight = FontWeight.Bold,
        )
      }

      // Appearance Section (Themes live ONLY inside Settings -> Appearance -> Themes)
      item {
        CalcoraSectionCard {
          Text(
            text = "Appearance",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
          )
          Card(
            onClick = onOpenThemes,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth().testTag("settings_open_themes"),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth().padding(14.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              ThemeMascotArtwork(
                themeId = preferences.themeId,
                modifier = Modifier.size(46.dp),
              )
              Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                  Icon(Icons.Outlined.ColorLens, contentDescription = null, modifier = Modifier.size(16.dp))
                  Text("Themes", style = MaterialTheme.typography.titleMedium)
                }
                Text(
                  text = "${preferences.themeId.displayName} • ${preferences.themeId.characterTag}",
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
              Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Open Themes")
            }
          }

          Text("Color Mode", style = MaterialTheme.typography.labelLarge)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            AppearanceMode.entries.forEach { mode ->
              FilterChip(
                selected = preferences.appearanceMode == mode,
                onClick = { onAppearanceChange(mode) },
                label = { Text(mode.label.replace(" Mode", "")) },
                modifier = Modifier.weight(1f).testTag("appearance_${mode.name.lowercase()}"),
              )
            }
          }
        }
      }

      // Calculator Preferences
      item {
        CalcoraSectionCard {
          Text(
            text = "Calculator Preferences",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
          )

          Column {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Text("Decimal Precision", style = MaterialTheme.typography.labelLarge)
              Text(
                "${preferences.decimalPrecision} digits",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
              )
            }
            Slider(
              value = preferences.decimalPrecision.toFloat(),
              onValueChange = { onPrecisionChange(it.toInt()) },
              valueRange = 0f..10f,
              steps = 9,
              modifier = Modifier.testTag("precision_slider"),
            )
          }

          Text("Default Angle Mode", style = MaterialTheme.typography.labelLarge)
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AngleMode.entries.forEach { angle ->
              FilterChip(
                selected = preferences.defaultAngleMode == angle,
                onClick = { onAngleModeChange(angle) },
                label = { Text(angle.label) },
                modifier = Modifier.weight(1f).testTag("settings_angle_${angle.name.lowercase()}"),
              )
            }
          }

          Text("Number Formatting", style = MaterialTheme.typography.labelLarge)
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NumberFormatMode.entries.forEach { fmt ->
              FilterChip(
                selected = preferences.numberFormatMode == fmt,
                onClick = { onNumberFormatChange(fmt) },
                label = { Text(fmt.label) },
                modifier = Modifier.weight(1f).testTag("settings_format_${fmt.name.lowercase()}"),
              )
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text("Thousands Separator", style = MaterialTheme.typography.bodyLarge)
              Text(
                "Format large numbers with commas (1,000)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
            Switch(
              checked = preferences.useThousandsSeparator,
              onCheckedChange = onThousandsSeparatorChange,
              modifier = Modifier.testTag("switch_thousands"),
            )
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text("Step-by-Step Result Breakdown", style = MaterialTheme.typography.bodyLarge)
              Text(
                "Show formula, substitution, and solution steps",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
            Switch(
              checked = preferences.showStepBreakdown,
              onCheckedChange = onShowBreakdownChange,
              modifier = Modifier.testTag("switch_breakdown"),
            )
          }
        }
      }

      // History Controls
      item {
        CalcoraSectionCard {
          Text(
            text = "History",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text("Session Calculations: $historyCount", style = MaterialTheme.typography.bodyLarge)
              if (clearedNotice) {
                Text(
                  "Session history cleared",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.primary,
                )
              }
            }
            OutlinedButton(
              onClick = {
                onClearHistory()
                clearedNotice = true
              },
              modifier = Modifier.testTag("settings_clear_history"),
            ) {
              Icon(Icons.Outlined.DeleteSweep, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.size(6.dp))
              Text("Clear History")
            }
          }
        }
      }

      // About Section
      item {
        CalcoraSectionCard {
          Text(
            text = "About CALCORA",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
          )
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
          ) {
            OfficialCalcoraIcon(size = 64.dp)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
              Text("CALCORA", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
              Text("Calculate Everything. Learn Every Step.", style = MaterialTheme.typography.bodyMedium)
              Text(
                "Version 1.0 • 100% Offline Engine",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
          Text(
            "An all-in-one academic, scientific, financial, and everyday calculation platform running completely locally with zero internet dependency.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          DeveloperCreditFooter()
        }
      }
    }
  }
}

@Composable
fun ThemesScreen(
  selectedTheme: CalcoraThemeId,
  onSelectTheme: (CalcoraThemeId) -> Unit,
  onBack: () -> Unit,
) {
  Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
    CalcoraTopBar(
      title = "Themes",
      subtitle = "Settings → Appearance → Themes",
      onBack = onBack,
    )
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
      LazyColumn(
        modifier = Modifier.fillMaxSize().widthIn(max = 720.dp).testTag("themes_lazy_column"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        items(CalcoraThemeId.entries, key = { it.id }) { theme ->
          val isSelected = theme == selectedTheme
          val palette = CalcoraThemePalettes.forTheme(theme)
          Card(
            onClick = { onSelectTheme(theme) },
            shape = MaterialTheme.shapes.large,
            colors =
              CardDefaults.cardColors(
                containerColor =
                  if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                  else MaterialTheme.colorScheme.surface,
              ),
            border =
              BorderStroke(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
              ),
            modifier = Modifier.fillMaxWidth().testTag("theme_card_${theme.id}"),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth().padding(16.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
              ThemeMascotArtwork(
                themeId = theme,
                modifier = Modifier.size(64.dp),
              )
              Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                  Text(
                    text = theme.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                  )
                  Surface(
                    color = palette.previewBg,
                    shape = CircleShape,
                  ) {
                    Text(
                      text = theme.characterTag,
                      style = MaterialTheme.typography.labelSmall,
                      color = palette.previewAccent,
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                  }
                }
                Text(
                  text = theme.description,
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                // Color swatch preview
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                  listOf(
                    palette.lightScheme.primary,
                    palette.lightScheme.secondary,
                    palette.lightScheme.tertiary,
                    palette.darkScheme.surface,
                  ).forEach { swatch ->
                    Box(
                      modifier =
                        Modifier
                          .size(16.dp)
                          .clip(CircleShape)
                          .background(swatch),
                    )
                  }
                }
              }
              if (isSelected) {
                Icon(
                  imageVector = Icons.Outlined.CheckCircle,
                  contentDescription = "Selected",
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(28.dp),
                )
              }
            }
          }
        }
      }
    }
  }
}
