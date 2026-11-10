package app.ukiyo.music

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import app.ukiyo.music.ui.UkiyoRoot
import app.ukiyo.music.ui.theme.UkiyoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UkiyoTheme {
                val viewModel: MainViewModel = viewModel()
                UkiyoRoot(viewModel)
            }
        }
    }
}
