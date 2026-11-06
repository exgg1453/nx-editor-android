package com.nxteam.nxeditor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.nxteam.nxeditor.ui.RootScreen
import com.nxteam.nxeditor.ui.theme.NXEditorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NXEditorTheme {
                RootScreen()
            }
        }
    }
}
