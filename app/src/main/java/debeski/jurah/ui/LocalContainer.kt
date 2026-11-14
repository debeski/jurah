package debeski.jurah.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import debeski.jurah.JurahApp
import debeski.jurah.di.AppContainer

@Composable
fun rememberContainer(): AppContainer {
    val context = LocalContext.current
    return remember(context) { (context.applicationContext as JurahApp).container }
}
