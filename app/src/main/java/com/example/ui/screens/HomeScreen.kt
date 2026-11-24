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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.Cake
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Functions
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Percent
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.engine.Routes
import com.example.engine.SmartSearchEngine
import com.example.model.AngleMode
import com.example.model.CalculationWorld
import com.example.model.CalculatorTool
import com.example.ui.components.DeveloperCreditFooter
import com.example.ui.components.OfficialCalcoraIcon
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalCalcoraPalette
import java.util.Calendar

private data class QuickShortcut(
  val label: String,
  val route: String,
  val icon: ImageVector,
)

@Composable
fun HomeScreen(
  angleMode: AngleMode,
  onNavigateToWorld: (CalculationWorld) -> Unit,
  onNavigateToRoute: (String) -> Unit,
  onOpenAllTools: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var searchQuery by rememberSaveable { mutableStateOf("") }
  val searchOutput = remember(searchQuery, angleMode) {
    SmartSearchEngine.search(searchQuery, angleMode)
  }
  val palette = LocalCalcoraPalette.current

  val greeting = remember {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    when {
      hour < 12 -> "Good morning — ready to solve?"
      hour < 17 -> "Good afternoon — let's calculate."
      else -> "Good evening — study & compute."
    }
  }

  val quickShortcuts = remember {
    listOf(
      QuickShortcut("Basic Calculator", Routes.ART_CALCULATOR, Icons.Outlined.Calculate),
      QuickShortcut("Scientific Calculator", Routes.SCI_CALCULATOR, Icons.Outlined.Functions),
      QuickShortcut("Percentage Calculator", Routes.ART_PERCENTAGE, Icons.Outlined.Percent),
      QuickShortcut("Age Calculator", Routes.UNI_AGE, Icons.Outlined.Cake),
      QuickShortcut("Unit Converter", Routes.UNI_UNIT_CONVERTER, Icons.Outlined.SwapHoriz),
      QuickShortcut("GPA Calculator", Routes.STU_GPA, Icons.Outlined.School),
    )
  }

  Box(
    modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
    contentAlignment = Alignment.TopCenter,
  ) {
    LazyColumn(
      modifier = Modifier.fillMaxSize().widthIn(max = 720.dp).testTag("home_lazy_column"),
      contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      // Top Brand Header
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          OfficialCalcoraIcon(size = 54.dp)
          Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Text(
                text = "CALCORA",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
              )
              Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = CircleShape,
              ) {
                Text(
                  text = "Offline",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onPrimaryContainer,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                )
              }
            }
            Text(
              text = "Calculate Everything. Learn Every Step.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
              text = greeting,
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.primary,
            )
          }
        }
      }

      // Smart Offline Search Bar
      item {
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          placeholder = { Text("What do you want to calculate?") },
          leadingIcon = {
            Icon(
              imageVector = Icons.Outlined.Search,
              contentDescription = "Search calculators",
            )
          },
          trailingIcon = {
            if (searchQuery.isNotEmpty()) {
              IconButton(onClick = { searchQuery = "" }) {
                Icon(imageVector = Icons.Outlined.Close, contentDescription = "Clear search")
              }
            }
          },
          singleLine = true,
          shape = MaterialTheme.shapes.large,
          colors =
            OutlinedTextFieldDefaults.colors(
              focusedContainerColor = MaterialTheme.colorScheme.surface,
              unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            ),
          modifier = Modifier.fillMaxWidth().testTag("home_search_input"),
        )
      }

      // Search Mode Results
      if (searchQuery.isNotBlank()) {
        searchOutput.quickAnswer?.let { quick ->
          item {
            Card(
              onClick = { onNavigateToRoute(quick.targetRoute) },
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
              shape = MaterialTheme.shapes.large,
              modifier = Modifier.fillMaxWidth().testTag("smart_quick_answer"),
            ) {
              Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                  text = "INSTANT LOCAL RESULT",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.primary,
                  fontWeight = FontWeight.Bold,
                )
                Text(
                  text = "${quick.title} = ${quick.answer}",
                  style = MaterialTheme.typography.titleLarge.copy(fontFamily = JetBrainsMonoFontFamily),
                  color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                  text = quick.subtitle,
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                )
              }
            }
          }
        }

        if (searchOutput.matchingTools.isEmpty() && searchOutput.quickAnswer == null) {
          item {
            Card(
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              shape = MaterialTheme.shapes.large,
              modifier = Modifier.fillMaxWidth().testTag("search_empty_state"),
            ) {
              Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Icon(
                  imageVector = Icons.Outlined.SearchOff,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(36.dp),
                )
                Text(
                  text = "No matching calculator found",
                  style = MaterialTheme.typography.titleMedium,
                )
                Text(
                  text = "Try \"Age calculator\", \"Compound interest\", \"BMI\", \"GPA\", or \"15% of 500\".",
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }
          }
        } else {
          items(searchOutput.matchingTools, key = { it.id }) { tool ->
            SearchResultToolCard(tool = tool, onClick = { onNavigateToRoute(tool.route) })
          }
        }
      } else {
        // Choose Your Calculation World Header
        item {
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
              text = "Choose Your Calculation World",
              style = MaterialTheme.typography.titleLarge,
              color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
              text = "Four dedicated calculator environments built for study, business, and everyday precision.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }

        // Four Large Interactive World Cards
        item {
          WorldSelectionCard(
            world = CalculationWorld.SCIENCE,
            accent = palette.scienceAccent,
            icon = Icons.Outlined.Science,
            onClick = { onNavigateToWorld(CalculationWorld.SCIENCE) },
            testTag = "world_card_science",
          )
        }
        item {
          WorldSelectionCard(
            world = CalculationWorld.COMMERCE,
            accent = palette.commerceAccent,
            icon = Icons.Outlined.AttachMoney,
            onClick = { onNavigateToWorld(CalculationWorld.COMMERCE) },
            testTag = "world_card_commerce",
          )
        }
        item {
          WorldSelectionCard(
            world = CalculationWorld.ARTS,
            accent = palette.artsAccent,
            icon = Icons.Outlined.Palette,
            onClick = { onNavigateToWorld(CalculationWorld.ARTS) },
            testTag = "world_card_arts",
          )
        }
        item {
          WorldSelectionCard(
            world = CalculationWorld.UNIVERSAL,
            accent = palette.universalAccent,
            icon = Icons.Outlined.Public,
            onClick = { onNavigateToWorld(CalculationWorld.UNIVERSAL) },
            testTag = "world_card_universal",
          )
        }

        // Quick Tools Section
        item {
          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
              text = "Quick Tools",
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onBackground,
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              items(quickShortcuts) { item ->
                Surface(
                  onClick = { onNavigateToRoute(item.route) },
                  shape = MaterialTheme.shapes.medium,
                  color = MaterialTheme.colorScheme.surface,
                  border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                  modifier = Modifier.testTag("quick_tool_${item.route}"),
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                  ) {
                    Icon(
                      imageVector = item.icon,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.primary,
                      modifier = Modifier.size(20.dp),
                    )
                    Text(
                      text = item.label,
                      style = MaterialTheme.typography.labelLarge,
                      maxLines = 1,
                    )
                  }
                }
              }
            }
          }
        }

        // All Tools CTA
        item {
          Button(
            onClick = onOpenAllTools,
            shape = MaterialTheme.shapes.large,
            contentPadding = PaddingValues(vertical = 15.dp, horizontal = 20.dp),
            modifier = Modifier.fillMaxWidth().testTag("home_all_tools_button"),
          ) {
            Icon(imageVector = Icons.Outlined.Apps, contentDescription = null)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "All Tools (${com.example.engine.ToolRegistry.allTools.size} Calculators)",
              style = MaterialTheme.typography.titleMedium,
            )
          }
        }
      }

      // Mandatory Developer Credit Footer
      item {
        DeveloperCreditFooter()
      }
    }
  }
}

@Composable
private fun WorldSelectionCard(
  world: CalculationWorld,
  accent: Color,
  icon: ImageVector,
  onClick: () -> Unit,
  testTag: String,
) {
  Card(
    onClick = onClick,
    shape = MaterialTheme.shapes.large,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.5.dp, accent.copy(alpha = 0.35f)),
    modifier = Modifier.fillMaxWidth().testTag(testTag),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(16.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      Box(
        modifier =
          Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(accent.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = icon,
          contentDescription = world.title,
          tint = accent,
          modifier = Modifier.size(30.dp),
        )
      }
      Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
          text = world.title,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = accent,
        )
        Text(
          text = world.subtitle,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
          text = world.examples,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
      Icon(
        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
        contentDescription = "Open ${world.title}",
        tint = accent,
      )
    }
  }
}

@Composable
private fun SearchResultToolCard(
  tool: CalculatorTool,
  onClick: () -> Unit,
) {
  Card(
    onClick = onClick,
    shape = MaterialTheme.shapes.medium,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
    modifier = Modifier.fillMaxWidth().testTag("search_result_${tool.id}"),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      if (tool.icon != null) {
        Icon(
          imageVector = tool.icon,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(26.dp),
        )
      }
      Column(modifier = Modifier.weight(1f)) {
        Text(text = tool.name, style = MaterialTheme.typography.titleMedium)
        Text(
          text = "${tool.category.label} • ${tool.shortDescription}",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }
  }
}
