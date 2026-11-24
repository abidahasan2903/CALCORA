package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.engine.SmartSearchEngine
import com.example.engine.ToolRegistry
import com.example.model.AngleMode
import com.example.model.CalculatorTool
import com.example.model.ToolCategory
import com.example.ui.theme.LocalCalcoraPalette

@Composable
fun ToolsDirectoryScreen(
  angleMode: AngleMode,
  onSelectTool: (CalculatorTool) -> Unit,
  modifier: Modifier = Modifier,
) {
  var query by rememberSaveable { mutableStateOf("") }
  var selectedCategory by rememberSaveable { mutableStateOf(ToolCategory.ALL) }
  val palette = LocalCalcoraPalette.current

  val filteredTools = remember(query, selectedCategory, angleMode) {
    val base =
      if (query.isBlank()) ToolRegistry.allTools
      else SmartSearchEngine.search(query, angleMode).matchingTools
    if (selectedCategory == ToolCategory.ALL) base
    else base.filter { it.category == selectedCategory }
  }

  Box(
    modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
    contentAlignment = Alignment.TopCenter,
  ) {
    Column(modifier = Modifier.fillMaxSize().widthIn(max = 720.dp)) {
      Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Text(
          text = "All Tools Directory",
          style = MaterialTheme.typography.headlineMedium,
          fontWeight = FontWeight.Bold,
        )
        OutlinedTextField(
          value = query,
          onValueChange = { query = it },
          placeholder = { Text("Search ${ToolRegistry.allTools.size} calculators...") },
          leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = "Search tools") },
          trailingIcon = {
            if (query.isNotEmpty()) {
              IconButton(onClick = { query = "" }) {
                Icon(Icons.Outlined.Close, contentDescription = "Clear")
              }
            }
          },
          singleLine = true,
          shape = MaterialTheme.shapes.large,
          modifier = Modifier.fillMaxWidth().testTag("tools_search_input"),
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          items(ToolCategory.entries) { category ->
            FilterChip(
              selected = selectedCategory == category,
              onClick = { selectedCategory = category },
              label = { Text(category.label) },
              modifier = Modifier.testTag("tools_filter_${category.name.lowercase()}"),
            )
          }
        }
      }

      if (filteredTools.isEmpty()) {
        Box(
          modifier = Modifier.fillMaxSize().padding(32.dp),
          contentAlignment = Alignment.Center,
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(
              imageVector = Icons.Outlined.SearchOff,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(40.dp),
            )
            Text("No tools match your filter", style = MaterialTheme.typography.titleMedium)
          }
        }
      } else {
        LazyColumn(
          contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          items(filteredTools, key = { it.id }) { tool ->
            val accent =
              when (tool.category) {
                ToolCategory.SCIENCE -> palette.scienceAccent
                ToolCategory.COMMERCE -> palette.commerceAccent
                ToolCategory.ARTS -> palette.artsAccent
                ToolCategory.UNIVERSAL -> palette.universalAccent
                else -> MaterialTheme.colorScheme.primary
              }
            Card(
              onClick = { onSelectTool(tool) },
              shape = MaterialTheme.shapes.medium,
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
              modifier = Modifier.fillMaxWidth().testTag("tool_item_${tool.id}"),
            ) {
              Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
              ) {
                Box(
                  modifier =
                    Modifier
                      .size(44.dp)
                      .clip(RoundedCornerShape(12.dp))
                      .background(accent.copy(alpha = 0.14f)),
                  contentAlignment = Alignment.Center,
                ) {
                  if (tool.icon != null) {
                    Icon(
                      imageVector = tool.icon,
                      contentDescription = tool.name,
                      tint = accent,
                      modifier = Modifier.size(24.dp),
                    )
                  }
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                  Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                      text = tool.name,
                      style = MaterialTheme.typography.titleMedium,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis,
                    )
                  }
                  Text(
                    text = tool.category.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = accent,
                    fontWeight = FontWeight.SemiBold,
                  )
                  Text(
                    text = tool.shortDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                  )
                }
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                  contentDescription = "Open ${tool.name}",
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }
          }
        }
      }
    }
  }
}
