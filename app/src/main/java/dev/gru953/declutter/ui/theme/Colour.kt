// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Aninda Sundar Howlader (GRU953)

package dev.gru953.declutter.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/*
 * The GRU953 role tokens, transcribed from the brand kit's tokens.json.
 *
 * The signature is one hue with two calibrated values: on a light ground the accent is
 * Daybreak #B45A39 (4.71:1 on white); on a dark ground it is Daybreak #FFAB8E (10.55:1 on
 * Ink). No single value clears 4.5:1 against both grounds, so neither is ever hard-coded
 * into a component -- reach for the scheme, and let the theme choose.
 */

// Light roles.
private val LightBg = Color(0xFFFFFFFF)
private val LightBgSubtle = Color(0xFFF3F6FF)
private val LightSurface = Color(0xFFFFFFFF)
private val LightSurfaceRaised = Color(0xFFFBFBFD)
private val LightSurfaceSunken = Color(0xFFF3F6FF)
private val LightInk = Color(0xFF0B0E14)
private val LightInkMuted = Color(0xFF4D5157)
private val LightInkSubtle = Color(0xFF6A6D74)
private val LightInkInverse = Color(0xFFFFFFFF)
private val LightBorder = Color(0xFFE6EBFF)
private val LightBorderStrong = Color(0xFF6469D3)
private val LightBrand = Color(0xFF1A1753)
private val LightBrandQuiet = Color(0xFFEEF0FF)
private val LightOnBrandQuiet = Color(0xFF4C4EAD)
private val LightOnBrand = Color(0xFFFFFFFF)
private val LightAccent = Color(0xFFB45A39)
private val LightAccentQuiet = Color(0xFFFFF2EE)
private val LightOnAccentQuiet = Color(0xFF914124)
private val LightOnAccent = Color(0xFFFFFFFF)
private val LightFocus = Color(0xFF6469D3)
private val LightInfo = Color(0xFF4C4EAD)
private val LightInfoQuiet = Color(0xFFEEF0FF)
private val LightOnInfoQuiet = Color(0xFF4C4EAD)
private val LightInfoBorder = Color(0xFF6469D3)
private val LightSuccess = Color(0xFF007131)
private val LightSuccessQuiet = Color(0xFFEEF9F1)
private val LightOnSuccessQuiet = Color(0xFF007131)
private val LightSuccessBorder = Color(0xFF009047)
private val LightWarning = Color(0xFF805100)
private val LightWarningQuiet = Color(0xFFFDF3E5)
private val LightOnWarningQuiet = Color(0xFF805100)
private val LightWarningBorder = Color(0xFFA36A00)
private val LightDanger = Color(0xFFCE393A)
private val LightDangerQuiet = Color(0xFFFFF2F0)
private val LightOnDangerQuiet = Color(0xFFA71F25)
private val LightDangerBorder = Color(0xFFF25855)
private val LightOnDanger = Color(0xFFFFFFFF)
private val LightDisabledBg = Color(0xFFF3F6FF)
private val LightDisabledInk = Color(0xFF8A8F9C)

// Dark roles.
private val DarkBg = Color(0xFF0B0E14)
private val DarkBgSubtle = Color(0xFF12161F)
private val DarkSurface = Color(0xFF141926)
private val DarkSurfaceRaised = Color(0xFF1B2130)
private val DarkSurfaceSunken = Color(0xFF080A0F)
private val DarkInk = Color(0xFFF4F5F9)
private val DarkInkMuted = Color(0xFFA8ACB4)
private val DarkInkSubtle = Color(0xFF858990)
private val DarkInkInverse = Color(0xFF0B0E14)
private val DarkBorder = Color(0xFF242B3A)
private val DarkBorderStrong = Color(0xFF6469D3)
private val DarkBrand = Color(0xFF7E86F6)
private val DarkBrandQuiet = Color(0xFF141728)
private val DarkOnBrandQuiet = Color(0xFF7E86F6)
private val DarkOnBrand = Color(0xFF0B0E14)
private val DarkAccent = Color(0xFFFFAB8E)
private val DarkAccentQuiet = Color(0xFF1B1518)
private val DarkOnAccentQuiet = Color(0xFFE26C42)
private val DarkOnAccent = Color(0xFF0B0E14)
private val DarkFocus = Color(0xFF6469D3)
private val DarkInfo = Color(0xFF7E86F6)
private val DarkInfoQuiet = Color(0xFF141728)
private val DarkOnInfoQuiet = Color(0xFF7E86F6)
private val DarkInfoBorder = Color(0xFF6469D3)
private val DarkSuccess = Color(0xFF32AE62)
private val DarkSuccessQuiet = Color(0xFF0E1A19)
private val DarkOnSuccessQuiet = Color(0xFF32AE62)
private val DarkSuccessBorder = Color(0xFF009047)
private val DarkWarning = Color(0xFFC88400)
private val DarkWarningQuiet = Color(0xFF181616)
private val DarkOnWarningQuiet = Color(0xFFC88400)
private val DarkWarningBorder = Color(0xFFA36A00)
private val DarkDanger = Color(0xFFF25855)
private val DarkDangerQuiet = Color(0xFF1E1318)
private val DarkOnDangerQuiet = Color(0xFFF25855)
private val DarkDangerBorder = Color(0xFFCE393A)
private val DarkOnDanger = Color(0xFF0B0E14)
private val DarkDisabledBg = Color(0xFF171B26)
private val DarkDisabledInk = Color(0xFF6A7183)

/**
 * The GRU953 roles Material 3 has no slot for. Colour never carries meaning on its own here --
 * every place these are used also carries a word and a shape.
 */
@Immutable
data class GruColours(
    val inkMuted: Color,
    val inkSubtle: Color,
    val inkInverse: Color,
    val borderStrong: Color,
    val focus: Color,
    val surfaceSunken: Color,
    val bgSubtle: Color,
    val info: Color,
    val infoQuiet: Color,
    val onInfoQuiet: Color,
    val infoBorder: Color,
    val success: Color,
    val successQuiet: Color,
    val onSuccessQuiet: Color,
    val successBorder: Color,
    val warning: Color,
    val warningQuiet: Color,
    val onWarningQuiet: Color,
    val warningBorder: Color,
    val danger: Color,
    val dangerQuiet: Color,
    val onDangerQuiet: Color,
    val dangerBorder: Color,
    val disabledBg: Color,
    val disabledInk: Color,
)

internal val LightGruColours = GruColours(
    inkMuted = LightInkMuted,
    inkSubtle = LightInkSubtle,
    inkInverse = LightInkInverse,
    borderStrong = LightBorderStrong,
    focus = LightFocus,
    surfaceSunken = LightSurfaceSunken,
    bgSubtle = LightBgSubtle,
    info = LightInfo,
    infoQuiet = LightInfoQuiet,
    onInfoQuiet = LightOnInfoQuiet,
    infoBorder = LightInfoBorder,
    success = LightSuccess,
    successQuiet = LightSuccessQuiet,
    onSuccessQuiet = LightOnSuccessQuiet,
    successBorder = LightSuccessBorder,
    warning = LightWarning,
    warningQuiet = LightWarningQuiet,
    onWarningQuiet = LightOnWarningQuiet,
    warningBorder = LightWarningBorder,
    danger = LightDanger,
    dangerQuiet = LightDangerQuiet,
    onDangerQuiet = LightOnDangerQuiet,
    dangerBorder = LightDangerBorder,
    disabledBg = LightDisabledBg,
    disabledInk = LightDisabledInk,
)

internal val DarkGruColours = GruColours(
    inkMuted = DarkInkMuted,
    inkSubtle = DarkInkSubtle,
    inkInverse = DarkInkInverse,
    borderStrong = DarkBorderStrong,
    focus = DarkFocus,
    surfaceSunken = DarkSurfaceSunken,
    bgSubtle = DarkBgSubtle,
    info = DarkInfo,
    infoQuiet = DarkInfoQuiet,
    onInfoQuiet = DarkOnInfoQuiet,
    infoBorder = DarkInfoBorder,
    success = DarkSuccess,
    successQuiet = DarkSuccessQuiet,
    onSuccessQuiet = DarkOnSuccessQuiet,
    successBorder = DarkSuccessBorder,
    warning = DarkWarning,
    warningQuiet = DarkWarningQuiet,
    onWarningQuiet = DarkOnWarningQuiet,
    warningBorder = DarkWarningBorder,
    danger = DarkDanger,
    dangerQuiet = DarkDangerQuiet,
    onDangerQuiet = DarkOnDangerQuiet,
    dangerBorder = DarkDangerBorder,
    disabledBg = DarkDisabledBg,
    disabledInk = DarkDisabledInk,
)

val LocalGruColours = staticCompositionLocalOf { LightGruColours }

internal val GruLightScheme = lightColorScheme(
    primary = LightAccent,
    onPrimary = LightOnAccent,
    primaryContainer = LightAccentQuiet,
    onPrimaryContainer = LightOnAccentQuiet,
    secondary = LightBrand,
    onSecondary = LightOnBrand,
    secondaryContainer = LightBrandQuiet,
    onSecondaryContainer = LightOnBrandQuiet,
    tertiary = LightInfo,
    onTertiary = LightInkInverse,
    tertiaryContainer = LightInfoQuiet,
    onTertiaryContainer = LightOnInfoQuiet,
    background = LightBg,
    onBackground = LightInk,
    surface = LightSurface,
    onSurface = LightInk,
    surfaceVariant = LightBgSubtle,
    onSurfaceVariant = LightInkMuted,
    surfaceContainerLowest = LightBg,
    surfaceContainerLow = LightSurfaceRaised,
    surfaceContainer = LightSurfaceRaised,
    surfaceContainerHigh = LightBgSubtle,
    surfaceContainerHighest = LightSurfaceSunken,
    inverseSurface = LightInk,
    inverseOnSurface = LightInkInverse,
    outline = LightBorderStrong,
    outlineVariant = LightBorder,
    error = LightDanger,
    onError = LightOnDanger,
    errorContainer = LightDangerQuiet,
    onErrorContainer = LightOnDangerQuiet,
    scrim = Color(0x8C0B0E14),
)

internal val GruDarkScheme = darkColorScheme(
    primary = DarkAccent,
    onPrimary = DarkOnAccent,
    primaryContainer = DarkAccentQuiet,
    onPrimaryContainer = DarkOnAccentQuiet,
    secondary = DarkBrand,
    onSecondary = DarkOnBrand,
    secondaryContainer = DarkBrandQuiet,
    onSecondaryContainer = DarkOnBrandQuiet,
    tertiary = DarkInfo,
    onTertiary = DarkInkInverse,
    tertiaryContainer = DarkInfoQuiet,
    onTertiaryContainer = DarkOnInfoQuiet,
    background = DarkBg,
    onBackground = DarkInk,
    surface = DarkSurface,
    onSurface = DarkInk,
    surfaceVariant = DarkBgSubtle,
    onSurfaceVariant = DarkInkMuted,
    surfaceContainerLowest = DarkSurfaceSunken,
    surfaceContainerLow = DarkBgSubtle,
    surfaceContainer = DarkSurface,
    surfaceContainerHigh = DarkSurfaceRaised,
    surfaceContainerHighest = DarkSurfaceRaised,
    inverseSurface = DarkInk,
    inverseOnSurface = DarkInkInverse,
    outline = DarkBorderStrong,
    outlineVariant = DarkBorder,
    error = DarkDanger,
    onError = DarkOnDanger,
    errorContainer = DarkDangerQuiet,
    onErrorContainer = DarkOnDangerQuiet,
    scrim = Color(0xA8030407),
)
