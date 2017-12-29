package de.maa.deepltranslatorapp

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import org.jetbrains.anko.db.*

class TranslationHistory(ctx: Context) : ManagedSQLiteOpenHelper(ctx, "translationhistory.db", null, 1) {
    companion object {
        val TRANSLATION_TABLE = "Translations"
        private var instance: TranslationHistory? = null

        @Synchronized
        fun getInstance(ctx: Context): TranslationHistory {
            if (instance == null)
                instance = TranslationHistory(ctx.getApplicationContext())

            return instance!!
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.createTable(TRANSLATION_TABLE, true,
                "id" to INTEGER + PRIMARY_KEY + AUTOINCREMENT,
                "sourceLanguage" to TEXT,
                "sourceText" to TEXT,
                "targetLanguage" to TEXT,
                "targetText" to TEXT)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.dropTable(TRANSLATION_TABLE, true)
    }

    fun getEntries(): List<TranslationEntry> {
        var entries = emptyList<TranslationEntry>()
        use {
            select(TRANSLATION_TABLE).exec {
                entries = parseList(classParser<TranslationEntry>())
            }
        }
        return entries
    }

    fun addEntry(from: String, text: String, to: String, translation: String) {
        use {
            insertOrThrow(TRANSLATION_TABLE,
                    "sourceLanguage" to from,
                    "sourceText" to text,
                    "targetLanguage" to to,
                    "targetText" to translation)
        }
    }

    fun addEntry(entry: TranslationEntry) {
        use {
                    insertOrThrow(TRANSLATION_TABLE,
                            "sourceLanguage" to entry.sourceLanguage,
                            "sourceText" to entry.sourceText,
                            "targetLanguage" to entry.targetLanguage,
                            "targetText" to entry.targetText)
        }
    }

    fun deleteEntry(index: Int) {
        use {
            delete(TRANSLATION_TABLE, "(id = {id})", "id" to index)
        }
    }
}

data class TranslationEntry(val id: Int,
                            val sourceLanguage: String, val sourceText: String,
                            val targetLanguage: String, val targetText: String)

val Context.database: TranslationHistory
    get() = TranslationHistory.getInstance(getApplicationContext())