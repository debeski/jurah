package debeski.jurah.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkScheme = darkColorScheme(
    primary = Magenta80,
    onPrimary = OnMagentaDark,
    primaryContainer = Magenta30,
    onPrimaryContainer = Magenta90,
    secondary = Clinical80,
    onSecondary = Clinical10,
    secondaryContainer = Clinical30,
    onSecondaryContainer = Clinical90,
    tertiary = Violet80,
    onTertiary = Violet10,
    tertiaryContainer = Violet30,
    onTertiaryContainer = Violet90,
    background = InkDark,
    onBackground = OnInkDark,
    surface = InkDark,
    onSurface = OnInkDark,
    surfaceVariant = InkSurfaceVariant,
    onSurfaceVariant = OnInkSurfaceVariant,
    surfaceContainer = InkDarkElevated,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
)

private val LightScheme = lightColorScheme(
    primary = Magenta40,
    onPrimary = Color.White,
    primaryContainer = Magenta90,
    onPrimaryContainer = Magenta10,
    secondary = Clinical40,
    onSecondary = Color.White,
    secondaryContainer = Clinical90,
    onSecondaryContainer = Clinical10,
    tertiary = Violet40,
    onTertiary = Color.White,
    tertiaryContainer = Violet90,
    onTertiaryContainer = Violet10,
    background = InkLight,
    onBackground = OnInkLight,
    surface = InkLight,
    onSurface = OnInkLight,
    surfaceVariant = BlushSurfaceVariant,
    onSurfaceVariant = OnBlushSurfaceVariant,
    outline = OutlineLight,
)

/** Brand colour is part of the product, so dynamic colour is deliberately not used. */
@Composable
fun JurahTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkScheme else LightScheme,
        typography = JurahTypography,
        content = content,
    )
}
