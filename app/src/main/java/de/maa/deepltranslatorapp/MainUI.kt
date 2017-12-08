package de.maa.deepltranslatorapp

import android.app.ProgressDialog
import android.content.Context
import android.graphics.Color
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.*
import org.jetbrains.anko.coroutines.experimental.bg
import org.jetbrains.anko.sdk25.coroutines.onClick

class MainUI : AnkoComponent<MainActivity> {
    private lateinit var input: EditText
    private lateinit var sourceLanguage: Spinner
    private lateinit var targetLanguage: Spinner
    private lateinit var translation: TextView
    private lateinit var loadingDialog: ProgressDialog

    override fun createView(ui: AnkoContext<MainActivity>) = ui.apply {
        verticalLayout {
            padding = dip(10)

            linearLayout {
                textView("From:").lparams {
                    rightMargin = dip(5)
                }
                sourceLanguage = spinner {
                    adapter = LanguageAdapter()
                }.lparams(width = wrapContent)

                textView("To:").lparams {
                    leftMargin = dip(15)
                    rightMargin = dip(5)
                }
                targetLanguage = spinner {
                    adapter = LanguageAdapter()
                    setSelection(1)
                }.lparams(width = wrapContent)
            }.lparams(width = matchParent)

            linearLayout {
                padding = dip(10)

                input = editText() {
                    hint = "Text to translate"
                }.lparams {
                    weight = 11f
                }

                imageButton(android.R.drawable.ic_menu_send) {
                    setBackgroundResource(R.drawable.ic_launcher_background)
                    onClick { translate(ctx) }
                }.lparams {
                    weight = 1f
                }
            }.lparams(height = dip(64), width = matchParent)

            translation = textView()//.lparams {gravity = Gravity.BOTTOM}

            loadingDialog = indeterminateProgressDialog("Translating...")
            loadingDialog.hide()
        }
    }.view

    private fun translate(ctx: Context) {
        val text = input.text.toString()

        if (text.isBlank()) {
            ctx.toast("No text specified")
            return
        }

        val from = sourceLanguage.selectedItem.toString()
        val to = targetLanguage.selectedItem.toString()

        println("text:$text, from:$from, to:$to")

        loadingDialog.show()

        async(UI) {
            val translations = bg {
                DeepL.getTranslations(text, from, to)
            }
            val result = translations.await()
            loadingDialog.hide()

            translation.text = ""
            result.forEach { translation.append(it + "\n") }
        }
    }
}