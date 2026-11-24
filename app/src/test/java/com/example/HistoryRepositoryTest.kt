package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.HistoryRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class HistoryRepositoryTest {
  @Test
  fun historySurvivesRepositoryRecreationAndCanBeCleared() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    context.getSharedPreferences("calcora_calculation_history", Context.MODE_PRIVATE).edit().clear().commit()

    val repository = HistoryRepository(context)
    repository.addEntry("2 + 2", "4", "Calculator", "Science", "science", "2 + 2")
    val saved = repository.entries.value.single()

    val restored = HistoryRepository(context).entries.value.single()
    assertEquals(saved.expression, restored.expression)
    assertEquals(saved.result, restored.result)
    assertEquals(saved.route, restored.route)
    assertEquals(saved.reusableInput, restored.reusableInput)
    assertNotNull(restored.timestampLabel)

    HistoryRepository(context).clearAll()
    assertEquals(0, HistoryRepository(context).entries.value.size)
  }
}