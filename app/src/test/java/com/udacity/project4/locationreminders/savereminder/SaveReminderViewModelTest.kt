package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.utils.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    @get:Rule
    var instantTaskExecutorRule=InstantTaskExecutorRule()

    @get:Rule
    val mainCoroutineRule= MainCoroutineRule()

    private lateinit var fakeDataSource: FakeDataSource
    private lateinit var saveReminderViewModel: SaveReminderViewModel

    @Before
    fun setUpFakeDataSourceAndViewModel(){
        fakeDataSource= FakeDataSource()
        saveReminderViewModel= SaveReminderViewModel(ApplicationProvider.getApplicationContext(),fakeDataSource)
    }


    //TODO: provide testing to the SaveReminderView and its live data objects

    @Test
    fun validateSaveReminderAndReminderData(){
        val reminderDataItem= validReminderDataItem

        val saved:Boolean=saveReminderViewModel.validateAndSaveReminder(reminderDataItem)
        assertThat(saved,`is`(true))
    }

    @Test
    fun validateSaveReminderAndNullReminderData(){
        val reminderDataItem= nullReminderDataItem

        val saved:Boolean=saveReminderViewModel.validateAndSaveReminder(reminderDataItem)
        assertThat(saved,`is`(false))

    }

    @Test
    fun saveRemainderValidateReminderDataShowToast(){
        val reminderDataItem= nullReminderDataItem

        saveReminderViewModel.saveReminder(reminderDataItem)
        assertThat(saveReminderViewModel.showToast.getOrAwaitValue(),`is`("Reminder Saved !"))
    }

    @Test
    fun ValidateEnteredDataLatAndLngNull(){
        val reminderDataItem= latAndLngNullReminderDataItem

        val boolean:Boolean=saveReminderViewModel.validateEnteredData(reminderDataItem)
        assertThat(boolean,`is`(true))
    }

    @Test
    fun ValidateEnteredDataTitleNull(){
        val reminderDataItem= titleNullReminderDataItem

        val boolean:Boolean=saveReminderViewModel.validateEnteredData(reminderDataItem)
        assertThat(boolean,`is`(false))
    }


}