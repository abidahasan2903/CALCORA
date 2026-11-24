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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Replay
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.model.HistoryEntry
import com.example.ui.theme.JetBrainsMonoFontFamily

@Composable
fun HistoryScreen(
  entries: List<HistoryEntry>,
  onReuseEntry: (HistoryEntry) -> Unit,
  onDeleteEntry: (Long) -> Unit,
  onClearAll: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val clipboard = LocalClipboardManager.current
  var selectedEntry by remember { mutableStateOf<HistoryEntry?>(null) }

  Box(
    modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
    contentAlignment = Alignment.TopCenter,
  ) {
    Column(modifier = Modifier.fillMaxSize().widthIn(max = 720.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column {
          Text(
            text = "Session History",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
          )
          Text(
            text = "Active session calculations (cleared on app exit)",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        if (entries.isNotEmpty()) {
          OutlinedButton(
            onClick = onClearAll,
            modifier = Modifier.testTag("clear_history_button"),
          ) {
            Text("Clear All")
          }
        }
      }

      if (entries.isEmpty()) {
        Box(
          modifier = Modifier.fillMaxSize().padding(32.dp).testTag("history_empty_state"),
          contentAlignment = Alignment.Center,
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Icon(
              imageVector = Icons.Outlined.History,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
              modifier = Modifier.size(56.dp),
            )
            Text(
              text = "No calculations yet in this session",
              style = MaterialTheme.typography.titleMedium,
            )
            Text(
              text = "Results from Science, Commerce, Arts, Universal, and Student calculators appear here automatically.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center,
            )
          }
        }
      } else {
        LazyColumn(
          contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          items(entries, key = { it.id }) { entry ->
            Card(
              onClick = { selectedEntry = entry },
              shape = MaterialTheme.shapes.medium,
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
              modifier = Modifier.fillMaxWidth().testTag("history_item_${entry.id}"),
            ) {
              Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Text(
                    text = "${entry.category} • ${entry.calculatorName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                  )
                  Text(
                    text = entry.timestampLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
                Text(
                  text = entry.expression,
                  style = MaterialTheme.typography.bodyMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Text(
                    text = "= ${entry.result}",
                    style = MaterialTheme.typography.titleLarge.copy(fontFamily = JetBrainsMonoFontFamily),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                  )
                  Row {
                    IconButton(onClick = { clipboard.setText(AnnotatedString(entry.result)) }) {
                      Icon(Icons.Outlined.ContentCopy, contentDescription = "Copy result")
                    }
                    IconButton(onClick = { onReuseEntry(entry) }) {
                      Icon(Icons.Outlined.Replay, contentDescription = "Reuse calculation")
                    }
                    IconButton(onClick = { onDeleteEntry(entry.id) }) {
                      Icon(Icons.Outlined.DeleteOutline, contentDescription = "Delete entry")
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  selectedEntry?.let { entry ->
    AlertDialog(
      onDismissRequest = { selectedEntry = null },
      title = { Text(entry.calculatorName) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("Category: ${entry.category}", style = MaterialTheme.typography.labelLarge)
          Text("Time: ${entry.timestampLabel}", style = MaterialTheme.typography.bodyMedium)
          Spacer(modifier = Modifier.height(4.dp))
          Text("Expression:", style = MaterialTheme.typography.labelSmall)
          Text(entry.expression, style = MaterialTheme.typography.bodyMedium.copy(fontFamily = JetBrainsMonoFontFamily))
          Text("Result:", style = MaterialTheme.typography.labelSmall)
          Text(entry.result, style = MaterialTheme.typography.titleLarge.copy(fontFamily = JetBrainsMonoFontFamily))
        }
      },
      confirmButton = {
        TextButton(onClick = {
          onReuseEntry(entry)
          selectedEntry = null
        }) {
          Text("Open & Reuse")
        }
      },
      dismissButton = {
        TextButton(onClick = {
          clipboard.setText(AnnotatedString(entry.result))
          selectedEntry = null
        }) {
          Text("Copy Result")
        }
      },
    )
  }
}
