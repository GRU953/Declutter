// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Aninda Sundar Howlader (GRU953)

package dev.gru953.declutter

import android.app.Application
import android.content.Context
import android.util.Log
import org.lsposed.hiddenapibypass.HiddenApiBypass

class DeclutterApp : Application() {

    /**
     * Since Android 9, apps are blocked from reflecting onto the framework's internal
     * interfaces. The calls this app needs -- the same ones the platform's own `pm` command
     * makes -- live behind that block, so the exemption is added here, before any of them
     * is touched, and only for `android.content.pm`. No version guard is needed: the app's
     * minimum is Android 9.
     */
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        runCatching {
            HiddenApiBypass.addHiddenApiExemptions("Landroid/content/pm")
        }.onFailure { Log.w(TAG, "could not lift the non-SDK interface block", it) }
    }

    private companion object {
        const val TAG = "DeclutterApp"
    }
}
