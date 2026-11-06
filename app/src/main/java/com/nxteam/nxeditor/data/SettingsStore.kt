package com.nxteam.nxeditor.data

import android.content.Context
import android.content.SharedPreferences

class SettingsStore(context: Context) {
    private val preferences: SharedPreferences =
        context.getSharedPreferences("nx_editor_settings", Context.MODE_PRIVATE)

    var apiKey: String
        get() = preferences.getString(KEY_API_KEY, "") ?: ""
        set(value) {
            preferences.edit().putString(KEY_API_KEY, value).apply()
        }

    var model: String
        get() = preferences.getString(KEY_MODEL, DEFAULT_MODEL) ?: DEFAULT_MODEL
        set(value) {
            preferences.edit().putString(KEY_MODEL, value).apply()
        }

    var baseUrl: String
        get() = preferences.getString(KEY_BASE_URL, DEFAULT_BASE_URL) ?: DEFAULT_BASE_URL
        set(value) {
            preferences.edit().putString(KEY_BASE_URL, value).apply()
        }

    var systemPrompt: String
        get() = preferences.getString(KEY_SYSTEM_PROMPT, DEFAULT_SYSTEM_PROMPT) ?: DEFAULT_SYSTEM_PROMPT
        set(value) {
            preferences.edit().putString(KEY_SYSTEM_PROMPT, value).apply()
        }

    companion object {
        private const val KEY_API_KEY = "api_key"
        private const val KEY_MODEL = "model"
        private const val KEY_BASE_URL = "base_url"
        private const val KEY_SYSTEM_PROMPT = "system_prompt"
        private const val DEFAULT_MODEL = "llama-3.3-70b-versatile"
        private const val DEFAULT_BASE_URL = "https://api.groq.com/openai/v1"
        private const val DEFAULT_SYSTEM_PROMPT = "You are a helpful assistant."
    }
}
