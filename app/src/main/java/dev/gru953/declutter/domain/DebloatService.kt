// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Aninda Sundar Howlader (GRU953)

package dev.gru953.declutter.domain

import android.util.Log
import dev.gru953.declutter.data.HistoryItem
import dev.gru953.declutter.data.HistoryStore
import dev.gru953.declutter.shizuku.PrivilegedPackages
import dev.gru953.declutter.shizuku.ShizukuGate
import dev.gru953.declutter.shizuku.ShizukuState

/**
 * Carries out a batch of changes, and refuses to carry out the ones it should not.
 *
 * Three rules, all of them the difference between a good afternoon and a bad one:
 *
 *  1. The restore record is written before the first change is attempted, not after the
 *     last. If the app is killed mid-batch -- and Motorola's battery software does kill
 *     background apps -- the record still says what was taken.
 *  2. A batch stops at the first failure. Continuing past one is how a phone ends up in a
 *     state nobody planned.
 *  3. Every package is re-checked against the guards at the moment it is acted on, not at
 *     the moment it was selected. The user may have changed their keyboard in between.
 */
class DebloatService(
    private val gate: ShizukuGate,
    private val privileged: PrivilegedPackages,
    private val history: HistoryStore,
    private val clock: () -> Long,
) {

    data class Report(
        val attempted: Int,
        val succeeded: Int,
        val stoppedEarly: Boolean,
        val firstFailure: String?,
    )

    sealed interface Refusal {
        data object NotReady : Refusal
        data object WrongUser : Refusal
        data class Guarded(val pkg: String, val reason: String) : Refusal
    }

    /** Why this batch cannot run at all, or null if it can. */
    fun preflight(rows: List<AppRow>): Refusal? {
        if (gate.state.value != ShizukuState.READY) return Refusal.NotReady
        if (gate.runningOutsideUserZero) return Refusal.WrongUser
        rows.firstOrNull { !it.actionable }?.let {
            return Refusal.Guarded(it.pkg, it.blockReason ?: "This one is not offered.")
        }
        return null
    }

    /**
     * Runs [action] over [rows]. [rescan] is called to re-resolve the guards for each
     * package immediately before acting on it.
     */
    suspend fun run(
        title: String,
        rows: List<AppRow>,
        action: Action,
        rescan: (String) -> AppRow?,
        onProgress: (done: Int, total: Int, current: String) -> Unit = { _, _, _ -> },
    ): Report {
        val batchId = "b${clock()}"
        val userId = gate.targetUserId

        history.stampDevice(clock())
        history.openBatch(batchId, clock(), title)

        var succeeded = 0
        var firstFailure: String? = null

        for ((index, row) in rows.withIndex()) {
            onProgress(index, rows.size, row.displayName)

            // Re-check at the moment of acting, not at the moment of choosing.
            val fresh = rescan(row.pkg) ?: row
            val permitted = when (action) {
                Action.REMOVE, Action.DISABLE -> fresh.actionable
                Action.RESTORE, Action.ENABLE -> Protection.refusalReason(fresh.pkg) == null
            }
            if (!permitted) {
                firstFailure = "${fresh.displayName} is no longer safe to change: " +
                    (fresh.blockReason ?: "it is in use.")
                history.record(
                    batchId,
                    HistoryItem(fresh.pkg, fresh.displayName, action.name, false, firstFailure),
                )
                break
            }

            val outcome = when (action) {
                Action.REMOVE -> privileged.remove(fresh.pkg, userId, fresh.isSystem)
                Action.DISABLE -> privileged.setEnabled(fresh.pkg, userId, enabled = false)
                Action.RESTORE -> privileged.restore(fresh.pkg, userId)
                Action.ENABLE -> privileged.setEnabled(fresh.pkg, userId, enabled = true)
            }

            when (outcome) {
                is PrivilegedPackages.Outcome.Done -> {
                    succeeded++
                    history.record(
                        batchId,
                        HistoryItem(fresh.pkg, fresh.displayName, action.name, true, null),
                    )
                }
                is PrivilegedPackages.Outcome.Failed -> {
                    firstFailure = "${fresh.displayName}: ${outcome.reason}"
                    Log.w(TAG, "stopping batch at ${fresh.pkg}: ${outcome.reason}")
                    history.record(
                        batchId,
                        HistoryItem(
                            fresh.pkg, fresh.displayName, action.name, false, outcome.reason,
                        ),
                    )
                    break
                }
            }
        }

        onProgress(rows.size, rows.size, "")
        history.closeBatch(batchId)

        return Report(
            attempted = rows.size,
            succeeded = succeeded,
            stoppedEarly = firstFailure != null,
            firstFailure = firstFailure,
        )
    }

    private companion object {
        const val TAG = "DebloatService"
    }
}
