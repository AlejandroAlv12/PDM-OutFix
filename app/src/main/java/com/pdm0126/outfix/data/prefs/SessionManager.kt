package com.pdm0126.outfix.data.prefs

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "OutfixSession"
        private const val KEY_USER_TOKEN = "user_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_DISPLAY_NAME = "user_display_name"
    }

    fun saveAuthToken(token: String) {
        prefs.edit().putString(KEY_USER_TOKEN, token).apply()
    }

    fun fetchAuthToken(): String? {
        return prefs.getString(KEY_USER_TOKEN, null)
    }

    fun saveUserId(id: String) {
        prefs.edit().putString(KEY_USER_ID, id).apply()
    }

    fun fetchUserId(): String? {
        return prefs.getString(KEY_USER_ID, null)
    }

    fun saveUserEmail(email: String) {
        prefs.edit().putString(KEY_USER_EMAIL, email).apply()
    }

    fun fetchUserEmail(): String? {
        return prefs.getString(KEY_USER_EMAIL, null)
    }

    fun saveUserDisplayName(name: String) {
        prefs.edit().putString(KEY_USER_DISPLAY_NAME, name).apply()
    }

    fun fetchUserDisplayName(): String? {
        return prefs.getString(KEY_USER_DISPLAY_NAME, null)
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
