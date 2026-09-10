// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Aninda Sundar Howlader (GRU953)

package dev.gru953.declutter.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.gru953.declutter.R
import dev.gru953.declutter.catalogue.Risk
import dev.gru953.declutter.catalogue.Vendor
import dev.gru953.declutter.domain.AppRow
import dev.gru953.declutter.domain.Block
import dev.gru953.declutter.domain.PackageState
import dev.gru953.declutter.ui.Lens
import dev.gru953.declutter.ui.UiState
import dev.gru953.declutter.ui.components.MonoText
import dev.gru953.declutter.ui.components.Notice
import dev.gru953.declutter.ui.components.NoticeTone
import dev.gru953.declutter.ui.components.RiskBadge
import dev.gru953.declutter.ui.components.formatBytes
import dev.gru953.declutter.ui.theme.GruSpace
import dev.gru953.declutter.ui.theme.LocalGruColours

@Composable
fun AppListScreen(
    state: UiState,
    onQuery: (String) -> Unit,
    onLens: (Lens) -> Unit,
    onToggleRisk: (Risk) -> Unit,
    onToggleVendor: (Vendor) -> Unit,
    onClearFilters: () -> Unit,
    onToggleSelect: (String) -> Unit,
    onSelectAllSafe: () -> Unit,
    onOpen: (AppRow) -> Unit,
    modifier: Modifier = Modifier,
) {
    val gru = LocalGruColours.current
    val visible = state.visible

    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = state.query,
            onValueChange = onQuery,
            singleLine = true,
            label = { Text("Search by name or package") },
            leadingIcon = { Icon(
            painter = painterResource(R.drawable.ic_search), contentDescription = null) },
            trailingIcon = {
                if (state.query.isNotEmpty()) {
                    IconButton(onClick = { onQuery("") }) {
                        Icon(
            painter = painterResource(R.drawable.ic_clear), contentDescription = "Clear the search")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = GruSpace.lg, vertical = GruSpace.sm),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = GruSpace.lg),
            horizontalArrangement = Arrangement.spacedBy(GruSpace.sm),
        ) {
            Lens.entries.forEach { lens ->
                FilterChip(
                    selected = state.lens == lens,
                    onClick = { onLens(lens) },
                    label = { Text(lens.label) },
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = GruSpace.lg, vertical = GruSpace.sm),
            horizontalArrangement = Arrangement.spacedBy(GruSpace.sm),
        ) {
            Risk.entries.forEach { risk ->
                FilterChip(
                    selected = risk in state.riskFilter,
                    onClick = { onToggleRisk(risk) },
                    label = { Text(risk.label) },
                )
            }
            listOf(
                Vendor.MOTOROLA, Vendor.GOOGLE, Vendor.QUALCOMM,
                Vendor.PREINSTALLED, Vendor.CARRIER,
            ).forEach { vendor ->
                FilterChip(
                    selected = vendor in state.vendorFilter,
                    onClick = { onToggleVendor(vendor) },
                    label = { Text(vendor.label) },
                )
            }
            if (state.riskFilter.isNotEmpty() || state.vendorFilter.isNotEmpty()) {
                TextButton(onClick = onClearFilters) { Text("Clear filters") }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = GruSpace.lg, vertical = GruSpace.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "${visible.size} shown" +
                    if (state.selected.isEmpty()) "" else ", ${state.selected.size} ticked",
                style = MaterialTheme.typography.bodySmall,
                color = gru.inkSubtle,
            )
            if (state.lens != Lens.CHANGED) {
                TextButton(onClick = onSelectAllSafe) { Text("Tick all Safe") }
            }
        }

        if (visible.isEmpty()) {
            Notice(
                tone = NoticeTone.INFO,
                title = "Nothing to show",
                body = if (state.query.isNotEmpty() || state.riskFilter.isNotEmpty()) {
                    "No app matches what you have searched or filtered for."
                } else if (state.lens == Lens.CHANGED) {
                    "You have not removed or turned off anything yet."
                } else {
                    "Nothing here needs your attention."
                },
                modifier = Modifier.padding(GruSpace.lg),
            )
            return@Column
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = GruSpace.lg,
                end = GruSpace.lg,
                bottom = 96.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(GruSpace.xs),
        ) {
            items(visible, key = { it.pkg }) { row ->
                AppRowItem(
                    row = row,
                    ticked = row.pkg in state.selected,
                    onTick = { onToggleSelect(row.pkg) },
                    onOpen = { onOpen(row) },
                )
            }
        }
    }
}

@Composable
private fun AppRowItem(
    row: AppRow,
    ticked: Boolean,
    onTick: () -> Unit,
    onOpen: () -> Unit,
) {
    val gru = LocalGruColours.current
    val stateWord = when (row.state) {
        PackageState.ACTIVE -> null
        PackageState.DISABLED -> "Turned off"
        PackageState.REMOVED -> "Removed"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceContainerLow,
                RoundedCornerShape(GruSpace.md),
            )
            .clickable(onClick = onOpen)
            .padding(GruSpace.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(GruSpace.md),
    ) {
        if (row.actionable && row.state == PackageState.ACTIVE) {
            Checkbox(
                checked = ticked,
                onCheckedChange = { onTick() },
                modifier = Modifier.semantics {
                    contentDescription = "Tick ${row.displayName} for removal"
                },
            )
        } else {
            Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(
                        if (row.state == PackageState.ACTIVE) {
                            R.drawable.ic_not_offered
                        } else {
                            R.drawable.ic_turned_off
                        },
                    ),
                    contentDescription = if (row.state == PackageState.ACTIVE) {
                        "Not offered"
                    } else {
                        stateWord
                    },
                    tint = gru.inkSubtle,
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = row.displayName,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            MonoText(row.pkg)
            Row(
                horizontalArrangement = Arrangement.spacedBy(GruSpace.sm),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp),
            ) {
                RiskBadge(row.risk)
                Text(
                    text = buildString {
                        stateWord?.let { append(it); append(" · ") }
                        append(row.vendor.label)
                        if (row.installedSizeBytes > 0) {
                            append(" · ")
                            append(formatBytes(row.installedSizeBytes))
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = gru.inkSubtle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (row.block == Block.PROTECTED || row.block == Block.IN_USE) {
                Text(
                    text = row.blockReason.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = gru.inkMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }
}
