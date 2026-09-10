// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Aninda Sundar Howlader (GRU953)

package dev.gru953.declutter.catalogue

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/**
 * Reads the bundled catalogue. It ships inside the app, so the list works with no internet
 * connection and cannot be changed underneath the user by a bad update to somebody's file.
 */
class CatalogueLoader(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    suspend fun load(): CatalogueFile = withContext(Dispatchers.IO) {
        runCatching {
            context.assets.open(ASSET).bufferedReader().use { reader ->
                json.decodeFromString<CatalogueFile>(reader.readText())
            }
        }.getOrElse {
            Log.e(TAG, "the bundled catalogue could not be read", it)
            CatalogueFile(version = 0, reviewed = "unknown", entries = emptyList())
        }
    }

    private companion object {
        const val TAG = "CatalogueLoader"
        const val ASSET = "catalogue.json"
    }
}
