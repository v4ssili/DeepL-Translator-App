package de.maa.deepltranslatorapp

import android.app.ProgressDialog
import android.content.Context
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
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

    override fun createView(ui: AnkoContext<MainActivity>) = with(ui) {
        relativeLayout {
            padding = dip(5)

            relativeLayout {
                sourceLanguage = spinner {
                    id = R.id.source_language
                    adapter = LanguageAdapter()
                }.lparams {
                    alignParentLeft()
                }
                imageView(R.drawable.ic_forward_black).lparams {
                    centerHorizontally()
                    horizontalMargin = dip(20)
                }
                targetLanguage = spinner {
                    id = R.id.target_language
                    adapter = LanguageAdapter()
                    setSelection(1)
                }.lparams {
                    alignParentRight()
                }
            }.lparams {
                width = matchParent
                alignParentTop()
            }

            relativeLayout {
                input = editText {
                    id = R.id.text_input
                    hintResource = R.string.text_to_translate
                }.lparams {
                    width = dip(270)
                    alignParentLeft()
                    alignParentBottom()
                }

                floatingActionButton {
                    imageResource = R.drawable.ic_send_white
                    onClick { translate(ctx) }
                }.lparams {
                    alignParentRight()
                    alignParentBottom()
                }
            }.lparams {
                width = matchParent
                alignParentBottom()
            }

            translation = textView().lparams {
                id = R.id.text_translations
                centerInParent()
            }

            loadingDialog = indeterminateProgressDialog(R.string.searching_translation)
            loadingDialog.hide()
        }
    }

    private fun translate(ctx: Context) {
        val text = input.text.toString()
        if (text.isBlank()) {
            ctx.toast(R.string.no_text)
            return
        }

        val from = sourceLanguage.selectedItem.toString()
        val to = targetLanguage.selectedItem.toString()
        loadingDialog.show()

        async(UI) {
            val translations = bg { DeepL.getTranslations(text, from, to) }
            val result = translations.await()
            loadingDialog.hide()
            translation.text = ""
            result.forEach { translation.append(it + "\n") }
        }
    }

    fun onStop() {
        loadingDialog.dismiss()
    }
}