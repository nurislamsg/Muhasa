package com.example.data.json

import com.example.data.model.AdhkarData
import com.example.data.model.AnalysisCategory
import com.example.data.model.AnalysisQuestion
import com.example.data.model.AppSettings
import com.example.data.model.BookItem
import com.example.data.model.CarelessnessSession
import com.example.data.model.DayRecord
import com.example.data.model.DhikrData
import com.example.data.model.NaflData
import com.example.data.model.NaflEntry
import com.example.data.model.PrayerEntry
import com.example.data.model.PrayerKey
import com.example.data.model.QuranData
import com.example.data.model.RawatibConfig
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import org.json.JSONArray
import org.json.JSONObject

data class BackupPayload(val settings: AppSettings, val records: Map<String, DayRecord>)

object MuhasabaJsonCodec {

  fun encodeSettings(settings: AppSettings): String = settingsToJson(settings).toString()

  fun decodeSettings(jsonStr: String): AppSettings =
    runCatching { jsonToSettings(JSONObject(jsonStr)) }.getOrDefault(AppSettings())

  fun encodeDayRecord(record: DayRecord): String = dayRecordToJson(record).toString()

  fun decodeDayRecord(dateStr: String, jsonStr: String): DayRecord =
    runCatching { jsonToDayRecord(dateStr, JSONObject(jsonStr)) }
      .getOrDefault(DayRecord(dateStr = dateStr))

  fun encodeBackup(settings: AppSettings, records: Map<String, DayRecord>): String {
    val root = JSONObject()
    root.put("settings", settingsToJson(settings))
    val dbObj = JSONObject()
    for ((dateStr, record) in records) {
      dbObj.put(dateStr, dayRecordToJson(record))
    }
    root.put("db", dbObj)
    val isoFormat =
      SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
      }
    root.put("exportedAt", isoFormat.format(Date()))
    return root.toString(2)
  }

  fun decodeBackup(jsonStr: String): BackupPayload? {
    return runCatching {
      val root = JSONObject(jsonStr)
      if (!root.has("settings") || !root.has("db")) return null
      val settings = jsonToSettings(root.getJSONObject("settings"))
      val dbObj = root.getJSONObject("db")
      val records = mutableMapOf<String, DayRecord>()
      val keys = dbObj.keys()
      while (keys.hasNext()) {
        val dateStr = keys.next()
        val dayObj = dbObj.optJSONObject(dateStr) ?: continue
        records[dateStr] = jsonToDayRecord(dateStr, dayObj)
      }
      BackupPayload(settings = settings, records = records)
    }.getOrNull()
  }

  private fun settingsToJson(settings: AppSettings): JSONObject {
    val obj = JSONObject()
    val rCfg = JSONObject().apply {
      put("fajr", settings.rawatibConfig.fajr)
      put("zuhr", settings.rawatibConfig.zuhr)
      put("asr", settings.rawatibConfig.asr)
      put("maghrib", settings.rawatibConfig.maghrib)
      put("isha", settings.rawatibConfig.isha)
    }
    obj.put("rawatibConfig", rCfg)

    val booksArr = JSONArray()
    settings.books.forEach { b ->
      booksArr.put(
        JSONObject().apply {
          put("id", b.id)
          put("title", b.title)
          put("dailyPlan", b.dailyPlan)
        }
      )
    }
    obj.put("books", booksArr)

    val qArr = JSONArray()
    settings.questions.forEach { q ->
      qArr.put(
        JSONObject().apply {
          put("id", q.id)
          put("cat", q.cat.key)
          put("text", q.text)
        }
      )
    }
    obj.put("questions", qArr)
    return obj
  }

  private fun jsonToSettings(obj: JSONObject): AppSettings {
    val rObj = obj.optJSONObject("rawatibConfig")
    val rawatib =
      if (rObj != null) {
        RawatibConfig(
          fajr = rObj.optInt("fajr", 2),
          zuhr = rObj.optInt("zuhr", 6),
          asr = rObj.optInt("asr", 4),
          maghrib = rObj.optInt("maghrib", 2),
          isha = rObj.optInt("isha", 2),
        )
      } else {
        RawatibConfig()
      }

    val booksArr = obj.optJSONArray("books")
    val books =
      if (booksArr != null) {
        buildList {
          for (i in 0 until booksArr.length()) {
            val b = booksArr.optJSONObject(i) ?: continue
            add(
              BookItem(
                id = b.optString("id", "b_$i"),
                title = b.optString("title", ""),
                dailyPlan = b.optInt("dailyPlan", 5).coerceAtLeast(1),
              )
            )
          }
        }
      } else {
        AppSettings.defaultBooks()
      }

    val qArr = obj.optJSONArray("questions")
    val questions =
      if (qArr != null) {
        buildList {
          for (i in 0 until qArr.length()) {
            val q = qArr.optJSONObject(i) ?: continue
            add(
              AnalysisQuestion(
                id = q.optString("id", "q_$i"),
                cat = AnalysisCategory.fromKey(q.optString("cat", "limbs")),
                text = q.optString("text", ""),
              )
            )
          }
        }
      } else {
        AppSettings.defaultQuestions()
      }

    return AppSettings(rawatibConfig = rawatib, books = books, questions = questions)
  }

  private fun dayRecordToJson(record: DayRecord): JSONObject {
    val obj = JSONObject()
    if (record.fasting != null) {
      obj.put("fasting", record.fasting)
    }

    val prayersObj = JSONObject()
    PrayerKey.entries.forEach { key ->
      val p = record.prayers[key] ?: PrayerEntry()
      prayersObj.put(
        key.key,
        JSONObject().apply {
          put("status", p.status)
          put("delayMinutes", p.delayMinutes)
          put("rawatibRead", p.rawatibRead)
          put("tumaninah", p.tumaninah)
          put("khushu", p.khushu)
          put("ihsan", p.ihsan)
          put("extraRakats", p.extraRakats)
        },
      )
    }
    obj.put("prayers", prayersObj)

    val naflObj = JSONObject().apply {
      put("duha", naflEntryToJson(record.nafl.duha))
      put("tahajjud", naflEntryToJson(record.nafl.tahajjud))
      put("tauba", naflEntryToJson(record.nafl.tauba))
      put("witr", JSONObject().apply { put("read", record.nafl.witrRead) })
    }
    obj.put("nafl", naflObj)

    obj.put(
      "quran",
      JSONObject().apply {
        put("pages", record.quran.pages)
        put("reflectPages", record.quran.reflectPages)
        put("memorizedLines", record.quran.memorizedLines)
      },
    )

    obj.put(
      "adhkar",
      JSONObject().apply {
        put("wake", record.adhkar.wake)
        put("morning", record.adhkar.morning)
        put("evening", record.adhkar.evening)
        put("sleep", record.adhkar.sleep)
      },
    )

    obj.put(
      "dhikr",
      JSONObject().apply {
        put("istighfar", record.dhikr.istighfar)
        put("salawat", record.dhikr.salawat)
        put("noDhikrCount", record.dhikr.noDhikrCount)
        put("noTaharahCount", record.dhikr.noTaharahCount)
        put("truth", record.dhikr.truth)
        put("patience", record.dhikr.patience)
      },
    )

    val bookPagesObj = JSONObject()
    record.bookPages.forEach { (id, pages) -> bookPagesObj.put(id, pages) }
    obj.put("bookPages", bookPagesObj)

    val saObj = JSONObject()
    record.selfAnalysis.forEach { (id, value) -> saObj.put(id, value) }
    obj.put("selfAnalysis", saObj)

    val sessionsArr = JSONArray()
    record.carelessnessSessions.forEach { s ->
      sessionsArr.put(
        JSONObject().apply {
          put("start", s.startMs)
          put("end", s.endMs)
          put("durationMs", s.durationMs)
        }
      )
    }
    obj.put("carelessnessSessions", sessionsArr)

    return obj
  }

  private fun naflEntryToJson(entry: NaflEntry): JSONObject =
    JSONObject().apply {
      put("read", entry.read)
      put("count", entry.count)
    }

  private fun jsonToDayRecord(dateStr: String, obj: JSONObject): DayRecord {
    val fasting = if (obj.has("fasting") && !obj.isNull("fasting")) obj.optBoolean("fasting") else null

    val prayersObj = obj.optJSONObject("prayers")
    val prayers =
      PrayerKey.entries.associateWith { key ->
        val pObj = prayersObj?.optJSONObject(key.key)
        if (pObj != null) {
          PrayerEntry(
            status = pObj.optBoolean("status", false),
            delayMinutes = pObj.optInt("delayMinutes", 0),
            rawatibRead = pObj.optBoolean("rawatibRead", false),
            tumaninah = pObj.optBoolean("tumaninah", false),
            khushu = pObj.optBoolean("khushu", false),
            ihsan = pObj.optBoolean("ihsan", false),
            extraRakats = pObj.optInt("extraRakats", 0),
          )
        } else {
          PrayerEntry()
        }
      }

    val naflObj = obj.optJSONObject("nafl")
    val nafl =
      NaflData(
        duha = jsonToNaflEntry(naflObj?.optJSONObject("duha")),
        tahajjud = jsonToNaflEntry(naflObj?.optJSONObject("tahajjud")),
        tauba = jsonToNaflEntry(naflObj?.optJSONObject("tauba")),
        witrRead = naflObj?.optJSONObject("witr")?.optBoolean("read", false) ?: false,
      )

    val qObj = obj.optJSONObject("quran")
    val quran =
      QuranData(
        pages = qObj?.optInt("pages", 0) ?: 0,
        reflectPages = qObj?.optInt("reflectPages", 0) ?: 0,
        memorizedLines = qObj?.optInt("memorizedLines", 0) ?: 0,
      )

    val aObj = obj.optJSONObject("adhkar")
    val adhkar =
      AdhkarData(
        wake = aObj?.optBoolean("wake", false) ?: false,
        morning = aObj?.optBoolean("morning", false) ?: false,
        evening = aObj?.optBoolean("evening", false) ?: false,
        sleep = aObj?.optBoolean("sleep", false) ?: false,
      )

    val dObj = obj.optJSONObject("dhikr")
    val dhikr =
      DhikrData(
        istighfar = dObj?.optInt("istighfar", 0) ?: 0,
        salawat = dObj?.optInt("salawat", 0) ?: 0,
        noDhikrCount = dObj?.optInt("noDhikrCount", 0) ?: 0,
        noTaharahCount = dObj?.optInt("noTaharahCount", 0) ?: 0,
        truth = dObj?.optBoolean("truth", false) ?: false,
        patience = dObj?.optBoolean("patience", false) ?: false,
      )

    val bookPages = mutableMapOf<String, Int>()
    obj.optJSONObject("bookPages")?.let { bpObj ->
      val keys = bpObj.keys()
      while (keys.hasNext()) {
        val k = keys.next()
        bookPages[k] = bpObj.optInt(k, 0)
      }
    }

    val selfAnalysis = mutableMapOf<String, Boolean>()
    obj.optJSONObject("selfAnalysis")?.let { saObj ->
      val keys = saObj.keys()
      while (keys.hasNext()) {
        val k = keys.next()
        if (!saObj.isNull(k)) {
          selfAnalysis[k] = saObj.optBoolean(k, false)
        }
      }
    }

    val sessions = mutableListOf<CarelessnessSession>()
    obj.optJSONArray("carelessnessSessions")?.let { arr ->
      for (i in 0 until arr.length()) {
        val s = arr.optJSONObject(i) ?: continue
        sessions.add(
          CarelessnessSession(
            startMs = s.optLong("start", 0L),
            endMs = s.optLong("end", 0L),
            durationMs = s.optLong("durationMs", 0L),
          )
        )
      }
    }

    return DayRecord(
      dateStr = dateStr,
      fasting = fasting,
      prayers = prayers,
      nafl = nafl,
      quran = quran,
      adhkar = adhkar,
      dhikr = dhikr,
      bookPages = bookPages,
      selfAnalysis = selfAnalysis,
      carelessnessSessions = sessions,
    )
  }

  private fun jsonToNaflEntry(obj: JSONObject?): NaflEntry =
    if (obj != null) {
      NaflEntry(read = obj.optBoolean("read", false), count = obj.optInt("count", 2))
    } else {
      NaflEntry()
    }
}
