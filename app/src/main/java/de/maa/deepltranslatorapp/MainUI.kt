package de.maa.deepltranslatorapp

import android.content.Context
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
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
import org.jetbrains.anko.design.longSnackbar
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.sdk25.coroutines.onClick


class MainUI : AnkoComponent<MainActivity> {
    private lateinit var ankoContext: Context
    private lateinit var mainView: View

    private lateinit var input: EditText
    private lateinit var sourceLanguage: Spinner
    private lateinit var targetLanguage: Spinner

    private lateinit var translationProgress: ProgressBar
    private lateinit var translationAdapter: TranslationAdapter

    override fun createView(ui: AnkoContext<MainActivity>): View {
        with(ui) {
            ankoContext = ctx
            mainView = verticalLayout {
                backgroundColorResource = android.R.color.secondary_text_light

                translationProgress = horizontalProgressBar {
                    isIndeterminate = true
                    visibility = View.GONE
                }

                recyclerView {
                    setHasFixedSize(true)
                    layoutManager = LinearLayoutManager(ctx)
                    translationAdapter = TranslationAdapter(owner, mutableListOf())
                    translationAdapter.updateItems(ctx.database.getEntries())
                    adapter = translationAdapter
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
                                if (!owner.hasInternetConnection()) {
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
        return mainView
    }

    fun copyToInput(language: String, text: String) {
        input.setText(text)
        sourceLanguage.setSelection(DeepL.VALID_LANGUAGES.indexOf(language))
    }

    private fun hideKeyboard(editText: EditText) {
        val inputManager = editText.context
                .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(editText.windowToken, 0)
    }

    private fun switchLanguages() {
        val from = sourceLanguage.selectedItemPosition
        val to = targetLanguage.selectedItemPosition

        sourceLanguage.setSelection(to, true)
        targetLanguage.setSelection(from, true)
    }

    private fun findAndShowTranslation() {
        translationProgress.visibility = View.VISIBLE

        val text = input.text.toString()
        val from = sourceLanguage.selectedItem.toString()
        val to = targetLanguage.selectedItem.toString()

        async(UI) {
            val translations = bg { DeepL.getTranslations(text, from, to) }
            val first = translations.await().first()
            translationProgress.visibility = View.GONE

            if (!first.isBlank()) {
                input.text.clear()
                addEntry(from, text, to, first)
            }
        }
    }

    private fun addEntry(sourceLanguage: String, sourceText: String,
                         targetLanguage: String, targetText: String) {

        ankoContext.database.addEntry(sourceLanguage, sourceText, targetLanguage, targetText)
        updateTranslations()
    }

    fun removeEntry(entry: TranslationEntry) {
        ankoContext.database.deleteEntry(entry.id)
        updateTranslations()

        longSnackbar(mainView, R.string.entry_deleted, R.string.undo) {
            addEntry(entry.sourceLanguage, entry.sourceText, entry.targetLanguage, entry.targetText)
        }
    }

    private fun updateTranslations() {
        async(UI) {
            val translations = bg { ankoContext.database.getEntries() }.await()
            translationAdapter.updateItems(translations)
        }
    }
}