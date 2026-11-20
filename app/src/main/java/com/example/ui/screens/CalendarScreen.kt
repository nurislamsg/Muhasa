package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MuhasabaUiState
import com.example.ui.calendar.CalendarBlock
import com.example.ui.calendar.CalendarItemFilter
import com.example.ui.calendar.CalendarLogic
import com.example.ui.calendar.CalendarSortOrder
import com.example.ui.calendar.CalendarStatusFilter
import com.example.ui.calendar.DayEvaluation
import com.example.ui.calendar.DayOutcome
import com.example.ui.components.SectionCard
import com.example.util.DateUtils

@Composable
fun CalendarScreen(
  state: MuhasabaUiState,
  onSelectDate: (String) -> Unit,
  onOpenDateInToday: (String) -> Unit,
) {
  val s = state.strings
  val isDark = state.isDarkTheme
  val todayStr = remember { DateUtils.todayStr() }

  var visibleYearMonth by remember { mutableStateOf(DateUtils.yearMonthOf(state.selectedDateStr)) }
  var selectedBlock by remember { mutableStateOf(CalendarBlock.ALL) }
  var selectedFilter by remember { mutableStateOf<CalendarItemFilter>(CalendarItemFilter.Overall) }
  var statusFilter by remember { mutableStateOf(CalendarStatusFilter.ALL) }
  var sortOrder by remember { mutableStateOf(CalendarSortOrder.RED_FIRST) }

  LaunchedEffect(state.selectedDateStr) {
    visibleYearMonth = DateUtils.yearMonthOf(state.selectedDateStr)
  }

  val (year, month) = visibleYearMonth
  val monthDays = remember(year, month) { DateUtils.daysInMonth(year, month) }
  val gridCells = remember(year, month) { DateUtils.daysInMonthGrid(year, month) }

  val evaluationsByDate =
    remember(monthDays, state.allRecords, state.settings, selectedFilter, state.language) {
      monthDays.associateWith { date ->
        CalendarLogic.evaluateDay(state.allRecords[date], state.settings, selectedFilter, s)
      }
    }

  val greenCount = evaluationsByDate.values.count { it.outcome == DayOutcome.GREEN }
  val redCount = evaluationsByDate.values.count { it.outcome == DayOutcome.RED }

  val greenBg = if (isDark) Color(0xFF14532D) else Color(0xFFDCFCE7)
  val greenFg = if (isDark) Color(0xFF4ADE80) else Color(0xFF15803D)
  val redBg = if (isDark) Color(0xFF450A0A) else Color(0xFFFEE2E2)
  val redFg = if (isDark) Color(0xFFF87171) else Color(0xFFDC2626)

  Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
    // 1. Filters + Month Calendar Grid
    SectionCard {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Filled.CalendarMonth,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
          Text(
            text = s.calendarTitle,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Text(
            text = s.calendarSubtitle,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Block Filter Row
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Filled.FilterList,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(15.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = s.calendarFilterBlockLabel,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
      Spacer(modifier = Modifier.height(6.dp))

      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        contentPadding = PaddingValues(end = 4.dp),
      ) {
        items(CalendarBlock.entries) { block ->
          val active = selectedBlock == block
          FilterChipPill(
            label = block.label(s),
            selected = active,
            onClick = {
              selectedBlock = block
              selectedFilter = CalendarLogic.defaultFilterForBlock(block)
            },
            testTag = "calendar_block_${block.name.lowercase()}",
          )
        }
      }

      // Specific Item Filter Row (when a block is selected)
      val subItems =
        remember(selectedBlock, state.settings, state.language) {
          CalendarLogic.itemsForBlock(selectedBlock, state.settings, s)
        }
      AnimatedVisibility(visible = subItems.isNotEmpty()) {
        Column {
          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = s.calendarFilterItemLabel,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Spacer(modifier = Modifier.height(6.dp))
          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(end = 4.dp),
          ) {
            items(subItems) { (itemFilter, itemLabel) ->
              FilterChipPill(
                label = itemLabel,
                selected = selectedFilter == itemFilter,
                onClick = { selectedFilter = itemFilter },
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Month Navigation & Counts
      Row(
        modifier =
          Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.background)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            .padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        IconButton(
          onClick = { visibleYearMonth = DateUtils.shiftMonth(year, month, -1) },
          modifier = Modifier.size(44.dp).testTag("calendar_prev_month"),
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = s.calendarPrevMonthDesc,
          )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = DateUtils.formatMonthYear(year, month, state.language),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Spacer(modifier = Modifier.height(2.dp))
          Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
              text = String.format(s.calendarGreenDaysCountFormat, greenCount),
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = greenFg,
            )
            Text(
              text = String.format(s.calendarRedDaysCountFormat, redCount),
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = redFg,
            )
          }
        }
        IconButton(
          onClick = { visibleYearMonth = DateUtils.shiftMonth(year, month, 1) },
          modifier = Modifier.size(44.dp).testTag("calendar_next_month"),
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = s.calendarNextMonthDesc,
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Status Filter (All / Green / Red)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        StatusFilterPill(
          label = s.calendarStatusAll,
          selected = statusFilter == CalendarStatusFilter.ALL,
          activeBg = MaterialTheme.colorScheme.primary,
          activeFg = MaterialTheme.colorScheme.onPrimary,
          onClick = { statusFilter = CalendarStatusFilter.ALL },
          modifier = Modifier.weight(1f).testTag("calendar_status_all"),
        )
        StatusFilterPill(
          label = s.calendarStatusGreenOnly,
          selected = statusFilter == CalendarStatusFilter.GREEN_ONLY,
          activeBg = greenFg,
          activeFg = Color.White,
          onClick = { statusFilter = CalendarStatusFilter.GREEN_ONLY },
          modifier = Modifier.weight(1f).testTag("calendar_status_green"),
        )
        StatusFilterPill(
          label = s.calendarStatusRedOnly,
          selected = statusFilter == CalendarStatusFilter.RED_ONLY,
          activeBg = redFg,
          activeFg = Color.White,
          onClick = { statusFilter = CalendarStatusFilter.RED_ONLY },
          modifier = Modifier.weight(1f).testTag("calendar_status_red"),
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Weekday Headers
      val weekHeaders = remember(state.language) { DateUtils.weekdayShortHeaders(state.language) }
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        weekHeaders.forEach { dayName ->
          Text(
            text = dayName,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
          )
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      // 7-Column Calendar Grid
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        gridCells.chunked(7).forEach { weekRow ->
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            weekRow.forEach { dateStr ->
              if (dateStr == null) {
                Spacer(modifier = Modifier.weight(1f).height(48.dp))
              } else {
                val eval =
                  evaluationsByDate[dateStr] ?: DayEvaluation(outcome = DayOutcome.NEUTRAL)
                val isSelected = dateStr == state.selectedDateStr
                val isToday = dateStr == todayStr
                val matchesStatusFilter =
                  when (statusFilter) {
                    CalendarStatusFilter.ALL -> true
                    CalendarStatusFilter.GREEN_ONLY -> eval.outcome == DayOutcome.GREEN
                    CalendarStatusFilter.RED_ONLY -> eval.outcome == DayOutcome.RED
                  }

                val cellBg =
                  when (eval.outcome) {
                    DayOutcome.GREEN -> greenBg
                    DayOutcome.RED -> redBg
                    DayOutcome.NEUTRAL -> MaterialTheme.colorScheme.background.copy(alpha = 0.65f)
                  }
                val cellFg =
                  when (eval.outcome) {
                    DayOutcome.GREEN -> greenFg
                    DayOutcome.RED -> redFg
                    DayOutcome.NEUTRAL -> MaterialTheme.colorScheme.onSurfaceVariant
                  }
                val borderStroke =
                  when {
                    isSelected -> BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                    eval.outcome == DayOutcome.GREEN -> BorderStroke(1.dp, greenFg.copy(alpha = 0.45f))
                    eval.outcome == DayOutcome.RED -> BorderStroke(1.dp, redFg.copy(alpha = 0.45f))
                    else -> BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                  }

                val dayNumber = dateStr.substringAfterLast("-").toIntOrNull()?.toString() ?: dateStr
                Surface(
                  shape = RoundedCornerShape(10.dp),
                  color = cellBg,
                  border = borderStroke,
                  modifier =
                    Modifier.weight(1f)
                      .height(48.dp)
                      .alpha(if (matchesStatusFilter) 1f else 0.24f)
                      .clip(RoundedCornerShape(10.dp))
                      .clickable { onSelectDate(dateStr) }
                      .testTag("calendar_day_$dateStr"),
                ) {
                  Column(
                    modifier = Modifier.padding(2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                  ) {
                    Text(
                      text = dayNumber,
                      fontSize = 13.sp,
                      fontWeight = if (isSelected || isToday) FontWeight.ExtraBold else FontWeight.SemiBold,
                      color = cellFg,
                    )
                    if (isToday) {
                      Box(
                        modifier =
                          Modifier.size(4.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                      )
                    } else if (eval.outcome != DayOutcome.NEUTRAL) {
                      Text(
                        text = if (eval.outcome == DayOutcome.GREEN) "✓" else "✕",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = cellFg,
                      )
                    }
                  }
                }
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Legend
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        LegendDot(color = greenFg, label = s.calendarLegendGreen)
        LegendDot(color = redFg, label = s.calendarLegendRed)
        LegendDot(color = MaterialTheme.colorScheme.outline, label = s.calendarLegendEmpty)
      }
    }

    // 2. Selected Day Complete Details Across All Blocks
    val selectedEval =
      CalendarLogic.evaluateDay(
        state.allRecords[state.selectedDateStr],
        state.settings,
        selectedFilter,
        s,
      )
    CalendarDayDetailsCard(
      dateStr = state.selectedDateStr,
      day = state.allRecords[state.selectedDateStr],
      settings = state.settings,
      evaluation = selectedEval,
      activeBlock = selectedBlock,
      isDarkTheme = isDark,
      strings = s,
      onOpenInToday = { onOpenDateInToday(state.selectedDateStr) },
    )

    // 3. Sorted List of Month Days (by Green/Red/Newest)
    val sortedDayEntries =
      remember(monthDays, evaluationsByDate, statusFilter, sortOrder) {
        val filtered =
          monthDays.mapNotNull { date ->
            val eval = evaluationsByDate[date] ?: return@mapNotNull null
            val include =
              when (statusFilter) {
                CalendarStatusFilter.ALL -> eval.outcome != DayOutcome.NEUTRAL
                CalendarStatusFilter.GREEN_ONLY -> eval.outcome == DayOutcome.GREEN
                CalendarStatusFilter.RED_ONLY -> eval.outcome == DayOutcome.RED
              }
            if (include) date to eval else null
          }
        when (sortOrder) {
          CalendarSortOrder.RED_FIRST ->
            filtered.sortedWith(
              compareBy<Pair<String, DayEvaluation>> {
                when (it.second.outcome) {
                  DayOutcome.RED -> 0
                  DayOutcome.GREEN -> 1
                  DayOutcome.NEUTRAL -> 2
                }
              }.thenByDescending { it.first }
            )
          CalendarSortOrder.GREEN_FIRST ->
            filtered.sortedWith(
              compareBy<Pair<String, DayEvaluation>> {
                when (it.second.outcome) {
                  DayOutcome.GREEN -> 0
                  DayOutcome.RED -> 1
                  DayOutcome.NEUTRAL -> 2
                }
              }.thenByDescending { it.first }
            )
          CalendarSortOrder.DATE_DESC -> filtered.sortedByDescending { it.first }
        }
      }

    SectionCard {
      Text(
        text = s.calendarMonthlyListTitle,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        FilterChipPill(
          label = s.calendarSortRedFirst,
          selected = sortOrder == CalendarSortOrder.RED_FIRST,
          onClick = { sortOrder = CalendarSortOrder.RED_FIRST },
        )
        FilterChipPill(
          label = s.calendarSortGreenFirst,
          selected = sortOrder == CalendarSortOrder.GREEN_FIRST,
          onClick = { sortOrder = CalendarSortOrder.GREEN_FIRST },
        )
        FilterChipPill(
          label = s.calendarSortNewest,
          selected = sortOrder == CalendarSortOrder.DATE_DESC,
          onClick = { sortOrder = CalendarSortOrder.DATE_DESC },
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      if (sortedDayEntries.isEmpty()) {
        Text(
          text = s.calendarNoMatchingDays,
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      } else {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          sortedDayEntries.forEach { (dateStr, eval) ->
            val rowBg = if (eval.outcome == DayOutcome.GREEN) greenBg else redBg
            val rowFg = if (eval.outcome == DayOutcome.GREEN) greenFg else redFg
            val isSelected = dateStr == state.selectedDateStr

            Row(
              modifier =
                Modifier.fillMaxWidth()
                  .clip(RoundedCornerShape(10.dp))
                  .background(rowBg.copy(alpha = 0.65f))
                  .border(
                    width = if (isSelected) 1.5.dp else 1.dp,
                    color =
                      if (isSelected) MaterialTheme.colorScheme.primary
                      else rowFg.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(10.dp),
                  )
                  .clickable { onSelectDate(dateStr) }
                  .padding(horizontal = 12.dp, vertical = 10.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
              ) {
                Box(
                  modifier =
                    Modifier.size(8.dp)
                      .clip(CircleShape)
                      .background(rowFg)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                  Text(
                    text = DateUtils.formatDisplayDate(dateStr, state.language),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                  )
                  if (eval.reason.isNotEmpty()) {
                    Text(
                      text = eval.reason,
                      fontSize = 10.sp,
                      color = rowFg,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis,
                    )
                  }
                }
              }
              if (eval.summaryBadge.isNotEmpty()) {
                Text(
                  text = eval.summaryBadge,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.ExtraBold,
                  color = rowFg,
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
private fun FilterChipPill(
  label: String,
  selected: Boolean,
  onClick: () -> Unit,
  testTag: String? = null,
) {
  Surface(
    shape = RoundedCornerShape(10.dp),
    color =
      if (selected) MaterialTheme.colorScheme.primary
      else MaterialTheme.colorScheme.background.copy(alpha = 0.7f),
    border =
      BorderStroke(
        1.dp,
        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
      ),
    modifier =
      Modifier.let { m -> if (testTag != null) m.testTag(testTag) else m }
        .clip(RoundedCornerShape(10.dp))
        .clickable(onClick = onClick),
  ) {
    Text(
      text = label,
      fontSize = 11.sp,
      fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
      color =
        if (selected) MaterialTheme.colorScheme.onPrimary
        else MaterialTheme.colorScheme.onSurface,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
    )
  }
}

@Composable
private fun StatusFilterPill(
  label: String,
  selected: Boolean,
  activeBg: Color,
  activeFg: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    shape = RoundedCornerShape(10.dp),
    color = if (selected) activeBg else MaterialTheme.colorScheme.background.copy(alpha = 0.7f),
    border =
      BorderStroke(
        1.dp,
        if (selected) activeBg else MaterialTheme.colorScheme.outlineVariant,
      ),
    modifier = modifier.clip(RoundedCornerShape(10.dp)).clickable(onClick = onClick),
  ) {
    Text(
      text = label,
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      textAlign = TextAlign.Center,
      color = if (selected) activeFg else MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
    )
  }
}

@Composable
private fun LegendDot(color: Color, label: String) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
    Spacer(modifier = Modifier.width(5.dp))
    Text(
      text = label,
      fontSize = 10.sp,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}
