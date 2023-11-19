package uvg.edu.gt.smartfridge.viewModels

import android.content.Context
import android.content.SharedPreferences
import uvg.edu.gt.smartfridge.models.UserSettings

class TokenManager(private val context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("SFapp_prefs", Context.MODE_PRIVATE)

    // Save JWT token to SharedPreferences
    fun saveJwtToken(token: String) {
        val editor = sharedPreferences.edit()
        editor.putString("jwt_token", token)
        editor.apply()
    }

    // Retrieve JWT token from SharedPreferences
    fun getJwtToken(): String? {
        return sharedPreferences.getString("jwt_token", null)
    }

    // Clear JWT token from SharedPreferences (logout)
    fun clearJwtToken() {
        val editor = sharedPreferences.edit()
        editor.remove("jwt_token")
        editor.apply()
    }
    // Save UserSettings to SharedPreferences
    fun saveUserSettings(userSettings: UserSettings) {
        val editor = sharedPreferences.edit()
        editor.putString("SettingsId", userSettings.SettingsId)
        editor.putString("UserId", userSettings.UserId)
        editor.putString("Theme", userSettings.Theme)
        editor.apply()
    }

    // Retrieve UserSettings from SharedPreferences
    fun getUserSettings(): UserSettings {
        val settingsId = sharedPreferences.getString("SettingsId", "") ?: ""
        val userId = sharedPreferences.getString("UserId", "") ?: ""
        val theme = sharedPreferences.getString("Theme", "") ?: ""
        return UserSettings(settingsId, userId,theme)
    }

    // Save theme preference to SharedPreferences
    fun saveThemePreference(isDarkTheme: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("isDarkTheme", isDarkTheme)
        editor.apply()
    }

    // Retrieve theme preference from SharedPreferences
    fun getThemePreference(): Boolean {
        return sharedPreferences.getBoolean("isDarkTheme", false)
    }
}