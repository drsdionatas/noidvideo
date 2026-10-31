package com.noidvideo.editor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.noidvideo.editor.ui.NoidVideoApp
import com.noidvideo.editor.ui.theme.NoidVideoTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NoidVideoTheme {
                NoidVideoApp()
            }
        }
    }
}
