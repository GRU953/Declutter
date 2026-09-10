// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Aninda Sundar Howlader (GRU953)

package dev.gru953.declutter.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.gru953.declutter.R
import dev.gru953.declutter.catalogue.Confidence
import dev.gru953.declutter.catalogue.Risk
import dev.gru953.declutter.ui.theme.GruSpace
import dev.gru953.declutter.ui.theme.LocalGruColours
import dev.gru953.declutter.ui.theme.MonoData

/**
 * A risk badge always carries three things: a colour, a shape and a word. Colour alone
 * would fail anyone who cannot distinguish these hues, and "unsafe" is not a message worth
 * losing.
 */
@Composable
fun RiskBadge(risk: Risk, modifier: Modifier = Modifier) {
    val gru = LocalGruColours.current
    val (background, foreground, border, icon) = when (risk) {
        Risk.SAFE -> Quad(
            gru.successQuiet, gru.onSuccessQuiet, gru.successBorder,
            painterResource(R.drawable.ic_ok),
        )
        Risk.ADVANCED -> Quad(
            gru.infoQuiet, gru.onInfoQuiet, gru.infoBorder,
            painterResource(R.drawable.ic_info),
        )
        Risk.EXPERT -> Quad(
            gru.warningQuiet, gru.onWarningQuiet, gru.warningBorder,
            painterResource(R.drawable.ic_caution),
        )
        Risk.UNSAFE -> Quad(
            gru.dangerQuiet, gru.onDangerQuiet, gru.dangerBorder,
            painterResource(R.drawable.ic_not_offered),
        )
        Risk.UNKNOWN -> Quad(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            MaterialTheme.colorScheme.outline,
            painterResource(R.drawable.ic_unchecked),
        )
    }
    Row(
        modifier = modifier
            .background(background, RoundedCornerShape(GruSpace.md))
            .border(BorderStroke(1.dp, border), RoundedCornerShape(GruSpace.md))
            .padding(horizontal = GruSpace.sm, vertical = GruSpace.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(GruSpace.xs),
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            tint = foreground,
            modifier = Modifier.size(14.dp),
        )
        Text(risk.label, style = MaterialTheme.typography.labelMedium, color = foreground)
    }
}

private data class Quad(
    val background: Color,
    val foreground: Color,
    val border: Color,
    val icon: Painter,
)

/** What a message means, spelled out rather than implied by a colour. */
enum class NoticeTone { INFO, GOOD, CAUTION, STOP }

@Composable
fun Notice(
    tone: NoticeTone,
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    val gru = LocalGruColours.current
    val (background, foreground, border, icon) = when (tone) {
        NoticeTone.INFO -> Quad(
            gru.infoQuiet, gru.onInfoQuiet, gru.infoBorder,
            painterResource(R.drawable.ic_info),
        )
        NoticeTone.GOOD -> Quad(
            gru.successQuiet, gru.onSuccessQuiet, gru.successBorder,
            painterResource(R.drawable.ic_ok),
        )
        NoticeTone.CAUTION -> Quad(
            gru.warningQuiet, gru.onWarningQuiet, gru.warningBorder,
            painterResource(R.drawable.ic_caution),
        )
        NoticeTone.STOP -> Quad(
            gru.dangerQuiet, gru.onDangerQuiet, gru.dangerBorder,
            painterResource(R.drawable.ic_stop_sign),
        )
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(background, RoundedCornerShape(GruSpace.md))
            .border(BorderStroke(1.dp, border), RoundedCornerShape(GruSpace.md))
            .padding(GruSpace.lg),
        horizontalArrangement = Arrangement.spacedBy(GruSpace.md),
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            tint = foreground,
            modifier = Modifier.size(20.dp),
        )
        Column(verticalArrangement = Arrangement.spacedBy(GruSpace.xs)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = foreground)
            Text(body, style = MaterialTheme.typography.bodyMedium, color = foreground)
            if (action != null) {
                Spacer(Modifier.size(GruSpace.xs))
                action()
            }
        }
    }
}

/** Package names, version numbers, timestamps -- data, so the monospaced face. */
@Composable
fun MonoText(text: String, modifier: Modifier = Modifier, maxLines: Int = 1) {
    Text(
        text = text,
        style = MonoData,
        color = LocalGruColours.current.inkSubtle,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )
}

@Composable
fun SectionHeading(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier.padding(top = GruSpace.xl, bottom = GruSpace.sm),
    )
}

@Composable
fun ConfidenceNote(confidence: Confidence?, modifier: Modifier = Modifier) {
    if (confidence == null) return
    val words = when (confidence) {
        Confidence.HIGH -> "Checked against this phone's own firmware."
        Confidence.MEDIUM -> "Based on community reports, not on this exact phone."
        Confidence.LOW -> "Not well established. Treat this entry with caution."
    }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(GruSpace.xs),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_info),
            contentDescription = null,
            tint = LocalGruColours.current.inkSubtle,
            modifier = Modifier.size(14.dp),
        )
        Text(
            words,
            style = MaterialTheme.typography.bodySmall,
            color = LocalGruColours.current.inkSubtle,
        )
    }
}

/** Reads bytes as something a person would say. */
fun formatBytes(bytes: Long): String = when {
    bytes <= 0 -> "unknown size"
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "${bytes / 1024} KB"
    bytes < 1024L * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
    else -> String.format(java.util.Locale.UK, "%.1f GB", bytes / (1024.0 * 1024 * 1024))
}

@Composable
fun Gap(size: androidx.compose.ui.unit.Dp = GruSpace.lg) {
    Spacer(Modifier.size(size))
}

@Composable
fun HorizontalGap(size: androidx.compose.ui.unit.Dp = GruSpace.sm) {
    Spacer(Modifier.width(size))
}

/** The Soaring Bird, never below 24px and never animated. */
@Composable
fun BrandMark(modifier: Modifier = Modifier, size: androidx.compose.ui.unit.Dp = 32.dp) {
    Box(modifier = modifier.size(size)) {
        Icon(
            painter = painterResource(R.drawable.ic_bird),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(size),
        )
    }
}
