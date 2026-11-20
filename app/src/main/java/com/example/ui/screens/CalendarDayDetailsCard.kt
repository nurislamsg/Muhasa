package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppSettings
import com.example.data.model.DayRecord
import com.example.data.model.PrayerEntry
import com.example.data.model.PrayerKey
import com.example.ui.calendar.CalendarBlock
import com.example.ui.calendar.CalendarLogic.hasAnyActivity
import com.example.ui.calendar.DayEvaluation
import com.example.ui.calendar.DayOutcome
import com.example.ui.components.SectionCard
import com.example.ui.i18n.AppStrings
import com.example.util.DateUtils

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CalendarDayDetailsCard(
  dateStr: String,
  day: DayRecord?,
  settings: AppSettings,
  evaluation: DayEvaluation,
  activeBlock: CalendarBlock,
  isDarkTheme: Boolean,
  strings: AppStrings,
  onOpenInToday: () -> Unit,
) {
  val hasData = day != null && day.hasAnyActivity()
  val safeDay = day ?: DayRecord(dateStr = dateStr)

  val greenBg = if (isDarkTheme) Color(0xFF14532D) else Color(0xFFDCFCE7)
  val greenFg = if (isDarkTheme) Color(0xFF4ADE80) else Color(0xFF15803D)
  val redBg = if (isDarkTheme) Color(0xFF450A0A) else Color(0xFFFEE2E2)
  val redFg = if (isDarkTheme) Color(0xFFF87171) else Color(0xFFDC2626)
  val neutralBg = MaterialTheme.colorScheme.surfaceVariant
  val neutralFg = MaterialTheme.colorScheme.onSurfaceVariant

  SectionCard {
    // Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
        Text(
          text = strings.calendarSelectedDayDetailsTitle,
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = DateUtils.formatDisplayDate(dateStr, strings.lang),
          fontSize = 15.sp,
          fontWeight = FontWeight.ExtraBold,
          color = MaterialTheme.colorScheme.onSurface,
        )
      }
      Button(
        onClick = onOpenInToday,
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        colors =
          ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
          ),
        modifier = Modifier.testTag("calendar_open_day_button"),
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.OpenInNew,
          contentDescription = null,
          modifier = Modifier.size(14.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(strings.calendarOpenInTodayButton, fontSize = 11.sp, fontWeight = FontWeight.Bold)
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Day Status Banner
    val (statusBg, statusFg, statusText) =
      when (evaluation.outcome) {
        DayOutcome.GREEN -> Triple(greenBg, greenFg, strings.calendarDayStatusGreen)
        DayOutcome.RED -> Triple(redBg, redFg, strings.calendarDayStatusRed)
        DayOutcome.NEUTRAL -> Triple(neutralBg, neutralFg, strings.calendarDayStatusEmpty)
      }

    Row(
      modifier =
        Modifier.fillMaxWidth()
          .clip(RoundedCornerShape(10.dp))
          .background(statusBg)
          .padding(horizontal = 12.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(statusText, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = statusFg)
        if (evaluation.reason.isNotEmpty() && evaluation.outcome != DayOutcome.NEUTRAL) {
          Text(evaluation.reason, fontSize = 10.sp, color = statusFg.copy(alpha = 0.85f))
        }
      }
      if (evaluation.summaryBadge.isNotEmpty()) {
        Text(
          text = evaluation.summaryBadge,
          fontSize = 12.sp,
          fontWeight = FontWeight.ExtraBold,
          color = statusFg,
        )
      }
    }

    if (!hasData) {
      return@SectionCard
    }

    Spacer(modifier = Modifier.height(12.dp))

    // 1. Prayers & Fasting
    BlockDetailContainer(
      title = strings.calendarBlockPrayers,
      highlighted = activeBlock == CalendarBlock.PRAYERS,
    ) {
      val fastingPillOutcome =
        when (safeDay.fasting) {
          true -> DayOutcome.GREEN
          false -> DayOutcome.RED
          null -> DayOutcome.NEUTRAL
        }
      val fastingText =
        when (safeDay.fasting) {
          true -> "${strings.calendarItemFasting}: ${strings.yesCheck}"
          false -> "${strings.calendarItemFasting}: ${strings.noCross}"
          null -> "${strings.calendarItemFasting}: —"
        }
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        DetailStatusChip(fastingText, fastingPillOutcome, isDarkTheme)
        PrayerKey.entries.forEach { pKey ->
          val p = safeDay.prayers[pKey] ?: PrayerEntry()
          val name = strings.prayerName(pKey)
          val pText =
            if (p.status) {
              val rawLabel = if (p.rawatibRead) " · R✓" else ""
              val khushuLabel = if (p.khushu && p.tumaninah) " · X✓" else ""
              "$name ✓ (${p.delayMinutes}m$rawLabel$khushuLabel)"
            } else {
              "$name ✕"
            }
          DetailStatusChip(
            text = pText,
            outcome = if (p.status) DayOutcome.GREEN else DayOutcome.RED,
            isDarkTheme = isDarkTheme,
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // 2. Nafl
    BlockDetailContainer(
      title = strings.calendarBlockNafl,
      highlighted = activeBlock == CalendarBlock.NAFL,
    ) {
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        DetailStatusChip(
          "${strings.naflDuha}: ${if (safeDay.nafl.duha.read) "${safeDay.nafl.duha.count} ${strings.rakShort}" else "✕"}",
          if (safeDay.nafl.duha.read) DayOutcome.GREEN else DayOutcome.RED,
          isDarkTheme,
        )
        DetailStatusChip(
          "${strings.naflTahajjud}: ${if (safeDay.nafl.tahajjud.read) "${safeDay.nafl.tahajjud.count} ${strings.rakShort}" else "✕"}",
          if (safeDay.nafl.tahajjud.read) DayOutcome.GREEN else DayOutcome.RED,
          isDarkTheme,
        )
        DetailStatusChip(
          "${strings.naflTauba}: ${if (safeDay.nafl.tauba.read) "${safeDay.nafl.tauba.count} ${strings.rakShort}" else "✕"}",
          if (safeDay.nafl.tauba.read) DayOutcome.GREEN else DayOutcome.RED,
          isDarkTheme,
        )
        DetailStatusChip(
          "${strings.naflWitr}: ${if (safeDay.nafl.witrRead) "3 ${strings.rakShort}" else "✕"}",
          if (safeDay.nafl.witrRead) DayOutcome.GREEN else DayOutcome.RED,
          isDarkTheme,
        )
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // 3. Quran & Adhkar
    BlockDetailContainer(
      title = "${strings.calendarBlockQuran} · ${strings.calendarBlockAdhkar}",
      highlighted = activeBlock == CalendarBlock.QURAN || activeBlock == CalendarBlock.ADHKAR,
    ) {
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        DetailStatusChip(
          "${strings.calendarItemQuranRead}: ${safeDay.quran.pages}",
          if (safeDay.quran.pages > 0) DayOutcome.GREEN else DayOutcome.RED,
          isDarkTheme,
        )
        DetailStatusChip(
          "${strings.calendarItemQuranReflect}: ${safeDay.quran.reflectPages}",
          if (safeDay.quran.reflectPages > 0) DayOutcome.GREEN else DayOutcome.RED,
          isDarkTheme,
        )
        DetailStatusChip(
          "${strings.calendarItemQuranLines}: ${safeDay.quran.memorizedLines}",
          if (safeDay.quran.memorizedLines > 0) DayOutcome.GREEN else DayOutcome.RED,
          isDarkTheme,
        )
        DetailStatusChip(
          "${strings.adhkarWake}: ${if (safeDay.adhkar.wake) "✓" else "✕"}",
          if (safeDay.adhkar.wake) DayOutcome.GREEN else DayOutcome.RED,
          isDarkTheme,
        )
        DetailStatusChip(
          "${strings.adhkarMorning}: ${if (safeDay.adhkar.morning) "✓" else "✕"}",
          if (safeDay.adhkar.morning) DayOutcome.GREEN else DayOutcome.RED,
          isDarkTheme,
        )
        DetailStatusChip(
          "${strings.adhkarEvening}: ${if (safeDay.adhkar.evening) "✓" else "✕"}",
          if (safeDay.adhkar.evening) DayOutcome.GREEN else DayOutcome.RED,
          isDarkTheme,
        )
        DetailStatusChip(
          "${strings.adhkarSleep}: ${if (safeDay.adhkar.sleep) "✓" else "✕"}",
          if (safeDay.adhkar.sleep) DayOutcome.GREEN else DayOutcome.RED,
          isDarkTheme,
        )
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // 4. Dhikr & Vigilance
    BlockDetailContainer(
      title = strings.dhikrSectionTitle,
      highlighted = activeBlock == CalendarBlock.DHIKR,
    ) {
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        DetailStatusChip(
          "${strings.calendarItemIstighfar}: ${safeDay.dhikr.istighfar}",
          if (safeDay.dhikr.istighfar > 0) DayOutcome.GREEN else DayOutcome.RED,
          isDarkTheme,
        )
        DetailStatusChip(
          "${strings.calendarItemSalawat}: ${safeDay.dhikr.salawat}",
          if (safeDay.dhikr.salawat > 0) DayOutcome.GREEN else DayOutcome.RED,
          isDarkTheme,
        )
        DetailStatusChip(
          "${strings.calendarItemNoDhikr}: ${safeDay.dhikr.noDhikrCount}",
          if (safeDay.dhikr.noDhikrCount == 0) DayOutcome.GREEN else DayOutcome.RED,
          isDarkTheme,
        )
        DetailStatusChip(
          "${strings.calendarItemNoTaharah}: ${safeDay.dhikr.noTaharahCount}",
          if (safeDay.dhikr.noTaharahCount == 0) DayOutcome.GREEN else DayOutcome.RED,
          isDarkTheme,
        )
        DetailStatusChip(
          "${strings.truthQuestion}: ${if (safeDay.dhikr.truth) strings.yesCheck else strings.noCross}",
          if (safeDay.dhikr.truth) DayOutcome.GREEN else DayOutcome.RED,
          isDarkTheme,
        )
        DetailStatusChip(
          "${strings.patienceQuestion}: ${if (safeDay.dhikr.patience) strings.yesCheck else strings.noCross}",
          if (safeDay.dhikr.patience) DayOutcome.GREEN else DayOutcome.RED,
          isDarkTheme,
        )
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // 5. Books
    if (settings.books.isNotEmpty()) {
      BlockDetailContainer(
        title = strings.calendarBlockBooks,
        highlighted = activeBlock == CalendarBlock.BOOKS,
      ) {
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          settings.books.forEach { book ->
            val read = safeDay.bookPages[book.id] ?: 0
            val plan = book.dailyPlan.coerceAtLeast(1)
            val title = strings.defaultBookTitleOrCustom(book.id, book.title)
            DetailStatusChip(
              text = "$title: $read/$plan ${strings.pagesShort}",
              outcome = if (read >= plan) DayOutcome.GREEN else DayOutcome.RED,
              isDarkTheme = isDarkTheme,
            )
          }
        }
      }
      Spacer(modifier = Modifier.height(8.dp))
    }

    // 6. Self Analysis (Уберёг / Согрешил)
    if (settings.questions.isNotEmpty()) {
      BlockDetailContainer(
        title = strings.selfAnalysisSectionTitle,
        highlighted = activeBlock == CalendarBlock.ANALYSIS,
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          settings.questions.forEach { q ->
            val ans = safeDay.selfAnalysis[q.id]
            val qOutcome =
              when (ans) {
                true -> DayOutcome.GREEN
                false -> DayOutcome.RED
                null -> DayOutcome.NEUTRAL
              }
            val ansLabel =
              when (ans) {
                true -> strings.analysisSafe
                false -> strings.analysisMistake
                null -> strings.analysisUnmarked
              }
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = strings.defaultQuestionTextOrCustom(q.id, q.text),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f).padding(end = 8.dp),
              )
              DetailStatusChip(text = ansLabel, outcome = qOutcome, isDarkTheme = isDarkTheme)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
          }
        }
      }
      Spacer(modifier = Modifier.height(8.dp))
    }

    // 7. Carelessness Timer
    BlockDetailContainer(
      title = strings.calendarBlockTimer,
      highlighted = activeBlock == CalendarBlock.TIMER,
    ) {
      val ms = safeDay.totalCarelessnessMs()
      val formatted = DateUtils.formatMsToHHMMSS(ms)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = strings.summaryCarelessness,
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Box(
          modifier =
            Modifier.clip(RoundedCornerShape(8.dp))
              .background(if (ms == 0L) greenBg else redBg)
              .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Text(
            text = "$formatted (${safeDay.carelessnessSessions.size})",
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = if (ms == 0L) greenFg else redFg,
          )
        }
      }
    }
  }
}

@Composable
private fun BlockDetailContainer(
  title: String,
  highlighted: Boolean,
  content: @Composable () -> Unit,
) {
  Column(
    modifier =
      Modifier.fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.65f))
        .border(
          width = if (highlighted) 1.5.dp else 1.dp,
          color =
            if (highlighted) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outlineVariant,
          shape = RoundedCornerShape(12.dp),
        )
        .padding(10.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    Text(
      text = title,
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      color =
        if (highlighted) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurfaceVariant,
    )
    content()
  }
}

@Composable
private fun DetailStatusChip(
  text: String,
  outcome: DayOutcome,
  isDarkTheme: Boolean,
) {
  val bg =
    when (outcome) {
      DayOutcome.GREEN -> if (isDarkTheme) Color(0xFF14532D) else Color(0xFFDCFCE7)
      DayOutcome.RED -> if (isDarkTheme) Color(0xFF450A0A) else Color(0xFFFEE2E2)
      DayOutcome.NEUTRAL -> MaterialTheme.colorScheme.surfaceVariant
    }
  val fg =
    when (outcome) {
      DayOutcome.GREEN -> if (isDarkTheme) Color(0xFF4ADE80) else Color(0xFF15803D)
      DayOutcome.RED -> if (isDarkTheme) Color(0xFFF87171) else Color(0xFFDC2626)
      DayOutcome.NEUTRAL -> MaterialTheme.colorScheme.onSurfaceVariant
    }
  val borderColor =
    when (outcome) {
      DayOutcome.GREEN -> fg.copy(alpha = 0.35f)
      DayOutcome.RED -> fg.copy(alpha = 0.35f)
      DayOutcome.NEUTRAL -> MaterialTheme.colorScheme.outlineVariant
    }
  Surface(
    shape = RoundedCornerShape(8.dp),
    color = bg,
    border = BorderStroke(1.dp, borderColor),
  ) {
    Text(
      text = text,
      fontSize = 10.sp,
      fontWeight = FontWeight.SemiBold,
      color = fg,
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
    )
  }
}
