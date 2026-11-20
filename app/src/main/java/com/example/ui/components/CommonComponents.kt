package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppTab
import com.example.ui.i18n.AppStrings
import com.example.ui.theme.AmberText
import com.example.ui.theme.AmberTimer
import com.example.util.DateUtils

@Composable
fun ActiveTimerBanner(
  isRunning: Boolean,
  elapsedMs: Long,
  strings: AppStrings,
  onStop: () -> Unit,
) {
  AnimatedVisibility(visible = isRunning) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by
      infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "pulseScale",
      )
    val pulseAlpha by
      infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "pulseAlpha",
      )

    Surface(
      color = AmberTimer,
      shadowElevation = 4.dp,
      modifier = Modifier.fillMaxWidth().statusBarsPadding(),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(contentAlignment = Alignment.Center, modifier = Modifier.size(16.dp)) {
            Box(
              modifier =
                Modifier.size(10.dp)
                  .graphicsLayer(scaleX = pulseScale, scaleY = pulseScale, alpha = pulseAlpha)
                  .background(Color.White, CircleShape)
            )
            Box(modifier = Modifier.size(8.dp).background(Color.White, CircleShape))
          }
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = String.format(strings.bannerTimerPrefix, DateUtils.formatMsToHHMMSS(elapsedMs)),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            fontSize = 14.sp,
          )
        }
        Button(
          onClick = onStop,
          colors =
            ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = AmberText),
          shape = RoundedCornerShape(999.dp),
          contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
          modifier = Modifier.testTag("banner_stop_timer_button"),
        ) {
          Text(text = strings.bannerStopButton, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeaderCard(
  selectedDateStr: String,
  isDarkTheme: Boolean,
  strings: AppStrings,
  onPrevDate: () -> Unit,
  onNextDate: () -> Unit,
  onTodayClick: () -> Unit,
  onDatePicked: (String) -> Unit,
  onTimerShortcut: () -> Unit,
  onToggleTheme: () -> Unit,
  onToggleLanguage: () -> Unit,
) {
  var showDatePicker by remember { mutableStateOf(false) }

  SectionCard {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
        Box(
          modifier =
            Modifier.size(40.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(MaterialTheme.colorScheme.primaryContainer),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Filled.MenuBook,
            contentDescription = strings.appTitle,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp),
          )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(
            text = strings.appTitle,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
          )
          Text(
            text = strings.appSubtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = MaterialTheme.colorScheme.primaryContainer,
          modifier =
            Modifier.clip(RoundedCornerShape(10.dp))
              .clickable(onClick = onToggleLanguage)
              .semantics { contentDescription = strings.switchLanguageDesc }
              .testTag("header_language_toggle"),
        ) {
          Text(
            text = strings.lang.shortBadge,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
          )
        }
        IconButton(
          onClick = onTimerShortcut,
          modifier = Modifier.testTag("header_timer_button"),
        ) {
          Icon(
            imageVector = Icons.Filled.Timer,
            contentDescription = strings.timerShortcutDesc,
            tint = MaterialTheme.colorScheme.tertiary,
          )
        }
        IconButton(
          onClick = onToggleTheme,
          modifier = Modifier.testTag("header_theme_toggle"),
        ) {
          Icon(
            imageVector = if (isDarkTheme) Icons.Filled.LightMode else Icons.Filled.DarkMode,
            contentDescription = strings.themeToggleDesc,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Date Selector Row
    Row(
      modifier =
        Modifier.fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(MaterialTheme.colorScheme.background)
          .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
          .padding(horizontal = 6.dp, vertical = 4.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      IconButton(
        onClick = onPrevDate,
        modifier = Modifier.size(40.dp).testTag("date_prev_button"),
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
          contentDescription = strings.prevDayDesc,
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Text(
          text = DateUtils.formatDisplayDate(selectedDateStr, strings.lang),
          fontSize = 13.sp,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface,
          modifier =
            Modifier.clip(RoundedCornerShape(8.dp))
              .clickable { showDatePicker = true }
              .padding(horizontal = 8.dp, vertical = 6.dp)
              .testTag("date_picker_trigger"),
        )
        Box(
          modifier =
            Modifier.clip(RoundedCornerShape(8.dp))
              .background(MaterialTheme.colorScheme.primary)
              .clickable(onClick = onTodayClick)
              .padding(horizontal = 10.dp, vertical = 5.dp)
              .testTag("date_today_button"),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = strings.todayButton,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary,
          )
        }
      }

      IconButton(
        onClick = onNextDate,
        modifier = Modifier.size(40.dp).testTag("date_next_button"),
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
          contentDescription = strings.nextDayDesc,
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }

  if (showDatePicker) {
    val pickerState =
      rememberDatePickerState(initialSelectedDateMillis = DateUtils.toEpochMillis(selectedDateStr))
    DatePickerDialog(
      onDismissRequest = { showDatePicker = false },
      confirmButton = {
        TextButton(
          onClick = {
            pickerState.selectedDateMillis?.let { ms ->
              onDatePicked(DateUtils.fromEpochMillis(ms))
            }
            showDatePicker = false
          }
        ) {
          Text(strings.datePickConfirm)
        }
      },
      dismissButton = {
        TextButton(onClick = { showDatePicker = false }) { Text(strings.datePickCancel) }
      },
    ) {
      DatePicker(state = pickerState)
    }
  }
}

@Composable
fun SectionCard(
  modifier: Modifier = Modifier,
  content: @Composable ColumnScope.() -> Unit,
) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(18.dp),
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 1.dp,
    shadowElevation = 1.dp,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
  ) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), content = content)
  }
}

@Composable
fun MuhasabaBottomBar(
  currentTab: AppTab,
  strings: AppStrings,
  onSelectTab: (AppTab) -> Unit,
) {
  NavigationBar(
    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f),
    tonalElevation = 4.dp,
  ) {
    AppTab.entries.forEach { tab ->
      val icon =
        when (tab) {
          AppTab.TODAY -> Icons.Filled.CalendarToday
          AppTab.CALENDAR -> Icons.Filled.CalendarMonth
          AppTab.TIMER -> Icons.Filled.Timer
          AppTab.REPORTS -> Icons.Filled.BarChart
          AppTab.SETTINGS -> Icons.Filled.Settings
        }
      val label = tab.label(strings)
      val selected = currentTab == tab
      NavigationBarItem(
        selected = selected,
        onClick = { onSelectTab(tab) },
        icon = { Icon(imageVector = icon, contentDescription = label) },
        label = {
          Text(
            text = label,
            fontSize = 10.sp,
            maxLines = 1,
            softWrap = false,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
          )
        },
        colors =
          NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
          ),
        modifier = Modifier.testTag("nav_${tab.name.lowercase()}"),
      )
    }
  }
}

@Composable
fun NumericCompactInput(
  value: Int,
  onValueChange: (Int) -> Unit,
  modifier: Modifier = Modifier,
  width: Dp = 72.dp,
  allowZeroPlaceholder: Boolean = true,
  minValue: Int = 0,
) {
  var textState by remember {
    mutableStateOf(if (allowZeroPlaceholder && value == 0) "" else value.toString())
  }

  LaunchedEffect(value) {
    val currentParsed = textState.toIntOrNull() ?: minValue
    if (value != currentParsed) {
      textState = if (allowZeroPlaceholder && value == 0) "" else value.toString()
    }
  }

  Box(
    modifier =
      modifier
        .width(width)
        .height(38.dp)
        .clip(RoundedCornerShape(10.dp))
        .background(MaterialTheme.colorScheme.surface)
        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
        .padding(horizontal = 8.dp),
    contentAlignment = Alignment.Center,
  ) {
    BasicTextField(
      value = textState,
      onValueChange = { raw ->
        val filtered = raw.filter { it.isDigit() }.take(6)
        textState = filtered
        val parsed = filtered.toIntOrNull() ?: minValue
        onValueChange(parsed.coerceAtLeast(minValue))
      },
      singleLine = true,
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
      textStyle =
        TextStyle(
          color = MaterialTheme.colorScheme.onSurface,
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          textAlign = TextAlign.Center,
        ),
      cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
      decorationBox = { innerTextField ->
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
          if (textState.isEmpty()) {
            Text(
              text = minValue.toString(),
              color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              textAlign = TextAlign.Center,
            )
          }
          innerTextField()
        }
      },
    )
  }
}
