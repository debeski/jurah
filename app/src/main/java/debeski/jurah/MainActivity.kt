package debeski.jurah

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import debeski.jurah.ui.nav.JurahNavHost
import debeski.jurah.ui.theme.JurahTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            JurahTheme {
                JurahNavHost()
            }
        }
    }
}
