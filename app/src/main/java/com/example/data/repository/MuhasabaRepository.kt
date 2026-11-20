package com.example.data.repository

import com.example.data.json.MuhasabaJsonCodec
import com.example.data.local.AppStateEntity
import com.example.data.local.DailyRecordEntity
import com.example.data.local.MuhasabaDao
import com.example.data.model.AppSettings
import com.example.data.model.DayRecord
import com.example.ui.i18n.AppLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MuhasabaRepository(private val dao: MuhasabaDao) {

  companion object {
    private const val KEY_SETTINGS = "settings"
    private const val KEY_THEME_DARK = "theme_dark"
    private const val KEY_ACTIVE_TIMER = "active_timer_start"
    private const val KEY_LANGUAGE = "app_language"
  }

  val allRecords: Flow<Map<String, DayRecord>> =
    dao.observeAllDays().map { entities ->
      entities.associate { entity ->
        entity.dateStr to MuhasabaJsonCodec.decodeDayRecord(entity.dateStr, entity.jsonPayload)
      }
    }

  private val stateMap: Flow<Map<String, String>> =
    dao.observeAppState().map { entities -> entities.associate { it.key to it.value } }

  val settings: Flow<AppSettings> =
    stateMap.map { map ->
      val raw = map[KEY_SETTINGS]
      if (raw != null) MuhasabaJsonCodec.decodeSettings(raw) else AppSettings()
    }

  val isDarkTheme: Flow<Boolean> =
    stateMap.map { map -> map[KEY_THEME_DARK]?.toBooleanStrictOrNull() ?: false }

  val language: Flow<AppLanguage> =
    stateMap.map { map -> AppLanguage.fromCode(map[KEY_LANGUAGE]) }

  val activeTimerStartMs: Flow<Long?> =
    stateMap.map { map -> map[KEY_ACTIVE_TIMER]?.toLongOrNull() }

  suspend fun saveDayRecord(record: DayRecord) {
    dao.upsertDay(
      DailyRecordEntity(
        dateStr = record.dateStr,
        jsonPayload = MuhasabaJsonCodec.encodeDayRecord(record),
      )
    )
  }

  suspend fun saveSettings(settings: AppSettings) {
    dao.upsertState(
      AppStateEntity(
        key = KEY_SETTINGS,
        value = MuhasabaJsonCodec.encodeSettings(settings),
      )
    )
  }

  suspend fun setDarkTheme(isDark: Boolean) {
    dao.upsertState(AppStateEntity(key = KEY_THEME_DARK, value = isDark.toString()))
  }

  suspend fun setLanguage(language: AppLanguage) {
    dao.upsertState(AppStateEntity(key = KEY_LANGUAGE, value = language.code))
  }

  suspend fun setActiveTimerStart(startMs: Long?) {
    if (startMs == null) {
      dao.deleteState(KEY_ACTIVE_TIMER)
    } else {
      dao.upsertState(AppStateEntity(key = KEY_ACTIVE_TIMER, value = startMs.toString()))
    }
  }

  suspend fun importBackup(jsonStr: String): Boolean {
    val payload = MuhasabaJsonCodec.decodeBackup(jsonStr) ?: return false
    saveSettings(payload.settings)
    val entities =
      payload.records.map { (dateStr, record) ->
        DailyRecordEntity(
          dateStr = dateStr,
          jsonPayload = MuhasabaJsonCodec.encodeDayRecord(record),
        )
      }
    if (entities.isNotEmpty()) {
      dao.upsertDays(entities)
    }
    return true
  }
}
