package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.StudyDatabase
import com.example.data.repository.StudyRepository
import com.example.ui.viewmodel.StudyViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Medical Study Companion", appName)
  }

  @Test
  fun testAppInitialization() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val database = StudyDatabase.getDatabase(context)
    val repository = StudyRepository(database.studyDao())
    val viewModel = StudyViewModel(repository)
    assertNotNull(viewModel)
  }

  @Test
  fun testFlashcardDeckSelection() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val database = StudyDatabase.getDatabase(context)
    val repository = StudyRepository(database.studyDao())
    val viewModel = StudyViewModel(repository)

    // Initially selected deck should be null
    assertEquals(null, viewModel.selectedDeck.value)

    // Selecting a deck updates state
    viewModel.selectDeck("Cardiology")
    assertEquals("Cardiology", viewModel.selectedDeck.value)

    // Clearing selection sets it back to null
    viewModel.selectDeck(null)
    assertEquals(null, viewModel.selectedDeck.value)
  }
}
