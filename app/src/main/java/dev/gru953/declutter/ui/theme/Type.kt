// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Aninda Sundar Howlader (GRU953)

package dev.gru953.declutter.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.gru953.declutter.R

/*
 * Four faces, one job each, per the GRU953 type rules:
 *   Sora           -- display and headings. Never body text.
 *   Noto Sans      -- every paragraph, list, label and UI string.
 *   JetBrains Mono -- anything that must read identically everywhere: package names,
 *                     version numbers, timestamps.
 *
 * All three are SIL OFL 1.1 and are bundled subset to Latin; their OFL texts travel with
 * them in assets/licences/. None declares a Reserved Font Name, so subsetting is permitted.
 */

val SoraDisplay = FontFamily(
    Font(R.font.sora_semibold, FontWeight.SemiBold),
    Font(R.font.sora_bold, FontWeight.Bold),
)

val NotoSansText = FontFamily(
    Font(R.font.notosans_regular, FontWeight.Normal),
    Font(R.font.notosans_medium, FontWeight.Medium),
    Font(R.font.notosans_bold, FontWeight.Bold),
)

val JetBrainsMono = FontFamily(
    Font(R.font.jetbrainsmono_regular, FontWeight.Normal),
)

/*
 * The brand's modular scale is a 1.25 ratio anchored at 16px. Compose works in sp, and a
 * phone is not a browser window, so the clamped display sizes resolve to their lower bound
 * and the tracking values come straight from the tokens (-0.022em display, -0.014em
 * headings, 0 body -- expressed here in sp against each size).
 */
val GruTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = SoraDisplay,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 39.sp,
        letterSpacing = (-0.79).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = SoraDisplay,
        fontWeight = FontWeight.Bold,
        fontSize = 29.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.41).sp,
    ),
    displaySmall = TextStyle(
        fontFamily = SoraDisplay,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.34).sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = SoraDisplay,
        fontWeight = FontWeight.Bold,
        fontSize = 29.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.41).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = SoraDisplay,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.34).sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = SoraDisplay,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 25.sp,
        letterSpacing = (-0.28).sp,
    ),
    titleLarge = TextStyle(
        fontFamily = SoraDisplay,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 25.sp,
        letterSpacing = (-0.28).sp,
    ),
    titleMedium = TextStyle(
        fontFamily = NotoSansText,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = NotoSansText,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = NotoSansText,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 26.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = NotoSansText,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = NotoSansText,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 20.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = NotoSansText,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = NotoSansText,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = NotoSansText,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 1.08.sp,
    ),
)

/** Package names, timestamps, build numbers -- data, not prose. */
val MonoData = TextStyle(
    fontFamily = JetBrainsMono,
    fontWeight = FontWeight.Normal,
    fontSize = 13.sp,
    lineHeight = 20.sp,
)

/** The brand's spacing step, used where Material 3 has no opinion. */
object GruSpace {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
}
