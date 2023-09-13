package uvg.edu.gt.smartfridge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import uvg.edu.gt.smartfridge.ui.theme.smartFridgeTheme
import uvg.edu.gt.smartfridge.views.FridgeView
import uvg.edu.gt.smartfridge.views.HomeView
import uvg.edu.gt.smartfridge.views.SettingsView

class MainActivity : ComponentActivity() {
    @ExperimentalMaterial3Api
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            smartFridgeTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainComponent()
                }
            }
        }
    }
}

@ExperimentalMaterial3Api
@Composable
fun MainComponent(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController = navController, startDestination = "Home", modifier = modifier) {
        composable("Home") { HomeView(navController) }
        composable("Settings") { SettingsView() }
        composable("Fridge") { FridgeView() }
    }
}

@Preview(showBackground = true)
@ExperimentalMaterial3Api
@Composable
fun GreetingPreview() {
    smartFridgeTheme {
        MainComponent()
    }
}