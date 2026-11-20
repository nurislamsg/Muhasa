package com.example.util

import com.example.ui.i18n.AppLanguage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
  private val isoFormat: SimpleDateFormat
    get() = SimpleDateFormat("yyyy-MM-dd", Locale.US)

  private val displayFormatRu: SimpleDateFormat
    get() = SimpleDateFormat("d MMM yyyy, EEE", Locale("ru"))

  private val timeFormat: SimpleDateFormat
    get() = SimpleDateFormat("HH:mm", Locale.getDefault())

  private val uzMonths =
    arrayOf("yan", "fev", "mar", "apr", "may", "iyun", "iyul", "avg", "sen", "okt", "noy", "dek")
  private val uzDays = arrayOf("", "Ya", "Du", "Se", "Ch", "Pa", "Ju", "Sh")

  fun todayStr(): String = isoFormat.format(Date())

  fun shiftDate(dateStr: String, deltaDays: Int): String {
    val date = runCatching { isoFormat.parse(dateStr) }.getOrNull() ?: Date()
    val cal = Calendar.getInstance().apply {
      time = date
      add(Calendar.DAY_OF_YEAR, deltaDays)
    }
    return isoFormat.format(cal.time)
  }

  fun toEpochMillis(dateStr: String): Long {
    val date = runCatching { isoFormat.parse(dateStr) }.getOrNull() ?: Date()
    return date.time
  }

  fun fromEpochMillis(millis: Long): String = isoFormat.format(Date(millis))

  fun formatDisplayDate(dateStr: String, language: AppLanguage = AppLanguage.RU): String {
    val date = runCatching { isoFormat.parse(dateStr) }.getOrNull() ?: return dateStr
    return when (language) {
      AppLanguage.RU -> displayFormatRu.format(date)
      AppLanguage.UZ -> {
        val cal = Calendar.getInstance().apply { time = date }
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val month = uzMonths[cal.get(Calendar.MONTH)]
        val year = cal.get(Calendar.YEAR)
        val dow = uzDays[cal.get(Calendar.DAY_OF_WEEK)]
        "$day $month $year, $dow"
      }
    }
  }

  fun formatTimeHHMM(epochMs: Long): String = timeFormat.format(Date(epochMs))

  private val ruFullMonths =
    arrayOf(
      "Январь",
      "Февраль",
      "Март",
      "Апрель",
      "Май",
      "Июнь",
      "Июль",
      "Август",
      "Сентябрь",
      "Октябрь",
      "Ноябрь",
      "Декабрь",
    )

  private val uzFullMonths =
    arrayOf(
      "Yanvar",
      "Fevral",
      "Mart",
      "Aprel",
      "May",
      "Iyun",
      "Iyul",
      "Avgust",
      "Sentabr",
      "Oktabr",
      "Noyabr",
      "Dekabr",
    )

  fun formatMsToHHMMSS(ms: Long): String {
    val totalSec = (ms.coerceAtLeast(0L)) / 1000L
    val h = totalSec / 3600L
    val m = (totalSec % 3600L) / 60L
    val s = totalSec % 60L
    return String.format(Locale.US, "%02d:%02d:%02d", h, m, s)
  }

  fun yearMonthOf(dateStr: String): Pair<Int, Int> {
    val date = runCatching { isoFormat.parse(dateStr) }.getOrNull() ?: Date()
    val cal = Calendar.getInstance().apply { time = date }
    return cal.get(Calendar.YEAR) to (cal.get(Calendar.MONTH) + 1)
  }

  fun shiftMonth(year: Int, month: Int, deltaMonths: Int): Pair<Int, Int> {
    val cal = Calendar.getInstance().apply {
      set(Calendar.YEAR, year)
      set(Calendar.MONTH, month - 1)
      set(Calendar.DAY_OF_MONTH, 1)
      add(Calendar.MONTH, deltaMonths)
    }
    return cal.get(Calendar.YEAR) to (cal.get(Calendar.MONTH) + 1)
  }

  fun daysInMonth(year: Int, month: Int): List<String> {
    val cal = Calendar.getInstance().apply {
      set(Calendar.YEAR, year)
      set(Calendar.MONTH, month - 1)
      set(Calendar.DAY_OF_MONTH, 1)
    }
    val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    return (1..maxDay).map { day ->
      String.format(Locale.US, "%04d-%02d-%02d", year, month, day)
    }
  }

  fun daysInMonthGrid(year: Int, month: Int): List<String?> {
    val cal = Calendar.getInstance().apply {
      set(Calendar.YEAR, year)
      set(Calendar.MONTH, month - 1)
      set(Calendar.DAY_OF_MONTH, 1)
    }
    // Calendar.DAY_OF_WEEK: Sun=1, Mon=2, ..., Sat=7 -> Monday-first offset 0..6
    val dow = cal.get(Calendar.DAY_OF_WEEK)
    val leadingBlanks = (dow + 5) % 7
    val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val result = MutableList<String?>(leadingBlanks) { null }
    for (day in 1..maxDay) {
      result.add(String.format(Locale.US, "%04d-%02d-%02d", year, month, day))
    }
    while (result.size % 7 != 0) {
      result.add(null)
    }
    return result
  }

  fun formatMonthYear(year: Int, month: Int, language: AppLanguage): String {
    val idx = (month - 1).coerceIn(0, 11)
    val mName = if (language == AppLanguage.UZ) uzFullMonths[idx] else ruFullMonths[idx]
    return "$mName $year"
  }

  fun weekdayShortHeaders(language: AppLanguage): List<String> =
    if (language == AppLanguage.UZ) {
      listOf("Du", "Se", "Ch", "Pa", "Ju", "Sh", "Ya")
    } else {
      listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
    }
}
