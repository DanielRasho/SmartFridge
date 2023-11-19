package uvg.edu.gt.smartfridge.views

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uvg.edu.gt.smartfridge.components.BottomNavBar
import uvg.edu.gt.smartfridge.components.CheckBoxList
import uvg.edu.gt.smartfridge.components.NavItem
import uvg.edu.gt.smartfridge.components.Title
import uvg.edu.gt.smartfridge.models.LightIngredient
import uvg.edu.gt.smartfridge.models.Recipe
import uvg.edu.gt.smartfridge.ui.theme.smartFridgeTheme
import uvg.edu.gt.smartfridge.components.PrimaryButton
import uvg.edu.gt.smartfridge.data.ResponseException
import uvg.edu.gt.smartfridge.models.Ingredient
import uvg.edu.gt.smartfridge.viewModels.FridgeViewModel
import uvg.edu.gt.smartfridge.viewModels.SharedViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@ExperimentalMaterial3Api
@Composable
fun RecipeView(navController: NavHostController, recipe: Recipe, sharedViewModel : SharedViewModel, modifier: Modifier = Modifier) {
    val recipeIngredients = remember { recipe.Ingredients.map { i -> i.Name } }
    val navItems = sequenceOf(
        NavItem.Fridge, NavItem.Home, NavItem.Settings
    )

    val jwtToken = sharedViewModel.jwtToken
    val coroutineScope = rememberCoroutineScope()
    val fridgeViewModel = viewModel<FridgeViewModel>()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        coroutineScope.launch(Dispatchers.IO) {
            println("JWT: $jwtToken")
            val result= fridgeViewModel.fetchUserIngredients(jwtToken)

            if(result.isFailure){
                val exception = result.exceptionOrNull() as ResponseException
                println("ERROR! " + "Error ${exception.statusCode} : ${exception.message}")

                withContext(Dispatchers.Main) {
                    Toast.makeText(context,
                        "Error ${exception.statusCode} : ${exception.message}",
                        Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val userIngredients: List<String> = fridgeViewModel.getIngredients().map { it.Name }

    Scaffold(bottomBar = { BottomNavBar(items = navItems, navController = navController) }) {
    Column(

        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .height(796.dp)
            .verticalScroll(rememberScrollState())

    ) {
        Title(recipe.Recipe)
        Text(
            text = "Ingredients",
            textDecoration = TextDecoration.Underline,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.width(40.dp))
        CheckBoxList(recipeIngredients, userIngredients)
        Spacer(modifier = Modifier.height(50.dp))

        PrimaryButton(text = "Prepare!", Modifier.fillMaxWidth()) {

        }
        Spacer(modifier = Modifier.height(200.dp))

    }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun RecipeViewPreview() {
    val recipe = Recipe(
        RecipeId = "recipe:Plant-Based-Breakfast-Bowl-9118197,recipe,list.recipe.trending",
        Recipe = "Asian Lime Dressing Recipe",
        Banner = "https://peartreekitchen.com/louisiana-crunch-cake.jpg",
        Tags = emptyList(),
        Source = "https://peartreekitchen.com/louisiana-crunch-cake",
        Ingredients = listOf(
            LightIngredient("Lime Juice", "½ cup riced sweet potato"),
            LightIngredient("Soy sauce", "2 Teaspoons of Soy sauce")
    )
    )
    val sharedViewModel = SharedViewModel()
    smartFridgeTheme {
        RecipeView(rememberNavController(), recipe, sharedViewModel)
    }
}