// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Aninda Sundar Howlader (GRU953)

package dev.gru953.declutter.shizuku

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.content.pm.VersionedPackage
import android.os.Build
import android.os.IBinder
import android.os.IInterface
import android.util.Log
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import rikka.shizuku.ShizukuBinderWrapper
import rikka.shizuku.SystemServiceHelper
import java.lang.reflect.Method

/**
 * The privileged half of the app: the calls that need Shizuku.
 *
 * These are the same framework calls the platform's own `pm` shell command makes. They are
 * reached by reflection because they are non-SDK interfaces; `HiddenApiBypass` lifts the
 * runtime restriction, and each lookup is done by exact signature so a mismatch fails
 * loudly here rather than silently later.
 *
 * Written from the AOSP AIDL definitions rather than adapted from any existing app, so the
 * only third-party code this links against is Shizuku's MIT-licensed API and the
 * Apache-2.0 HiddenApiBypass.
 */
class PrivilegedPackages(private val context: Context) {

    /** The reversible default: mark a preinstalled app uninstalled for this user only. */
    private val deleteFlagsForSystemApp =
        DELETE_SYSTEM_APP or DELETE_KEEP_DATA

    /**
     * Remove [pkg] for [userId] only. The factory copy stays on the read-only system
     * partition, so [restore] can bring it back.
     *
     * `DELETE_SYSTEM_APP` is the flag that makes this possible and is the one most easily
     * missed: without it AOSP refuses any system app that has no update installed over it,
     * and the call fails with a bare internal error.
     *
     * The result is *not* taken from the call returning without throwing -- that is a known
     * source of "it said it worked but it didn't". The real answer arrives asynchronously,
     * and is then checked against what the phone actually reports.
     */
    suspend fun remove(pkg: String, userId: Int, isSystemApp: Boolean): Outcome {
        val flags = if (isSystemApp) deleteFlagsForSystemApp else DELETE_KEEP_DATA
        return try {
            val installer = packageInstaller()
            val method = installer.javaClass.getMethod(
                "uninstall",
                VersionedPackage::class.java,
                String::class.java,
                Int::class.javaPrimitiveType,
                IntentSender::class.java,
                Int::class.javaPrimitiveType,
            )
            val versioned = VersionedPackage(pkg, PackageManager.VERSION_CODE_HIGHEST)
            val awaiter = ResultAwaiter(context)
            awaiter.use {
                method.invoke(
                    installer,
                    versioned,
                    SHELL_PACKAGE,
                    flags,
                    it.intentSender,
                    userId,
                )
                val reported = it.await()
                verify(pkg, expectInstalled = false, reported = reported)
            }
        } catch (e: Exception) {
            Outcome.Failed(explain(e))
        }
    }

    /**
     * Put a removed preinstalled app back, which is what `pm install-existing` does.
     * Synchronous, so there is no broadcast to wait for.
     */
    suspend fun restore(pkg: String, userId: Int): Outcome = try {
        val pm = packageManager()
        val result = invokeFirstMatching(
            target = pm,
            name = "installExistingPackageAsUser",
            candidates = listOf(
                // Android 11+ (API 30): whitelisted permissions were added last.
                Signature(
                    types = arrayOf(
                        String::class.java,
                        Int::class.javaPrimitiveType!!,
                        Int::class.javaPrimitiveType!!,
                        Int::class.javaPrimitiveType!!,
                        List::class.java,
                    ),
                    args = arrayOf(
                        pkg,
                        userId,
                        INSTALL_ALL_WHITELIST_RESTRICTED_PERMISSIONS,
                        INSTALL_REASON_UNKNOWN,
                        null,
                    ),
                ),
                Signature(
                    types = arrayOf(
                        String::class.java,
                        Int::class.javaPrimitiveType!!,
                        Int::class.javaPrimitiveType!!,
                        Int::class.javaPrimitiveType!!,
                    ),
                    args = arrayOf(
                        pkg,
                        userId,
                        INSTALL_ALL_WHITELIST_RESTRICTED_PERMISSIONS,
                        INSTALL_REASON_UNKNOWN,
                    ),
                ),
                Signature(
                    types = arrayOf(String::class.java, Int::class.javaPrimitiveType!!),
                    args = arrayOf(pkg, userId),
                ),
            ),
        )
        val code = (result as? Int) ?: INSTALL_SUCCEEDED
        val reported = if (code == INSTALL_SUCCEEDED) {
            Reported.Success
        } else {
            Reported.Failure("the system returned install code $code")
        }
        verify(pkg, expectInstalled = true, reported = reported)
    } catch (e: NoSuchMethodException) {
        // This version of Android has no installExistingPackageAsUser shape we know.
        // Undo is the one thing that must not be lost, so try the installer's own route.
        restoreViaInstaller(pkg, userId)
    } catch (e: Exception) {
        Outcome.Failed(explain(e))
    }

    /**
     * The second way back: `IPackageInstaller.installExistingPackage`, which reports
     * asynchronously like the uninstall does.
     */
    private suspend fun restoreViaInstaller(pkg: String, userId: Int): Outcome = try {
        val installer = packageInstaller()
        val method = installer.javaClass.getMethod(
            "installExistingPackage",
            String::class.java,
            Int::class.javaPrimitiveType,
            Int::class.javaPrimitiveType,
            IntentSender::class.java,
            Int::class.javaPrimitiveType,
            List::class.java,
        )
        val awaiter = ResultAwaiter(context)
        awaiter.use {
            method.invoke(
                installer,
                pkg,
                INSTALL_ALL_WHITELIST_RESTRICTED_PERMISSIONS,
                INSTALL_REASON_UNKNOWN,
                it.intentSender,
                userId,
                null,
            )
            verify(pkg, expectInstalled = true, reported = it.await())
        }
    } catch (e: Exception) {
        Outcome.Failed(explain(e))
    }

    /**
     * Turn [pkg] off for [userId], the equivalent of `pm disable-user`.
     *
     * Only `DISABLED_USER` is permitted to a shell-privileged caller for a whole package;
     * the fully-disabled state is refused outright, so it is not offered.
     */
    suspend fun setEnabled(pkg: String, userId: Int, enabled: Boolean): Outcome = try {
        val pm = packageManager()
        val newState = if (enabled) {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        } else {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER
        }
        val method = pm.javaClass.getMethod(
            "setApplicationEnabledSetting",
            String::class.java,
            Int::class.javaPrimitiveType,
            Int::class.javaPrimitiveType,
            Int::class.javaPrimitiveType,
            String::class.java,
        )
        method.invoke(pm, pkg, newState, 0, userId, SHELL_PACKAGE)
        Outcome.Done
    } catch (e: Exception) {
        Outcome.Failed(explain(e))
    }

    /**
     * Ask the phone what actually happened, rather than trusting the call.
     * A reported success that the phone contradicts is reported as a failure.
     */
    private fun verify(pkg: String, expectInstalled: Boolean, reported: Reported): Outcome {
        val installed = runCatching {
            val info = context.packageManager.getApplicationInfo(
                pkg,
                PackageManager.MATCH_UNINSTALLED_PACKAGES,
            )
            info.flags and FLAG_INSTALLED != 0
        }.getOrDefault(!expectInstalled)

        return when {
            installed == expectInstalled -> Outcome.Done
            reported is Reported.Failure -> Outcome.Failed(reported.reason)
            reported is Reported.TimedOut -> Outcome.Failed(
                "the phone did not answer in time, and nothing changed",
            )
            else -> Outcome.Failed("the phone reported success but nothing changed")
        }
    }

    // -- plumbing ----------------------------------------------------------------------

    // Lint objects to reflecting onto framework internals, and it is right to in general.
    // Here it is the entire point: these are the calls the platform's own `pm` command
    // makes, there is no public equivalent, and each lookup is by exact signature so a
    // change in a future Android version fails loudly instead of silently.
    @Suppress("PrivateApi")
    private fun packageManager(): Any {
        val binder = ShizukuBinderWrapper(SystemServiceHelper.getSystemService("package"))
        val stub = Class.forName("android.content.pm.IPackageManager\$Stub")
        return stub.getMethod("asInterface", IBinder::class.java).invoke(null, binder)
            ?: error("the system did not hand back a package manager")
    }

    @Suppress("PrivateApi")
    private fun packageInstaller(): Any {
        val pm = packageManager()
        val raw = pm.javaClass.getMethod("getPackageInstaller").invoke(pm)
            ?: error("the system did not hand back a package installer")
        val wrapped = ShizukuBinderWrapper((raw as IInterface).asBinder())
        val stub = Class.forName("android.content.pm.IPackageInstaller\$Stub")
        return stub.getMethod("asInterface", IBinder::class.java).invoke(null, wrapped)
            ?: error("the system did not hand back a package installer")
    }

    private class Signature(val types: Array<Class<*>>, val args: Array<Any?>)

    /**
     * Framework method signatures drift between Android versions. Rather than branch on
     * `SDK_INT` and hope, try the known shapes newest-first and use whichever exists.
     */
    private fun invokeFirstMatching(
        target: Any,
        name: String,
        candidates: List<Signature>,
    ): Any? {
        var lastError: Throwable? = null
        for (candidate in candidates) {
            val method: Method = try {
                target.javaClass.getMethod(name, *candidate.types)
            } catch (e: NoSuchMethodException) {
                lastError = e
                continue
            }
            return method.invoke(target, *candidate.args)
        }
        throw lastError ?: NoSuchMethodException(
            "this version of Android has no $name this app recognises",
        )
    }

    /** Turns an exception into something worth showing a person. */
    private fun explain(e: Throwable): String {
        val cause = e.cause ?: e
        Log.w(TAG, "privileged call failed", cause)
        val message = cause.message.orEmpty()
        return when {
            cause is SecurityException && message.contains("non-disable", true) ->
                "The phone refuses to turn this one off -- Motorola marks it as " +
                    "‘must stay on’. Nothing was changed."
            cause is SecurityException ->
                "The phone refused the change. Shizuku may have stopped; try starting it again."
            cause is IllegalStateException && message.contains("binder", true) ->
                "Shizuku is not running. Start it again and retry."
            cause is TimeoutCancellationException ->
                "The phone did not answer in time. Nothing was changed."
            message.isBlank() -> "The change failed, and Android gave no reason."
            else -> "The change failed: $message"
        }
    }

    /** What the app did, in a form the interface can show without interpreting it. */
    sealed interface Outcome {
        data object Done : Outcome
        data class Failed(val reason: String) : Outcome
    }

    private sealed interface Reported {
        data object Success : Reported
        data class Failure(val reason: String) : Reported
        data object TimedOut : Reported
    }

    /**
     * Receives the asynchronous result of an uninstall. The framework answers through an
     * `IntentSender`, and a `PendingIntent` is the ordinary, documented way to make one.
     */
    private class ResultAwaiter(private val context: Context) : AutoCloseable {
        private val token = tokenCounter++
        private val action = "$RESULT_ACTION.$token"
        private val answer = CompletableDeferred<Reported>()

        private val receiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                val status = intent?.getIntExtra(
                    PackageInstaller.EXTRA_STATUS,
                    PackageInstaller.STATUS_FAILURE,
                ) ?: PackageInstaller.STATUS_FAILURE
                val message = intent?.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
                answer.complete(
                    if (status == PackageInstaller.STATUS_SUCCESS) {
                        Reported.Success
                    } else {
                        Reported.Failure(message ?: "Android reported status $status")
                    },
                )
            }
        }

        init {
            val filter = android.content.IntentFilter(action)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(
                    receiver,
                    filter,
                    Context.RECEIVER_NOT_EXPORTED,
                )
            } else {
                @Suppress("UnspecifiedRegisterReceiverFlag")
                context.registerReceiver(receiver, filter)
            }
        }

        val intentSender: IntentSender
            get() = android.app.PendingIntent.getBroadcast(
                context,
                token,
                Intent(action).setPackage(context.packageName),
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or
                    android.app.PendingIntent.FLAG_MUTABLE,
            ).intentSender

        suspend fun await(): Reported = try {
            withTimeout(RESULT_TIMEOUT_MS) { answer.await() }
        } catch (_: TimeoutCancellationException) {
            Reported.TimedOut
        }

        override fun close() {
            runCatching { context.unregisterReceiver(receiver) }
        }

        private companion object {
            var tokenCounter = 1
        }
    }

    private companion object {
        const val TAG = "PrivilegedPackages"

        /** `pm` runs as the shell, and the framework only skips its app-ops check for it. */
        const val SHELL_PACKAGE = "com.android.shell"

        const val RESULT_ACTION = "dev.gru953.declutter.UNINSTALL_RESULT"
        const val RESULT_TIMEOUT_MS = 30_000L

        // PackageManager delete flags. Values are fixed platform constants.
        const val DELETE_KEEP_DATA = 0x00000001
        const val DELETE_SYSTEM_APP = 0x00000004

        // Install flags and reasons for the restore path.
        const val INSTALL_ALL_WHITELIST_RESTRICTED_PERMISSIONS = 0x00400000
        const val INSTALL_REASON_UNKNOWN = 0
        const val INSTALL_SUCCEEDED = 1

        /** ApplicationInfo.FLAG_INSTALLED -- hidden, but a fixed value since API 24. */
        const val FLAG_INSTALLED = 1 shl 23
    }
}
