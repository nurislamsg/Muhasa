package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.json.MuhasabaJsonCodec
import com.example.data.model.AdhkarKey
import com.example.data.model.AnalysisCategory
import com.example.data.model.AnalysisQuestion
import com.example.data.model.AppSettings
import com.example.data.model.BookItem
import com.example.data.model.CarelessnessSession
import com.example.data.model.DayRecord
import com.example.data.model.NaflKey
import com.example.data.model.PrayerEntry
import com.example.data.model.PrayerKey
import com.example.data.model.PrayerQuality
import com.example.data.model.RakatBreakdown
import com.example.data.model.RawatibConfig
import com.example.data.repository.MuhasabaRepository
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.AppStrings
import com.example.ui.i18n.stringsFor
import com.example.util.DateUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class AppTab {
  TODAY,
  CALENDAR,
  TIMER,
  REPORTS,
  SETTINGS;

  fun label(strings: AppStrings): String =
    when (this) {
      TODAY -> strings.tabToday
      CALENDAR -> strings.tabCalendar
      TIMER -> strings.tabTimer
      REPORTS -> strings.tabReports
      SETTINGS -> strings.tabSettings
    }
}

data class DailyRakatPoint(val dateStr: String, val total: Int)

data class MistakeStat(val questionText: String, val count: Int)

data class ReportsSummary(
  val periodDays: Int,
  val todayRakats: Int,
  val yesterdayRakats: Int,
  val todayBooks: Int,
  val yesterdayBooks: Int,
  val todayQuranPages: Int,
  val todayQuranLines: Int,
  val todayIstighfar: Int,
  val todaySalawat: Int,
  val totalRakats: Int,
  val avgRakatsPerDay: Double,
  val diffVsPrevPeriod: Int,
  val dailyRakatSeries: List<DailyRakatPoint>,
  val totalIstighfar: Int,
  val avgIstighfarPerDay: Double,
  val totalSalawat: Int,
  val avgSalawatPerDay: Double,
  val totalBookPages: Int,
  val avgBookPagesPerDay: Double,
  val totalQuranPages: Int,
  val avgQuranPagesPerDay: Double,
  val totalQuranReflect: Int,
  val totalQuranLines: Int,
  val totalCarelessnessMs: Long,
  val avgCarelessnessMsPerDay: Long,
  val topMistakes: List<MistakeStat>,
  val ontimePct: Int,
  val rawatibPct: Int,
  val khushuPct: Int,
)

data class MuhasabaUiState(
  val currentTab: AppTab = AppTab.TODAY,
  val selectedDateStr: String = DateUtils.todayStr(),
  val selectedPrayer: PrayerKey = PrayerKey.FAJR,
  val reportPeriodDays: Int = 7,
  val isDarkTheme: Boolean = false,
  val language: AppLanguage = AppLanguage.RU,
  val settings: AppSettings = AppSettings(),
  val allRecords: Map<String, DayRecord> = emptyMap(),
  val activeTimerStartMs: Long? = null,
  val elapsedTimerMs: Long = 0L,
  val scrollBooksTrigger: Int = 0,
) {
  val strings: AppStrings
    get() = stringsFor(language)

  val currentDay: DayRecord
    get() = allRecords[selectedDateStr] ?: DayRecord(dateStr = selectedDateStr)

  val yesterdayStr: String
    get() = DateUtils.shiftDate(selectedDateStr, -1)

  val yesterdayDay: DayRecord
    get() = allRecords[yesterdayStr] ?: DayRecord(dateStr = yesterdayStr)

  val todayRakats: RakatBreakdown
    get() = currentDay.calculateRakats(settings.rawatibConfig)

  val yesterdayRakatsTotal: Int
    get() = yesterdayDay.calculateRakats(settings.rawatibConfig).total

  val currentWeekAvgRakats: Double
    get() = avgRakatsEndingAt(selectedDateStr, 7)

  val prevWeekAvgRakats: Double
    get() = avgRakatsEndingAt(DateUtils.shiftDate(selectedDateStr, -7), 7)

  private fun avgRakatsEndingAt(endDateStr: String, days: Int): Double {
    var sum = 0
    var cursor = endDateStr
    repeat(days) {
      val rec = allRecords[cursor]
      if (rec != null) {
        sum += rec.calculateRakats(settings.rawatibConfig).total
      }
      cursor = DateUtils.shiftDate(cursor, -1)
    }
    return sum.toDouble() / days.toDouble()
  }

  fun computeReports(): ReportsSummary {
    val s = strings
    var totalRakats = 0
    var totalBooks = 0
    var totalQPages = 0
    var totalQReflect = 0
    var totalQLines = 0
    var totalIst = 0
    var totalSal = 0
    var totalCareMs = 0L

    var checkedPrayers = 0
    var ontimeCount = 0
    var rawatibCount = 0
    var khushuCount = 0

    val mistakeCounts = mutableMapOf<String, Int>()
    val series = mutableListOf<DailyRakatPoint>()

    var cursor = selectedDateStr
    repeat(reportPeriodDays) {
      val day = allRecords[cursor] ?: DayRecord(cursor)
      val r = day.calculateRakats(settings.rawatibConfig).total
      totalRakats += r
      series.add(0, DailyRakatPoint(cursor, r))

      totalBooks += day.totalBookPages()
      totalQPages += day.quran.pages
      totalQReflect += day.quran.reflectPages
      totalQLines += day.quran.memorizedLines
      totalIst += day.dhikr.istighfar
      totalSal += day.dhikr.salawat
      totalCareMs += day.totalCarelessnessMs()

      for (pKey in PrayerKey.entries) {
        val p = day.prayers[pKey] ?: PrayerEntry()
        if (p.status) {
          checkedPrayers++
          if (p.delayMinutes <= 15) ontimeCount++
          if (p.rawatibRead) rawatibCount++
          if (p.khushu && p.tumaninah) khushuCount++
        }
      }

      day.selfAnalysis.forEach { (qId, safe) ->
        if (!safe) {
          mistakeCounts[qId] = (mistakeCounts[qId] ?: 0) + 1
        }
      }

      cursor = DateUtils.shiftDate(cursor, -1)
    }

    var prevPeriodRakats = 0
    repeat(reportPeriodDays) {
      val day = allRecords[cursor]
      if (day != null) {
        prevPeriodRakats += day.calculateRakats(settings.rawatibConfig).total
      }
      cursor = DateUtils.shiftDate(cursor, -1)
    }

    val topMistakes =
      mistakeCounts.entries
        .sortedByDescending { it.value }
        .take(5)
        .map { (qId, count) ->
          val qObj = settings.questions.firstOrNull { it.id == qId }
          val qText =
            if (qObj != null) s.defaultQuestionTextOrCustom(qObj.id, qObj.text)
            else s.reportDeletedQuestion
          MistakeStat(questionText = qText, count = count)
        }

    val ontimePct = if (checkedPrayers > 0) (ontimeCount * 100) / checkedPrayers else 0
    val rawatibPct = if (checkedPrayers > 0) (rawatibCount * 100) / checkedPrayers else 0
    val khushuPct = if (checkedPrayers > 0) (khushuCount * 100) / checkedPrayers else 0

    return ReportsSummary(
      periodDays = reportPeriodDays,
      todayRakats = todayRakats.total,
      yesterdayRakats = yesterdayRakatsTotal,
      todayBooks = currentDay.totalBookPages(),
      yesterdayBooks = yesterdayDay.totalBookPages(),
      todayQuranPages = currentDay.quran.pages,
      todayQuranLines = currentDay.quran.memorizedLines,
      todayIstighfar = currentDay.dhikr.istighfar,
      todaySalawat = currentDay.dhikr.salawat,
      totalRakats = totalRakats,
      avgRakatsPerDay = totalRakats.toDouble() / reportPeriodDays,
      diffVsPrevPeriod = totalRakats - prevPeriodRakats,
      dailyRakatSeries = series,
      totalIstighfar = totalIst,
      avgIstighfarPerDay = totalIst.toDouble() / reportPeriodDays,
      totalSalawat = totalSal,
      avgSalawatPerDay = totalSal.toDouble() / reportPeriodDays,
      totalBookPages = totalBooks,
      avgBookPagesPerDay = totalBooks.toDouble() / reportPeriodDays,
      totalQuranPages = totalQPages,
      avgQuranPagesPerDay = totalQPages.toDouble() / reportPeriodDays,
      totalQuranReflect = totalQReflect,
      totalQuranLines = totalQLines,
      totalCarelessnessMs = totalCareMs,
      avgCarelessnessMsPerDay = totalCareMs / reportPeriodDays,
      topMistakes = topMistakes,
      ontimePct = ontimePct,
      rawatibPct = rawatibPct,
      khushuPct = khushuPct,
    )
  }
}

class MuhasabaViewModel(private val repository: MuhasabaRepository) : ViewModel() {

  private val currentTab = MutableStateFlow(AppTab.TODAY)
  private val selectedDateStr = MutableStateFlow(DateUtils.todayStr())
  private val selectedPrayer = MutableStateFlow(PrayerKey.FAJR)
  private val reportPeriodDays = MutableStateFlow(7)
  private val elapsedTimerMs = MutableStateFlow(0L)
  private val scrollBooksTrigger = MutableStateFlow(0)

  private val _toastMessages = MutableSharedFlow<String>(extraBufferCapacity = 8)
  val toastMessages: SharedFlow<String> = _toastMessages.asSharedFlow()

  private data class LocalNavState(
    val tab: AppTab,
    val dateStr: String,
    val prayer: PrayerKey,
    val periodDays: Int,
    val elapsedMs: Long,
    val scrollBooks: Int,
  )

  private val navPartFlow =
    combine(
      currentTab,
      selectedDateStr,
      selectedPrayer,
      reportPeriodDays,
      elapsedTimerMs,
    ) { tab, dateStr, prayer, periodDays, elapsedMs ->
      LocalNavState(
        tab = tab,
        dateStr = dateStr,
        prayer = prayer,
        periodDays = periodDays,
        elapsedMs = elapsedMs,
        scrollBooks = 0,
      )
    }

  private val localNavFlow =
    combine(navPartFlow, scrollBooksTrigger) { nav, scrollBooks ->
      nav.copy(scrollBooks = scrollBooks)
    }

  private data class RepoSnapshot(
    val isDarkTheme: Boolean,
    val language: AppLanguage,
    val settings: AppSettings,
    val allRecords: Map<String, DayRecord>,
    val activeTimerStartMs: Long?,
  )

  private val repoSnapshotFlow =
    combine(
      repository.isDarkTheme,
      repository.language,
      repository.settings,
      repository.allRecords,
      repository.activeTimerStartMs,
    ) { isDarkTheme, language, settings, allRecords, activeTimerStartMs ->
      RepoSnapshot(
        isDarkTheme = isDarkTheme,
        language = language,
        settings = settings,
        allRecords = allRecords,
        activeTimerStartMs = activeTimerStartMs,
      )
    }

  val uiState: StateFlow<MuhasabaUiState> =
    combine(localNavFlow, repoSnapshotFlow) { nav, repo ->
        MuhasabaUiState(
          currentTab = nav.tab,
          selectedDateStr = nav.dateStr,
          selectedPrayer = nav.prayer,
          reportPeriodDays = nav.periodDays,
          isDarkTheme = repo.isDarkTheme,
          language = repo.language,
          settings = repo.settings,
          allRecords = repo.allRecords,
          activeTimerStartMs = repo.activeTimerStartMs,
          elapsedTimerMs = nav.elapsedMs,
          scrollBooksTrigger = nav.scrollBooks,
        )
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MuhasabaUiState(),
      )

  init {
    viewModelScope.launch {
      repository.activeTimerStartMs.collect { startMs ->
        if (startMs == null) {
          elapsedTimerMs.value = 0L
        } else {
          while (isActive) {
            elapsedTimerMs.value = (System.currentTimeMillis() - startMs).coerceAtLeast(0L)
            delay(1000L)
          }
        }
      }
    }
  }

  private fun currentStrings(): AppStrings = uiState.value.strings

  private fun emitToast(msg: String) {
    _toastMessages.tryEmit(msg)
  }

  fun switchTab(tab: AppTab) {
    currentTab.value = tab
  }

  fun goToBookSettings() {
    currentTab.value = AppTab.SETTINGS
    scrollBooksTrigger.value += 1
  }

  fun changeDate(deltaDays: Int) {
    selectedDateStr.value = DateUtils.shiftDate(selectedDateStr.value, deltaDays)
  }

  fun setToday() {
    selectedDateStr.value = DateUtils.todayStr()
  }

  fun selectDate(dateStr: String) {
    selectedDateStr.value = dateStr
  }

  fun openDateInToday(dateStr: String) {
    selectedDateStr.value = dateStr
    currentTab.value = AppTab.TODAY
  }

  fun toggleTheme() {
    val next = !uiState.value.isDarkTheme
    viewModelScope.launch { repository.setDarkTheme(next) }
  }

  fun setLanguage(language: AppLanguage) {
    viewModelScope.launch { repository.setLanguage(language) }
  }

  fun toggleLanguage() {
    val next = if (uiState.value.language == AppLanguage.RU) AppLanguage.UZ else AppLanguage.RU
    setLanguage(next)
  }

  fun selectPrayer(prayerKey: PrayerKey) {
    selectedPrayer.value = prayerKey
  }

  private fun updateCurrentDay(transform: (DayRecord) -> DayRecord) {
    val cur = uiState.value.currentDay
    val updated = transform(cur)
    viewModelScope.launch { repository.saveDayRecord(updated) }
  }

  private fun updateCurrentPrayer(transform: (PrayerEntry) -> PrayerEntry) {
    val key = uiState.value.selectedPrayer
    updateCurrentDay { day ->
      val currentEntry = day.prayers[key] ?: PrayerEntry()
      val updatedEntry = transform(currentEntry)
      day.copy(prayers = day.prayers + (key to updatedEntry))
    }
  }

  fun setFasting(fasting: Boolean) {
    updateCurrentDay { it.copy(fasting = fasting) }
  }

  fun toggleCurrentPrayerStatus() {
    updateCurrentPrayer { it.copy(status = !it.status) }
  }

  fun setPrayerDelay(minutes: Int) {
    val clamped = minutes.coerceAtLeast(0)
    updateCurrentPrayer { it.copy(delayMinutes = clamped, status = true) }
  }

  fun toggleCurrentRawatib() {
    updateCurrentPrayer { it.copy(rawatibRead = !it.rawatibRead, status = true) }
  }

  fun togglePrayerQuality(quality: PrayerQuality) {
    updateCurrentPrayer { p ->
      when (quality) {
        PrayerQuality.TUMANINAH -> p.copy(tumaninah = !p.tumaninah, status = true)
        PrayerQuality.KHUSHU -> p.copy(khushu = !p.khushu, status = true)
        PrayerQuality.IHSAN -> p.copy(ihsan = !p.ihsan, status = true)
      }
    }
  }

  fun setPrayerExtraRakats(rakats: Int) {
    updateCurrentPrayer { it.copy(extraRakats = rakats.coerceAtLeast(0), status = true) }
  }

  fun toggleNafl(key: NaflKey) {
    updateCurrentDay { day ->
      val nextNafl =
        when (key) {
          NaflKey.DUHA -> day.nafl.copy(duha = day.nafl.duha.copy(read = !day.nafl.duha.read))
          NaflKey.TAHAJJUD ->
            day.nafl.copy(tahajjud = day.nafl.tahajjud.copy(read = !day.nafl.tahajjud.read))
          NaflKey.TAUBA -> day.nafl.copy(tauba = day.nafl.tauba.copy(read = !day.nafl.tauba.read))
          NaflKey.WITR -> day.nafl.copy(witrRead = !day.nafl.witrRead)
        }
      day.copy(nafl = nextNafl)
    }
  }

  fun setNaflCount(key: NaflKey, count: Int) {
    val safe = count.coerceAtLeast(1)
    updateCurrentDay { day ->
      val nextNafl =
        when (key) {
          NaflKey.DUHA -> day.nafl.copy(duha = day.nafl.duha.copy(count = safe))
          NaflKey.TAHAJJUD -> day.nafl.copy(tahajjud = day.nafl.tahajjud.copy(count = safe))
          NaflKey.TAUBA -> day.nafl.copy(tauba = day.nafl.tauba.copy(count = safe))
          NaflKey.WITR -> day.nafl
        }
      day.copy(nafl = nextNafl)
    }
  }

  fun updateQuran(pages: Int? = null, reflectPages: Int? = null, memorizedLines: Int? = null) {
    updateCurrentDay { day ->
      day.copy(
        quran =
          day.quran.copy(
            pages = (pages ?: day.quran.pages).coerceAtLeast(0),
            reflectPages = (reflectPages ?: day.quran.reflectPages).coerceAtLeast(0),
            memorizedLines = (memorizedLines ?: day.quran.memorizedLines).coerceAtLeast(0),
          )
      )
    }
  }

  fun toggleAdhkar(key: AdhkarKey) {
    updateCurrentDay { day ->
      val next =
        when (key) {
          AdhkarKey.WAKE -> day.adhkar.copy(wake = !day.adhkar.wake)
          AdhkarKey.MORNING -> day.adhkar.copy(morning = !day.adhkar.morning)
          AdhkarKey.EVENING -> day.adhkar.copy(evening = !day.adhkar.evening)
          AdhkarKey.SLEEP -> day.adhkar.copy(sleep = !day.adhkar.sleep)
        }
      day.copy(adhkar = next)
    }
  }

  fun updateDhikr(
    istighfar: Int? = null,
    salawat: Int? = null,
    noDhikrCount: Int? = null,
    noTaharahCount: Int? = null,
  ) {
    updateCurrentDay { day ->
      day.copy(
        dhikr =
          day.dhikr.copy(
            istighfar = (istighfar ?: day.dhikr.istighfar).coerceAtLeast(0),
            salawat = (salawat ?: day.dhikr.salawat).coerceAtLeast(0),
            noDhikrCount = (noDhikrCount ?: day.dhikr.noDhikrCount).coerceAtLeast(0),
            noTaharahCount = (noTaharahCount ?: day.dhikr.noTaharahCount).coerceAtLeast(0),
          )
      )
    }
  }

  fun toggleTruth() {
    updateCurrentDay { day -> day.copy(dhikr = day.dhikr.copy(truth = !day.dhikr.truth)) }
  }

  fun togglePatience() {
    updateCurrentDay { day -> day.copy(dhikr = day.dhikr.copy(patience = !day.dhikr.patience)) }
  }

  fun updateBookPages(bookId: String, pages: Int) {
    val safe = pages.coerceAtLeast(0)
    updateCurrentDay { day -> day.copy(bookPages = day.bookPages + (bookId to safe)) }
  }

  fun cycleSelfAnalysis(questionId: String) {
    updateCurrentDay { day ->
      val cur = day.selfAnalysis[questionId]
      val nextMap =
        when (cur) {
          null -> day.selfAnalysis + (questionId to true)
          true -> day.selfAnalysis + (questionId to false)
          false -> day.selfAnalysis - questionId
        }
      day.copy(selfAnalysis = nextMap)
    }
  }

  fun setSelfAnalysisAnswer(questionId: String, safe: Boolean) {
    updateCurrentDay { day ->
      val cur = day.selfAnalysis[questionId]
      val nextMap =
        if (cur == safe) day.selfAnalysis - questionId
        else day.selfAnalysis + (questionId to safe)
      day.copy(selfAnalysis = nextMap)
    }
  }

  // TIMER
  fun startCarelessnessTimer() {
    val now = System.currentTimeMillis()
    val s = currentStrings()
    viewModelScope.launch {
      repository.setActiveTimerStart(now)
      emitToast(s.toastTimerStarted)
    }
  }

  fun stopCarelessnessTimer() {
    val startMs = uiState.value.activeTimerStartMs ?: return
    val endMs = System.currentTimeMillis()
    val durationMs = (endMs - startMs).coerceAtLeast(0L)
    val session = CarelessnessSession(startMs = startMs, endMs = endMs, durationMs = durationMs)
    val s = currentStrings()

    viewModelScope.launch {
      repository.setActiveTimerStart(null)
      val curDay = uiState.value.currentDay
      repository.saveDayRecord(
        curDay.copy(carelessnessSessions = curDay.carelessnessSessions + session)
      )
      emitToast(String.format(s.toastTimerStoppedFormat, DateUtils.formatMsToHHMMSS(durationMs)))
    }
  }

  fun deleteTimerSession(index: Int) {
    updateCurrentDay { day ->
      val list = day.carelessnessSessions.toMutableList()
      if (index in list.indices) {
        list.removeAt(index)
      }
      day.copy(carelessnessSessions = list)
    }
  }

  // REPORTS
  fun setReportPeriod(days: Int) {
    reportPeriodDays.value = days
  }

  // SETTINGS
  private fun updateSettings(transform: (AppSettings) -> AppSettings) {
    val updated = transform(uiState.value.settings)
    viewModelScope.launch { repository.saveSettings(updated) }
  }

  fun updateRawatibConfig(config: RawatibConfig) {
    updateSettings { it.copy(rawatibConfig = config) }
    emitToast(currentStrings().toastRawatibSaved)
  }

  fun addBook(title: String, dailyPlan: Int) {
    val s = currentStrings()
    val trimmed = title.trim()
    if (trimmed.isEmpty()) {
      emitToast(s.toastEnterBookTitle)
      return
    }
    val newBook =
      BookItem(
        id = "b_${System.currentTimeMillis()}",
        title = trimmed,
        dailyPlan = dailyPlan.coerceAtLeast(1),
      )
    updateSettings { it.copy(books = it.books + newBook) }
    emitToast(s.toastBookAdded)
  }

  fun updateBook(index: Int, title: String? = null, dailyPlan: Int? = null) {
    updateSettings { s ->
      val list = s.books.toMutableList()
      if (index in list.indices) {
        val cur = list[index]
        list[index] =
          cur.copy(
            title = title?.trim() ?: cur.title,
            dailyPlan = (dailyPlan ?: cur.dailyPlan).coerceAtLeast(1),
          )
      }
      s.copy(books = list)
    }
  }

  fun deleteBook(index: Int) {
    updateSettings { s ->
      val list = s.books.toMutableList()
      if (index in list.indices) list.removeAt(index)
      s.copy(books = list)
    }
    emitToast(currentStrings().toastBookDeleted)
  }

  fun addQuestion(text: String, category: AnalysisCategory) {
    val s = currentStrings()
    val trimmed = text.trim()
    if (trimmed.isEmpty()) {
      emitToast(s.toastEnterQuestion)
      return
    }
    val newQ =
      AnalysisQuestion(
        id = "q_${System.currentTimeMillis()}",
        cat = category,
        text = trimmed,
      )
    updateSettings { it.copy(questions = it.questions + newQ) }
    emitToast(s.toastQuestionAdded)
  }

  fun updateQuestionText(index: Int, text: String) {
    updateSettings { s ->
      val list = s.questions.toMutableList()
      if (index in list.indices) {
        list[index] = list[index].copy(text = text.trim())
      }
      s.copy(questions = list)
    }
  }

  fun deleteQuestion(index: Int) {
    updateSettings { s ->
      val list = s.questions.toMutableList()
      if (index in list.indices) list.removeAt(index)
      s.copy(questions = list)
    }
    emitToast(currentStrings().toastQuestionDeleted)
  }

  fun buildBackupJson(): String {
    val state = uiState.value
    return MuhasabaJsonCodec.encodeBackup(state.settings, state.allRecords)
  }

  fun importBackupJson(jsonStr: String) {
    val s = currentStrings()
    viewModelScope.launch {
      val ok = repository.importBackup(jsonStr)
      if (ok) {
        emitToast(s.toastImportSuccess)
      } else {
        emitToast(s.toastImportError)
      }
    }
  }

  fun notifyExportSuccess() {
    emitToast(currentStrings().toastExportSuccess)
  }

  class Factory(private val repository: MuhasabaRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return MuhasabaViewModel(repository) as T
    }
  }
}
