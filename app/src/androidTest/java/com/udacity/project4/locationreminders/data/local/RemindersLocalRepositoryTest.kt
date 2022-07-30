package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.android.gms.maps.model.LatLng
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.udacity.project4.locationreminders.*
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.MainAndroidCoroutineRule

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

//    TODO: Add testing implementation to the RemindersLocalRepository.kt
    @get:Rule
    val instantTaskExecutorRule=InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase
    private lateinit var repository: RemindersLocalRepository


    private val bengaluru= LatLng(12.971599,77.594566)

    @get:Rule
    val mainCoroutineRule= MainAndroidCoroutineRule()

    @Before
    fun initDBAndRepository(){
        database=Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(),
        RemindersDatabase::class.java).build()
        repository= RemindersLocalRepository(database.reminderDao(),Dispatchers.Main)
    }

    @After
    fun closeDB(){
        database.close()
    }

    @Test
    fun savereminder()= runBlocking {
        val validReminderDTO=ReminderDTO(
        "Title",
        "Description",
        "Bengaluru",
        bengaluru.latitude,
        bengaluru.longitude
    )

        repository.saveReminder(validReminderDTO)
        val retrievedReminderDTO=repository.getReminder(validReminderDTO.id)

        assertThat(retrievedReminderDTO is Result.Success, notNullValue())
        retrievedReminderDTO as Result.Success

        assertThat(retrievedReminderDTO.data.title,`is`(validReminderDTO.title))
        assertThat(retrievedReminderDTO.data.description,`is`(validReminderDTO.description))
        assertThat(retrievedReminderDTO.data.location,`is`(validReminderDTO.location))
        assertThat(retrievedReminderDTO.data.latitude,`is`(validReminderDTO.latitude))
        assertThat(retrievedReminderDTO.data.longitude,`is`(validReminderDTO.longitude))

    }

    @Test
    fun getRemainderDataNotFound()= runBlocking {
        val validateReminderDTO= ReminderDTO(
            "Title",
            "Description",
            "Bengaluru",
            bengaluru.latitude,
            bengaluru.longitude)
        val retrieveReminderDTO=repository.getReminder(validateReminderDTO.id)
        assertThat(retrieveReminderDTO is Result.Error, notNullValue())
        retrieveReminderDTO as Result.Error
        assertThat(retrieveReminderDTO.message,`is`("Reminder not found!"))
    }

}