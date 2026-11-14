package debeski.jurah.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * The app's own icon set.
 *
 * Material's icon artifacts are deprecated and frozen, and none of them carries a
 * pill or dose glyph, so the set is drawn here: no dependency, one visual language,
 * and Icon() tints every glyph from LocalContentColor as usual.
 */
object JurahIcons {

    private fun stroked(
        name: String,
        width: Float = 2f,
        autoMirror: Boolean = false,
        block: PathBuilder.() -> Unit,
    ) =
        ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
            autoMirror = autoMirror,
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = width,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathBuilder = block,
            )
        }.build()

    private fun filled(name: String, block: PathBuilder.() -> Unit) =
        ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply { path(fill = SolidColor(Color.Black), pathBuilder = block) }.build()

    private fun PathBuilder.circle(cx: Float, cy: Float, r: Float) {
        moveTo(cx - r, cy)
        arcTo(r, r, 0f, true, true, cx + r, cy)
        arcTo(r, r, 0f, true, true, cx - r, cy)
        close()
    }

    val Add: ImageVector by lazy {
        stroked("Add") {
            moveTo(12f, 5f); lineTo(12f, 19f)
            moveTo(5f, 12f); lineTo(19f, 12f)
        }
    }

    val Minus: ImageVector by lazy {
        stroked("Minus") {
            moveTo(5f, 12f); lineTo(19f, 12f)
        }
    }

    val Close: ImageVector by lazy {
        stroked("Close") {
            moveTo(6f, 6f); lineTo(18f, 18f)
            moveTo(18f, 6f); lineTo(6f, 18f)
        }
    }

    val Check: ImageVector by lazy {
        stroked("Check", width = 2.2f) {
            moveTo(4.5f, 12.5f); lineTo(9.5f, 17.5f); lineTo(19.5f, 6.5f)
        }
    }

    // autoMirror: a back chevron must point the other way under an RTL layout.
    val Back: ImageVector by lazy {
        stroked("Back", autoMirror = true) {
            moveTo(15f, 4.5f); lineTo(7.5f, 12f); lineTo(15f, 19.5f)
        }
    }

    val Info: ImageVector by lazy {
        stroked("Info") {
            circle(12f, 12f, 8.5f)
            moveTo(12f, 11f); lineTo(12f, 16.5f)
            moveTo(12f, 7.6f); lineTo(12f, 7.9f)
        }
    }

    /** Clock face with a counter-clockwise tail: the history/log affordance. */
    val History: ImageVector by lazy {
        stroked("History") {
            moveTo(3.6f, 12f)
            arcTo(8.5f, 8.5f, 0f, true, true, 12f, 20.5f)
            moveTo(3.6f, 12f); lineTo(3.6f, 7.6f)
            moveTo(3.6f, 12f); lineTo(7.8f, 12f)
            moveTo(12f, 8f); lineTo(12f, 12.4f); lineTo(15.4f, 14.4f)
        }
    }

    val Clock: ImageVector by lazy {
        stroked("Clock") {
            circle(12f, 12f, 8.5f)
            moveTo(12f, 7.2f); lineTo(12f, 12.4f); lineTo(15.6f, 14.6f)
        }
    }

    val Calendar: ImageVector by lazy {
        stroked("Calendar") {
            moveTo(4.5f, 7.5f)
            lineTo(4.5f, 19f); lineTo(19.5f, 19f); lineTo(19.5f, 7.5f); close()
            moveTo(4.5f, 11.5f); lineTo(19.5f, 11.5f)
            moveTo(8.5f, 4.5f); lineTo(8.5f, 7.5f)
            moveTo(15.5f, 4.5f); lineTo(15.5f, 7.5f)
        }
    }

    val Bell: ImageVector by lazy {
        stroked("Bell") {
            moveTo(6f, 16.5f)
            lineTo(6f, 11f)
            arcTo(6f, 6f, 0f, false, true, 18f, 11f)
            lineTo(18f, 16.5f); close()
            moveTo(4.5f, 16.5f); lineTo(19.5f, 16.5f)
            moveTo(10f, 19.5f); lineTo(14f, 19.5f)
        }
    }

    val Play: ImageVector by lazy {
        filled("Play") {
            moveTo(8f, 5.2f); lineTo(19f, 12f); lineTo(8f, 18.8f); close()
        }
    }

    val Pause: ImageVector by lazy {
        filled("Pause") {
            moveTo(8f, 5f); horizontalLineTo(11f); verticalLineTo(19f); horizontalLineTo(8f); close()
            moveTo(13f, 5f); horizontalLineTo(16f); verticalLineTo(19f); horizontalLineTo(13f); close()
        }
    }

    val Delete: ImageVector by lazy {
        stroked("Delete") {
            moveTo(5.5f, 7.5f); lineTo(18.5f, 7.5f)
            moveTo(7.5f, 7.5f); lineTo(8.4f, 20f); lineTo(15.6f, 20f); lineTo(16.5f, 7.5f)
            moveTo(9.8f, 7.5f); lineTo(9.8f, 4.5f); lineTo(14.2f, 4.5f); lineTo(14.2f, 7.5f)
        }
    }

    val Edit: ImageVector by lazy {
        stroked("Edit") {
            moveTo(4.5f, 19.5f); lineTo(8.2f, 18.8f); lineTo(20f, 7f)
            lineTo(17f, 4f); lineTo(5.2f, 15.8f); close()
        }
    }

    /**
     * A capsule on the diagonal: the app's dose glyph.
     *
     * Axis runs (8,16) to (16,8) with half-width 3.2, so the two long sides are that
     * axis offset along the perpendicular and the caps are exact half-circles joining
     * them. Both arcs sweep positive, which bulges them away from the centre.
     */
    val Pill: ImageVector by lazy {
        stroked("Pill", width = 1.9f) {
            moveTo(5.737f, 13.737f)
            lineTo(13.737f, 5.737f)
            arcTo(3.2f, 3.2f, 0f, false, true, 18.263f, 10.263f)
            lineTo(10.263f, 18.263f)
            arcTo(3.2f, 3.2f, 0f, false, true, 5.737f, 13.737f)
            close()
            moveTo(9.737f, 9.737f)
            lineTo(14.263f, 14.263f)
        }
    }

    val Accessibility: ImageVector by lazy {
        stroked("Accessibility") {
            circle(12f, 4.6f, 1.9f)
            moveTo(12f, 8f); lineTo(12f, 14f)
            moveTo(5.5f, 10f); lineTo(18.5f, 10f)
            moveTo(12f, 14f); lineTo(8.5f, 20.5f)
            moveTo(12f, 14f); lineTo(15.5f, 20.5f)
        }
    }
}
