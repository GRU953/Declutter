// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Aninda Sundar Howlader (GRU953)

package dev.gru953.declutter.domain

/**
 * The hard blocklist.
 *
 * Android will not protect the user here. Reading AOSP's own PackageManagerService and
 * ProtectedPackages, the only packages the framework itself refuses to remove are the
 * device/profile owner, the provisioning package, active device admins, in-use static
 * shared libraries, and anything covered by a user restriction. `com.android.systemui`,
 * `com.android.settings` and even `android` are not on that list: a shell-privileged caller
 * can remove them for user 0 and the phone will break. So every safety guarantee this app
 * makes has to come from here.
 *
 * Three deliberate decisions:
 *
 *  1. This list lives in Kotlin source, never in the JSON catalogue and never behind a
 *     network fetch. A bad catalogue update must not be able to unlock a bricking package.
 *  2. There is no override. No long-press, no developer mode, no "I know what I am doing"
 *     toggle. The user this app is written for cannot recover a phone that will not boot,
 *     and the cost of being over-cautious is one package left in place.
 *  3. Patterns matter as much as names. Resource overlays and chipset packages carry
 *     device-specific suffixes that cannot be enumerated in advance, so a name-only list is
 *     always incomplete.
 */
object Protection {

    /** Removing any of these takes away the means to undo anything else this app has done. */
    val ESCAPE_ROUTE: Set<String> = setOf(
        "com.android.shell",
        "moe.shizuku.privileged.api",
        "moe.shizuku.redirect",
        "rikka.sui",
        "com.android.packageinstaller",
        "com.google.android.packageinstaller",
        "com.android.vending",
        "com.android.settings",
        "com.android.systemui",
    )

    /**
     * Never removable. Grouped by the reason, because the reason is what the app shows the
     * user when it declines.
     */
    val NEVER: Map<String, String> = buildMap {
        fun add(reason: String, vararg pkgs: String) = pkgs.forEach { put(it, reason) }

        add(
            "The phone will not start up without it.",
            "android",
            "com.android.systemui",
            "com.android.settings",
            "com.android.providers.settings",
            "com.motorola.android.providers.settings",
            "com.android.providers.contacts",
            "com.android.providers.media",
            "com.android.providers.media.module",
            "com.google.android.providers.media.module",
            "com.android.providers.downloads",
            "com.android.externalstorage",
            "com.android.documentsui",
            "com.android.modulemetadata",
            "com.google.android.modulemetadata",
            "com.google.android.overlay.modules.modulemetadata.forframework",
            "com.android.sdksandbox",
            "com.google.android.sdksandbox",
            "android.ext.services",
            "com.google.android.ext.services",
            "com.android.ext.services",
            "com.google.android.ext.shared",
            "com.motorola.timezonedata",
        )
        add(
            "Nothing could be installed on the phone again, including the repair for whatever went wrong.",
            "com.android.packageinstaller",
            "com.google.android.packageinstaller",
            "com.android.permissioncontroller",
            "com.google.android.permissioncontroller",
        )
        add(
            "The phone could no longer make or receive calls, including emergency calls.",
            "com.android.phone",
            "com.android.server.telecom",
            "com.android.providers.telephony",
            "com.android.mms.service",
            "com.android.ims",
            "com.android.ims.rcsservice",
            "org.codeaurora.ims",
            "com.qualcomm.qti.telephonyservice",
            "com.qualcomm.qcrilmsgtunnel",
            "com.motorola.carrierconfig",
            "com.motorola.msimsettings",
            "com.android.carrierconfig",
            "com.android.imsserviceentitlement",
            "com.qti.primarycardcontroller",
            "com.qualcomm.qti.carrierconfigure",
            "com.qualcomm.qti.extsettings",
            "com.qualcomm.qti.qs",
            "com.qualcomm.qti.tetherservice",
            "com.qualcomm.qti.uceShimService",
        )
        add(
            "The eSIM would stop working, which on an eSIM-only setup means no phone service at all.",
            "com.google.android.euicc",
            "com.android.euicc",
        )
        add(
            "Contactless payments would stop working.",
            "com.motorola.hce",
        )
        add(
            "Choosing a file or a photo would stop working in every app that asks for one.",
            "com.google.android.documentsui",
            "com.google.android.photopicker",
            "com.android.photopicker",
        )
        add(
            "It delivers Google's security configuration updates -- revoked certificates and " +
                "the like -- which arrive silently and matter.",
            "com.google.android.configupdater",
        )
        add(
            "One community list ties it to system updates. Nobody has confirmed what it does " +
                "on this handset, and the upside of removing it is nothing, so it stays.",
            "com.motorola.settings",
        )
        add(
            "The phone would stop receiving government emergency and disaster warnings.",
            "com.android.cellbroadcastreceiver",
            "com.google.android.cellbroadcastreceiver",
            "com.android.cellbroadcastservice",
            "com.google.android.cellbroadcastservice",
            "com.android.emergency",
        )
        add(
            "Wi-Fi and mobile data would stop working.",
            "com.android.networkstack",
            "com.google.android.networkstack",
            "com.android.networkstack.tethering",
            "com.google.android.networkstack.tethering",
            "com.android.connectivity.resources",
            "com.google.android.connectivity.resources",
            "com.android.wifi.resources",
            "com.google.android.wifi.resources",
            "com.android.wifi.dialog",
            "com.android.captiveportallogin",
            "com.google.android.captiveportallogin",
            "com.android.bluetooth",
            "com.android.nfc",
            "com.android.certinstaller",
            "com.android.keychain",
        )
        add(
            "Every app that shows a web page inside itself would crash, and no replacement can be installed without a computer.",
            "com.android.webview",
            "com.google.android.webview",
            "com.google.android.webview.dev",
        )
        add(
            "The phone would stop receiving security updates, with no warning at the time.",
            "com.motorola.ccc.ota",
            "com.motorola.android.fota",
            "com.motorola.installer",
            "com.motorola.setup",
            "com.google.android.setupwizard",
            "com.android.provision",
            "com.google.android.gms",
            "com.google.android.gsf",
            "com.android.vending",
        )
        add(
            "It protects the hardware. Without it the phone would not throttle or shut down when it overheats, or would not limit radio output near your body.",
            "com.motorola.thermalservice",
            "com.sar.main",
        )
        add(
            "Sound would break, and on some Motorola phones this causes a start-up loop.",
            "com.motorola.audiofx",
            "com.dolby.daxservice",
        )
        add(
            "The screen would go black or garbled -- these are the actual graphics drivers.",
            "com.qualcomm.qti.gpudrivers",
        )
        add(
            "Recent apps would stop working even after installing a different launcher. Change the default launcher instead.",
            "com.motorola.launcher3",
            "com.android.launcher3",
        )
        add(
            "It is part of a device instalment plan. Interfering with it can lock the phone or breach the finance agreement.",
            "com.motorola.paks",
            "com.motorola.paks.notification",
            "com.payjoy.access",
        )
        add(
            "It is a system module, not an ordinary app, and cannot be removed safely.",
            "com.motorola.motosecure",
            "com.android.art",
            "com.android.adbd",
            "com.android.conscrypt",
            "com.android.i18n",
            "com.android.media",
            "com.android.media.swcodec",
            "com.android.mediaprovider",
            "com.android.os.statsd",
            "com.android.permission",
            "com.android.resolv",
            "com.android.runtime",
            "com.android.scheduling",
            "com.android.sdkext",
            "com.android.tethering",
            "com.android.tzdata",
            "com.android.wifi",
        )
        add(
            "It carries the phone's own hardware description, and removing it is a leading cause of start-up loops.",
            "com.motorola.android.overlay.common",
            "com.motorola.android.overlay.wfd",
            "com.motorola.android.connectivity.resources.overlay",
        )
        add(
            "Taking it away would leave this app unable to put anything back.",
            *ESCAPE_ROUTE.toTypedArray(),
        )
        add(
            "It can hard-brick a Lenovo or Motorola device.",
            "com.lenovo.ue.device",
        )
        add(
            "This app cannot remove itself.",
            "dev.gru953.declutter",
            "dev.gru953.declutter.debug",
        )
    }

    /**
     * Name patterns that are blocked whatever the catalogue says. Each carries the plain
     * reason the app gives when it declines, so a refusal is never a bare "no".
     */
    private val PATTERNS: List<Pair<Regex, String>> = listOf(
        Regex("""(^|\.)overlay(\.|$)""") to
            "It is a configuration overlay -- part of the phone's own hardware description.",
        Regex("""\.auto_generated_rro_(product|vendor|system)__""") to
            "It is a configuration overlay generated for this exact handset.",
        Regex("""\.res(ources)?\.overlay""") to
            "It is a configuration overlay for the phone's resources.",
        Regex("""^com\.android\.internal\.display\.cutout\.emulation\.""") to
            "It describes the shape of the screen. Removing it causes a start-up loop.",
        Regex("""trichromelibrary""") to
            "It is the shared code the browser and every in-app web page load.",
        Regex("""^com\.qualcomm\.qti\.gpudrivers\.""") to
            "These are the graphics drivers.",
        Regex("""\.gamedriver$""") to "It is a graphics driver.",
        Regex("""\.vklayer\.""") to "It is part of the graphics stack.",
        Regex("""^com\.android\.wifi\.resources""") to "Wi-Fi would stop working.",
        Regex("""networkstack""") to "Wi-Fi and mobile data would stop working.",
        Regex("""cellbroadcast""") to
            "The phone would stop receiving government emergency warnings.",
        Regex("""modulemetadata""") to
            "It is part of the system update mechanism and removing it causes a start-up loop.",
        Regex("""permissioncontroller""") to
            "App permissions could no longer be granted.",
        Regex("""packageinstaller""") to "Nothing could be installed again.",
        Regex("""providers\.settings""") to "The phone stores its settings there.",
        Regex("""providers\.telephony""") to
            "All text messages and mobile data settings live there.",
        Regex("""server\.telecom""") to "The phone could no longer make calls.",
        Regex("""webview""") to
            "Every app that shows a web page inside itself would crash.",
        Regex("""^com\.motorola\.hardware\.""") to
            "It is not an app -- it is a hardware feature marker.",
        Regex("""^com\.motorola\.software\.""") to
            "It is not an app -- it is a software feature marker.",
        Regex("""^com\.motorola\.permission\.""") to
            "It is not an app -- it is a permission name.",
    )

    /**
     * Why the app refuses to touch [pkg], or `null` if it does not refuse.
     * Exact names are checked first so a specific reason beats a generic pattern.
     */
    fun refusalReason(pkg: String): String? {
        NEVER[pkg]?.let { return it }
        val lower = pkg.lowercase()
        for ((pattern, reason) in PATTERNS) {
            if (pattern.containsMatchIn(lower)) return reason
        }
        return null
    }

    fun isProtected(pkg: String): Boolean = refusalReason(pkg) != null
}
