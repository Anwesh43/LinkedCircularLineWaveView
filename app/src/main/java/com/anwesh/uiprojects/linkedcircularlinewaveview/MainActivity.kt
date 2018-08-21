package com.anwesh.uiprojects.linkedcircularlinewaveview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.anwesh.uiprojects.circularlinewaveview.CircularLineWaveView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CircularLineWaveView.create(this)
    }
}
