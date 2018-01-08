package de.maa.deepltranslatorapp

import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick

class TranslationAdapter(
        private val owner: MainActivity, private val translations: MutableList<TranslationEntry>) :
        RecyclerView.Adapter<TranslationView>() {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): TranslationView {
        val view = with(parent!!.context) {
            verticalLayout {
                backgroundResource = android.R.color.white
                padding = dip(5)
                linearLayout {
                    id = R.id.source_language_layout
                    imageView {
                        id = R.id.source_language_icon
                    }.lparams {
                        rightMargin = dip(6)
                    }

                    imageButton {
                        id = R.id.source_speak
                        imageResource = R.drawable.ic_volume_up_black
                        backgroundColor = android.R.color.transparent
                    }.lparams {
                        height = dip(18)
                        rightMargin = dip(6)
                    }

                    textView {
                        id = R.id.source_language_text
                    }.lparams {
                        weight = 1f
                    }

                    imageButton {
                        id = R.id.remove_entry_icon
                        imageResource = R.drawable.ic_clear_black
                        backgroundColor = android.R.color.transparent
                    }.lparams {
                        gravity = Gravity.END
                        height = dip(24)
                        marginEnd = dip(8)
                    }
                }.lparams {
                    width = parent.width
                }

                linearLayout {
                    id = R.id.target_language_layout
                    imageView {
                        id = R.id.target_language_icon
                    }.lparams {
                        rightMargin = dip(6)
                    }

                    imageButton {
                        id = R.id.target_speak
                        imageResource = R.drawable.ic_volume_up_black
                        backgroundColor = android.R.color.transparent
                    }.lparams {
                        height = dip(18)
                        rightMargin = dip(6)
                    }

                    textView {
                        id = R.id.target_language_text
                    }.lparams {
                        weight = 1f
                    }
                }.lparams {
                    width = parent.width
                }
            }
        }
        return TranslationView(view, owner)
    }

    override fun onBindViewHolder(holder: TranslationView?, position: Int) {
        val entry = translations[position]
        holder?.bind(entry)
    }

    override fun getItemCount(): Int = translations.size

    fun updateItems(items: List<TranslationEntry>) {
        translations.clear()
        translations.addAll(items)
        notifyDataSetChanged()
    }
}

class TranslationView(itemView: View?, private val owner: MainActivity) :
        RecyclerView.ViewHolder(itemView) {

    fun bind(entry: TranslationEntry) {
        itemView.find<LinearLayout>(R.id.source_language_layout)
                .onClick { owner.ui.copyToInput(entry.sourceLanguage, entry.sourceText) }
        itemView.find<LinearLayout>(R.id.target_language_layout)
                .onClick { owner.ui.copyToInput(entry.targetLanguage, entry.targetText) }

        itemView.find<ImageView>(R.id.source_language_icon)
                .imageResource = DeepL.getLanguageResource(entry.sourceLanguage)
        itemView.find<ImageView>(R.id.target_language_icon)
                .imageResource = DeepL.getLanguageResource(entry.targetLanguage)

        itemView.find<TextView>(R.id.source_language_text).text = entry.sourceText
        itemView.find<TextView>(R.id.target_language_text).text = entry.targetText

        itemView.find<ImageButton>(R.id.source_speak)
                .onClick { owner.readText(entry.sourceLanguage, entry.sourceText) }
        itemView.find<ImageButton>(R.id.target_speak)
                .onClick { owner.readText(entry.targetLanguage, entry.targetText) }

        itemView.find<ImageButton>(R.id.remove_entry_icon).onClick { owner.ui.removeEntry(entry) }
    }
}
