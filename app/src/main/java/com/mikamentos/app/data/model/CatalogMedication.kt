package com.mikamentos.app.data.model

import androidx.annotation.Keep
import java.util.UUID

@Keep
data class CatalogMedication(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
