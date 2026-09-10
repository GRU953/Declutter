// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Aninda Sundar Howlader (GRU953)

package dev.gru953.declutter

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import dev.gru953.declutter.ui.theme.DarkGruColours
import dev.gru953.declutter.ui.theme.GruColours
import dev.gru953.declutter.ui.theme.GruDarkScheme
import dev.gru953.declutter.ui.theme.GruLightScheme
import dev.gru953.declutter.ui.theme.LightGruColours
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 * Measures the contrast of every colour pair the app actually renders, in both themes.
 *
 * The GRU953 rule is 4.5:1 for text and 3:1 for a border or icon that has to be seen, in
 * both themes -- and the brand's own review script cannot check a Compose app, so this is
 * where that promise is kept. A pair that drops below the floor fails the build rather
 * than shipping.
 */
class ContrastTest {

    private fun luminance(colour: Color): Double {
        fun channel(value: Float): Double {
            val v = value.toDouble()
            return if (v <= 0.03928) v / 12.92 else ((v + 0.055) / 1.055).pow(2.4)
        }
        return 0.2126 * channel(colour.red) +
            0.7152 * channel(colour.green) +
            0.0722 * channel(colour.blue)
    }

    private fun ratio(a: Color, b: Color): Double {
        val la = luminance(a)
        val lb = luminance(b)
        return (max(la, lb) + 0.05) / (min(la, lb) + 0.05)
    }

    private data class Pair(val name: String, val fg: Color, val bg: Color, val floor: Double)

    private fun pairsFor(
        theme: String,
        scheme: ColorScheme,
        gru: GruColours,
    ): List<Pair> {
        val text = 4.5
        val seen = 3.0
        return listOf(
            // Body and heading text on every ground it can land on.
            Pair("$theme onSurface/background", scheme.onSurface, scheme.background, text),
            Pair("$theme onSurface/surface", scheme.onSurface, scheme.surface, text),
            Pair("$theme onSurface/containerLow", scheme.onSurface, scheme.surfaceContainerLow, text),
            Pair("$theme onSurface/containerHigh", scheme.onSurface, scheme.surfaceContainerHigh, text),
            Pair("$theme onSurfaceVariant/surfaceVariant", scheme.onSurfaceVariant, scheme.surfaceVariant, text),
            // Muted and subtle text, including the monospaced package names in list rows.
            Pair("$theme inkMuted/background", gru.inkMuted, scheme.background, text),
            Pair("$theme inkMuted/containerLow", gru.inkMuted, scheme.surfaceContainerLow, text),
            Pair("$theme inkSubtle/background", gru.inkSubtle, scheme.background, text),
            Pair("$theme inkSubtle/surface", gru.inkSubtle, scheme.surface, text),
            Pair("$theme inkSubtle/containerLow", gru.inkSubtle, scheme.surfaceContainerLow, text),
            Pair("$theme inkSubtle/containerHighest", gru.inkSubtle, scheme.surfaceContainerHighest, text),
            // The signature, and text on it.
            Pair("$theme primary/background", scheme.primary, scheme.background, text),
            Pair("$theme onPrimary/primary", scheme.onPrimary, scheme.primary, text),
            Pair("$theme onPrimaryContainer/primaryContainer", scheme.onPrimaryContainer, scheme.primaryContainer, text),
            Pair("$theme onSecondary/secondary", scheme.onSecondary, scheme.secondary, text),
            Pair("$theme onSecondaryContainer/secondaryContainer", scheme.onSecondaryContainer, scheme.secondaryContainer, text),
            Pair("$theme onTertiaryContainer/tertiaryContainer", scheme.onTertiaryContainer, scheme.tertiaryContainer, text),
            Pair("$theme onErrorContainer/errorContainer", scheme.onErrorContainer, scheme.errorContainer, text),
            Pair("$theme onError/error", scheme.onError, scheme.error, text),
            // Every risk badge and notice: the word inside its own tinted ground.
            Pair("$theme onInfoQuiet/infoQuiet", gru.onInfoQuiet, gru.infoQuiet, text),
            Pair("$theme onSuccessQuiet/successQuiet", gru.onSuccessQuiet, gru.successQuiet, text),
            Pair("$theme onWarningQuiet/warningQuiet", gru.onWarningQuiet, gru.warningQuiet, text),
            Pair("$theme onDangerQuiet/dangerQuiet", gru.onDangerQuiet, gru.dangerQuiet, text),
            // The failure detail in the change log, and the done marker beside it.
            Pair("$theme danger/containerLow", gru.danger, scheme.surfaceContainerLow, text),
            Pair("$theme success/containerLow", gru.success, scheme.surfaceContainerLow, text),
            // Borders and icons that carry meaning, so 3:1 rather than 4.5:1.
            Pair("$theme outline/background", scheme.outline, scheme.background, seen),
            Pair("$theme outline/containerLow", scheme.outline, scheme.surfaceContainerLow, seen),
            Pair("$theme focus/background", gru.focus, scheme.background, seen),
            Pair("$theme infoBorder/infoQuiet", gru.infoBorder, gru.infoQuiet, seen),
            Pair("$theme successBorder/successQuiet", gru.successBorder, gru.successQuiet, seen),
            Pair("$theme warningBorder/warningQuiet", gru.warningBorder, gru.warningQuiet, seen),
            Pair("$theme dangerBorder/dangerQuiet", gru.dangerBorder, gru.dangerQuiet, seen),
        )
    }

    @Test
    fun `every rendered colour pair clears its contrast floor in both themes`() {
        val all = pairsFor("light", GruLightScheme, LightGruColours) +
            pairsFor("dark", GruDarkScheme, DarkGruColours)

        val failures = all.mapNotNull { pair ->
            val measured = ratio(pair.fg, pair.bg)
            if (measured < pair.floor) {
                "${pair.name} measures %.2f:1, needs %.1f:1".format(measured, pair.floor)
            } else {
                null
            }
        }

        assertEquals(emptyList<String>(), failures)
    }

    @Test
    fun `the signature is not one value pretending to work on both grounds`() {
        // Daybreak's pale value measures 1.83:1 on white and vanishes. That is arithmetic,
        // not taste, and it is why the light and dark schemes must not share the accent.
        assertEquals(
            "the light and dark accents must be different values",
            true,
            GruLightScheme.primary != GruDarkScheme.primary,
        )
        val wrongWayRound = ratio(GruDarkScheme.primary, GruLightScheme.background)
        assertEquals(
            "the dark accent on a light ground should be unusable, proving the pair matters",
            true,
            wrongWayRound < 3.0,
        )
    }
}
