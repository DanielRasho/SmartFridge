package uvg.edu.gt.smartfridge.views

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.rounded.ExitToApp
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import uvg.edu.gt.smartfridge.components.BottomNavBar
import uvg.edu.gt.smartfridge.components.IconPrimaryButton
import uvg.edu.gt.smartfridge.components.NavItem
import uvg.edu.gt.smartfridge.components.SearchBar
import uvg.edu.gt.smartfridge.components.Title
import uvg.edu.gt.smartfridge.models.Ingredient
import uvg.edu.gt.smartfridge.ui.theme.smartFridgeTheme

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FridgeView(navHostController: NavHostController) {

    val navItems = sequenceOf(
        NavItem.Fridge, NavItem.Home, NavItem.Settings
    )

    val ingredients: List<Ingredient> = listOf(
        Ingredient("", "Ingredient1", "Category1", 100.0f, "g", "12/12/2023"),
        Ingredient("","Ingredient2", "Category2", 200.0f, "ml"),
        Ingredient("","Ingredient3", "Category1", 50.0f, "g"),
        Ingredient("","Ingredient4", "Category3", 300.0f, "g"),
        Ingredient("","Ingredient5", "Category2", 150.0f, "ml"),
        Ingredient("","Ingredient1", "Category4", 100.0f, "g", "12/12/2023"),
        Ingredient("","Ingredient2", "Category5", 200.0f, "ml"),
        Ingredient("","Ingredient3", "Category6", 50.0f, "g"),
        Ingredient("","Ingredient4", "Category4", 300.0f, "g"),
        Ingredient("","Ingredient5", "Category5", 150.0f, "bottles")
    )

    val groupedIngredients: Map<String, List<Ingredient>> = ingredients.groupBy { it.category }

    Scaffold(bottomBar = { BottomNavBar(items = navItems, navController = navHostController) },
        floatingActionButton = { addIngredientButton() }) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Title(text = "Fridge")
            Spacer(modifier = Modifier.height(24.dp))
            SearchBar()
            for ((categoryName, ingredientList) in groupedIngredients){
                categoryList(categoryName, ingredientList)
                Spacer(modifier = Modifier.height(36.dp))
            }
            Spacer(modifier = Modifier.height(96.dp))
        }
    }

}

@Composable
fun categoryList (category : String,  ingredients : List<Ingredient>){
    Column ( modifier = Modifier.fillMaxWidth()){
        Text(text = category,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Divider(thickness = 1.dp,
            color = MaterialTheme.colorScheme.outline)
        ingredients.forEach{ingredient ->  
            ingredientEntry(ingredient)
            Divider(thickness = 1.dp,
                color = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
fun ingredientEntry (ingredient : Ingredient){

    Row (modifier = Modifier
        .fillMaxWidth()
        .clickable { println("TESTING HERE") },
        verticalAlignment = Alignment.CenterVertically){

        Row ( verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(2f)
        ){
            Text(text = ingredient.expireDate,
                color = MaterialTheme.colorScheme.outline,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.defaultMinSize(64.dp))
            Spacer(modifier = Modifier.width(24.dp))
            Column {
                Text(text = ingredient.name,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelMedium)
                Text(text = ingredient.category,
                    color = MaterialTheme.colorScheme.outline,
                    style = MaterialTheme.typography.labelSmall)
            }
        }
        Row (verticalAlignment = Alignment.Bottom,
            modifier = Modifier.weight(1f)){
            Text(text = ingredient.quantity.toString(),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.weight(1f),)
            Text(text = "    ${ingredient.unit}",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun addIngredientButton() {
    FloatingActionButton(
        onClick = { /*TODO*/ },
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

@Preview
@Composable
fun testFridgeView(){
    smartFridgeTheme {
        FridgeView(rememberNavController())
    }
}