package de.maa.deepltranslatorapp

import android.content.Context
import android.content.res.Resources
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Spinner
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.*
import org.jetbrains.anko.coroutines.experimental.bg
import org.jetbrains.anko.design.floatingActionButton
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.sdk25.coroutines.onClick
import android.net.NetworkInfo
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.PersistableBundle
import android.speech.tts.TextToSpeech
import android.support.v4.content.res.TypedArrayUtils
import android.util.Log
import android.widget.CheckBox
import org.jetbrains.anko.design.longSnackbar
import org.w3c.dom.Text
import java.util.*


class MainUI : AnkoComponent<MainActivity>, AnkoLogger {
    private lateinit var input: EditText
    private lateinit var sourceLanguage: Spinner
    private lateinit var targetLanguage: Spinner
    private lateinit var readResult: CheckBox
    private lateinit var tts: TextToSpeech
    private var ttsAvailable = false
    //    private lateinit var translation: TextView
    private lateinit var translationProgress: ProgressBar
    private lateinit var translationHistory: RecyclerView

    private var translations = emptyList<TranslationEntry>()
    private lateinit var ankoContext: Context
    private lateinit var ankoContext2: AnkoContext<MainActivity>

    override fun createView(ui: AnkoContext<MainActivity>) = with(ui) {
        ankoContext = ctx
        ankoContext2 = ui

        tts = TextToSpeech(ankoContext, TextToSpeech.OnInitListener { status ->
            ttsAvailable = status != TextToSpeech.ERROR
        })

        verticalLayout {
            backgroundColorResource = android.R.color.secondary_text_light

            translationProgress = horizontalProgressBar {
                isIndeterminate = true
                visibility = View.INVISIBLE
            }

            translationHistory = recyclerView {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(ctx)
                adapter = TranslationAdapter(this@MainUI, ctx.database.getEntries())
                addItemDecoration(DividerItemDecoration(ctx, DividerItemDecoration.VERTICAL))
            }.lparams {
                width = matchParent
                height = dip(0)
                weight = 1f
            }

            verticalLayout {
                backgroundColorResource = android.R.color.white
                padding = dip(5)

                linearLayout {
                    sourceLanguage = spinner {
                        id = R.id.source_language
                        adapter = LanguageAdapter()
                    }

                    relativeLayout {
                        imageButton {
                            imageResource = R.drawable.ic_compare_arrows_black
                            backgroundColor = android.R.color.transparent
                            onClick { switchLanguages() }
                        }.lparams {
                            height = dip(24)
                            centerInParent()
                        }
                    }.lparams {
                        weight = 1f
                        gravity = Gravity.CENTER_HORIZONTAL
                    }

                    targetLanguage = spinner {
                        id = R.id.target_language
                        adapter = LanguageAdapter()
                        setSelection(1)
                    }
                }.lparams {
                    width = matchParent
                }

                relativeLayout {
                    backgroundResource = android.R.drawable.divider_horizontal_bright
                }

                readResult = checkBox {
                    id = R.id.read_result
                    textResource = R.string.read_result
                    isChecked = true
                }

                relativeLayout {
                    backgroundResource = android.R.drawable.divider_horizontal_bright
                }

                linearLayout {
                    input = editText {
                        id = R.id.text_input
                        hintResource = R.string.text_to_translate
                    }.lparams {
                        width = dip(0)
                        weight = 1f
                        gravity = Gravity.BOTTOM
                    }

                    floatingActionButton {
                        size = FloatingActionButton.SIZE_AUTO
                        imageResource = R.drawable.ic_send_white
                        onClick {
                            if (!hasInternetConnection()) {
                                ctx.toast(R.string.no_internet)
                            } else if (input.text.toString().isBlank()) {
                                ctx.toast(R.string.no_text)
                            } else {
                                hideKeyboard(input)
                                findAndShowTranslation()
                            }
                        }
                    }.lparams {
                        leftMargin = dip(5)
                        bottomMargin = dip(5)
                        gravity = Gravity.BOTTOM
                    }
                }.lparams {
                    topMargin = dip(10)
                    width = matchParent
                }
            }.lparams {
                width = matchParent
                gravity = Gravity.BOTTOM
            }
        }
    }

    fun terminateTTS() {
        tts.stop()
        tts.shutdown()
    }

    fun hideKeyboard(editText: EditText) {
        val inputManager = editText.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(editText.windowToken, 0)
    }

    fun switchLanguages() {
        val from = sourceLanguage.selectedItemPosition
        val to = targetLanguage.selectedItemPosition

        sourceLanguage.setSelection(to, true)
        targetLanguage.setSelection(from, true)
    }

    fun findAndShowTranslation() {
        translationProgress.visibility = View.VISIBLE

        val text = input.text.toString()
        val from = sourceLanguage.selectedItem.toString()
        val to = targetLanguage.selectedItem.toString()

        async(UI) {
            val translations = bg { DeepL.getTranslations(text, from, to) }
            val first = translations.await().first()
            addEntry(from, text, to, first)
            if (readResult.isChecked) {
                if (!ttsAvailable) {
                    ankoContext.toast(R.string.tts_not_available)
                    return@async
                }
                val locale: Locale
                when (to) {
                    "EN" -> locale = Locale.ENGLISH
                    "DE" -> locale = Locale.GERMAN
                    "FR" -> locale = Locale.FRENCH
                    "IT" -> locale = Locale.ITALIAN
                    else -> {
                        ankoContext.toast(R.string.tts_language_not_available)
                        return@async
                    }
                }
                tts.setLanguage(locale)
                tts.speak(first, TextToSpeech.QUEUE_FLUSH, null, "0")
            }


        }
    }

    fun addEntry(sourceLanguage: String, sourceText: String, targetLanguage: String, targetText: String) {
        async(UI) {
            val action = bg { ankoContext.database.addEntry(sourceLanguage, sourceText, targetLanguage, targetText) }
            action.await()
            updateUI()
        }
    }

    fun removeEntry(id: Int) {
        async(UI) {
            val translationHistory = bg { ankoContext.database.getEntries() }.await()
            val entryBackup = translationHistory.find({ entry -> entry.id == id })
            val action = bg { ankoContext.database.deleteEntry(id) }
            action.await()
            updateUI()
            if (entryBackup != null) longSnackbar(ankoContext2.view, R.string.entry_deleted, R.string.undo) { undoRemoveEntry(entryBackup) }
        }
    }

    fun undoRemoveEntry(entry: TranslationEntry) {
        async(UI)
        {
            ankoContext.database.addEntry(entry)
            updateUI()
        }
    }

    fun copyEntryToInput(id: Int) {
        async(UI) {
            val translationHistory = bg { ankoContext.database.getEntries() }.await()
            val entry = translationHistory.find({ entry -> entry.id == id })
            if (entry != null) {
                input.setText(entry.sourceText)
                sourceLanguage.setSelection(getSpinnerItemIndex(sourceLanguage, entry.sourceLanguage))
                targetLanguage.setSelection(getSpinnerItemIndex(targetLanguage, entry.targetLanguage))
            }
        }
    }

    fun readTranslation(id: Int) {
        if (!ttsAvailable) {
            ankoContext.toast(R.string.tts_not_available)
            return
        }

        async(UI) {
            val translationHistory = bg { ankoContext.database.getEntries() }.await()
            val entry = translationHistory.find({ entry -> entry.id == id })
            if (entry != null) {
                val locale: Locale
                when (entry.targetLanguage) {
                    "EN" -> locale = Locale.ENGLISH
                    "DE" -> locale = Locale.GERMAN
                    "FR" -> locale = Locale.FRENCH
                    "IT" -> locale = Locale.ITALIAN
                    else -> {
                        ankoContext.toast(R.string.tts_language_not_available)
                        return@async
                    }
                }
                tts.setLanguage(locale)
                tts.speak(entry.targetText, TextToSpeech.QUEUE_FLUSH, null, "0")
            }
        }
    }

    suspend fun updateUI() {
        translationProgress.visibility = View.INVISIBLE
        input.text.clear()

        translations = bg { ankoContext.database.getEntries() }.await()
        translationHistory.adapter = TranslationAdapter(this, translations)
    }

    private fun hasInternetConnection(): Boolean {
        val connectivityManager = ankoContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.activeNetworkInfo != null && connectivityManager.activeNetworkInfo.isConnected
    }

    private fun getSpinnerItemIndex(spinner: Spinner, str: String): Int {
        var index = 0

        for (i in 0 until spinner.count) {
            if (spinner.getItemAtPosition(i).toString().equals(str, ignoreCase = true)) {
                index = i
                break
            }
        }
        return index
    }
}