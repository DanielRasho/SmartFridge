package uvg.edu.gt.smartfridge.models

import kotlinx.serialization.Serializable

@Serializable
class UserSettings (
    settingsID : String,
    userID : String,
    theme : String
)