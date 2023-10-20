package uvg.edu.gt.smartfridge.models

import kotlinx.serialization.Serializable

@Serializable
class Settings (
    settingsID : String,
    userID : String,
    theme : String
)