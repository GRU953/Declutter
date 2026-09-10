// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Aninda Sundar Howlader (GRU953)

package dev.gru953.declutter.catalogue

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * How much you stand to lose by removing something.
 *
 * The four tiers follow the vocabulary the Android debloating community settled on, because
 * a user who reads about this elsewhere should meet the same words here. The fifth,
 * [UNKNOWN], is the honest answer for a package the catalogue has never seen -- and it is
 * the default, not a fallback, because "we have not checked this one" is information.
 */
@Serializable
enum class Risk {
    /** Removing it loses that feature and nothing else. */
    @SerialName("safe")
    SAFE,

    /** Removing it loses a feature some people genuinely rely on. */
    @SerialName("advanced")
    ADVANCED,

    /** Removing it can break something with no visible connection to it. */
    @SerialName("expert")
    EXPERT,

    /** Never remove. Refused by the app, not merely discouraged. */
    @SerialName("unsafe")
    UNSAFE,

    /** No catalogue entry. Not offered, and never described as safe. */
    @SerialName("unknown")
    UNKNOWN,
    ;

    val label: String
        get() = when (this) {
            SAFE -> "Safe"
            ADVANCED -> "Advanced"
            EXPERT -> "Expert"
            UNSAFE -> "Never remove"
            UNKNOWN -> "Unchecked"
        }
}

/** What the catalogue advises doing with a package. */
@Serializable
enum class Advice {
    @SerialName("uninstall")
    UNINSTALL,

    @SerialName("disable")
    DISABLE,

    @SerialName("never")
    NEVER,
}

/** Who put the package on the phone. Lets the user think in families rather than in strings. */
@Serializable
enum class Vendor {
    @SerialName("motorola")
    MOTOROLA,

    @SerialName("lenovo")
    LENOVO,

    @SerialName("google")
    GOOGLE,

    @SerialName("android")
    ANDROID,

    @SerialName("qualcomm")
    QUALCOMM,

    @SerialName("carrier")
    CARRIER,

    @SerialName("preinstalled")
    PREINSTALLED,

    @SerialName("unknown")
    UNKNOWN,
    ;

    val label: String
        get() = when (this) {
            MOTOROLA -> "Motorola"
            LENOVO -> "Lenovo"
            GOOGLE -> "Google"
            ANDROID -> "Android system"
            QUALCOMM -> "Qualcomm"
            CARRIER -> "Network operator"
            PREINSTALLED -> "Preinstalled app"
            UNKNOWN -> "Unknown"
        }
}

/** How sure the catalogue is about an entry. Shown to the user, not hidden in a comment. */
@Serializable
enum class Confidence {
    @SerialName("high")
    HIGH,

    @SerialName("medium")
    MEDIUM,

    @SerialName("low")
    LOW,
}

/**
 * One catalogue entry. Every field except [pkg] exists to answer a question the user would
 * otherwise have to search the web for, so nothing here is decoration.
 */
@Serializable
data class CatalogueEntry(
    @SerialName("pkg") val pkg: String,
    @SerialName("label") val label: String,
    @SerialName("vendor") val vendor: Vendor = Vendor.UNKNOWN,
    /** What the package is for, in one or two plain sentences. */
    @SerialName("what") val what: String,
    @SerialName("risk") val risk: Risk = Risk.UNKNOWN,
    @SerialName("advice") val advice: Advice = Advice.NEVER,
    /** What the user actually loses. Shown before the action, never in a help page. */
    @SerialName("loses") val loses: String,
    @SerialName("confidence") val confidence: Confidence = Confidence.MEDIUM,
    /** Other things on the phone that stop working properly without it. */
    @SerialName("breaks") val breaks: List<String> = emptyList(),
)

/** The bundled catalogue, as read from `assets/catalogue.json`. */
@Serializable
data class CatalogueFile(
    @SerialName("version") val version: Int,
    /** The date the ratings were last reviewed, ISO-8601. Shown in the app. */
    @SerialName("reviewed") val reviewed: String,
    @SerialName("sources") val sources: List<String> = emptyList(),
    @SerialName("entries") val entries: List<CatalogueEntry>,
)
