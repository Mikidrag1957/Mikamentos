package com.mikamentos.app.data.model

import androidx.annotation.Keep

@Keep
data class UserSettings(
    val language: String = "es",
    val silenceSeconds: Int = 2,
    val is24HourFormat: Boolean = true,
    val userGender: String = "",
    val userName: String = "",
    val defaultRepetitions: Int = 1,
    val repetitionMinutes: Int = 1,
    val themeMode: Int = 0,
    val alarmRingtoneUri: String = "",
    val alarmRingtoneName: String = ""
)
