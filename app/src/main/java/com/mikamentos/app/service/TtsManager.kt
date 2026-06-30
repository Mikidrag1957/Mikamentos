package com.mikamentos.app.service

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

class TtsManager(private val context: Context) {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    fun init(callback: (() -> Unit)? = null) {
        if (isInitialized) {
            callback?.invoke()
            return
        }
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
            }
            callback?.invoke()
        }
    }

    fun speak(text: String, languageCode: String = "es", onDone: (() -> Unit)? = null) {
        if (!isInitialized) return

        val locale = resolveLocale(languageCode)
        val result = tts?.isLanguageAvailable(locale)

        if (result != null && result >= TextToSpeech.LANG_AVAILABLE) {
            tts?.language = locale
        } else {
            val langOnly = Locale(languageCode)
            val langResult = tts?.isLanguageAvailable(langOnly)
            if (langResult != null && langResult >= TextToSpeech.LANG_AVAILABLE) {
                tts?.language = langOnly
            } else {
                Log.w("TtsManager", "Language $languageCode not available (result=$result), using English fallback")
                tts?.language = Locale.UK
            }
        }

        val utteranceId = System.currentTimeMillis().toString()
        val capturedId = utteranceId
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(id: String?) {}
            override fun onDone(id: String?) {
                if (id == capturedId) {
                    onDone?.invoke()
                }
            }
            @Deprecated("Deprecated in Java")
            override fun onError(id: String?) {}
        })

        val cleanText = text.lowercase(locale)
        tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun speakOld(text: String, languageCode: String = "es") {
        if (!isInitialized) return

        val locale = resolveLocale(languageCode)
        val result = tts?.isLanguageAvailable(locale)

        if (result != null && result >= TextToSpeech.LANG_AVAILABLE) {
            tts?.language = locale
        } else {
            val langOnly = Locale(languageCode)
            val langResult = tts?.isLanguageAvailable(langOnly)
            if (langResult != null && langResult >= TextToSpeech.LANG_AVAILABLE) {
                tts?.language = langOnly
            } else {
                Log.w("TtsManager", "Language $languageCode not available (result=$result), using English fallback")
                tts?.language = Locale.UK
            }
        }

        val cleanText = text.lowercase(locale)
        tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, System.currentTimeMillis().toString())
    }

    private fun resolveLocale(languageCode: String): Locale {
        return when (languageCode) {
            "es" -> Locale.forLanguageTag("es-ES")
            "en" -> Locale.forLanguageTag("en-US")
            "sk" -> Locale.forLanguageTag("sk-SK")
            "ca" -> Locale.forLanguageTag("ca-ES")
            "fr" -> Locale.forLanguageTag("fr-FR")
            "it" -> Locale.forLanguageTag("it-IT")
            else -> Locale.getDefault()
        }
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
}
