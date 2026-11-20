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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NaflKey
import com.example.data.model.PrayerEntry
import com.example.data.model.PrayerKey
import com.example.data.model.PrayerQuality
import com.example.ui.MuhasabaUiState
import com.example.ui.components.NumericCompactInput
import com.example.ui.components.SectionCard
import com.example.ui.theme.DarkEmeraldDeep
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldSecondary
import java.util.Locale

@Composable
fun PrayerAndNaflSection(
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
) {
  val s = state.strings
  val day = state.currentDay
  val rakats = state.todayRakats
  val yesterdayTotal = state.yesterdayRakatsTotal
  val diff = rakats.total - yesterdayTotal
  val diffSign = if (diff >= 0) "+$diff" else "$diff"
  val curWeek = String.format(Locale.US, "%.1f", state.currentWeekAvgRakats)
  val prevWeek = String.format(Locale.US, "%.1f", state.prevWeekAvgRakats)

  SectionCard {
    // 1. Summary Rakat Gradient Card
    val gradientColors =
      if (state.isDarkTheme) {
        listOf(DarkEmeraldDeep, EmeraldPrimary)
      } else {
        listOf(EmeraldPrimary, EmeraldSecondary)
      }

    Box(
      modifier =
        Modifier.fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .background(Brush.horizontalGradient(gradientColors))
          .padding(16.dp)
    ) {
      Column {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.Top,
        ) {
          Column {
            Text(
              text = s.rakatsForDayTitle,
              fontSize = 11.sp,
              fontWeight = FontWeight.Medium,
              color = Color(0xFFD1FAE5).copy(alpha = 0.85f),
            )
            Text(
              text = rakats.total.toString(),
              fontSize = 32.sp,
              fontWeight = FontWeight.ExtraBold,
              color = Color.White,
            )
          }
          Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(String.format(s.fardLabel, rakats.fard), fontSize = 12.sp, color = Color(0xFFD1FAE5))
            Text(String.format(s.rawatibLabel, rakats.rawatib), fontSize = 12.sp, color = Color(0xFFD1FAE5))
            Text(String.format(s.naflExtraLabel, rakats.nafl), fontSize = 12.sp, color = Color(0xFFD1FAE5))
          }
        }
        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = Color(0xFF10B981).copy(alpha = 0.35f))
        Spacer(modifier = Modifier.height(8.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = String.format(s.yesterdayRakatsCompare, yesterdayTotal, diffSign),
            fontSize = 11.sp,
            color = Color(0xFFD1FAE5),
          )
          Text(
            text = String.format(s.weeklyCompare, curWeek, prevWeek),
            fontSize = 10.sp,
            color = Color(0xFFA7F3D0),
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // 2. Daily Fasting Status
    Row(
      modifier =
        Modifier.fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(MaterialTheme.colorScheme.background.copy(alpha = 0.65f))
          .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
          .padding(horizontal = 12.dp, vertical = 10.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = s.fastingQuestion,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.weight(1f),
      )
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StatusPillButton(
          text = s.yes,
          selected = day.fasting == true,
          onClick = { onSetFasting(true) },
          testTag = "fasting_yes_button",
        )
        StatusPillButton(
          text = s.no,
          selected = day.fasting == false,
          onClick = { onSetFasting(false) },
          testTag = "fasting_no_button",
        )
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // 3. 5 Prayers Selector Tabs
    Row(
      modifier =
        Modifier.fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(MaterialTheme.colorScheme.background)
          .padding(4.dp),
      horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      PrayerKey.entries.forEach { pKey ->
        val pEntry = day.prayers[pKey] ?: PrayerEntry()
        val isSelected = state.selectedPrayer == pKey
        Surface(
          shape = RoundedCornerShape(10.dp),
          color =
            if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
          shadowElevation = if (isSelected) 1.dp else 0.dp,
          modifier =
            Modifier.weight(1f)
              .clip(RoundedCornerShape(10.dp))
              .clickable { onSelectPrayer(pKey) }
              .testTag("prayer_tab_${pKey.key}"),
        ) {
          Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            Text(
              text = s.prayerName(pKey),
              fontSize = 11.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
              color =
                if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
            )
            Text(
              text = "${pKey.fardRakats} ${s.fardShort}",
              fontSize = 9.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
              modifier =
                Modifier.size(8.dp)
                  .clip(CircleShape)
                  .background(
                    if (pEntry.status) Color(0xFF10B981)
                    else MaterialTheme.colorScheme.outline
                  )
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // 4. Selected Prayer Details Box
    val curPrayer = day.prayers[state.selectedPrayer] ?: PrayerEntry()
    val targetRawatib = state.settings.rawatibConfig.forPrayer(state.selectedPrayer)

    Column(
      modifier =
        Modifier.fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .background(MaterialTheme.colorScheme.background.copy(alpha = 0.6f))
          .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
          .padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = String.format(s.prayerTitlePrefix, s.prayerName(state.selectedPrayer)),
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
        )
        StatusPillButton(
          text = if (curPrayer.status) s.prayerDone else s.prayerUnmarked,
          selected = curPrayer.status,
          onClick = onTogglePrayerStatus,
          testTag = "prayer_status_toggle",
        )
      }

      // Delay after Azan
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
          text = s.delayAfterAzan,
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          val presetValues = listOf(0, 10, 20, 35)
          s.delayPresets.zip(presetValues).forEach { (label, mins) ->
            val activePreset = curPrayer.status && curPrayer.delayMinutes == mins
            Surface(
              shape = RoundedCornerShape(8.dp),
              color =
                if (activePreset) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surface,
              border =
                BorderStroke(
                  1.dp,
                  if (activePreset) MaterialTheme.colorScheme.primary
                  else MaterialTheme.colorScheme.outline,
                ),
              modifier =
                Modifier.weight(1f)
                  .clip(RoundedCornerShape(8.dp))
                  .clickable { onSetPrayerDelay(mins) },
            ) {
              Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color =
                  if (activePreset) MaterialTheme.colorScheme.primary
                  else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(vertical = 8.dp),
              )
            }
          }
        }
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Text(
            text = s.exactMinutesLabel,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          NumericCompactInput(
            value = curPrayer.delayMinutes,
            onValueChange = onSetPrayerDelay,
            width = 72.dp,
          )
        }
      }

      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

      // Rawatib Sunnah
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column {
          Text(s.rawatibTitle, fontSize = 12.sp, fontWeight = FontWeight.Bold)
          Text(
            String.format(s.rawatibPlanFormat, targetRawatib),
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        StatusPillButton(
          text = if (curPrayer.rawatibRead) s.rawatibRead else s.rawatibUnread,
          selected = curPrayer.rawatibRead,
          onClick = onToggleRawatib,
          testTag = "rawatib_toggle",
        )
      }

      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

      // Quality Toggles
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        QualityToggleTile(
          label = s.qualityTumaninah,
          checked = curPrayer.tumaninah,
          yesText = s.yesCheck,
          noText = s.noCross,
          onClick = { onTogglePrayerQuality(PrayerQuality.TUMANINAH) },
          modifier = Modifier.weight(1f),
        )
        QualityToggleTile(
          label = s.qualityKhushu,
          checked = curPrayer.khushu,
          yesText = s.yesCheck,
          noText = s.noCross,
          onClick = { onTogglePrayerQuality(PrayerQuality.KHUSHU) },
          modifier = Modifier.weight(1f),
        )
      }
      QualityToggleTile(
        label = s.qualityIhsan,
        checked = curPrayer.ihsan,
        yesText = s.yesCheck,
        noText = s.noCross,
        onClick = { onTogglePrayerQuality(PrayerQuality.IHSAN) },
        modifier = Modifier.fillMaxWidth(0.5f),
      )

      // Extra Rakats
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = s.extraRakatsLabel,
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.weight(1f).padding(end = 8.dp),
        )
        NumericCompactInput(
          value = curPrayer.extraRakats,
          onValueChange = onSetPrayerExtraRakats,
          width = 68.dp,
        )
      }
    }

    Spacer(modifier = Modifier.height(14.dp))
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    Spacer(modifier = Modifier.height(12.dp))

    // 5. Nafl Prayers Sub-block
    Text(
      text = s.naflSectionTitle,
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(modifier = Modifier.height(8.dp))

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      NaflRowWithInput(
        title = s.naflDuha,
        rakShort = s.rakShort,
        readLabel = s.naflRead,
        unreadLabel = s.naflUnread,
        count = day.nafl.duha.count,
        isRead = day.nafl.duha.read,
        onCountChange = { onSetNaflCount(NaflKey.DUHA, it) },
        onToggle = { onToggleNafl(NaflKey.DUHA) },
      )
      NaflRowWithInput(
        title = s.naflTahajjud,
        rakShort = s.rakShort,
        readLabel = s.naflRead,
        unreadLabel = s.naflUnread,
        count = day.nafl.tahajjud.count,
        isRead = day.nafl.tahajjud.read,
        onCountChange = { onSetNaflCount(NaflKey.TAHAJJUD, it) },
        onToggle = { onToggleNafl(NaflKey.TAHAJJUD) },
      )
      NaflRowWithInput(
        title = s.naflTauba,
        rakShort = s.rakShort,
        readLabel = s.naflRead,
        unreadLabel = s.naflUnread,
        count = day.nafl.tauba.count,
        isRead = day.nafl.tauba.read,
        onCountChange = { onSetNaflCount(NaflKey.TAUBA, it) },
        onToggle = { onToggleNafl(NaflKey.TAUBA) },
      )
      // Witr (3 rakats fixed)
      Row(
        modifier =
          Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.6f))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(s.naflWitr, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            s.witrThreeRakats,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        StatusPillButton(
          text = if (day.nafl.witrRead) s.naflRead else s.naflUnread,
          selected = day.nafl.witrRead,
          onClick = { onToggleNafl(NaflKey.WITR) },
        )
      }
    }
  }
}

@Composable
fun StatusPillButton(
  text: String,
  selected: Boolean,
  onClick: () -> Unit,
  testTag: String? = null,
) {
  Surface(
    shape = RoundedCornerShape(10.dp),
    color =
      if (selected) MaterialTheme.colorScheme.primary
      else MaterialTheme.colorScheme.surfaceVariant,
    shadowElevation = if (selected) 1.dp else 0.dp,
    modifier =
      Modifier.let { m -> if (testTag != null) m.testTag(testTag) else m }
        .clip(RoundedCornerShape(10.dp))
        .clickable(onClick = onClick),
  ) {
    Text(
      text = text,
      fontSize = 12.sp,
      fontWeight = FontWeight.Bold,
      color =
        if (selected) MaterialTheme.colorScheme.onPrimary
        else MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
    )
  }
}

@Composable
private fun QualityToggleTile(
  label: String,
  checked: Boolean,
  yesText: String,
  noText: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color =
      if (checked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
      else MaterialTheme.colorScheme.surface,
    border =
      BorderStroke(
        1.dp,
        if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
      ),
    modifier = modifier.clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 10.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = label,
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.weight(1f),
      )
      Text(
        text = if (checked) yesText else noText,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color =
          if (checked) MaterialTheme.colorScheme.primary
          else MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

@Composable
private fun NaflRowWithInput(
  title: String,
  rakShort: String,
  readLabel: String,
  unreadLabel: String,
  count: Int,
  isRead: Boolean,
  onCountChange: (Int) -> Unit,
  onToggle: () -> Unit,
) {
  Row(
    modifier =
      Modifier.fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.6f))
        .padding(horizontal = 10.dp, vertical = 8.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      Text(title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
      NumericCompactInput(
        value = count,
        onValueChange = onCountChange,
        width = 52.dp,
        allowZeroPlaceholder = false,
        minValue = 1,
      )
      Text(rakShort, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    StatusPillButton(
      text = if (isRead) readLabel else unreadLabel,
      selected = isRead,
      onClick = onToggle,
    )
  }
}
