// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Aninda Sundar Howlader (GRU953)

package dev.gru953.declutter.shizuku

import android.content.pm.PackageManager
import android.os.Process
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import rikka.shizuku.Shizuku

/** Where the app stands with Shizuku right now. */
enum class ShizukuState {
    /** The Shizuku app is not installed. */
    NOT_INSTALLED,

    /** Installed, but its service is not running -- which is the state after every reboot. */
    NOT_RUNNING,

    /** Running, but the user has not tapped Allow yet. */
    NEEDS_PERMISSION,

    /** The user declined, and chose not to be asked again. */
    DENIED,

    /** Ready. */
    READY,
}

/**
 * The gate in front of every privileged call.
 *
 * Two facts drive the whole design. First, Shizuku's service dies on every reboot -- the
 * official manual says the setup steps must be repeated each time -- so "not running" is a
 * normal, frequent state and not an error. Second, every Shizuku call except `pingBinder`
 * throws if the binder has not arrived yet, so nothing may be attempted before the gate
 * says READY.
 */
class ShizukuGate {

    private val _state = MutableStateFlow(ShizukuState.NOT_RUNNING)
    val state: StateFlow<ShizukuState> = _state.asStateFlow()

    private val binderReceived = Shizuku.OnBinderReceivedListener { refresh() }
    private val binderDead = Shizuku.OnBinderDeadListener { refresh() }
    private val permissionResult =
        Shizuku.OnRequestPermissionResultListener { _, _ -> refresh() }

    fun start() {
        // Sticky, so an already-delivered binder is not missed.
        Shizuku.addBinderReceivedListenerSticky(binderReceived)
        Shizuku.addBinderDeadListener(binderDead)
        Shizuku.addRequestPermissionResultListener(permissionResult)
        refresh()
    }

    fun stop() {
        Shizuku.removeBinderReceivedListener(binderReceived)
        Shizuku.removeBinderDeadListener(binderDead)
        Shizuku.removeRequestPermissionResultListener(permissionResult)
    }

    fun refresh() {
        _state.value = evaluate()
    }

    private fun evaluate(): ShizukuState {
        val alive = runCatching { Shizuku.pingBinder() }.getOrDefault(false)
        if (!alive) return ShizukuState.NOT_RUNNING
        return runCatching {
            when {
                Shizuku.isPreV11() -> ShizukuState.NOT_RUNNING
                Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED ->
                    ShizukuState.READY
                Shizuku.shouldShowRequestPermissionRationale() -> ShizukuState.DENIED
                else -> ShizukuState.NEEDS_PERMISSION
            }
        }.getOrElse {
            Log.w(TAG, "could not read Shizuku permission state", it)
            ShizukuState.NOT_RUNNING
        }
    }

    fun requestPermission() {
        runCatching { Shizuku.requestPermission(REQUEST_CODE) }
            .onFailure { Log.w(TAG, "permission request failed", it) }
    }

    /**
     * Which account Shizuku is acting as. 2000 is the ADB shell, which is what a phone
     * without root gives us; 0 would mean it is running as root.
     */
    val backend: Backend
        get() = runCatching {
            when (Shizuku.getUid()) {
                0 -> Backend.ROOT
                SHELL_UID -> Backend.SHELL
                else -> Backend.OTHER
            }
        }.getOrDefault(Backend.UNKNOWN)

    /**
     * The user this app is allowed to change. Under the ADB shell that is always user 0.
     * Acting on any other user is refused rather than guessed at, because a work profile is
     * managed by somebody else's policy.
     */
    val targetUserId: Int get() = 0

    /** True when the app is running as a secondary user or in a work profile. */
    val runningOutsideUserZero: Boolean
        get() = Process.myUserHandle().hashCode() != 0

    enum class Backend { SHELL, ROOT, OTHER, UNKNOWN }

    companion object {
        private const val TAG = "ShizukuGate"
        private const val REQUEST_CODE = 5391
        private const val SHELL_UID = 2000
    }
}
