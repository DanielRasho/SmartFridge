package uvg.edu.gt.smartfridge.models

import kotlinx.serialization.Serializable

@Serializable
data class UserSettings (
    val SettingsId : String,
    val UserId : String,
    val Theme : String
)