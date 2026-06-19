package com.mikamentos.app.ui

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalContext

@Composable
@ReadOnlyComposable
fun tr(@StringRes id: Int): String {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("mikamentos_prefs", Context.MODE_PRIVATE)
    val lang = prefs.getString("language", "es") ?: "es"
    val key = context.resources.getResourceEntryName(id)
    return MikamentosTranslations.get(lang, key) ?: context.resources.getString(id)
}
