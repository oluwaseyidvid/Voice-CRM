package com.neuralic.voicecrm.network

import android.content.Context
import android.util.Log

object ApiPlaceholders {
    // NOTE: replace with real implementations. Keys will be injected via GitHub Actions / BuildConfig or env vars.
    suspend fun analyzeTextWithOpenAI(text: String): String {
        // TODO: call OpenAI and return suggested draft text
        return "Draft: $text"
    }

    suspend fun createPipedriveActivity(summary: String) {
        // TODO: call Pipedrive to create activity
        Log.d("ApiPlaceholders", "createPipedriveActivity -> $summary")
    }

    suspend fun searchWeb(query: String): String {
        // TODO: call SerpAPI / Apollo with fallback to Google
        return "Search results for: $query"
    }
}
