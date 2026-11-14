package debeski.jurah.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp

val GlassShape = RoundedCornerShape(26.dp)
val GlassShapeSmall = RoundedCornerShape(18.dp)

/**
 * Frosted panel: a translucent vertical wash plus a light-catching hairline edge,
 * layered over the gradient backdrop drawn by JurahBackground.
 *
 * Android only exposes real backdrop blur from API 31, so the glass reads through
 * translucency and the edge highlight instead — identical on every supported device.
 */
@Composable
fun Modifier.glass(
    shape: Shape = GlassShape,
    strength: Float = 1f,
): Modifier {
    val onDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val fill = if (onDark) {
        Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.16f * strength),
                Color.White.copy(alpha = 0.07f * strength),
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.80f * strength),
                Color.White.copy(alpha = 0.58f * strength),
            )
        )
    }
    val edge = Brush.linearGradient(
        listOf(
            Color.White.copy(alpha = if (onDark) 0.30f else 0.95f),
            Color.White.copy(alpha = 0.04f),
        )
    )
    return this.clip(shape).background(fill).border(1.dp, edge, shape)
}
