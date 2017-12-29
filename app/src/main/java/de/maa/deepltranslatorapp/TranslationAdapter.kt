package de.maa.deepltranslatorapp

import android.content.Context
import android.speech.tts.TextToSpeech
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

class TranslationAdapter(val ui: MainUI, val translations: List<TranslationEntry>) : RecyclerView.Adapter<TranslationView>() {
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): TranslationView {
        val view = with(parent!!.context) {
            verticalLayout {
                id = R.id.translation_layout
                backgroundResource = android.R.color.white
                padding = dip(5)
                linearLayout {
                    imageView {
                        id = R.id.source_language_icon
                    }.lparams {
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
                    imageView {
                        id = R.id.target_language_icon
                    }.lparams {
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
        return TranslationView(view, parent.context, ui)
    }

    override fun onBindViewHolder(holder: TranslationView?, position: Int) {
        val entry = translations.get(position)
        holder?.bind(
                entry.id,
                entry.sourceLanguage.toLowerCase(), entry.sourceText,
                entry.targetLanguage.toLowerCase(), entry.targetText)
    }

    override fun getItemCount(): Int = translations.size
}

class TranslationView(itemView: View?, val context: Context, val ui: MainUI) : RecyclerView.ViewHolder(itemView) {
    fun bind(id: Int, from: String, text: String, to: String, translation: String) {
        val sourceResource = context.resources.getIdentifier(from, "drawable", context.packageName)
        val sourceIcon = itemView.find<ImageView>(R.id.source_language_icon)
        sourceIcon.imageResource = sourceResource

        val sourceText = itemView.find<TextView>(R.id.source_language_text)
        sourceText.text = text

        val targetResource = context.resources.getIdentifier(to, "drawable", context.packageName)
        val targetIcon = itemView.find<ImageView>(R.id.target_language_icon)
        targetIcon.imageResource = targetResource

        val targetText = itemView.find<TextView>(R.id.target_language_text)
        targetText.text = translation

        val removeBtn = itemView.find<ImageButton>(R.id.remove_entry_icon)
        removeBtn.onClick { ui.removeEntry(id) }

        val translationLayout = itemView.find<LinearLayout>(R.id.translation_layout)
        translationLayout.setOnClickListener(object: DoubleClickListener() {
            override fun onSingleClick(v: View) {
                ui.copyEntryToInput(id)
            }

            override fun onDoubleClick(v: View) {
                ui.readTranslation(id)
            }
        })
    }
}

abstract class DoubleClickListener : View.OnClickListener {

    val DOUBLE_CLICK_DELTA: Long = 400
    internal var lastClickTime: Long = 0

    override fun onClick(v: View) {
        val clickTime = System.currentTimeMillis()
        if (clickTime - lastClickTime < DOUBLE_CLICK_DELTA) {
            onDoubleClick(v)
            lastClickTime = 0
        } else {
            onSingleClick(v)
        }
        lastClickTime = clickTime
    }

    abstract fun onSingleClick(v: View)
    abstract fun onDoubleClick(v: View)
}
