package uvg.edu.gt.smartfridge.views

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import uvg.edu.gt.smartfridge.components.BottomNavBar
import uvg.edu.gt.smartfridge.components.NavItem
import uvg.edu.gt.smartfridge.components.Title
import uvg.edu.gt.smartfridge.components.searchBar
import uvg.edu.gt.smartfridge.data.ResponseException
import uvg.edu.gt.smartfridge.models.Ingredient
import uvg.edu.gt.smartfridge.ui.theme.smartFridgeTheme
import uvg.edu.gt.smartfridge.viewModels.FridgeViewModel
import uvg.edu.gt.smartfridge.viewModels.SharedViewModel
import uvg.edu.gt.smartfridge.viewModels.TokenManager
import java.text.SimpleDateFormat
import java.util.Locale

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FridgeView(sharedViewModel: SharedViewModel, navController: NavHostController) {
    val jwtToken = sharedViewModel.jwtToken
    val coroutineScope = rememberCoroutineScope()
    val fridgeViewModel = viewModel<FridgeViewModel>()
    val context = LocalContext.current
    val navItems = sequenceOf(
        NavItem.Fridge, NavItem.Home, NavItem.Settings
    )

    LaunchedEffect(Unit) {
        coroutineScope.launch(Dispatchers.IO) {
            println("JWT: $jwtToken")
            val result = fridgeViewModel.fetchUserIngredients(jwtToken)

            if (result.isFailure) {
                val exception = result.exceptionOrNull() as ResponseException
                println("ERROR! " + "Error ${exception.statusCode} : ${exception.message}")
                val code = exception.statusCode

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "Error ${exception.statusCode} : ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                if (code == 401) {
                    withContext(Dispatchers.Main) {
                        val tokenManager = TokenManager(context)
                        tokenManager.clearJwtToken()
                        // Navigate to the login view
                        navController.navigate("Login") {
                            // Clear the back stack to prevent going back to Login
                            popUpTo("Home") {
                                inclusive = true
                            }
                        }
                    }
                }
            }
        }
    }

    }

    val groupedIngredients: Map<String, List<Ingredient>> =
        fridgeViewModel.getIngredients().groupBy { it.Category }

    Scaffold(bottomBar = { BottomNavBar(items = navItems, navController = navController) },
        floatingActionButton = { addIngredientButton(navController) }) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Title(text = "Fridge")
            Spacer(modifier = Modifier.height(24.dp))
            searchBar("Search for Ingredients...")
            Spacer(modifier = Modifier.height(40.dp))
            for ((categoryName, ingredientList) in groupedIngredients) {
                categoryList(categoryName, ingredientList)
                Spacer(modifier = Modifier.height(36.dp))
            }
            Spacer(modifier = Modifier.height(96.dp))
        }
    }

}

@Composable
fun categoryList(category: String, ingredients: List<Ingredient>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = category,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Divider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outline
        )
        ingredients.forEach { ingredient ->
            ingredientEntry(ingredient)
            Divider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun ingredientEntry(ingredient: Ingredient) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { println("TESTING HERE") },
        verticalAlignment = Alignment.CenterVertically
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(2f)
        ) {
            Text(
                text = formatDateString(ingredient.ExpireDate),
                color = MaterialTheme.colorScheme.outline,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.defaultMinSize(64.dp)
            )
            Spacer(modifier = Modifier.width(24.dp))
            Column {
                Text(
                    text = ingredient.Name,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = ingredient.Category,
                    color = MaterialTheme.colorScheme.outline,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
        Row(
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = ingredient.Quantity.toString(),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "    ${ingredient.Unit}",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun addIngredientButton(navHostController: NavHostController) {
    FloatingActionButton(
        onClick = { navHostController.navigate("NewIngredient") },
        shape = CircleShape,
        containerColor = MaterialTheme.colorScheme.primary
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = "Add Item",
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(36.dp)
        )

    }
}

fun formatDateString(inputDateString: String): String {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
    val outputFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())

    val date = inputFormat.parse(inputDateString)
    return outputFormat.format(date)
}

@Preview
@Composable
fun testFridgeView() {
    smartFridgeTheme {
        FridgeView(SharedViewModel(), rememberNavController())
    }
}

@Preview
@Composable
fun testIngredientItem() {
    smartFridgeTheme {
        ingredientEntry(
            ingredient = Ingredient(
                "32",
                "UserID",
                "Soy Sause",
                "Souce",
                3.0f,
                "3",
                "2023-11-03T03:03:49.309844585Z"
            )
        )
    }
}