package uvg.edu.gt.smartfridge.viewModels

import android.content.Context
import android.content.SharedPreferences

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
}