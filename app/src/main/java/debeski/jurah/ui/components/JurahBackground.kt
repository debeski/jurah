package debeski.jurah.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import debeski.jurah.ui.theme.Clinical60
import debeski.jurah.ui.theme.Magenta60
import debeski.jurah.ui.theme.Violet40

/** The soft magenta/blue field every screen floats on; gives the glass panels something to frost. */
@Composable
fun JurahBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val onDark = scheme.surface.luminance() < 0.5f
    Box(modifier.fillMaxSize().background(scheme.background)) {
        Canvas(Modifier.fillMaxSize()) {
            fun blob(color: Color, fx: Float, fy: Float, fr: Float, alpha: Float) {
                val center = Offset(size.width * fx, size.height * fy)
                val radius = size.minDimension * fr
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(color.copy(alpha = alpha), Color.Transparent),
                        center = center,
                        radius = radius,
                    ),
                    radius = radius,
                    center = center,
                )
            }
            blob(Magenta60, 0.10f, 0.02f, 0.95f, if (onDark) 0.50f else 0.34f)
            blob(Clinical60, 0.98f, 0.24f, 0.72f, if (onDark) 0.30f else 0.20f)
            blob(Violet40, 0.50f, 1.04f, 0.90f, if (onDark) 0.32f else 0.18f)
        }
        content()
    }
}
