package de.maa.deepltranslatorapp

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class DeepL private constructor() {

    companion object {
        val API_URL = URL("https://www.deepl.com/jsonrpc")
        val VALID_LANGUAGES = listOf("DE", "EN", "FR", "ES", "IT", "NL", "PL")
        private val deepL = DeepL()

        fun getTranslations(textToTranslate: String, fromLanguage: String,
                            toLanguage: String): List<String> {

            assert(fromLanguage in VALID_LANGUAGES)
            assert(toLanguage in VALID_LANGUAGES)

            val request = deepL.buildJsonRequest(textToTranslate, fromLanguage, toLanguage)
            val response = deepL.sendRequestToAPI(request)

            return deepL.getTranslationsFromResponse(response)
        }

        fun getLanguageResource(language: String): Int {
            assert(language in VALID_LANGUAGES)
            return when (language) {
                "DE" -> R.drawable.de
                "EN" -> R.drawable.en
                "FR" -> R.drawable.fr
                "ES" -> R.drawable.es
                "IT" -> R.drawable.it
                "NL" -> R.drawable.nl
                "PL" -> R.drawable.pl
                else -> R.drawable.ic_clear_black
            }
        }
    }

    private fun buildJsonRequest(text: String, from: String, to: String): JSONObject {
        val jsonRequest = """{
                jsonrpc: '2.0',
                method: LMT_handle_jobs,
                params: {
                    jobs: [{kind: default, raw_en_sentence: '$text'}],
                    lang: {
                        user_preferred_langs: [$from, $to],
                        source_lang_user_selected: $from,
                        target_lang: $to
                    },
                    priority: 1
                }
            }"""
        return JSONObject(jsonRequest)
    }

    private fun sendRequestToAPI(request: JSONObject): JSONObject {
        val connection = DeepL.API_URL.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.connect()

        val bytes = request.toString().toByteArray()
        connection.outputStream?.write(bytes)

        val response = connection.inputStream.bufferedReader().readText()
        return JSONObject(response)
    }

    private fun getTranslationsFromResponse(response: JSONObject): List<String> {
        var processedSentences = emptyList<String>()

        val result = response.getJSONObject("result")
        val translations = result.getJSONArray("translations")

        for (i in 0 until translations.length()) {
            val translation = translations.getJSONObject(i)
            val beams = translation.getJSONArray("beams")

            for (j in 0 until beams.length()) {
                val beam = beams.getJSONObject(j)
                val sentence = beam.getString("postprocessed_sentence")
                processedSentences += sentence
            }
        }
        return processedSentences
    }
}