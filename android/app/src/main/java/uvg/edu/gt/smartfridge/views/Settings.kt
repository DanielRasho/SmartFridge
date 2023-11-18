package uvg.edu.gt.smartfridge.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExitToApp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import uvg.edu.gt.smartfridge.components.BottomNavBar
import uvg.edu.gt.smartfridge.components.IconPrimaryButton
import uvg.edu.gt.smartfridge.components.NavItem
import uvg.edu.gt.smartfridge.components.Title
import uvg.edu.gt.smartfridge.models.UserSettings
import uvg.edu.gt.smartfridge.ui.theme.smartFridgeTheme
import uvg.edu.gt.smartfridge.viewModels.SettingsViewModel
import uvg.edu.gt.smartfridge.viewModels.SharedViewModel
import uvg.edu.gt.smartfridge.viewModels.TokenManager

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsView(
    sharedViewModel: SharedViewModel,
    useDarkTheme: Boolean,
    setUseDarkTheme: (Boolean) -> Unit,
    navController: NavHostController,
    context: Context,
    startDestination: String,
    modifier: Modifier = Modifier
) {
    val navItems = sequenceOf(
        NavItem.Fridge, NavItem.Home, NavItem.Settings
    )
    val coroutineScope = rememberCoroutineScope()
    val settingsViewModel = viewModel<SettingsViewModel>()
    val context = LocalContext.current


    Scaffold(bottomBar = { BottomNavBar(items = navItems, navController = navController) }) {
        Column(
            modifier = modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(10.dp, 0.dp)
        ) {
            Title("Settings")

            Spacer(modifier = modifier.height(20.dp))
            Text(
                "Style",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyLarge
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = !useDarkTheme, onClick = {
                    setUseDarkTheme(false)
                    println("SETTINGS (before api call) " + sharedViewModel.preferences.SettingsId)
                    coroutineScope.launch(Dispatchers.IO) {
                        settingsViewModel.saveSettings(
                            sharedViewModel.jwtToken,
                            UserSettings(
                                sharedViewModel.preferences.SettingsId,
                                sharedViewModel.preferences.UserId,
                                "Light"
                            )
                        )
                    }
                })
                Text(
                    "Light Theme",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = useDarkTheme,
                    onClick = {
                        setUseDarkTheme(true);

                        coroutineScope.launch(Dispatchers.IO) {
                            settingsViewModel.saveSettings(
                                sharedViewModel.jwtToken,
                                UserSettings(
                                    sharedViewModel.preferences.SettingsId,
                                    sharedViewModel.preferences.UserId,
                                    "Dark"
                                )
                            )
                        }
                        Log.d(
                            "DarkTheme Radio Button",
                            "Clicked!"
                        )
                    })
                Text(
                    "Dark Theme",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = modifier.fillMaxWidth()
            ) {
                IconPrimaryButton(text = "Logout", icon = Icons.Rounded.ExitToApp) {
                    coroutineScope.launch(Dispatchers.IO) {
                        settingsViewModel.logout(sharedViewModel.jwtToken)
                    }
                    val tokenManager = TokenManager(context)
                    tokenManager.clearJwtToken()
                    navController.navigate("Principal") {
                        // Clear the back stack to prevent going back to Login
                        popUpTo(startDestination) {
                            inclusive = true
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun SettingsViewPreview() {
    val sharedViewModel = SharedViewModel()
    smartFridgeTheme {
        SettingsView(
            sharedViewModel,
            false,
            {},
            rememberNavController(),
            context = LocalContext.current,
            "Home"
        )
    }
}