package de.maa.deepltranslatorapp

import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import org.jetbrains.anko.*
import java.util.*

class LanguageAdapter : BaseAdapter() {
    val languages = DeepL.VALID_LANGUAGES

    override fun getView(position: Int, view: View?, parent: ViewGroup?): View {
        return with(parent!!.context) {
            linearLayout {
                padding = dip(4)

                val languageTag = getItem(position)
                val imageID = ctx.resources.getIdentifier(languageTag.toLowerCase(), "drawable", ctx.packageName)
                val locale = Locale.forLanguageTag(languageTag)

                imageView(imageID).lparams {
                    rightMargin = dip(6)
                }
                textView(locale.getDisplayLanguage(locale))
            }
        }
    }

    override fun getItem(position: Int): String = languages[position]

    override fun getCount(): Int = languages.size

    override fun getItemId(position: Int): Long = 0L
}