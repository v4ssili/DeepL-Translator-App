package de.maa.deepltranslatorapp

import android.app.Activity
import android.os.Bundle
import kotlinx.coroutines.experimental.async

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val fromLanguage = "DE"
        val toLanguage = "EN"
        val textToTranslate = "Haben wir heute nicht ein fantastisches Wetter?"

        async {
            val translations = DeepL.getTranslations(textToTranslate, fromLanguage, toLanguage)
            translations.forEach { println(it) }
        }

    }

}
