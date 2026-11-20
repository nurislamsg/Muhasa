package com.example.ui.calendar

import com.example.data.model.AdhkarKey
import com.example.data.model.AnalysisCategory
import com.example.data.model.AppSettings
import com.example.data.model.DayRecord
import com.example.data.model.NaflKey
import com.example.data.model.PrayerEntry
import com.example.data.model.PrayerKey
import com.example.ui.i18n.AppStrings
import com.example.util.DateUtils

enum class CalendarBlock {
  ALL,
  PRAYERS,
  NAFL,
  QURAN,
  ADHKAR,
  DHIKR,
  BOOKS,
  ANALYSIS,
  TIMER;

  fun label(s: AppStrings): String =
    when (this) {
      ALL -> s.calendarBlockAll
      PRAYERS -> s.calendarBlockPrayers
      NAFL -> s.calendarBlockNafl
      QURAN -> s.calendarBlockQuran
      ADHKAR -> s.calendarBlockAdhkar
      DHIKR -> s.calendarBlockDhikr
      BOOKS -> s.calendarBlockBooks
      ANALYSIS -> s.calendarBlockAnalysis
      TIMER -> s.calendarBlockTimer
    }
}

sealed interface CalendarItemFilter {
  val block: CalendarBlock

  data object Overall : CalendarItemFilter {
    override val block = CalendarBlock.ALL
  }

  // Prayers & Fasting
  data object PrayersWhole : CalendarItemFilter {
    override val block = CalendarBlock.PRAYERS
  }

  data object Fasting : CalendarItemFilter {
    override val block = CalendarBlock.PRAYERS
  }

  data class Prayer(val key: PrayerKey) : CalendarItemFilter {
    override val block = CalendarBlock.PRAYERS
  }

  data object RawatibAll : CalendarItemFilter {
    override val block = CalendarBlock.PRAYERS
  }

  data object OntimeAll : CalendarItemFilter {
    override val block = CalendarBlock.PRAYERS
  }

  data object KhushuAll : CalendarItemFilter {
    override val block = CalendarBlock.PRAYERS
  }

  // Nafl
  data object NaflWhole : CalendarItemFilter {
    override val block = CalendarBlock.NAFL
  }

  data class Nafl(val key: NaflKey) : CalendarItemFilter {
    override val block = CalendarBlock.NAFL
  }

  // Quran
  data object QuranWhole : CalendarItemFilter {
    override val block = CalendarBlock.QURAN
  }

  data object QuranRead : CalendarItemFilter {
    override val block = CalendarBlock.QURAN
  }

  data object QuranReflect : CalendarItemFilter {
    override val block = CalendarBlock.QURAN
  }

  data object QuranLines : CalendarItemFilter {
    override val block = CalendarBlock.QURAN
  }

  // Adhkar
  data object AdhkarWhole : CalendarItemFilter {
    override val block = CalendarBlock.ADHKAR
  }

  data class Adhkar(val key: AdhkarKey) : CalendarItemFilter {
    override val block = CalendarBlock.ADHKAR
  }

  // Dhikr & Vigilance
  data object DhikrWhole : CalendarItemFilter {
    override val block = CalendarBlock.DHIKR
  }

  data object Istighfar : CalendarItemFilter {
    override val block = CalendarBlock.DHIKR
  }

  data object Salawat : CalendarItemFilter {
    override val block = CalendarBlock.DHIKR
  }

  data object NoDhikr : CalendarItemFilter {
    override val block = CalendarBlock.DHIKR
  }

  data object NoTaharah : CalendarItemFilter {
    override val block = CalendarBlock.DHIKR
  }

  data object Truth : CalendarItemFilter {
    override val block = CalendarBlock.DHIKR
  }

  data object Patience : CalendarItemFilter {
    override val block = CalendarBlock.DHIKR
  }

  // Books
  data object BooksWhole : CalendarItemFilter {
    override val block = CalendarBlock.BOOKS
  }

  data class Book(val bookId: String) : CalendarItemFilter {
    override val block = CalendarBlock.BOOKS
  }

  // Self Analysis
  data object AnalysisWhole : CalendarItemFilter {
    override val block = CalendarBlock.ANALYSIS
  }

  data object AnalysisLimbsGroup : CalendarItemFilter {
    override val block = CalendarBlock.ANALYSIS
  }

  data object AnalysisHeartGroup : CalendarItemFilter {
    override val block = CalendarBlock.ANALYSIS
  }

  data class Question(val questionId: String) : CalendarItemFilter {
    override val block = CalendarBlock.ANALYSIS
  }

  // Timer
  data object TimerWhole : CalendarItemFilter {
    override val block = CalendarBlock.TIMER
  }
}

enum class DayOutcome {
  GREEN,
  RED,
  NEUTRAL,
}

enum class CalendarStatusFilter {
  ALL,
  GREEN_ONLY,
  RED_ONLY,
}

enum class CalendarSortOrder {
  RED_FIRST,
  GREEN_FIRST,
  DATE_DESC,
}

data class DayEvaluation(
  val outcome: DayOutcome,
  val summaryBadge: String = "",
  val reason: String = "",
)

object CalendarLogic {

  fun defaultFilterForBlock(block: CalendarBlock): CalendarItemFilter =
    when (block) {
      CalendarBlock.ALL -> CalendarItemFilter.Overall
      CalendarBlock.PRAYERS -> CalendarItemFilter.PrayersWhole
      CalendarBlock.NAFL -> CalendarItemFilter.NaflWhole
      CalendarBlock.QURAN -> CalendarItemFilter.QuranWhole
      CalendarBlock.ADHKAR -> CalendarItemFilter.AdhkarWhole
      CalendarBlock.DHIKR -> CalendarItemFilter.DhikrWhole
      CalendarBlock.BOOKS -> CalendarItemFilter.BooksWhole
      CalendarBlock.ANALYSIS -> CalendarItemFilter.AnalysisWhole
      CalendarBlock.TIMER -> CalendarItemFilter.TimerWhole
    }

  fun itemsForBlock(
    block: CalendarBlock,
    settings: AppSettings,
    s: AppStrings,
  ): List<Pair<CalendarItemFilter, String>> =
    when (block) {
      CalendarBlock.ALL -> emptyList()
      CalendarBlock.PRAYERS ->
        listOf(
          CalendarItemFilter.PrayersWhole to s.calendarWholeBlockOption,
          CalendarItemFilter.Fasting to s.calendarItemFasting,
          CalendarItemFilter.Prayer(PrayerKey.FAJR) to s.prayerName(PrayerKey.FAJR),
          CalendarItemFilter.Prayer(PrayerKey.ZUHR) to s.prayerName(PrayerKey.ZUHR),
          CalendarItemFilter.Prayer(PrayerKey.ASR) to s.prayerName(PrayerKey.ASR),
          CalendarItemFilter.Prayer(PrayerKey.MAGHRIB) to s.prayerName(PrayerKey.MAGHRIB),
          CalendarItemFilter.Prayer(PrayerKey.ISHA) to s.prayerName(PrayerKey.ISHA),
          CalendarItemFilter.RawatibAll to s.calendarItemRawatibAll,
          CalendarItemFilter.OntimeAll to s.calendarItemOntimeAll,
          CalendarItemFilter.KhushuAll to s.calendarItemKhushuAll,
        )
      CalendarBlock.NAFL ->
        listOf(
          CalendarItemFilter.NaflWhole to s.calendarWholeBlockOption,
          CalendarItemFilter.Nafl(NaflKey.DUHA) to s.naflDuha,
          CalendarItemFilter.Nafl(NaflKey.TAHAJJUD) to s.naflTahajjud,
          CalendarItemFilter.Nafl(NaflKey.TAUBA) to s.naflTauba,
          CalendarItemFilter.Nafl(NaflKey.WITR) to s.naflWitr,
        )
      CalendarBlock.QURAN ->
        listOf(
          CalendarItemFilter.QuranWhole to s.calendarWholeBlockOption,
          CalendarItemFilter.QuranRead to s.calendarItemQuranRead,
          CalendarItemFilter.QuranReflect to s.calendarItemQuranReflect,
          CalendarItemFilter.QuranLines to s.calendarItemQuranLines,
        )
      CalendarBlock.ADHKAR ->
        listOf(
          CalendarItemFilter.AdhkarWhole to s.calendarWholeBlockOption,
          CalendarItemFilter.Adhkar(AdhkarKey.WAKE) to s.adhkarWake,
          CalendarItemFilter.Adhkar(AdhkarKey.MORNING) to s.adhkarMorning,
          CalendarItemFilter.Adhkar(AdhkarKey.EVENING) to s.adhkarEvening,
          CalendarItemFilter.Adhkar(AdhkarKey.SLEEP) to s.adhkarSleep,
        )
      CalendarBlock.DHIKR ->
        listOf(
          CalendarItemFilter.DhikrWhole to s.calendarWholeBlockOption,
          CalendarItemFilter.Istighfar to s.calendarItemIstighfar,
          CalendarItemFilter.Salawat to s.calendarItemSalawat,
          CalendarItemFilter.NoDhikr to s.calendarItemNoDhikr,
          CalendarItemFilter.NoTaharah to s.calendarItemNoTaharah,
          CalendarItemFilter.Truth to s.truthQuestion,
          CalendarItemFilter.Patience to s.patienceQuestion,
        )
      CalendarBlock.BOOKS ->
        listOf(CalendarItemFilter.BooksWhole to s.calendarWholeBlockOption) +
          settings.books.map { book ->
            CalendarItemFilter.Book(book.id) to s.defaultBookTitleOrCustom(book.id, book.title)
          }
      CalendarBlock.ANALYSIS ->
        listOf(
          CalendarItemFilter.AnalysisWhole to s.calendarWholeBlockOption,
          CalendarItemFilter.AnalysisLimbsGroup to s.calendarItemLimbsGroup,
          CalendarItemFilter.AnalysisHeartGroup to s.calendarItemHeartGroup,
        ) +
          settings.questions.map { q ->
            CalendarItemFilter.Question(q.id) to s.defaultQuestionTextOrCustom(q.id, q.text)
          }
      CalendarBlock.TIMER ->
        listOf(CalendarItemFilter.TimerWhole to s.calendarItemCarelessness)
    }

  fun DayRecord.hasAnyActivity(): Boolean {
    if (fasting != null) return true
    if (
      prayers.values.any {
        it.status ||
          it.rawatibRead ||
          it.tumaninah ||
          it.khushu ||
          it.ihsan ||
          it.extraRakats > 0 ||
          it.delayMinutes > 0
      }
    ) {
      return true
    }
    if (nafl.duha.read || nafl.tahajjud.read || nafl.tauba.read || nafl.witrRead) return true
    if (quran.pages > 0 || quran.reflectPages > 0 || quran.memorizedLines > 0) return true
    if (adhkar.wake || adhkar.morning || adhkar.evening || adhkar.sleep) return true
    if (
      dhikr.istighfar > 0 ||
        dhikr.salawat > 0 ||
        dhikr.noDhikrCount > 0 ||
        dhikr.noTaharahCount > 0 ||
        dhikr.truth ||
        dhikr.patience
    ) {
      return true
    }
    if (bookPages.values.any { it > 0 }) return true
    if (selfAnalysis.isNotEmpty()) return true
    if (carelessnessSessions.isNotEmpty()) return true
    return false
  }

  fun evaluateDay(
    day: DayRecord?,
    settings: AppSettings,
    filter: CalendarItemFilter,
    s: AppStrings,
  ): DayEvaluation {
    if (day == null || !day.hasAnyActivity()) {
      return DayEvaluation(outcome = DayOutcome.NEUTRAL, reason = s.calendarDayStatusEmpty)
    }

    return when (filter) {
      CalendarItemFilter.Overall -> evaluateOverall(day, settings, s)
      CalendarItemFilter.PrayersWhole -> {
        val done = PrayerKey.entries.count { (day.prayers[it] ?: PrayerEntry()).status }
        val outcome = if (done == 5) DayOutcome.GREEN else DayOutcome.RED
        DayEvaluation(outcome, summaryBadge = "$done/5", reason = String.format(s.calendarFardSummaryFormat, done))
      }
      CalendarItemFilter.Fasting -> {
        when (day.fasting) {
          true -> DayEvaluation(DayOutcome.GREEN, summaryBadge = s.yesCheck)
          false -> DayEvaluation(DayOutcome.RED, summaryBadge = s.noCross)
          null -> DayEvaluation(DayOutcome.NEUTRAL)
        }
      }
      is CalendarItemFilter.Prayer -> {
        val p = day.prayers[filter.key] ?: PrayerEntry()
        if (p.status) {
          val timing = if (p.delayMinutes <= 15) s.calendarOnTimeShort else "${p.delayMinutes}m"
          DayEvaluation(DayOutcome.GREEN, summaryBadge = timing)
        } else {
          DayEvaluation(DayOutcome.RED, summaryBadge = "✕")
        }
      }
      CalendarItemFilter.RawatibAll -> {
        val count = PrayerKey.entries.count { (day.prayers[it] ?: PrayerEntry()).rawatibRead }
        val outcome = if (count == 5) DayOutcome.GREEN else DayOutcome.RED
        DayEvaluation(outcome, summaryBadge = "$count/5")
      }
      CalendarItemFilter.OntimeAll -> {
        val donePrayers = PrayerKey.entries.map { day.prayers[it] ?: PrayerEntry() }.filter { it.status }
        val ontimeCount = donePrayers.count { it.delayMinutes <= 15 }
        val outcome = if (donePrayers.size == 5 && ontimeCount == 5) DayOutcome.GREEN else DayOutcome.RED
        DayEvaluation(outcome, summaryBadge = "$ontimeCount/5")
      }
      CalendarItemFilter.KhushuAll -> {
        val donePrayers = PrayerKey.entries.map { day.prayers[it] ?: PrayerEntry() }.filter { it.status }
        val khushuCount = donePrayers.count { it.khushu && it.tumaninah }
        val outcome =
          if (donePrayers.isNotEmpty() && khushuCount == donePrayers.size) DayOutcome.GREEN
          else DayOutcome.RED
        DayEvaluation(outcome, summaryBadge = "$khushuCount/${donePrayers.size.coerceAtLeast(5)}")
      }
      CalendarItemFilter.NaflWhole -> {
        val done =
          listOf(day.nafl.duha.read, day.nafl.tahajjud.read, day.nafl.tauba.read, day.nafl.witrRead)
            .count { it }
        val outcome = if (done > 0) DayOutcome.GREEN else DayOutcome.RED
        DayEvaluation(outcome, summaryBadge = "$done/4")
      }
      is CalendarItemFilter.Nafl -> {
        val (read, count) =
          when (filter.key) {
            NaflKey.DUHA -> day.nafl.duha.read to day.nafl.duha.count
            NaflKey.TAHAJJUD -> day.nafl.tahajjud.read to day.nafl.tahajjud.count
            NaflKey.TAUBA -> day.nafl.tauba.read to day.nafl.tauba.count
            NaflKey.WITR -> day.nafl.witrRead to 3
          }
        if (read) DayEvaluation(DayOutcome.GREEN, summaryBadge = "$count ${s.rakShort}")
        else DayEvaluation(DayOutcome.RED, summaryBadge = "✕")
      }
      CalendarItemFilter.QuranWhole -> {
        val ok = day.quran.pages > 0 || day.quran.memorizedLines > 0
        DayEvaluation(
          outcome = if (ok) DayOutcome.GREEN else DayOutcome.RED,
          summaryBadge = "${day.quran.pages}/${day.quran.memorizedLines}",
        )
      }
      CalendarItemFilter.QuranRead -> {
        val p = day.quran.pages
        DayEvaluation(if (p > 0) DayOutcome.GREEN else DayOutcome.RED, summaryBadge = "$p ${s.pagesShort}")
      }
      CalendarItemFilter.QuranReflect -> {
        val p = day.quran.reflectPages
        DayEvaluation(if (p > 0) DayOutcome.GREEN else DayOutcome.RED, summaryBadge = "$p ${s.pagesShort}")
      }
      CalendarItemFilter.QuranLines -> {
        val l = day.quran.memorizedLines
        DayEvaluation(if (l > 0) DayOutcome.GREEN else DayOutcome.RED, summaryBadge = "$l")
      }
      CalendarItemFilter.AdhkarWhole -> {
        val count =
          listOf(day.adhkar.wake, day.adhkar.morning, day.adhkar.evening, day.adhkar.sleep).count { it }
        DayEvaluation(if (count == 4) DayOutcome.GREEN else DayOutcome.RED, summaryBadge = "$count/4")
      }
      is CalendarItemFilter.Adhkar -> {
        val ok =
          when (filter.key) {
            AdhkarKey.WAKE -> day.adhkar.wake
            AdhkarKey.MORNING -> day.adhkar.morning
            AdhkarKey.EVENING -> day.adhkar.evening
            AdhkarKey.SLEEP -> day.adhkar.sleep
          }
        DayEvaluation(if (ok) DayOutcome.GREEN else DayOutcome.RED, summaryBadge = if (ok) "✓" else "✕")
      }
      CalendarItemFilter.DhikrWhole -> {
        val hasDhikr = day.dhikr.istighfar > 0 || day.dhikr.salawat > 0
        val hasLapses = day.dhikr.noDhikrCount > 0 || day.dhikr.noTaharahCount > 0
        val outcome = if (hasDhikr && !hasLapses) DayOutcome.GREEN else DayOutcome.RED
        DayEvaluation(outcome, summaryBadge = "${day.dhikr.istighfar}/${day.dhikr.salawat}")
      }
      CalendarItemFilter.Istighfar -> {
        val c = day.dhikr.istighfar
        DayEvaluation(if (c > 0) DayOutcome.GREEN else DayOutcome.RED, summaryBadge = "$c")
      }
      CalendarItemFilter.Salawat -> {
        val c = day.dhikr.salawat
        DayEvaluation(if (c > 0) DayOutcome.GREEN else DayOutcome.RED, summaryBadge = "$c")
      }
      CalendarItemFilter.NoDhikr -> {
        val c = day.dhikr.noDhikrCount
        DayEvaluation(if (c == 0) DayOutcome.GREEN else DayOutcome.RED, summaryBadge = "$c")
      }
      CalendarItemFilter.NoTaharah -> {
        val c = day.dhikr.noTaharahCount
        DayEvaluation(if (c == 0) DayOutcome.GREEN else DayOutcome.RED, summaryBadge = "$c")
      }
      CalendarItemFilter.Truth -> {
        val ok = day.dhikr.truth
        DayEvaluation(if (ok) DayOutcome.GREEN else DayOutcome.RED, summaryBadge = if (ok) s.yesCheck else s.noCross)
      }
      CalendarItemFilter.Patience -> {
        val ok = day.dhikr.patience
        DayEvaluation(if (ok) DayOutcome.GREEN else DayOutcome.RED, summaryBadge = if (ok) s.yesCheck else s.noCross)
      }
      CalendarItemFilter.BooksWhole -> {
        val books = settings.books
        val totalPages = day.totalBookPages()
        if (books.isEmpty()) {
          DayEvaluation(
            if (totalPages > 0) DayOutcome.GREEN else DayOutcome.RED,
            summaryBadge = "$totalPages ${s.pagesShort}",
          )
        } else {
          val metCount = books.count { (day.bookPages[it.id] ?: 0) >= it.dailyPlan.coerceAtLeast(1) }
          val outcome = if (metCount == books.size) DayOutcome.GREEN else DayOutcome.RED
          DayEvaluation(outcome, summaryBadge = "$totalPages ${s.pagesShort}")
        }
      }
      is CalendarItemFilter.Book -> {
        val book = settings.books.firstOrNull { it.id == filter.bookId }
        val read = day.bookPages[filter.bookId] ?: 0
        val plan = book?.dailyPlan?.coerceAtLeast(1) ?: 1
        DayEvaluation(
          outcome = if (read >= plan) DayOutcome.GREEN else DayOutcome.RED,
          summaryBadge = "$read/$plan",
        )
      }
      CalendarItemFilter.AnalysisWhole -> evaluateAnalysisGroup(day, settings.questions.map { it.id }, s)
      CalendarItemFilter.AnalysisLimbsGroup ->
        evaluateAnalysisGroup(
          day,
          settings.questions.filter { it.cat == AnalysisCategory.LIMBS }.map { it.id },
          s,
        )
      CalendarItemFilter.AnalysisHeartGroup ->
        evaluateAnalysisGroup(
          day,
          settings.questions.filter { it.cat == AnalysisCategory.HEART }.map { it.id },
          s,
        )
      is CalendarItemFilter.Question -> {
        when (day.selfAnalysis[filter.questionId]) {
          true -> DayEvaluation(DayOutcome.GREEN, summaryBadge = s.analysisSafe)
          false -> DayEvaluation(DayOutcome.RED, summaryBadge = s.analysisMistake)
          null -> DayEvaluation(DayOutcome.NEUTRAL, summaryBadge = "—")
        }
      }
      CalendarItemFilter.TimerWhole -> {
        val ms = day.totalCarelessnessMs()
        val outcome = if (ms == 0L) DayOutcome.GREEN else DayOutcome.RED
        DayEvaluation(outcome, summaryBadge = DateUtils.formatMsToHHMMSS(ms).substring(0, 5))
      }
    }
  }

  private fun evaluateOverall(
    day: DayRecord,
    settings: AppSettings,
    s: AppStrings,
  ): DayEvaluation {
    val fardDone = PrayerKey.entries.count { (day.prayers[it] ?: PrayerEntry()).status }
    val sinnedCount = day.selfAnalysis.values.count { !it }
    val rakats = day.calculateRakats(settings.rawatibConfig).total

    if (sinnedCount > 0) {
      return DayEvaluation(
        outcome = DayOutcome.RED,
        summaryBadge = "$fardDone/5 · ✕$sinnedCount",
        reason = s.calendarReasonSinned,
      )
    }
    if (fardDone < 5) {
      return DayEvaluation(
        outcome = DayOutcome.RED,
        summaryBadge = "$fardDone/5",
        reason = s.calendarReasonMissedFard,
      )
    }
    return DayEvaluation(
      outcome = DayOutcome.GREEN,
      summaryBadge = "$rakats ${s.rakShort}",
      reason = s.calendarReasonAllGood,
    )
  }

  private fun evaluateAnalysisGroup(
    day: DayRecord,
    questionIds: List<String>,
    s: AppStrings,
  ): DayEvaluation {
    if (questionIds.isEmpty()) return DayEvaluation(DayOutcome.NEUTRAL)
    val answers = questionIds.mapNotNull { day.selfAnalysis[it] }
    if (answers.isEmpty()) return DayEvaluation(DayOutcome.NEUTRAL, summaryBadge = "—")
    val sinned = answers.count { !it }
    val safe = answers.count { it }
    return if (sinned > 0) {
      DayEvaluation(DayOutcome.RED, summaryBadge = "✕$sinned", reason = s.calendarReasonSinned)
    } else {
      DayEvaluation(DayOutcome.GREEN, summaryBadge = "✓$safe")
    }
  }
}
