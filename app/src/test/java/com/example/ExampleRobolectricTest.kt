package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.json.MuhasabaJsonCodec
import com.example.data.model.AppSettings
import com.example.data.model.DayRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Дневник Мухасабы", appName)
  }

  @Test
  fun `json backup round trip preserves settings and day data`() {
    val settings = AppSettings()
    val day =
      DayRecord(
        dateStr = "2026-11-20",
        fasting = true,
        bookPages = mapOf("b1" to 7),
        selfAnalysis = mapOf("q1" to true, "q5" to false),
      )
    val json = MuhasabaJsonCodec.encodeBackup(settings, mapOf("2026-11-20" to day))
    val decoded = MuhasabaJsonCodec.decodeBackup(json)

    assertNotNull(decoded)
    assertEquals(2, decoded!!.settings.books.size)
    assertEquals(true, decoded.records["2026-11-20"]?.fasting)
    assertEquals(7, decoded.records["2026-11-20"]?.bookPages?.get("b1"))
    assertEquals(false, decoded.records["2026-11-20"]?.selfAnalysis?.get("q5"))
  }
}
