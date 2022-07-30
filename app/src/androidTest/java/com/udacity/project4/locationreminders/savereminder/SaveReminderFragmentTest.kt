package com.udacity.project4.locationreminders.savereminder

import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.udacity.project4.R
import org.hamcrest.core.Is.`is`
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.mockito.Mockito
import com.udacity.project4.locationreminders.*
import com.udacity.project4.locationreminders.utils.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource



@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@MediumTest
class SaveReminderFragmentTest {
    private lateinit var saveReminderViewModel: SaveReminderViewModel

    @get:Rule
    val instantTaskExecutorRule=InstantTaskExecutorRule()

    @Before
    fun initRepo(){
        stopKoin()

        val module= module {
            viewModel{
                SaveReminderViewModel(ApplicationProvider.getApplicationContext(),
                get() as ReminderDataSource)
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(ApplicationProvider.getApplicationContext()) }
        }
        startKoin {
            androidContext(ApplicationProvider.getApplicationContext())
            modules(listOf(module))
        }
        saveReminderViewModel=GlobalContext.get().koin.get()
    }

    @Test
    fun noTitleFails(){
        val navController=Mockito.mock(NavController::class.java)
        val scenario= launchFragmentInContainer<SaveReminderFragment>(Bundle.EMPTY, R.style.AppTheme)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!,navController)
        }
        onView(withId(R.id.saveReminder)).perform(ViewActions.click())
        onView(withId(R.id.snackbar_text)).check(matches(withText(R.string.err_enter_title)))
    }

    @Test
    fun noTitleSuccess(){
        val reminder= validReminderDataItem

        val navController=Mockito.mock(NavController::class.java)
        val scenario= launchFragmentInContainer<SaveReminderFragment>(Bundle.EMPTY, R.style.AppTheme)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!,navController)
        }
        onView(withId(R.id.reminderTitle)).perform(ViewActions.typeText(reminder.title))
        onView(withId(R.id.reminderDescription)).perform(ViewActions.typeText(reminder.description))

        saveReminderViewModel.saveReminder(reminder)

        assertThat(saveReminderViewModel.showToast.getOrAwaitValue(),`is`("Reminder Saved"))
    }

    @Test
    fun saveReminder(){
        val reminder=validReminderDataItem

        val navController=Mockito.mock(NavController::class.java)
        val scenario= launchFragmentInContainer<SaveReminderFragment>(Bundle.EMPTY,R.style.AppTheme)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!,navController)
        }

        onView(withId(R.id.reminderTitle)).perform(ViewActions.typeText(reminder.title))
        onView(withId(R.id.reminderDescription)).perform(ViewActions.typeText(reminder.description))

        saveReminderViewModel.saveReminder(reminder)

        assertThat(saveReminderViewModel.showToast.getOrAwaitValue(),`is`("Reminder Saved!"))

    }
}