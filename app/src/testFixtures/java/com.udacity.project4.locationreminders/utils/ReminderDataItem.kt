package com.udacity.project4.locationreminders.utils

import com.google.android.gms.maps.model.LatLng
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

private val bengaluru=LatLng(12.971599,77.594566)
val validReminderDataItem=ReminderDataItem(
    "Title",
    "Description",
    "Bengaluru",
    bengaluru.latitude,
    bengaluru.longitude
)
val validReminderDTO=ReminderDTO(
    "Title",
    "Description",
    "Bengaluru",
    bengaluru.latitude,
    bengaluru.longitude
)
val nullReminderDataItem=ReminderDataItem(
    "title",
    "description",
    null,
    null,
    null
)
val nullLocationReminderDataItem=ReminderDataItem(
    title = null,
    description = null,
    location = null,
    latitude = null,
    longitude = null
)
val titleNullReminderDataItem=ReminderDataItem(
    null,"Description","Bengaluru", bengaluru.latitude, bengaluru.longitude
)
val latAndLngNullReminderDataItem=ReminderDataItem(
    "Title","Description","Bengaluru", null, null
)
