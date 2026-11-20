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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AnalysisCategory
import com.example.data.model.PrayerKey
import com.example.data.model.RawatibConfig
import com.example.ui.MuhasabaUiState
import com.example.ui.components.NumericCompactInput
import com.example.ui.components.SectionCard
import com.example.ui.i18n.AppLanguage
import com.example.ui.theme.AmberSoft
import com.example.ui.theme.AmberText

@Composable
fun SettingsScreen(
  state: MuhasabaUiState,
  onSelectLanguage: (AppLanguage) -> Unit,
  onAddBook: (String, Int) -> Unit,
  onUpdateBook: (Int, String?, Int?) -> Unit,
  onDeleteBook: (Int) -> Unit,
  onSaveRawatibConfig: (RawatibConfig) -> Unit,
  onAddQuestion: (String, AnalysisCategory) -> Unit,
  onUpdateQuestion: (Int, String) -> Unit,
  onDeleteQuestion: (Int) -> Unit,
  onExportJson: () -> Unit,
  onImportJson: () -> Unit,
  onShareBackup: () -> Unit,
) {
  val s = state.strings
  var newBookTitle by remember { mutableStateOf("") }
  var newBookPlan by remember { mutableStateOf(5) }
  var newQuestionText by remember { mutableStateOf("") }
  var newQuestionCategory by remember { mutableStateOf(AnalysisCategory.LIMBS) }
  var categoryExpanded by remember { mutableStateOf(false) }

  val rCfg = state.settings.rawatibConfig

  Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
    // 0. Language Selector
    SectionCard {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Filled.Language,
          contentDescription = s.settingsLanguageHeader,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = s.settingsLanguageHeader,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = s.settingsLanguageSubtitle,
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Spacer(modifier = Modifier.height(10.dp))
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AppLanguage.entries.forEach { lang ->
          val selected = state.language == lang
          Surface(
            shape = RoundedCornerShape(12.dp),
            color =
              if (selected) MaterialTheme.colorScheme.primary
              else MaterialTheme.colorScheme.background.copy(alpha = 0.65f),
            border =
              BorderStroke(
                1.dp,
                if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outline,
              ),
            modifier =
              Modifier.weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .clickable { onSelectLanguage(lang) }
                .testTag("language_option_${lang.code}"),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 12.dp),
              horizontalArrangement = Arrangement.Center,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              if (selected) {
                Icon(
                  imageVector = Icons.Filled.Check,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.onPrimary,
                  modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
              }
              Text(
                text = lang.nativeName,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color =
                  if (selected) MaterialTheme.colorScheme.onPrimary
                  else MaterialTheme.colorScheme.onSurface,
              )
            }
          }
        }
      }
    }

    // 1. Books & Reading Plan
    SectionCard {
      Text(
        text = s.settingsBooksHeader,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Spacer(modifier = Modifier.height(10.dp))

      Column(
        modifier =
          Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.65f))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Text(s.settingsAddBookTitle, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        OutlinedTextField(
          value = newBookTitle,
          onValueChange = { newBookTitle = it },
          placeholder = { Text(s.settingsBookTitlePlaceholder, fontSize = 12.sp) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("new_book_title_input"),
        )
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Text(
            s.settingsPlanPerDayLabel,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          NumericCompactInput(
            value = newBookPlan,
            onValueChange = { newBookPlan = it },
            width = 72.dp,
            allowZeroPlaceholder = false,
            minValue = 1,
          )
        }
        Button(
          onClick = {
            onAddBook(newBookTitle, newBookPlan)
            if (newBookTitle.isNotBlank()) newBookTitle = ""
          },
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.fillMaxWidth().testTag("add_book_button"),
        ) {
          Text(s.settingsAddBookButton, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        state.settings.books.forEachIndexed { idx, book ->
          val displayTitle = s.defaultBookTitleOrCustom(book.id, book.title)
          var titleDraft by remember(book.id, displayTitle) { mutableStateOf(displayTitle) }
          Column(
            modifier =
              Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.65f))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              OutlinedTextField(
                value = titleDraft,
                onValueChange = {
                  titleDraft = it
                  onUpdateBook(idx, it, null)
                },
                singleLine = true,
                modifier = Modifier.weight(1f),
              )
              IconButton(
                onClick = { onDeleteBook(idx) },
                modifier = Modifier.size(40.dp),
              ) {
                Icon(
                  imageVector = Icons.Filled.DeleteOutline,
                  contentDescription = s.settingsDeleteBookDesc,
                  tint = MaterialTheme.colorScheme.error,
                )
              }
            }
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                s.settingsDailyPlanPagesLabel,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
              NumericCompactInput(
                value = book.dailyPlan,
                onValueChange = { onUpdateBook(idx, null, it) },
                width = 68.dp,
                allowZeroPlaceholder = false,
                minValue = 1,
              )
            }
          }
        }
      }
    }

    // 2. Rawatib Rakats Settings
    SectionCard {
      Text(
        text = s.settingsRawatibHeader,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = s.settingsRawatibSubtitle,
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Spacer(modifier = Modifier.height(10.dp))

      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        RawatibSettingRow(
          String.format(s.settingsRawatibRowFormat, s.prayerName(PrayerKey.FAJR)),
          rCfg.fajr,
        ) {
          onSaveRawatibConfig(rCfg.copy(fajr = it))
        }
        RawatibSettingRow(
          String.format(s.settingsRawatibRowFormat, s.prayerName(PrayerKey.ZUHR)),
          rCfg.zuhr,
        ) {
          onSaveRawatibConfig(rCfg.copy(zuhr = it))
        }
        RawatibSettingRow(
          String.format(s.settingsRawatibRowFormat, s.prayerName(PrayerKey.ASR)),
          rCfg.asr,
        ) {
          onSaveRawatibConfig(rCfg.copy(asr = it))
        }
        RawatibSettingRow(
          String.format(s.settingsRawatibRowFormat, s.prayerName(PrayerKey.MAGHRIB)),
          rCfg.maghrib,
        ) {
          onSaveRawatibConfig(rCfg.copy(maghrib = it))
        }
        RawatibSettingRow(
          String.format(s.settingsRawatibRowFormat, s.prayerName(PrayerKey.ISHA)),
          rCfg.isha,
        ) {
          onSaveRawatibConfig(rCfg.copy(isha = it))
        }
      }
    }

    // 3. Self-Analysis Questions Editor
    SectionCard {
      Text(
        text = s.settingsQuestionsHeader,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Spacer(modifier = Modifier.height(10.dp))

      Column(
        modifier =
          Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.65f))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        OutlinedTextField(
          value = newQuestionText,
          onValueChange = { newQuestionText = it },
          placeholder = { Text(s.settingsNewQuestionPlaceholder, fontSize = 12.sp) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("new_question_input"),
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Box {
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = MaterialTheme.colorScheme.surface,
              modifier =
                Modifier.clip(RoundedCornerShape(10.dp))
                  .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                  .clickable { categoryExpanded = true },
            ) {
              Text(
                text = s.categoryLabel(newQuestionCategory),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
              )
            }
            DropdownMenu(
              expanded = categoryExpanded,
              onDismissRequest = { categoryExpanded = false },
            ) {
              AnalysisCategory.entries.forEach { cat ->
                DropdownMenuItem(
                  text = { Text(s.categoryLabel(cat), fontSize = 12.sp) },
                  onClick = {
                    newQuestionCategory = cat
                    categoryExpanded = false
                  },
                )
              }
            }
          }
          Button(
            onClick = {
              onAddQuestion(newQuestionText, newQuestionCategory)
              if (newQuestionText.isNotBlank()) newQuestionText = ""
            },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.weight(1f).testTag("add_question_button"),
          ) {
            Text(s.settingsAddQuestionButton, fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        state.settings.questions.forEachIndexed { idx, q ->
          val displayQText = s.defaultQuestionTextOrCustom(q.id, q.text)
          var qDraft by remember(q.id, displayQText) { mutableStateOf(displayQText) }
          Row(
            modifier =
              Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.65f))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            OutlinedTextField(
              value = qDraft,
              onValueChange = {
                qDraft = it
                onUpdateQuestion(idx, it)
              },
              singleLine = true,
              modifier = Modifier.weight(1f),
            )
            val badgeBg =
              if (q.cat == AnalysisCategory.LIMBS) MaterialTheme.colorScheme.primaryContainer
              else if (state.isDarkTheme) Color(0xFF451A03) else AmberSoft
            val badgeText =
              if (q.cat == AnalysisCategory.LIMBS) MaterialTheme.colorScheme.primary
              else if (state.isDarkTheme) MaterialTheme.colorScheme.tertiary else AmberText
            Box(
              modifier =
                Modifier.clip(RoundedCornerShape(6.dp))
                  .background(badgeBg)
                  .padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
              Text(
                text = s.categoryBadge(q.cat),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = badgeText,
              )
            }
            IconButton(
              onClick = { onDeleteQuestion(idx) },
              modifier = Modifier.size(36.dp),
            ) {
              Icon(
                imageVector = Icons.Filled.DeleteOutline,
                contentDescription = s.settingsDeleteQuestionDesc,
                tint = MaterialTheme.colorScheme.error,
              )
            }
          }
        }
      }
    }

    // 4. Backup & Share
    SectionCard {
      Text(
        text = s.settingsBackupHeader,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Spacer(modifier = Modifier.height(10.dp))
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
          onClick = onExportJson,
          colors =
            ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant,
              contentColor = MaterialTheme.colorScheme.onSurface,
            ),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.weight(1f).testTag("export_json_button"),
        ) {
          Text(s.settingsExportJson, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Button(
          onClick = onImportJson,
          colors =
            ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant,
              contentColor = MaterialTheme.colorScheme.onSurface,
            ),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.weight(1f).testTag("import_json_button"),
        ) {
          Text(s.settingsImportJson, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
      }
      Spacer(modifier = Modifier.height(8.dp))
      Button(
        onClick = onShareBackup,
        colors =
          ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
          ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().testTag("share_backup_button"),
      ) {
        Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(s.settingsShareBackup, fontSize = 12.sp, fontWeight = FontWeight.Bold)
      }
    }
  }
}

@Composable
private fun RawatibSettingRow(label: String, value: Int, onChange: (Int) -> Unit) {
  Row(
    modifier =
      Modifier.fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.6f))
        .padding(horizontal = 12.dp, vertical = 8.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    NumericCompactInput(
      value = value,
      onValueChange = onChange,
      width = 64.dp,
      allowZeroPlaceholder = false,
      minValue = 0,
    )
  }
}
