package com.pdm0126.outfix.data.prefs

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    companion object {
        private const val PREFS_NAME = "outfix_settings"
        private const val KEY_LIQUID_BLUR = "liquid_glass_blur"
        private const val DEFAULT_BLUR = 12f
    }

    private val prefs by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val _liquidBlur = MutableStateFlow(
        prefs.getFloat(KEY_LIQUID_BLUR, DEFAULT_BLUR)
    )
    val liquidBlur: StateFlow<Float> = _liquidBlur.asStateFlow()

    fun setLiquidBlur(value: Float) {
        _liquidBlur.value = value
        prefs.edit().putFloat(KEY_LIQUID_BLUR, value).apply()
    }
}
