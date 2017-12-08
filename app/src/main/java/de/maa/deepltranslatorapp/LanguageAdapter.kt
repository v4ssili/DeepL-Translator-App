package de.maa.deepltranslatorapp

import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import org.jetbrains.anko.*

class LanguageAdapter : BaseAdapter() {
    val languages = DeepL.VALID_LANGUAGES

    override fun getView(position: Int, view: View?, parent: ViewGroup?): View {
        return with(parent!!.context) {
            linearLayout {
                padding = dip(4)

                val languageText = getItem(position)
                val imageID = ctx.resources.getIdentifier(languageText.toLowerCase(), "drawable", ctx.packageName)

                imageView(imageID).lparams {
                    rightMargin = dip(6)
                }
                textView(languageText)
            }
        }
    }

    override fun getItem(position: Int): String {
        return languages[position]
    }

    override fun getCount(): Int {
        return languages.size
    }

    override fun getItemId(position: Int): Long {
        return 0L
    }
}