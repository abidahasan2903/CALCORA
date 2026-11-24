package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.StepBreakdown
import com.example.ui.theme.JetBrainsMonoFontFamily

@Composable
fun OfficialCalcoraIcon(
  size: Dp = 56.dp,
  modifier: Modifier = Modifier,
) {
  Image(
    painter = painterResource(id = R.drawable.calcora_official_icon),
    contentDescription = "CALCORA official logo",
    contentScale = ContentScale.Crop,
    modifier = modifier
      .size(size)
      .clip(RoundedCornerShape(size * 0.24f)),
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalcoraTopBar(
  title: String,
  subtitle: String? = null,
  onBack: (() -> Unit)? = null,
  actions: @Composable () -> Unit = {},
) {
  TopAppBar(
    title = {
      Column {
        Text(
          text = title,
          style = MaterialTheme.typography.titleLarge,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        if (!subtitle.isNullOrBlank()) {
          Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }
    },
    navigationIcon = {
      if (onBack != null) {
        IconButton(
          onClick = onBack,
          modifier = Modifier.testTag("top_bar_back"),
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Navigate back",
          )
        }
      }
    },
    actions = { actions() },
    colors =
      TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.background,
        titleContentColor = MaterialTheme.colorScheme.onBackground,
      ),
  )
}

@Composable
fun ValidationErrorCard(message: String, modifier: Modifier = Modifier) {
  Card(
    modifier = modifier.fillMaxWidth().testTag("validation_error_card"),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
    shape = MaterialTheme.shapes.medium,
  ) {
    Row(
      modifier = Modifier.padding(14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Icon(
        imageVector = Icons.Outlined.ErrorOutline,
        contentDescription = "Validation error",
        tint = MaterialTheme.colorScheme.onErrorContainer,
      )
      Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onErrorContainer,
      )
    }
  }
}

@Composable
fun StepBreakdownCard(
  breakdown: StepBreakdown,
  showDetails: Boolean = true,
  modifier: Modifier = Modifier,
) {
  val clipboard = LocalClipboardManager.current
  Card(
    modifier = modifier.fillMaxWidth().testTag("step_breakdown_card"),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
    shape = MaterialTheme.shapes.large,
  ) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "FINAL ANSWER",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = breakdown.finalAnswer,
            style = MaterialTheme.typography.headlineMedium.copy(fontFamily = JetBrainsMonoFontFamily),
            color = MaterialTheme.colorScheme.onSurface,
          )
        }
        IconButton(
          onClick = { clipboard.setText(AnnotatedString(breakdown.finalAnswer)) },
          modifier = Modifier.testTag("copy_answer_button"),
        ) {
          Icon(
            imageVector = Icons.Outlined.ContentCopy,
            contentDescription = "Copy result",
            tint = MaterialTheme.colorScheme.primary,
          )
        }
      }

      AnimatedVisibility(visible = showDetails) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          BreakdownRow(label = "Formula", value = breakdown.formula)
          BreakdownRow(label = "Input Values", value = breakdown.inputValues)
          BreakdownRow(label = "Substitution", value = breakdown.substitution)
          if (breakdown.calculationSteps.isNotEmpty()) {
            Column(
              modifier =
                Modifier.fillMaxWidth()
                  .clip(MaterialTheme.shapes.small)
                  .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
                  .padding(12.dp),
              verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(
                  imageVector = Icons.Outlined.Lightbulb,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(16.dp),
                )
                Text(
                  text = "Step-by-Step Calculation",
                  style = MaterialTheme.typography.labelLarge,
                  color = MaterialTheme.colorScheme.primary,
                )
              }
              breakdown.calculationSteps.forEachIndexed { idx, step ->
                Text(
                  text = "${idx + 1}. $step",
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurface,
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun BreakdownRow(label: String, value: String) {
  Column(modifier = Modifier.fillMaxWidth()) {
    Text(
      text = label.uppercase(),
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodyMedium.copy(fontFamily = JetBrainsMonoFontFamily),
      color = MaterialTheme.colorScheme.onSurface,
    )
  }
}

@Composable
fun CalcoraNumberField(
  value: String,
  onValueChange: (String) -> Unit,
  label: String,
  modifier: Modifier = Modifier,
  placeholder: String = "",
  allowDecimal: Boolean = true,
  allowNegative: Boolean = true,
  testTag: String? = null,
) {
  val kType =
    when {
      allowDecimal -> KeyboardType.Decimal
      allowNegative -> KeyboardType.Number
      else -> KeyboardType.NumberPassword
    }
  OutlinedTextField(
    value = value,
    onValueChange = onValueChange,
    label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
    placeholder = if (placeholder.isNotEmpty()) ({ Text(placeholder) }) else null,
    singleLine = true,
    keyboardOptions = KeyboardOptions(keyboardType = kType),
    shape = MaterialTheme.shapes.small,
    modifier = modifier.fillMaxWidth().let { m -> if (testTag != null) m.testTag(testTag) else m },
  )
}

@Composable
fun CalcoraSectionCard(
  modifier: Modifier = Modifier,
  content: @Composable ColumnScope.() -> Unit,
) {
  Card(
    modifier = modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
    shape = MaterialTheme.shapes.large,
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
      content = content,
    )
  }
}

@Composable
fun ActionButtonRow(
  primaryLabel: String,
  onPrimaryClick: () -> Unit,
  onResetClick: () -> Unit,
  primaryTestTag: String = "calculate_button",
) {
  Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
    OutlinedButton(
      onClick = onResetClick,
      modifier = Modifier.weight(0.35f).heightIn(min = 50.dp).testTag("reset_button"),
      shape = MaterialTheme.shapes.medium,
      contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
    ) {
      Text("Reset", maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
    Button(
      onClick = onPrimaryClick,
      modifier = Modifier.weight(0.65f).heightIn(min = 50.dp).testTag(primaryTestTag),
      shape = MaterialTheme.shapes.medium,
      contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    ) {
      Text(primaryLabel, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
  }
}

enum class KeypadKeyStyle {
  NUMERIC,
  OPERATOR,
  FUNCTION,
  ACCENT,
  CLEAR,
}

@Composable
fun CalcKeyButton(
  label: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  style: KeypadKeyStyle = KeypadKeyStyle.NUMERIC,
  isToggled: Boolean = false,
  height: Dp = 54.dp,
  testTag: String? = null,
) {
  val interactionSource = remember { MutableInteractionSource() }
  val pressed by interactionSource.collectIsPressedAsState()
  val scale by animateFloatAsState(
    targetValue = if (pressed) 0.94f else 1f,
    animationSpec = spring(stiffness = 700f),
    label = "key_scale",
  )

  val (bg, fg) =
    when {
      isToggled -> MaterialTheme.colorScheme.tertiary to MaterialTheme.colorScheme.onTertiary
      style == KeypadKeyStyle.ACCENT -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
      style == KeypadKeyStyle.OPERATOR -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
      style == KeypadKeyStyle.FUNCTION -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
      style == KeypadKeyStyle.CLEAR -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
      else -> MaterialTheme.colorScheme.surface to MaterialTheme.colorScheme.onSurface
    }

  Surface(
    onClick = onClick,
    interactionSource = interactionSource,
    modifier =
      modifier
        .scale(scale)
        .heightIn(min = height)
        .let { m -> if (testTag != null) m.testTag(testTag) else m },
    color = bg,
    shape = RoundedCornerShape(14.dp),
    border =
      if (style == KeypadKeyStyle.NUMERIC) {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
      } else null,
  ) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)) {
      Text(
        text = label,
        color = fg,
        fontFamily = if (style == KeypadKeyStyle.NUMERIC || style == KeypadKeyStyle.OPERATOR) JetBrainsMonoFontFamily else MaterialTheme.typography.labelLarge.fontFamily,
        fontWeight = if (style == KeypadKeyStyle.ACCENT || style == KeypadKeyStyle.OPERATOR) FontWeight.Bold else FontWeight.SemiBold,
        fontSize = when {
          label.length <= 2 -> 17.sp
          label.length <= 4 -> 13.5.sp
          else -> 11.5.sp
        },
        maxLines = 1,
        softWrap = false,
        textAlign = TextAlign.Center,
      )
    }
  }
}

@Composable
fun DeveloperCreditFooter(modifier: Modifier = Modifier) {
  Column(
    modifier = modifier.fillMaxWidth().padding(vertical = 12.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Box(
      modifier =
        Modifier
          .width(36.dp)
          .height(2.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = "Developed by @Abid Ahasan",
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center,
      modifier = Modifier.testTag("developer_credit"),
    )
  }
}
