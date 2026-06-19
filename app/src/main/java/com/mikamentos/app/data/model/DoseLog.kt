package com.mikamentos.app.data.model

import androidx.annotation.Keep

@Keep
data class DoseLog(
    val medicationTitle: String,
    val hour: Int,
    val minute: Int,
    val action: String,
    val timestamp: Long = System.currentTimeMillis()
)
