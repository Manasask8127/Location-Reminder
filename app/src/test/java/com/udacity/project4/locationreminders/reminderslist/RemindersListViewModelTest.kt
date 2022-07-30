package com.udacity.project4.locationreminders.reminderslist

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.utils.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.utils.getOrAwaitValue
import com.udacity.project4.locationreminders.utils.validReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resumeDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    //TODO: provide testing to the RemindersListViewModel and its live data objects
    @get:Rule
    var instantTaskExecutorRule=InstantTaskExecutorRule()

    @get:Rule
    val mainCoroutineRule= MainCoroutineRule()

    private lateinit var fakeDataSource: FakeDataSource
    private lateinit var remindersListViewModel: RemindersListViewModel

    @Before
    fun setUpFakeRepositoryAndViewModel(){
        MockitoAnnotations.initMocks(this)
        fakeDataSource= FakeDataSource()
        remindersListViewModel= RemindersListViewModel(ApplicationProvider.getApplicationContext(),fakeDataSource)
    }

    @Test
    fun fetchAndLoadReminders()=mainCoroutineRule.runBlockingTest{

        fakeDataSource.deleteAllReminders()
        val reminderDataItem= validReminderDTO
        fakeDataSource.saveReminder(reminderDataItem)

        mainCoroutineRule.dispatcher.pauseDispatcher()
        remindersListViewModel.loadReminders()

        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(),`is`(true))
        mainCoroutineRule.resumeDispatcher()

        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(),`is`(false))
        assertThat(remindersListViewModel.showNoData.getOrAwaitValue(),`is`(false))
    }

    @Test
    fun loadRemindersWithEmptyLit()=mainCoroutineRule.runBlockingTest{
        fakeDataSource.deleteAllReminders()
        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.showNoData.getOrAwaitValue(),`is`(true))
    }


    @Test
    fun loadRemindersWhenUnavailable()= runBlockingTest {
        fakeDataSource.setShouldReturnError(true)
        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.showSnackBar.getOrAwaitValue(),`is`("Reminders not found"))
    }

}