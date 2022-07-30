package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {

    //    TODO: Create a fake data source to act as a double to the real data source
    private var reminderDataItem:MutableList<ReminderDTO> = mutableListOf()

    private var shouldReturnError=false

    fun setShouldReturnError(shouldReturn:Boolean){
        this.shouldReturnError=shouldReturn
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if(shouldReturnError){
            return Result.Error("Reminders not found")
        }
        else
        {
            return Result.Success(reminderDataItem)
        }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminderDataItem.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if(shouldReturnError){
            return Result.Error("Reminder not found")
        }
        else{
            val reminder=reminderDataItem.find { it.id==id }
            if (reminder!=null){
                return Result.Success(reminder)
            }
            else{
                return Result.Error("Not found")
            }
        }

    }

    override suspend fun deleteAllReminders() {
        reminderDataItem.clear()
    }


}