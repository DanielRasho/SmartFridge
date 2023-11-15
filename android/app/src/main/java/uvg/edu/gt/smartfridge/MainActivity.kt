package uvg.edu.gt.smartfridge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import uvg.edu.gt.smartfridge.ui.theme.smartFridgeTheme
import uvg.edu.gt.smartfridge.viewModels.SharedViewModel
import uvg.edu.gt.smartfridge.views.FridgeView
import uvg.edu.gt.smartfridge.views.HomeView
import uvg.edu.gt.smartfridge.views.LoginView
import uvg.edu.gt.smartfridge.views.NewIngredientView
import uvg.edu.gt.smartfridge.views.PrincipalView
import uvg.edu.gt.smartfridge.views.RecipeView
import uvg.edu.gt.smartfridge.views.RegisterView
import uvg.edu.gt.smartfridge.views.SettingsView

class MainActivity : ComponentActivity() {
    @ExperimentalMaterial3Api
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedViewModel = ViewModelProvider(this).get(SharedViewModel::class.java)

        setContent {

            MainComponent(sharedViewModel)
        }
    }
}

@ExperimentalMaterial3Api
@Composable
fun MainComponent(
    sharedViewModel: SharedViewModel,
    navController: NavHostController = rememberNavController()
) {
    val systemInDark = isSystemInDarkTheme()
    val (useDarkTheme, setUseDarkTheme) = remember { mutableStateOf(systemInDark) }
    smartFridgeTheme(
        useDarkTheme = useDarkTheme
    ) {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            NavHost(navController = navController, startDestination = "Principal") {
                composable("Principal") { PrincipalView(navController) }
                composable("Login") { LoginView(sharedViewModel, navController) }
                composable("Register") { RegisterView(navController) }
                composable("Home") { HomeView(sharedViewModel, navController) }
                composable("Settings") { SettingsView(useDarkTheme, setUseDarkTheme, navController) }
                composable("Fridge") { FridgeView(sharedViewModel, navController) }
                composable("Recipe") { RecipeView(navController) }
                composable("NewIngredient") { NewIngredientView(sharedViewModel, navController) }
            }
        }
    }

}

@Preview(showBackground = true)
@ExperimentalMaterial3Api
@Composable
fun GreetingPreview() {
    val sharedViewModel =
        SharedViewModel() // Create a new instance of SharedViewModel for the preview

    smartFridgeTheme {
        MainComponent(sharedViewModel)
    }
}