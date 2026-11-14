package debeski.jurah.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import debeski.jurah.R

/**
 * Cairo ships as a single variable font covering Arabic and Latin, so one
 * resource serves both locales. Weights are instantiated from the wght axis
 * rather than synthesised, which keeps Arabic letterforms correct when bold.
 */
private fun cairo(weight: Int) = Font(
    resId = R.font.cairo,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

val Cairo = FontFamily(cairo(300), cairo(400), cairo(500), cairo(600), cairo(700))

private val d = Typography()

val JurahTypography = Typography(
    displayLarge = d.displayLarge.copy(fontFamily = Cairo),
    displayMedium = d.displayMedium.copy(fontFamily = Cairo),
    displaySmall = d.displaySmall.copy(fontFamily = Cairo),
    headlineLarge = d.headlineLarge.copy(fontFamily = Cairo, fontWeight = FontWeight.W600),
    headlineMedium = d.headlineMedium.copy(fontFamily = Cairo, fontWeight = FontWeight.W600),
    headlineSmall = d.headlineSmall.copy(fontFamily = Cairo, fontWeight = FontWeight.W600),
    titleLarge = d.titleLarge.copy(fontFamily = Cairo, fontWeight = FontWeight.W600),
    titleMedium = d.titleMedium.copy(fontFamily = Cairo, fontWeight = FontWeight.W600),
    titleSmall = d.titleSmall.copy(fontFamily = Cairo, fontWeight = FontWeight.W500),
    bodyLarge = d.bodyLarge.copy(fontFamily = Cairo),
    bodyMedium = d.bodyMedium.copy(fontFamily = Cairo),
    bodySmall = d.bodySmall.copy(fontFamily = Cairo),
    labelLarge = d.labelLarge.copy(fontFamily = Cairo, fontWeight = FontWeight.W600),
    labelMedium = d.labelMedium.copy(fontFamily = Cairo, fontWeight = FontWeight.W500),
    labelSmall = d.labelSmall.copy(fontFamily = Cairo, fontWeight = FontWeight.W500),
)
