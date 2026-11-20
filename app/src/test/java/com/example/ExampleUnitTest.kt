package com.example

import com.example.data.model.AppSettings
import com.example.data.model.DayRecord
import com.example.data.model.NaflData
import com.example.data.model.NaflEntry
import com.example.data.model.PrayerEntry
import com.example.data.model.PrayerKey
import com.example.data.model.QuranData
import com.example.data.model.RawatibConfig
import com.example.ui.calendar.CalendarBlock
import com.example.ui.calendar.CalendarItemFilter
import com.example.ui.calendar.CalendarLogic
import com.example.ui.calendar.DayOutcome
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.stringsFor
import com.example.util.DateUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun rakatCalculation_computesFardRawatibAndNaflCorrectly() {
    val config = RawatibConfig(fajr = 2, zuhr = 6, asr = 4, maghrib = 2, isha = 2)
    val day =
      DayRecord(
        dateStr = "2026-11-20",
        prayers =
          mapOf(
            PrayerKey.FAJR to PrayerEntry(status = true, rawatibRead = true, extraRakats = 2),
            PrayerKey.ZUHR to PrayerEntry(status = true, rawatibRead = false),
          ),
        nafl =
          NaflData(
            duha = NaflEntry(read = true, count = 4),
            witrRead = true,
          ),
      )

    val breakdown = day.calculateRakats(config)
    assertEquals(6, breakdown.fard) // 2 + 4
    assertEquals(2, breakdown.rawatib) // Fajr 2
    assertEquals(9, breakdown.nafl) // 2 extra + 4 duha + 3 witr
    assertEquals(17, breakdown.total)
  }

  @Test
  fun uzbekLocalization_translatesStringsDatesAndFormatSpecifiers() {
    val uz = stringsFor(AppLanguage.UZ)
    val ru = stringsFor(AppLanguage.RU)

    assertEquals("Уберёг ✓", ru.analysisSafe)
    assertEquals("Согрешил ✕", ru.analysisMistake)
    assertEquals("Saqlandim ✓", uz.analysisSafe)
    assertEquals("Gunoh qildim ✕", uz.analysisMistake)

    assertEquals("Muhosaba kundaligi", uz.appTitle)
    assertEquals("Bomdod", uz.prayerName(PrayerKey.FAJR))
    assertEquals("Peshin", uz.prayerName(PrayerKey.ZUHR))
    assertEquals("Xufton", uz.prayerName(PrayerKey.ISHA))
    assertEquals("Nafsni poklash", uz.defaultBookTitleOrCustom("b1", "Очищение души"))
    assertEquals("Mening kitobim", uz.defaultBookTitleOrCustom("b1", "Mening kitobim"))
    assertEquals("Til g‘iybat qildimi?", uz.defaultQuestionTextOrCustom("q1", "Произносил ли язык сплетни?"))

    val formattedUzDate = DateUtils.formatDisplayDate("2026-11-20", AppLanguage.UZ)
    assertEquals("20 noy 2026, Ju", formattedUzDate)

    // Verify all format strings work safely for both languages without FormatException
    for (s in listOf(ru, uz)) {
      assertTrue(String.format(s.bannerTimerPrefix, "00:05:10").isNotEmpty())
      assertTrue(String.format(s.fardLabel, 20).isNotEmpty())
      assertTrue(String.format(s.yesterdayRakatsCompare, 12, "+4").isNotEmpty())
      assertTrue(String.format(s.weeklyCompare, "100", "+10").isNotEmpty())
      assertTrue(String.format(s.quranYesterdayFormat, 2, 10).isNotEmpty())
      assertTrue(String.format(s.bookPlanRemainingFormat, 5).isNotEmpty())
      assertTrue(String.format(s.booksYesterdayCompareFormat, 10, "+2").isNotEmpty())
      assertTrue(String.format(s.timerTotalFormat, "00:15:00").isNotEmpty())
      assertTrue(String.format(s.reportVsYesterdayFormat, 12).isNotEmpty())
      assertTrue(String.format(s.reportAvgPerDayFormat, "14.5").isNotEmpty())
      assertTrue(String.format(s.reportVsPrevPeriodFormat, "+5").isNotEmpty())
      assertTrue(String.format(s.reportPerDayIntFormat, "50").isNotEmpty())
      assertTrue(String.format(s.reportMistakeCountFormat, 3).isNotEmpty())
      assertTrue(String.format(s.settingsRawatibRowFormat, s.prayerName(PrayerKey.FAJR)).isNotEmpty())
      assertTrue(String.format(s.toastTimerStoppedFormat, "00:02:00").isNotEmpty())
      assertTrue(String.format(s.calendarGreenDaysCountFormat, 4).isNotEmpty())
      assertTrue(String.format(s.calendarRedDaysCountFormat, 2).isNotEmpty())
      assertTrue(String.format(s.calendarFardSummaryFormat, 5).isNotEmpty())
    }
  }

  @Test
  fun calendarLogic_evaluatesGreenRedAndSpecificItemFiltersCorrectly() {
    val settings = AppSettings()
    val ru = stringsFor(AppLanguage.RU)

    // Empty day is NEUTRAL
    val emptyEval = CalendarLogic.evaluateDay(null, settings, CalendarItemFilter.Overall, ru)
    assertEquals(DayOutcome.NEUTRAL, emptyEval.outcome)

    // All 5 prayers done and self-analysis safe -> GREEN overall
    val allFivePrayers = PrayerKey.entries.associateWith { PrayerEntry(status = true) }
    val goodDay =
      DayRecord(
        dateStr = "2026-11-20",
        prayers = allFivePrayers,
        quran = QuranData(pages = 3),
        bookPages = mapOf("b1" to 6),
        selfAnalysis = mapOf("q1" to true, "q5" to true),
      )
    assertEquals(
      DayOutcome.GREEN,
      CalendarLogic.evaluateDay(goodDay, settings, CalendarItemFilter.Overall, ru).outcome,
    )

    // Sinned on one question -> RED overall & RED on that question, GREEN on Quran
    val sinnedDay = goodDay.copy(selfAnalysis = mapOf("q1" to true, "q5" to false))
    assertEquals(
      DayOutcome.RED,
      CalendarLogic.evaluateDay(sinnedDay, settings, CalendarItemFilter.Overall, ru).outcome,
    )
    assertEquals(
      DayOutcome.RED,
      CalendarLogic.evaluateDay(sinnedDay, settings, CalendarItemFilter.Question("q5"), ru).outcome,
    )
    assertEquals(
      DayOutcome.GREEN,
      CalendarLogic.evaluateDay(sinnedDay, settings, CalendarItemFilter.Question("q1"), ru).outcome,
    )
    assertEquals(
      DayOutcome.GREEN,
      CalendarLogic.evaluateDay(sinnedDay, settings, CalendarItemFilter.QuranRead, ru).outcome,
    )
    assertEquals(
      DayOutcome.GREEN,
      CalendarLogic.evaluateDay(sinnedDay, settings, CalendarItemFilter.Book("b1"), ru).outcome,
    )
    assertEquals(
      DayOutcome.RED,
      CalendarLogic.evaluateDay(sinnedDay, settings, CalendarItemFilter.Book("b2"), ru).outcome,
    )

    // All blocks expose selectable items
    assertTrue(CalendarLogic.itemsForBlock(CalendarBlock.BOOKS, settings, ru).size >= 3)
    assertTrue(CalendarLogic.itemsForBlock(CalendarBlock.ANALYSIS, settings, ru).size >= 14)

    // November 2026 starts on Sunday (index 6 in Monday-first grid) and has 30 days
    val grid = DateUtils.daysInMonthGrid(2026, 11)
    assertEquals(0, grid.size % 7)
    assertEquals(6, grid.indexOf("2026-11-01"))
    assertTrue(grid.contains("2026-11-30"))
  }
}
