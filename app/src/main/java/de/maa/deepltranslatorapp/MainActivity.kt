package de.maa.deepltranslatorapp

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.support.v7.app.AppCompatActivity
import org.jetbrains.anko.setContentView
import org.jetbrains.anko.toast
import java.util.*

class MainActivity : AppCompatActivity() {
    lateinit var ui: MainUI

    private lateinit var tts: TextToSpeech
    private var ttsAvailable: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = MainUI()
        ui.setContentView(this)

        tts = TextToSpeech(applicationContext, TextToSpeech.OnInitListener {
            status -> ttsAvailable = status != TextToSpeech.ERROR
        })
    }

    override fun onDestroy() {
        tts.stop()
        tts.shutdown()

        super.onDestroy()
    }

    fun readText(language: String, text: String) {
        if (!ttsAvailable) {
            applicationContext.toast(R.string.tts_not_available)
            return
        }

        val locale = Locale.forLanguageTag(language)
        val languageAvailable = tts.isLanguageAvailable(locale) == TextToSpeech.LANG_AVAILABLE

        if (languageAvailable) {
            tts.language = locale
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "0")
        } else {
            applicationContext.toast(R.string.tts_language_not_available)
        }
    }

    fun hasInternetConnection(): Boolean {
        val connectivityManager = applicationContext
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        return connectivityManager.activeNetworkInfo != null
                && connectivityManager.activeNetworkInfo.isConnected
    }

}