package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AdhkarKey
import com.example.data.model.AnalysisCategory
import com.example.data.model.NaflKey
import com.example.data.model.PrayerKey
import com.example.data.model.PrayerQuality
import com.example.ui.MuhasabaUiState
import com.example.ui.components.NumericCompactInput
import com.example.ui.components.SectionCard
import com.example.ui.i18n.AppStrings
import com.example.ui.theme.DarkAmber
import com.example.ui.theme.SummaryDarkEnd
import com.example.ui.theme.SummaryDarkStart
import com.example.util.DateUtils

@Composable
fun TodayScreen(
  state: MuhasabaUiState,
  onSetFasting: (Boolean) -> Unit,
  onSelectPrayer: (PrayerKey) -> Unit,
  onTogglePrayerStatus: () -> Unit,
  onSetPrayerDelay: (Int) -> Unit,
  onToggleRawatib: () -> Unit,
  onTogglePrayerQuality: (PrayerQuality) -> Unit,
  onSetPrayerExtraRakats: (Int) -> Unit,
  onToggleNafl: (NaflKey) -> Unit,
  onSetNaflCount: (NaflKey, Int) -> Unit,
  onUpdateQuran: (pages: Int?, reflect: Int?, lines: Int?) -> Unit,
  onToggleAdhkar: (AdhkarKey) -> Unit,
  onUpdateDhikr: (istighfar: Int?, salawat: Int?, noDhikr: Int?, noTaharah: Int?) -> Unit,
  onToggleTruth: () -> Unit,
  onTogglePatience: () -> Unit,
  onUpdateBookPages: (String, Int) -> Unit,
  onGoToBookSettings: () -> Unit,
  onSetSelfAnalysisAnswer: (String, Boolean) -> Unit,
) {
  val s = state.strings
  val day = state.currentDay
  val yesterday = state.yesterdayDay

  Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
    // BLOCK 1: RAKATS, FASTING, 5 PRAYERS, NAFL
    PrayerAndNaflSection(
      state = state,
      onSetFasting = onSetFasting,
      onSelectPrayer = onSelectPrayer,
      onTogglePrayerStatus = onTogglePrayerStatus,
      onSetPrayerDelay = onSetPrayerDelay,
      onToggleRawatib = onToggleRawatib,
      onTogglePrayerQuality = onTogglePrayerQuality,
      onSetPrayerExtraRakats = onSetPrayerExtraRakats,
      onToggleNafl = onToggleNafl,
      onSetNaflCount = onSetNaflCount,
    )

    // BLOCK 2: QURAN
    SectionCard {
      SectionHeader(icon = Icons.Filled.Book, title = s.quranSectionTitle)
      Spacer(modifier = Modifier.height(12.dp))
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        LabeledNumberRow(
          label = s.quranPagesLabel,
          value = day.quran.pages,
          onValueChange = { onUpdateQuran(it, null, null) },
          testTag = "quran_pages_input",
        )
        LabeledNumberRow(
          label = s.quranReflectLabel,
          value = day.quran.reflectPages,
          onValueChange = { onUpdateQuran(null, it, null) },
        )
        LabeledNumberRow(
          label = s.quranLinesLabel,
          value = day.quran.memorizedLines,
          onValueChange = { onUpdateQuran(null, null, it) },
        )
      }
      Spacer(modifier = Modifier.height(10.dp))
      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = String.format(s.quranYesterdayFormat, yesterday.quran.pages, yesterday.quran.memorizedLines),
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }

    // BLOCK 3: ADHKAR STATUS
    SectionCard {
      SectionHeader(icon = Icons.Filled.AutoAwesome, title = s.adhkarSectionTitle)
      Spacer(modifier = Modifier.height(12.dp))
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          AdhkarTile(
            title = s.adhkarWake,
            done = day.adhkar.wake,
            strings = s,
            onClick = { onToggleAdhkar(AdhkarKey.WAKE) },
            modifier = Modifier.weight(1f),
          )
          AdhkarTile(
            title = s.adhkarMorning,
            done = day.adhkar.morning,
            strings = s,
            onClick = { onToggleAdhkar(AdhkarKey.MORNING) },
            modifier = Modifier.weight(1f),
          )
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          AdhkarTile(
            title = s.adhkarEvening,
            done = day.adhkar.evening,
            strings = s,
            onClick = { onToggleAdhkar(AdhkarKey.EVENING) },
            modifier = Modifier.weight(1f),
          )
          AdhkarTile(
            title = s.adhkarSleep,
            done = day.adhkar.sleep,
            strings = s,
            onClick = { onToggleAdhkar(AdhkarKey.SLEEP) },
            modifier = Modifier.weight(1f),
          )
        }
      }
    }

    // BLOCK 4: DHIKR & VIGILANCE
    SectionCard {
      SectionHeader(icon = Icons.Filled.MonitorHeart, title = s.dhikrSectionTitle)
      Spacer(modifier = Modifier.height(12.dp))
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        LabeledNumberRow(
          label = s.istighfarLabel,
          value = day.dhikr.istighfar,
          onValueChange = { onUpdateDhikr(it, null, null, null) },
          width = 88.dp,
        )
        LabeledNumberRow(
          label = s.salawatLabel,
          value = day.dhikr.salawat,
          onValueChange = { onUpdateDhikr(null, it, null, null) },
          width = 88.dp,
        )
        LabeledNumberRow(
          label = s.noDhikrLabel,
          value = day.dhikr.noDhikrCount,
          onValueChange = { onUpdateDhikr(null, null, it, null) },
        )
        LabeledNumberRow(
          label = s.noTaharahLabel,
          value = day.dhikr.noTaharahCount,
          onValueChange = { onUpdateDhikr(null, null, null, it) },
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            s.truthQuestion,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f).padding(end = 8.dp),
          )
          StatusPillButton(
            text = if (day.dhikr.truth) s.yesCheck else s.noCross,
            selected = day.dhikr.truth,
            onClick = onToggleTruth,
          )
        }
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            s.patienceQuestion,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f).padding(end = 8.dp),
          )
          StatusPillButton(
            text = if (day.dhikr.patience) s.yesCheck else s.noCross,
            selected = day.dhikr.patience,
            onClick = onTogglePatience,
          )
        }
      }
    }

    // BLOCK 5: BOOKS & READING PLAN
    SectionCard {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        SectionHeader(icon = Icons.Filled.LocalLibrary, title = s.booksSectionTitle)
        Text(
          text = s.editBooksLink,
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold,
          textDecoration = TextDecoration.Underline,
          color = MaterialTheme.colorScheme.primary,
          modifier =
            Modifier.clip(RoundedCornerShape(6.dp))
              .clickable(onClick = onGoToBookSettings)
              .padding(horizontal = 6.dp, vertical = 4.dp)
              .testTag("edit_books_link"),
        )
      }
      Spacer(modifier = Modifier.height(12.dp))

      if (state.settings.books.isEmpty()) {
        Text(
          text = s.noBooksConfigured,
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      } else {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          state.settings.books.forEach { book ->
            val readToday = day.bookPages[book.id] ?: 0
            val plan = book.dailyPlan.coerceAtLeast(1)
            val progress = (readToday.toFloat() / plan.toFloat()).coerceIn(0f, 1f)
            val pct = (progress * 100).toInt()
            val completed = readToday >= plan

            Column(
              modifier =
                Modifier.fillMaxWidth()
                  .clip(RoundedCornerShape(12.dp))
                  .background(MaterialTheme.colorScheme.background.copy(alpha = 0.6f))
                  .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                  .padding(12.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    s.defaultBookTitleOrCustom(book.id, book.title),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                  )
                  Text(
                    String.format(s.bookPlanFormat, plan),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                  NumericCompactInput(
                    value = readToday,
                    onValueChange = { onUpdateBookPages(book.id, it) },
                    width = 64.dp,
                  )
                  Text(s.pagesShort, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
              }
              LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(999.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.outline,
              )
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                Text(
                  text =
                    if (completed) s.bookPlanDone
                    else String.format(s.bookPlanRemainingFormat, (plan - readToday).coerceAtLeast(0)),
                  fontSize = 10.sp,
                  fontWeight = if (completed) FontWeight.Bold else FontWeight.Normal,
                  color =
                    if (completed) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                  text = "$pct%",
                  fontSize = 10.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))
      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
      Spacer(modifier = Modifier.height(8.dp))
      val totalBooksToday = day.totalBookPages()
      val totalBooksYesterday = yesterday.totalBookPages()
      val bookDiff = totalBooksToday - totalBooksYesterday
      val bookDiffStr = if (bookDiff >= 0) "+$bookDiff" else "$bookDiff"
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Text(s.totalBooksTodayLabel, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Text(
          "$totalBooksToday ${s.pagesShort}",
          fontSize = 12.sp,
          fontWeight = FontWeight.ExtraBold,
          color = MaterialTheme.colorScheme.primary,
        )
      }
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        String.format(s.booksYesterdayCompareFormat, totalBooksYesterday, bookDiffStr),
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }

    // BLOCK 6: SELF ANALYSIS
    SectionCard {
      SectionHeader(icon = Icons.Filled.VerifiedUser, title = s.selfAnalysisSectionTitle)
      Spacer(modifier = Modifier.height(12.dp))
      val questions = state.settings.questions
      if (questions.isEmpty()) {
        Text(
          s.noQuestionsConfigured,
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      } else {
        val limbs = questions.filter { it.cat == AnalysisCategory.LIMBS }
        val heart = questions.filter { it.cat == AnalysisCategory.HEART }
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          if (limbs.isNotEmpty()) {
            Text(
              s.categoryLimbsHeader,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            limbs.forEach { q ->
              SelfAnalysisRow(
                text = s.defaultQuestionTextOrCustom(q.id, q.text),
                answer = day.selfAnalysis[q.id],
                strings = s,
                onSelectAnswer = { safe -> onSetSelfAnalysisAnswer(q.id, safe) },
              )
            }
          }
          if (heart.isNotEmpty()) {
            if (limbs.isNotEmpty()) {
              HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
            Text(
              s.categoryHeartHeader,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            heart.forEach { q ->
              SelfAnalysisRow(
                text = s.defaultQuestionTextOrCustom(q.id, q.text),
                answer = day.selfAnalysis[q.id],
                strings = s,
                onSelectAnswer = { safe -> onSetSelfAnalysisAnswer(q.id, safe) },
              )
            }
          }
        }
      }
    }

    // BLOCK 7: DAILY SUMMARY DARK CARD
    Box(
      modifier =
        Modifier.fillMaxWidth()
          .clip(RoundedCornerShape(18.dp))
          .background(Brush.linearGradient(listOf(SummaryDarkStart, SummaryDarkEnd)))
          .padding(16.dp)
    ) {
      Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Filled.AssignmentTurnedIn,
            contentDescription = null,
            tint = DarkAmber,
            modifier = Modifier.size(18.dp),
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = s.dailySummaryHeader,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = DarkAmber,
          )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          SummaryMiniTile(s.summaryRakats, state.todayRakats.total.toString(), Modifier.weight(1f))
          SummaryMiniTile(s.summaryBooks, day.totalBookPages().toString(), Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          SummaryMiniTile(
            s.summaryQuran,
            "${day.quran.pages} / ${day.quran.memorizedLines}",
            Modifier.weight(1f),
          )
          SummaryMiniTile(
            s.summaryDhikr,
            "${day.dhikr.istighfar} / ${day.dhikr.salawat}",
            Modifier.weight(1f),
          )
        }
        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.12f))
        Spacer(modifier = Modifier.height(10.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(s.summaryCarelessness, fontSize = 12.sp, color = Color(0xFFCBD5E1))
          Text(
            text = DateUtils.formatMsToHHMMSS(day.totalCarelessnessMs()),
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFCD34D),
          )
        }
      }
    }
  }
}

@Composable
private fun SectionHeader(icon: ImageVector, title: String) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.primary,
      modifier = Modifier.size(18.dp),
    )
    Spacer(modifier = Modifier.width(8.dp))
    Text(
      text = title,
      fontSize = 14.sp,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurface,
    )
  }
}

@Composable
private fun LabeledNumberRow(
  label: String,
  value: Int,
  onValueChange: (Int) -> Unit,
  width: androidx.compose.ui.unit.Dp = 76.dp,
  testTag: String? = null,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = label,
      fontSize = 12.sp,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.weight(1f).padding(end = 8.dp),
    )
    NumericCompactInput(
      value = value,
      onValueChange = onValueChange,
      width = width,
      modifier = if (testTag != null) Modifier.testTag(testTag) else Modifier,
    )
  }
}

@Composable
private fun AdhkarTile(
  title: String,
  done: Boolean,
  strings: AppStrings,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color =
      if (done) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
      else MaterialTheme.colorScheme.background.copy(alpha = 0.6f),
    border =
      BorderStroke(
        1.dp,
        if (done) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
      ),
    modifier = modifier.clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick),
  ) {
    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
      Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Text(
        text = if (done) strings.adhkarDone else strings.adhkarUndone,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color =
          if (done) MaterialTheme.colorScheme.primary
          else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
      )
    }
  }
}

@Composable
private fun SelfAnalysisRow(
  text: String,
  answer: Boolean?,
  strings: AppStrings,
  onSelectAnswer: (Boolean) -> Unit,
) {
  Column(
    modifier =
      Modifier.fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.6f))
        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
        .padding(horizontal = 12.dp, vertical = 10.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    Text(
      text = text,
      fontSize = 12.sp,
      fontWeight = FontWeight.Medium,
      color = MaterialTheme.colorScheme.onSurface,
    )
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      val isSafe = answer == true
      val isSinned = answer == false

      Surface(
        shape = RoundedCornerShape(10.dp),
        color =
          if (isSafe) MaterialTheme.colorScheme.primary
          else MaterialTheme.colorScheme.surface,
        border =
          BorderStroke(
            1.dp,
            if (isSafe) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outline,
          ),
        modifier =
          Modifier.weight(1f)
            .clip(RoundedCornerShape(10.dp))
            .clickable { onSelectAnswer(true) },
      ) {
        Text(
          text = strings.analysisSafe,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          textAlign = TextAlign.Center,
          color =
            if (isSafe) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 9.dp),
        )
      }

      Surface(
        shape = RoundedCornerShape(10.dp),
        color =
          if (isSinned) MaterialTheme.colorScheme.error
          else MaterialTheme.colorScheme.surface,
        border =
          BorderStroke(
            1.dp,
            if (isSinned) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.outline,
          ),
        modifier =
          Modifier.weight(1f)
            .clip(RoundedCornerShape(10.dp))
            .clickable { onSelectAnswer(false) },
      ) {
        Text(
          text = strings.analysisMistake,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          textAlign = TextAlign.Center,
          color =
            if (isSinned) MaterialTheme.colorScheme.onError
            else MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 9.dp),
        )
      }
    }
  }
}

@Composable
private fun SummaryMiniTile(label: String, value: String, modifier: Modifier = Modifier) {
  Column(
    modifier =
      modifier
        .clip(RoundedCornerShape(12.dp))
        .background(Color.White.copy(alpha = 0.1f))
        .padding(10.dp)
  ) {
    Text(label, fontSize = 10.sp, color = Color(0xFF94A3B8))
    Spacer(modifier = Modifier.height(2.dp))
    Text(value, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
  }
}
