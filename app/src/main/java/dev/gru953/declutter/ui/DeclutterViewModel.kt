// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Aninda Sundar Howlader (GRU953)

package dev.gru953.declutter.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.gru953.declutter.catalogue.CatalogueEntry
import dev.gru953.declutter.catalogue.CatalogueLoader
import dev.gru953.declutter.catalogue.Risk
import dev.gru953.declutter.catalogue.Vendor
import dev.gru953.declutter.data.HistoryBatch
import dev.gru953.declutter.data.HistoryStore
import dev.gru953.declutter.domain.Action
import dev.gru953.declutter.domain.AppRow
import dev.gru953.declutter.domain.DebloatService
import dev.gru953.declutter.domain.PackageScanner
import dev.gru953.declutter.domain.PackageState
import dev.gru953.declutter.domain.debuggingIsBlockedByPolicy
import dev.gru953.declutter.domain.securityPatchLevel
import dev.gru953.declutter.shizuku.PrivilegedPackages
import dev.gru953.declutter.shizuku.ShizukuGate
import dev.gru953.declutter.shizuku.ShizukuState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Which packages the list is showing. */
enum class Lens {
    /** Preinstalled apps still active, worth reviewing. */
    REVIEW,

    /** What has already been removed or turned off, and can be put back. */
    CHANGED,

    /** Everything, including what the app will not touch. */
    EVERYTHING,
    ;

    val label: String
        get() = when (this) {
            REVIEW -> "To review"
            CHANGED -> "Changed"
            EVERYTHING -> "All apps"
        }
}

data class Progress(val done: Int, val total: Int, val current: String)

data class UiState(
    val loading: Boolean = true,
    val shizuku: ShizukuState = ShizukuState.NOT_RUNNING,
    val rows: List<AppRow> = emptyList(),
    val lens: Lens = Lens.REVIEW,
    val query: String = "",
    val riskFilter: Set<Risk> = emptySet(),
    val vendorFilter: Set<Vendor> = emptySet(),
    val selected: Set<String> = emptySet(),
    val catalogueReviewed: String = "",
    val catalogueSize: Int = 0,
    val busy: Progress? = null,
    val message: String? = null,
    val history: List<HistoryBatch> = emptyList(),
    /** The phone forbids developer options, so Shizuku cannot work at all. */
    val debuggingBlockedByPolicy: Boolean = false,
    /** The phone's OS security patch level, shown as information, not as a verdict. */
    val securityPatch: String? = null,
    val runningOutsideUserZero: Boolean = false,
) {
    val visible: List<AppRow>
        get() {
            val byLens = when (lens) {
                Lens.REVIEW -> rows.filter {
                    it.state == PackageState.ACTIVE && it.isSystem && it.actionable
                }
                Lens.CHANGED -> rows.filter { it.state != PackageState.ACTIVE }
                Lens.EVERYTHING -> rows
            }
            val needle = query.trim().lowercase()
            return byLens
                .filter { row ->
                    (needle.isEmpty() ||
                        row.displayName.lowercase().contains(needle) ||
                        row.pkg.lowercase().contains(needle)) &&
                        (riskFilter.isEmpty() || row.risk in riskFilter) &&
                        (vendorFilter.isEmpty() || row.vendor in vendorFilter)
                }
                .sortedWith(
                    compareBy({ it.risk.ordinal }, { it.displayName.lowercase() }),
                )
        }

    /** The Safe-rated, still-active packages a one-tap sweep is allowed to touch. */
    val sweepCandidates: List<AppRow> get() = rows.filter { it.sweepable }

    val selectedRows: List<AppRow> get() = rows.filter { it.pkg in selected }

    val reclaimableBytes: Long get() = selectedRows.sumOf { it.installedSizeBytes }
}

class DeclutterViewModel(app: Application) : AndroidViewModel(app) {

    private val gate = ShizukuGate()
    private val privileged = PrivilegedPackages(app)
    private val historyStore = HistoryStore(app)
    private val service = DebloatService(
        gate = gate,
        privileged = privileged,
        history = historyStore,
        clock = System::currentTimeMillis,
    )

    private var catalogue: Map<String, CatalogueEntry> = emptyMap()

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
        gate.start()
        viewModelScope.launch {
            gate.state.collect { shizuku ->
                _state.value = _state.value.copy(shizuku = shizuku)
            }
        }
        viewModelScope.launch {
            historyStore.history.collect { file ->
                _state.value = _state.value.copy(history = file.batches)
            }
        }
        viewModelScope.launch {
            historyStore.load()
            val file = CatalogueLoader(getApplication()).load()
            catalogue = file.entries.associateBy { it.pkg }
            _state.value = _state.value.copy(
                catalogueReviewed = file.reviewed,
                catalogueSize = file.entries.size,
                debuggingBlockedByPolicy = getApplication<Application>()
                    .debuggingIsBlockedByPolicy(),
                securityPatch = securityPatchLevel(),
                runningOutsideUserZero = gate.runningOutsideUserZero,
            )
            rescan()
        }
    }

    override fun onCleared() {
        gate.stop()
    }

    fun refreshShizuku() = gate.refresh()

    fun requestShizukuPermission() = gate.requestPermission()

    fun rescan() = viewModelScope.launch {
        _state.value = _state.value.copy(loading = true)
        val rows = withContext(Dispatchers.IO) {
            PackageScanner(getApplication(), catalogue).scan()
        }
        val stillPresent = rows.map { it.pkg }.toSet()
        _state.value = _state.value.copy(
            loading = false,
            rows = rows,
            selected = _state.value.selected.intersect(stillPresent),
        )
    }

    fun setLens(lens: Lens) {
        _state.value = _state.value.copy(lens = lens)
    }

    fun setQuery(query: String) {
        _state.value = _state.value.copy(query = query)
    }

    fun toggleRiskFilter(risk: Risk) {
        val current = _state.value.riskFilter
        _state.value = _state.value.copy(
            riskFilter = if (risk in current) current - risk else current + risk,
        )
    }

    fun toggleVendorFilter(vendor: Vendor) {
        val current = _state.value.vendorFilter
        _state.value = _state.value.copy(
            vendorFilter = if (vendor in current) current - vendor else current + vendor,
        )
    }

    fun clearFilters() {
        _state.value = _state.value.copy(riskFilter = emptySet(), vendorFilter = emptySet())
    }

    fun toggleSelection(pkg: String) {
        val row = _state.value.rows.firstOrNull { it.pkg == pkg } ?: return
        if (!row.actionable) return
        val current = _state.value.selected
        _state.value = _state.value.copy(
            selected = if (pkg in current) current - pkg else current + pkg,
        )
    }

    /** Ticks every Safe-rated package on screen. It selects; it never acts. */
    fun selectAllSafeOnScreen() {
        val safe = _state.value.visible
            .filter { it.risk == Risk.SAFE && it.actionable && it.state == PackageState.ACTIVE }
            .map { it.pkg }
        _state.value = _state.value.copy(selected = _state.value.selected + safe)
    }

    fun clearSelection() {
        _state.value = _state.value.copy(selected = emptySet())
    }

    fun dismissMessage() {
        _state.value = _state.value.copy(message = null)
    }

    fun removeSelected(rows: List<AppRow> = _state.value.selectedRows) =
        perform(if (rows.size == 1) "Removed ${rows.first().displayName}" else "Removed ${rows.size} apps", rows, Action.REMOVE)

    fun disableSelected(rows: List<AppRow> = _state.value.selectedRows) =
        perform(if (rows.size == 1) "Turned off ${rows.first().displayName}" else "Turned off ${rows.size} apps", rows, Action.DISABLE)

    /** The one-tap sweep. Bounded to the Safe tier; the confirmation lists every package. */
    fun quickDeclutter() = perform(
        "Quick Declutter",
        _state.value.sweepCandidates,
        Action.REMOVE,
    )

    fun restore(pkg: String) {
        val row = _state.value.rows.firstOrNull { it.pkg == pkg } ?: return
        val action = if (row.state == PackageState.DISABLED) Action.ENABLE else Action.RESTORE
        perform("Put back ${row.displayName}", listOf(row), action)
    }

    /** Undo one batch: everything it removed or turned off, in reverse order. */
    fun undoBatch(batch: HistoryBatch) {
        val rows = batch.restorable.reversed().mapNotNull { item ->
            _state.value.rows.firstOrNull { it.pkg == item.pkg }
        }
        if (rows.isEmpty()) {
            _state.value = _state.value.copy(
                message = "Nothing in that batch is still removed.",
            )
            return
        }
        performMixed("Undid ‘${batch.title}’", rows)
    }

    /** Put everything back, newest change first. */
    fun restoreEverything() {
        val rows = _state.value.rows.filter { it.state != PackageState.ACTIVE }
        if (rows.isEmpty()) {
            _state.value = _state.value.copy(message = "Nothing has been removed.")
            return
        }
        performMixed("Put everything back", rows)
    }

    fun historyAsText(): String = historyStore.asPlainText()

    /** Restores and re-enables in one pass, choosing the right call for each package. */
    private fun performMixed(title: String, rows: List<AppRow>) {
        val removed = rows.filter { it.state == PackageState.REMOVED }
        val disabled = rows.filter { it.state == PackageState.DISABLED }
        viewModelScope.launch {
            val first = runBatch(title, removed, Action.RESTORE)
            val second = runBatch(title, disabled, Action.ENABLE)
            report(listOfNotNull(first, second))
        }
    }

    private fun perform(title: String, rows: List<AppRow>, action: Action) {
        if (rows.isEmpty()) {
            _state.value = _state.value.copy(message = "Nothing was selected.")
            return
        }
        viewModelScope.launch {
            report(listOfNotNull(runBatch(title, rows, action)))
        }
    }

    private suspend fun runBatch(
        title: String,
        rows: List<AppRow>,
        action: Action,
    ): DebloatService.Report? {
        if (rows.isEmpty()) return null

        service.preflight(rows)?.let { refusal ->
            _state.value = _state.value.copy(
                message = when (refusal) {
                    DebloatService.Refusal.NotReady ->
                        "Shizuku is not running. Start it, then try again."
                    DebloatService.Refusal.WrongUser ->
                        "Declutter only works in the phone's main profile, and this is not it."
                    is DebloatService.Refusal.Guarded ->
                        "${refusal.pkg} cannot be changed: ${refusal.reason}"
                },
            )
            return null
        }

        _state.value = _state.value.copy(busy = Progress(0, rows.size, ""))
        val report = service.run(
            title = title,
            rows = rows,
            action = action,
            rescan = { pkg -> _state.value.rows.firstOrNull { it.pkg == pkg } },
            onProgress = { done, total, current ->
                _state.value = _state.value.copy(busy = Progress(done, total, current))
            },
        )
        _state.value = _state.value.copy(busy = null, selected = emptySet())
        rescan().join()
        return report
    }

    private fun report(reports: List<DebloatService.Report>) {
        if (reports.isEmpty()) return
        val done = reports.sumOf { it.succeeded }
        val attempted = reports.sumOf { it.attempted }
        val failure = reports.firstNotNullOfOrNull { it.firstFailure }
        _state.value = _state.value.copy(
            message = when {
                failure != null && done == 0 -> "Nothing was changed. $failure"
                failure != null ->
                    "$done of $attempted done, then it stopped: $failure"
                done == 1 -> "One app done."
                else -> "$done apps done."
            },
        )
    }
}
