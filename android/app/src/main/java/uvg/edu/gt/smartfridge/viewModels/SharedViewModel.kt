package uvg.edu.gt.smartfridge.viewModels

import androidx.lifecycle.ViewModel
import uvg.edu.gt.smartfridge.models.UserSettings

class SharedViewModel : ViewModel() {
    var jwtToken: String = ""
    var preferences : UserSettings = UserSettings("", "", "light")
    var useDarkTheme: Boolean = false
}