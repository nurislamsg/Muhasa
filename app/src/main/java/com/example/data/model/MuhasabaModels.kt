package com.example.data.model

enum class PrayerKey(val key: String, val ruName: String, val fardRakats: Int) {
  FAJR("fajr", "Фаджр", 2),
  ZUHR("zuhr", "Зухр", 4),
  ASR("asr", "Аср", 4),
  MAGHRIB("maghrib", "Магриб", 3),
  ISHA("isha", "Иша", 4);

  companion object {
    fun fromKey(key: String): PrayerKey = entries.firstOrNull { it.key == key } ?: FAJR
  }
}

enum class PrayerQuality {
  TUMANINAH,
  KHUSHU,
  IHSAN,
}

enum class AdhkarKey {
  WAKE,
  MORNING,
  EVENING,
  SLEEP,
}

enum class NaflKey {
  DUHA,
  TAHAJJUD,
  TAUBA,
  WITR,
}

data class PrayerEntry(
  val status: Boolean = false,
  val delayMinutes: Int = 0,
  val rawatibRead: Boolean = false,
  val tumaninah: Boolean = false,
  val khushu: Boolean = false,
  val ihsan: Boolean = false,
  val extraRakats: Int = 0,
)

data class NaflEntry(val read: Boolean = false, val count: Int = 2)

data class NaflData(
  val duha: NaflEntry = NaflEntry(),
  val tahajjud: NaflEntry = NaflEntry(),
  val tauba: NaflEntry = NaflEntry(),
  val witrRead: Boolean = false,
)

data class QuranData(
  val pages: Int = 0,
  val reflectPages: Int = 0,
  val memorizedLines: Int = 0,
)

data class AdhkarData(
  val wake: Boolean = false,
  val morning: Boolean = false,
  val evening: Boolean = false,
  val sleep: Boolean = false,
)

data class DhikrData(
  val istighfar: Int = 0,
  val salawat: Int = 0,
  val noDhikrCount: Int = 0,
  val noTaharahCount: Int = 0,
  val truth: Boolean = false,
  val patience: Boolean = false,
)

data class CarelessnessSession(val startMs: Long, val endMs: Long, val durationMs: Long)

data class BookItem(val id: String, val title: String, val dailyPlan: Int)

enum class AnalysisCategory(val key: String, val ruLabel: String, val badgeLabel: String) {
  LIMBS("limbs", "Защита органов", "Органы"),
  HEART("heart", "Мысли и болезни сердца", "Сердце");

  companion object {
    fun fromKey(key: String): AnalysisCategory =
      entries.firstOrNull { it.key == key } ?: LIMBS
  }
}

data class AnalysisQuestion(val id: String, val cat: AnalysisCategory, val text: String)

data class RawatibConfig(
  val fajr: Int = 2,
  val zuhr: Int = 6,
  val asr: Int = 4,
  val maghrib: Int = 2,
  val isha: Int = 2,
) {
  fun forPrayer(key: PrayerKey): Int =
    when (key) {
      PrayerKey.FAJR -> fajr
      PrayerKey.ZUHR -> zuhr
      PrayerKey.ASR -> asr
      PrayerKey.MAGHRIB -> maghrib
      PrayerKey.ISHA -> isha
    }
}

data class AppSettings(
  val rawatibConfig: RawatibConfig = RawatibConfig(),
  val books: List<BookItem> = defaultBooks(),
  val questions: List<AnalysisQuestion> = defaultQuestions(),
) {
  companion object {
    fun defaultBooks(): List<BookItem> =
      listOf(
        BookItem(id = "b1", title = "Очищение души", dailyPlan = 5),
        BookItem(id = "b2", title = "Сады праведных", dailyPlan = 5),
      )

    fun defaultQuestions(): List<AnalysisQuestion> =
      listOf(
        AnalysisQuestion("q1", AnalysisCategory.LIMBS, "Произносил ли язык сплетни?"),
        AnalysisQuestion("q2", AnalysisCategory.LIMBS, "Произносил ли язык ложь?"),
        AnalysisQuestion("q3", AnalysisCategory.LIMBS, "Слушали ли уши запретное?"),
        AnalysisQuestion("q4", AnalysisCategory.LIMBS, "Смотрели ли глаза на запретное?"),
        AnalysisQuestion("q5", AnalysisCategory.HEART, "Были ли греховные мысли?"),
        AnalysisQuestion("q6", AnalysisCategory.HEART, "Была ли показуха (рия) в моих действиях?"),
        AnalysisQuestion("q7", AnalysisCategory.HEART, "Было ли самолюбование (уджб)?"),
        AnalysisQuestion("q8", AnalysisCategory.HEART, "Было ли высокомерие?"),
        AnalysisQuestion("q9", AnalysisCategory.HEART, "Была ли жадность?"),
        AnalysisQuestion("q10", AnalysisCategory.HEART, "Был ли гнев?"),
        AnalysisQuestion("q11", AnalysisCategory.HEART, "Была ли обида у меня?"),
      )
  }
}

data class DayRecord(
  val dateStr: String,
  val fasting: Boolean? = null,
  val prayers: Map<PrayerKey, PrayerEntry> = defaultPrayers(),
  val nafl: NaflData = NaflData(),
  val quran: QuranData = QuranData(),
  val adhkar: AdhkarData = AdhkarData(),
  val dhikr: DhikrData = DhikrData(),
  val bookPages: Map<String, Int> = emptyMap(),
  val selfAnalysis: Map<String, Boolean> = emptyMap(),
  val carelessnessSessions: List<CarelessnessSession> = emptyList(),
) {
  companion object {
    fun defaultPrayers(): Map<PrayerKey, PrayerEntry> =
      PrayerKey.entries.associateWith { PrayerEntry() }
  }

  fun calculateRakats(rawatibConfig: RawatibConfig): RakatBreakdown {
    var fard = 0
    var rawatib = 0
    var naflSum = 0

    for (key in PrayerKey.entries) {
      val entry = prayers[key] ?: PrayerEntry()
      if (entry.status) fard += key.fardRakats
      if (entry.rawatibRead) rawatib += rawatibConfig.forPrayer(key)
      if (entry.extraRakats > 0) naflSum += entry.extraRakats
    }

    if (nafl.duha.read) naflSum += nafl.duha.count.coerceAtLeast(0)
    if (nafl.tahajjud.read) naflSum += nafl.tahajjud.count.coerceAtLeast(0)
    if (nafl.tauba.read) naflSum += nafl.tauba.count.coerceAtLeast(0)
    if (nafl.witrRead) naflSum += 3

    return RakatBreakdown(fard = fard, rawatib = rawatib, nafl = naflSum)
  }

  fun totalBookPages(): Int = bookPages.values.sum()

  fun totalCarelessnessMs(): Long = carelessnessSessions.sumOf { it.durationMs }
}

data class RakatBreakdown(val fard: Int, val rawatib: Int, val nafl: Int) {
  val total: Int
    get() = fard + rawatib + nafl
}
