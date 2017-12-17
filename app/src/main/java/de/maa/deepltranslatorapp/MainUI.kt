package de.maa.deepltranslatorapp

import android.app.ProgressDialog
import android.content.Context
import android.view.Gravity
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.*
import org.jetbrains.anko.coroutines.experimental.bg
import org.jetbrains.anko.design.floatingActionButton
import org.jetbrains.anko.sdk25.coroutines.onClick

class MainUI : AnkoComponent<MainActivity> {
    private lateinit var input: EditText
    private lateinit var sourceLanguage: Spinner
    private lateinit var targetLanguage: Spinner
    private lateinit var translation: TextView
    private lateinit var loadingDialog: ProgressDialog
    private lateinit var noTextToast: Toast

    override fun createView(ui: AnkoContext<MainActivity>) = with(ui) {
        verticalLayout {
            padding = dip(5)

            linearLayout {
                sourceLanguage = spinner {
                    id = R.id.source_language
                    adapter = LanguageAdapter()
                }.lparams {
                    width = dip(0)
                    weight = 4f
                }
                imageView(R.drawable.ic_forward_black).lparams {
                    width = dip(0)
                    weight = 1f
                }
                targetLanguage = spinner {
                    id = R.id.target_language
                    adapter = LanguageAdapter()
                    setSelection(1)
                }.lparams {
                    width = dip(0)
                    weight = 4f
                }
            }.lparams {
                width = matchParent
            }

            relativeLayout {
                translation = textView {
                    id = R.id.text_translations
                }.lparams {
                    centerInParent()
                }
            }.lparams {
                width = matchParent
                height = dip(0)
                weight = 1f
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
                    imageResource = R.drawable.ic_send_white
                    onClick { translate() }
                }.lparams {
                    leftMargin = dip(10)
                    gravity = Gravity.BOTTOM
                }
            }.lparams {
                width = matchParent
                gravity = Gravity.BOTTOM
            }

            loadingDialog = indeterminateProgressDialog(R.string.searching_translation)
            loadingDialog.hide()

            noTextToast = ctx.toast(R.string.no_text)
            noTextToast.cancel()
        }
    }

    private fun translate() {
        val text = input.text.toString()
        if (text.isBlank()) {
            noTextToast.show()
            return
        }

        val from = sourceLanguage.selectedItem.toString()
        val to = targetLanguage.selectedItem.toString()

        hideKeyboard(input)
        loadingDialog.show()

        async(UI) {
            val translations = bg { DeepL.getTranslations(text, from, to) }
            val result = translations.await().also { }
            loadingDialog.hide()
            translation.text = ""
            result.forEach { translation.append(it + "\n") }
        }
    }

    fun hideKeyboard(editText: EditText) {
        val inputManager = editText.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(editText.getWindowToken(), 0)
    }

    fun onStop() {
        loadingDialog.dismiss()
    }
}