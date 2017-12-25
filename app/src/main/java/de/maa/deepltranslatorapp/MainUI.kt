package de.maa.deepltranslatorapp

import android.content.Context
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
import org.jetbrains.anko.design.indefiniteSnackbar
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.sdk25.coroutines.onClick

class MainUI : AnkoComponent<MainActivity>, AnkoLogger {
    private lateinit var input: EditText
    private lateinit var sourceLanguage: Spinner
    private lateinit var targetLanguage: Spinner
//    private lateinit var translation: TextView
    private lateinit var translationProgress: ProgressBar
    private lateinit var translationHistory: RecyclerView

    private var translations = emptyList<TranslationEntry>()
    private lateinit var ankoContext: Context

    override fun createView(ui: AnkoContext<MainActivity>) = with(ui) {
        ankoContext = ctx

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
                            if (input.text.toString().isBlank()) {
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
            val action = bg { ankoContext.database.deleteEntry(id) }
            action.await()
            updateUI()
        }
    }

    suspend fun updateUI() {
        translationProgress.visibility = View.INVISIBLE
        input.text.clear()

        translations = bg { ankoContext.database.getEntries() }.await()
        translationHistory.adapter = TranslationAdapter(this, translations)
    }
}