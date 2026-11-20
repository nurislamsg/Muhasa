package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.DailyRakatPoint
import com.example.ui.MuhasabaUiState
import com.example.ui.components.SectionCard
import com.example.util.DateUtils
import java.util.Locale

@Composable
fun ReportsScreen(
  state: MuhasabaUiState,
  onSelectPeriod: (Int) -> Unit,
) {
  val s = state.strings
  val report =
    remember(
      state.allRecords,
      state.selectedDateStr,
      state.reportPeriodDays,
      state.settings,
      state.language,
    ) {
      state.computeReports()
    }

  Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
    // Period Switcher
    Surface(
      shape = RoundedCornerShape(14.dp),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 1.dp,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Row(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
        PeriodButton(
          label = s.reportPeriodWeek,
          selected = state.reportPeriodDays == 7,
          onClick = { onSelectPeriod(7) },
          modifier = Modifier.weight(1f).testTag("report_period_7"),
        )
        PeriodButton(
          label = s.reportPeriodMonth,
          selected = state.reportPeriodDays == 30,
          onClick = { onSelectPeriod(30) },
          modifier = Modifier.weight(1f).testTag("report_period_30"),
        )
      }
    }

    // Block 1: Today vs Yesterday
    SectionCard {
      Text(
        text = s.reportTodayVsYesterday,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Spacer(modifier = Modifier.height(10.dp))
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        CompareTile(
          label = s.reportRakatsLabel,
          value = report.todayRakats.toString(),
          sub = String.format(s.reportVsYesterdayFormat, report.yesterdayRakats),
          modifier = Modifier.weight(1f),
        )
        CompareTile(
          label = s.reportBooksLabel,
          value = report.todayBooks.toString(),
          sub = String.format(s.reportVsYesterdayFormat, report.yesterdayBooks),
          modifier = Modifier.weight(1f),
        )
      }
      Spacer(modifier = Modifier.height(8.dp))
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        CompareTile(
          label = "${s.summaryQuran}:",
          value = "${report.todayQuranPages} / ${report.todayQuranLines}",
          sub = null,
          modifier = Modifier.weight(1f),
        )
        CompareTile(
          label = "${s.summaryDhikr}:",
          value = "${report.todayIstighfar} / ${report.todaySalawat}",
          sub = null,
          modifier = Modifier.weight(1f),
        )
      }
    }

    // Block 2: Period Summary & Charts
    SectionCard {
      Text(
        text = s.reportPeriodSummaryHeader,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Spacer(modifier = Modifier.height(10.dp))

      // Rakat Analytics + Bar Chart
      Column(
        modifier =
          Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.65f))
            .padding(12.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.Top,
        ) {
          Column {
            Text(
              s.reportRakatsDone,
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
              text = report.totalRakats.toString(),
              fontSize = 22.sp,
              fontWeight = FontWeight.ExtraBold,
              color = MaterialTheme.colorScheme.primary,
            )
          }
          Column(horizontalAlignment = Alignment.End) {
            val avgStr = String.format(Locale.US, "%.1f", report.avgRakatsPerDay)
            Text(
              String.format(s.reportAvgPerDayFormat, avgStr),
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            val diffStr =
              if (report.diffVsPrevPeriod >= 0) "+${report.diffVsPrevPeriod}"
              else "${report.diffVsPrevPeriod}"
            Text(
              String.format(s.reportVsPrevPeriodFormat, diffStr),
              fontSize = 10.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
          s.reportRakatDynamics,
          fontSize = 10.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(6.dp))
        RakatBarChart(series = report.dailyRakatSeries)
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Dhikr Cards
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Column(
          modifier =
            Modifier.weight(1f)
              .clip(RoundedCornerShape(12.dp))
              .background(MaterialTheme.colorScheme.background.copy(alpha = 0.65f))
              .padding(12.dp)
        ) {
          Text(
            s.reportIstighfarTotal,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Text("${report.totalIstighfar}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
          Text(
            String.format(
              s.reportPerDayIntFormat,
              String.format(Locale.US, "%.0f", report.avgIstighfarPerDay),
            ),
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        Column(
          modifier =
            Modifier.weight(1f)
              .clip(RoundedCornerShape(12.dp))
              .background(MaterialTheme.colorScheme.background.copy(alpha = 0.65f))
              .padding(12.dp)
        ) {
          Text(
            s.reportSalawatTotal,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Text("${report.totalSalawat}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
          Text(
            String.format(
              s.reportPerDayIntFormat,
              String.format(Locale.US, "%.0f", report.avgSalawatPerDay),
            ),
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Books & Quran Summary
      Column(
        modifier =
          Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.65f))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        Text(s.reportBooksAndQuran, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        val bookAvgStr = String.format(Locale.US, "%.1f", report.avgBookPagesPerDay)
        val quranAvgStr = String.format(Locale.US, "%.1f", report.avgQuranPagesPerDay)
        StatRow(
          s.reportBookPagesRow,
          "${report.totalBookPages} ($bookAvgStr)",
        )
        StatRow(
          s.reportQuranPagesRow,
          "${report.totalQuranPages} ($quranAvgStr)",
        )
        StatRow(
          s.reportQuranReflectRow,
          String.format(s.reportPagesUnitFormat, report.totalQuranReflect),
        )
        StatRow(
          s.reportQuranLinesRow,
          String.format(s.reportLinesUnitFormat, report.totalQuranLines),
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Carelessness Period Summary
      Row(
        modifier =
          Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.65f))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column {
          Text(
            s.reportCarelessnessPeriod,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Text(
            text = DateUtils.formatMsToHHMMSS(report.totalCarelessnessMs),
            fontSize = 15.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.tertiary,
          )
        }
        val avgShort = DateUtils.formatMsToHHMMSS(report.avgCarelessnessMsPerDay).substring(0, 5)
        Text(
          text = String.format(s.reportAvgShortPerDayFormat, avgShort),
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }

    // Block 3: Weak Spots & Prayer Quality
    SectionCard {
      Text(
        text = s.reportWeakSpotsHeader,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Spacer(modifier = Modifier.height(10.dp))

      Text(s.reportTopMistakesLabel, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
      Spacer(modifier = Modifier.height(6.dp))
      if (report.topMistakes.isEmpty()) {
        Text(
          text = s.reportNoMistakes,
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      } else {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          report.topMistakes.forEach { m ->
            Row(
              modifier =
                Modifier.fillMaxWidth()
                  .clip(RoundedCornerShape(10.dp))
                  .background(MaterialTheme.colorScheme.background.copy(alpha = 0.65f))
                  .padding(horizontal = 10.dp, vertical = 8.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = m.questionText,
                fontSize = 12.sp,
                modifier = Modifier.weight(1f).padding(end = 8.dp),
              )
              Box(
                modifier =
                  Modifier.clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
              ) {
                Text(
                  text = String.format(s.reportMistakeCountFormat, m.count),
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.error,
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))
      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
      Spacer(modifier = Modifier.height(10.dp))

      Text(s.reportPrayerQualityLabel, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
      Spacer(modifier = Modifier.height(8.dp))
      QualityProgressBar(s.reportQualityOntime, report.ontimePct)
      Spacer(modifier = Modifier.height(8.dp))
      QualityProgressBar(s.reportQualityRawatib, report.rawatibPct)
      Spacer(modifier = Modifier.height(8.dp))
      QualityProgressBar(s.reportQualityKhushu, report.khushuPct)
    }
  }
}

@Composable
private fun PeriodButton(
  label: String,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier =
      modifier
        .clip(RoundedCornerShape(10.dp))
        .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
        .clickable(onClick = onClick)
        .padding(vertical = 10.dp),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = label,
      fontSize = 12.sp,
      fontWeight = FontWeight.Bold,
      color =
        if (selected) MaterialTheme.colorScheme.onPrimary
        else MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}

@Composable
private fun CompareTile(
  label: String,
  value: String,
  sub: String?,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier =
      modifier
        .clip(RoundedCornerShape(12.dp))
        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.65f))
        .padding(10.dp)
  ) {
    Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(modifier = Modifier.height(2.dp))
    Row(verticalAlignment = Alignment.Bottom) {
      Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold)
      if (sub != null) {
        Text(
          "  $sub",
          fontSize = 10.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}

@Composable
private fun RakatBarChart(series: List<DailyRakatPoint>) {
  val maxVal = (series.maxOfOrNull { it.total } ?: 0).coerceAtLeast(1)
  Column {
    Row(
      modifier = Modifier.fillMaxWidth().height(84.dp),
      horizontalArrangement = Arrangement.spacedBy(2.dp),
      verticalAlignment = Alignment.Bottom,
    ) {
      series.forEach { point ->
        val frac = (point.total.toFloat() / maxVal.toFloat()).coerceIn(0f, 1f)
        Column(
          modifier = Modifier.weight(1f).fillMaxHeight(),
          verticalArrangement = Arrangement.Bottom,
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          if (series.size <= 10 || point.total > 0) {
            Text(
              text = point.total.toString(),
              fontSize = 8.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center,
            )
          }
          Box(
            modifier =
              Modifier.fillMaxWidth(0.8f)
                .fillMaxHeight(frac.coerceAtLeast(0.03f))
                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                .background(
                  if (point.total > 0) MaterialTheme.colorScheme.primary
                  else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
          )
        }
      }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
  }
}

@Composable
private fun StatRow(label: String, value: String) {
  Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
    Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold)
  }
}

@Composable
private fun QualityProgressBar(label: String, pct: Int) {
  Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
      Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
      Text("$pct%", fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
    LinearProgressIndicator(
      progress = { (pct / 100f).coerceIn(0f, 1f) },
      modifier = Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(999.dp)),
      color = MaterialTheme.colorScheme.primary,
      trackColor = MaterialTheme.colorScheme.outline,
    )
  }
}
