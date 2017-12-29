package de.maa.deepltranslatorapp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import org.jetbrains.anko.setContentView

class MainActivity : AppCompatActivity() {
    lateinit var ui : MainUI
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = MainUI()
        ui.setContentView(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        ui.terminateTTS()
    }

}