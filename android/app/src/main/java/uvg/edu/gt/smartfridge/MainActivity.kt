package uvg.edu.gt.smartfridge

import android.content.Context
import android.os.Bundle
import android.util.Log
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.serialization.json.Json
import uvg.edu.gt.smartfridge.ui.theme.smartFridgeTheme
import uvg.edu.gt.smartfridge.viewModels.SharedViewModel
import uvg.edu.gt.smartfridge.viewModels.TokenManager
import uvg.edu.gt.smartfridge.views.EditIngredientView
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

            MainComponent(sharedViewModel, context = LocalContext.current)
        }
    }
}

@ExperimentalMaterial3Api
@Composable
fun MainComponent(
    sharedViewModel: SharedViewModel,
    navController: NavHostController = rememberNavController(),
    context: Context
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
            val tokenManager = TokenManager(LocalContext.current)
            val jwtToken = tokenManager.getJwtToken()

            // Set the start destination based on the presence of the JWT token
            val startDestination = if (jwtToken != null) "Home" else "Principal"
            if (jwtToken != null) {
                sharedViewModel.jwtToken = jwtToken
            }
            NavHost(navController = navController, startDestination = startDestination) {
                composable("Principal") { PrincipalView(navController) }
                composable("Login") { LoginView(sharedViewModel, navController) }
                composable("Register") { RegisterView(navController) }
                composable("Home") { HomeView(sharedViewModel, setUseDarkTheme, navController) }
                composable("Settings") {
                    SettingsView(
                        sharedViewModel,
                        useDarkTheme,
                        setUseDarkTheme,
                        navController,
                        context = context,
                        startDestination
                    )
                }
                composable("Fridge") { FridgeView(sharedViewModel, navController) }
                composable(
                    "Recipe/{recipe}", arguments = listOf(
                        navArgument("recipe") { type = NavType.StringType })
                ) {
                    val recipe = it.arguments?.getString("recipe")
                    println(recipe)
                    requireNotNull(recipe) { Log.e("Error", "ingredient must NOT be null") }
                    RecipeView(
                        navController,
                        Json.decodeFromString(recipe.replace("@", "/")),
                        sharedViewModel
                    )
                }
                composable("NewIngredient") { NewIngredientView(sharedViewModel, navController) }
                composable(route = "EditIngredient/{ingredient}", arguments = listOf(
                    navArgument("ingredient") { type = NavType.StringType }
                )) {
                    val ingredient = it.arguments?.getString("ingredient")
                    requireNotNull(ingredient) { Log.e("Error", "ingredient must NOT be null") }
                    EditIngredientView(navController, Json.decodeFromString(ingredient))
                }
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
        MainComponent(sharedViewModel, context = LocalContext.current)
    }
}