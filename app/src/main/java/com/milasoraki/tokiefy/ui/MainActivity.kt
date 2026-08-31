package com.milasoraki.tokiefy.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.milasoraki.tokiefy.ui.theme.TokiefyTheme

/** Single-activity entry point. Hosts the Compose navigation graph. */
public class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TokiefyTheme {
                MainScaffold()
            }
        }
    }
}
