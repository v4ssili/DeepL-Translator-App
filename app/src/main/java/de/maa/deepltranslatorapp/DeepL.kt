package de.maa.deepltranslatorapp

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class DeepL private constructor() {

    companion object {
        val API_URL = URL("https://www.deepl.com/jsonrpc")
        val VALID_LANGUAGES = listOf("DE", "EN", "FR", "ES", "IT", "NL", "PL")

        private val deepL = DeepL()

        /**
         * Translates the given text from the source language to the target language.
         * The DeepL API response may contain multiple translations.
         *
         * @param textToTranslate The text that shall be translated
         * @param fromLanguage The source language
         * @param toLanguage The target language
         *
         * @return The translated text (multiple)
         */
        fun getTranslations(textToTranslate: String, fromLanguage: String, toLanguage: String): List<String> {
            assert(fromLanguage in VALID_LANGUAGES)
            assert(toLanguage in VALID_LANGUAGES)

            val request = deepL.buildJsonRequest(textToTranslate, fromLanguage, toLanguage)
            val response = deepL.sendRequestToAPI(request)

            return deepL.getTranslationsFromResponse(response)
        }

    }

    /**
     * Builds a special [JSONObject] with the given parameters.
     * This object is meant to be sent as request to the DeepL API.
     *
     * @param text A given text (sentence, phrase, word)
     * @param from The source language identifier
     * @param to The target language identifier
     *
     * @return The resulting JSON object
     */
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

    /**
     * Sends the given request ([JSONObject]) to the DeepL API URL
     * and returns the resulting API response.
     *
     * @param request The constructed API request object
     *
     * @return The response from the API
     */
    private fun sendRequestToAPI(request: JSONObject): JSONObject {
        val connection = DeepL.API_URL.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
        connection.connect()

        val bytes = request.toString().toByteArray()
        connection.outputStream?.write(bytes)

        val response = connection.inputStream.bufferedReader().readText()
        return JSONObject(response)
    }

    /**
     * Takes the DeepL API response and filters the processed sentences out of it.
     *
     * @param response The DeepL API response
     *
     * @return The processed sentences / translations
     */
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