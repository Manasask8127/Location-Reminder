package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

//    TODO: Add testing implementation to the RemindersDao.kt
    @get:Rule
    val instantTaskExecutorRule=InstantTaskExecutorRule()

    private lateinit var database:RemindersDatabase

    @Before
    fun initDB(){
        database=Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDB(){
        database.close()
    }

    @Test
    fun saveAndRetrieveRemainder()= runBlockingTest {
        val reminderDTO=validReminderDTO
        database.reminderDao().saveReminder(reminderDTO)

        val retrieveDTO=database.reminderDao().getReminderById(reminderDTO.id)

        assertThat<ReminderDTO>(retrieveDTO as ReminderDTO, notNullValue())
        assertThat(retrieveDTO.title,`is`(reminderDTO.title))
        assertThat(retrieveDTO.description,`is`(reminderDTO.description))
        assertThat(retrieveDTO.location,`is`(reminderDTO.location))
        assertThat(retrieveDTO.latitude,`is`(reminderDTO.latitude))
        assertThat(retrieveDTO.longitude,`is`(reminderDTO.longitude))
    }

}