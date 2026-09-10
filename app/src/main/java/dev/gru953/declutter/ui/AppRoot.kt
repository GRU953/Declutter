// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Aninda Sundar Howlader (GRU953)

package dev.gru953.declutter.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Button
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.gru953.declutter.R
import dev.gru953.declutter.domain.AppRow
import dev.gru953.declutter.shizuku.ShizukuState
import dev.gru953.declutter.ui.components.BrandMark
import dev.gru953.declutter.ui.components.MonoText
import dev.gru953.declutter.ui.components.Notice
import dev.gru953.declutter.ui.components.NoticeTone
import dev.gru953.declutter.ui.components.formatBytes
import dev.gru953.declutter.ui.screens.AboutScreen
import dev.gru953.declutter.ui.screens.AppListScreen
import dev.gru953.declutter.ui.screens.DetailSheet
import dev.gru953.declutter.ui.screens.HistoryScreen
import dev.gru953.declutter.ui.screens.SetupScreen
import dev.gru953.declutter.ui.theme.GruSpace

private enum class Tab(val label: String) {
    APPS("Apps"), HISTORY("Changes"), SETUP("Setup"), ABOUT("About")
}

/** What the user is being asked to confirm. */
private sealed interface Confirming {
    data class Remove(val rows: List<AppRow>, val title: String) : Confirming
    data class Disable(val rows: List<AppRow>) : Confirming
    data object Sweep : Confirming
    data object RestoreAll : Confirming
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot(vm: DeclutterViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    var tab by remember { mutableStateOf(Tab.APPS) }
    var open by remember { mutableStateOf<AppRow?>(null) }
    var confirming by remember { mutableStateOf<Confirming?>(null) }
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it)
            vm.dismissMessage()
        }
    }

    // The setup screen is where a first-time user has to start, so start them there.
    LaunchedEffect(state.shizuku) {
        if (state.shizuku != ShizukuState.READY && tab == Tab.APPS && state.rows.isEmpty()) {
            tab = Tab.SETUP
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(GruSpace.sm),
                    ) {
                        BrandMark(size = 28.dp)
                        Text("Declutter", style = MaterialTheme.typography.titleLarge)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        bottomBar = {
            NavigationBar {
                Tab.entries.forEach { entry ->
                    NavigationBarItem(
                        selected = tab == entry,
                        onClick = { tab = entry },
                        icon = {
                            Icon(
                                painter = when (entry) {
                                    Tab.APPS -> painterResource(R.drawable.ic_apps)
                                    Tab.HISTORY -> painterResource(R.drawable.ic_changes)
                                    Tab.SETUP -> painterResource(R.drawable.ic_settings)
                                    Tab.ABOUT -> painterResource(R.drawable.ic_info)
                                },
                                contentDescription = null,
                            )
                        },
                        label = { Text(entry.label) },
                    )
                }
            }
        },
        floatingActionButton = {
            if (tab == Tab.APPS && state.shizuku == ShizukuState.READY && state.busy == null) {
                if (state.selected.isNotEmpty()) {
                    ExtendedFloatingActionButton(
                        onClick = {
                            confirming = Confirming.Remove(
                                state.selectedRows,
                                "Remove the ${state.selected.size} apps you ticked",
                            )
                        },
                        text = { Text("Remove ${state.selected.size}") },
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_clear),
                                contentDescription = null,
                            )
                        },
                    )
                } else if (state.sweepCandidates.isNotEmpty()) {
                    ExtendedFloatingActionButton(
                        onClick = { confirming = Confirming.Sweep },
                        text = { Text("Quick Declutter") },
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_apps),
                                contentDescription = null,
                            )
                        },
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column {
                state.busy?.let { progress ->
                    Column(Modifier.fillMaxWidth().padding(GruSpace.lg)) {
                        Text(
                            "Working: ${progress.done} of ${progress.total}" +
                                if (progress.current.isNotBlank()) {
                                    " · ${progress.current}"
                                } else {
                                    ""
                                },
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        LinearProgressIndicator(
                            progress = {
                                if (progress.total == 0) 0f
                                else progress.done.toFloat() / progress.total
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = GruSpace.sm),
                        )
                    }
                }

                when (tab) {
                    Tab.APPS -> {
                        if (state.shizuku != ShizukuState.READY) {
                            Notice(
                                tone = NoticeTone.CAUTION,
                                title = "Shizuku is not ready",
                                body = "You can read the whole list and every explanation " +
                                    "now. Nothing can be changed until Shizuku is running.",
                                modifier = Modifier.padding(GruSpace.lg),
                                action = {
                                    TextButton(onClick = { tab = Tab.SETUP }) {
                                        Text("Show me how")
                                    }
                                },
                            )
                        }
                        AppListScreen(
                            state = state,
                            onQuery = vm::setQuery,
                            onLens = vm::setLens,
                            onToggleRisk = vm::toggleRiskFilter,
                            onToggleVendor = vm::toggleVendorFilter,
                            onClearFilters = vm::clearFilters,
                            onToggleSelect = vm::toggleSelection,
                            onSelectAllSafe = vm::selectAllSafeOnScreen,
                            onOpen = { open = it },
                        )
                    }

                    Tab.HISTORY -> HistoryScreen(
                        state = state,
                        onUndoBatch = { vm.undoBatch(it) },
                        onRestoreEverything = { confirming = Confirming.RestoreAll },
                        historyText = vm::historyAsText,
                    )

                    Tab.SETUP -> SetupScreen(
                        state = state,
                        onRecheck = vm::refreshShizuku,
                        onGrant = vm::requestShizukuPermission,
                    )

                    Tab.ABOUT -> AboutScreen(state)
                }
            }
        }
    }

    open?.let { row ->
        DetailSheet(
            row = row,
            onDismiss = { open = null },
            onRemove = {
                confirming = Confirming.Remove(listOf(row), "Remove ${row.displayName}")
                open = null
            },
            onDisable = {
                confirming = Confirming.Disable(listOf(row))
                open = null
            },
            onRestore = {
                vm.restore(row.pkg)
                open = null
            },
        )
    }

    confirming?.let { pending ->
        ConfirmDialog(
            pending = pending,
            state = state,
            onDismiss = { confirming = null },
            onGo = {
                when (pending) {
                    is Confirming.Remove -> vm.removeSelected(pending.rows)
                    is Confirming.Disable -> vm.disableSelected(pending.rows)
                    Confirming.Sweep -> vm.quickDeclutter()
                    Confirming.RestoreAll -> vm.restoreEverything()
                }
                confirming = null
            },
        )
    }
}

/**
 * The confirmation always lists the packages by name. A count alone is not consent: the
 * whole difference between a deliberate change and an accident is seeing what is in it.
 */
@Composable
private fun ConfirmDialog(
    pending: Confirming,
    state: UiState,
    onDismiss: () -> Unit,
    onGo: () -> Unit,
) {
    val rows = when (pending) {
        is Confirming.Remove -> pending.rows
        is Confirming.Disable -> pending.rows
        Confirming.Sweep -> state.sweepCandidates
        Confirming.RestoreAll -> state.rows.filter {
            it.state != dev.gru953.declutter.domain.PackageState.ACTIVE
        }
    }
    val heading = when (pending) {
        is Confirming.Remove -> pending.title
        is Confirming.Disable -> "Turn off ${rows.size} ${plural(rows.size)}"
        Confirming.Sweep -> "Quick Declutter: remove ${rows.size} ${plural(rows.size)}"
        Confirming.RestoreAll -> "Put back ${rows.size} ${plural(rows.size)}"
    }
    val bytes = rows.sumOf { it.installedSizeBytes }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(heading) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 380.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(GruSpace.sm),
            ) {
                when (pending) {
                    Confirming.Sweep -> Text(
                        "These are every app rated Safe that is still active. Safe means " +
                            "removing it loses only that app's own feature -- but it does " +
                            "lose it, so read the list. Each one can be put back from the " +
                            "Changes tab.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    is Confirming.Disable -> Text(
                        "Turning off keeps the app on the phone and keeps any updates it " +
                            "has had. It can be turned back on instantly.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Confirming.RestoreAll -> Text(
                        "Everything you have removed or turned off will be put back.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    is Confirming.Remove -> Text(
                        "Removing takes these away for you only. The copy your phone was " +
                            "built with stays on it, so they can be put back from the " +
                            "Changes tab -- while the phone still works. Any Play Store " +
                            "updates they had are discarded, so a restored app is the " +
                            "version the phone shipped with.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                if (bytes > 0) {
                    Text(
                        "About ${formatBytes(bytes)} of installed files.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                rows.forEach { row ->
                    Column(modifier = Modifier.padding(top = GruSpace.xs)) {
                        Text(row.displayName, style = MaterialTheme.typography.titleSmall)
                        MonoText(row.pkg)
                        row.entry?.let {
                            Text(
                                it.loses,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onGo, enabled = rows.isNotEmpty()) {
                Text(
                    when (pending) {
                        is Confirming.Disable -> "Turn off"
                        Confirming.RestoreAll -> "Put back"
                        else -> "Remove"
                    },
                )
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

private fun plural(count: Int) = if (count == 1) "app" else "apps"
