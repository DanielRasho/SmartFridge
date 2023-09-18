package uvg.edu.gt.smartfridge.views

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExitToApp
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import uvg.edu.gt.smartfridge.R
import uvg.edu.gt.smartfridge.components.BottomNavBar
import uvg.edu.gt.smartfridge.components.CheckBoxList
import uvg.edu.gt.smartfridge.components.IconPrimaryButton
import uvg.edu.gt.smartfridge.components.IconSecondaryButton
import uvg.edu.gt.smartfridge.components.NavItem
import uvg.edu.gt.smartfridge.components.Title
import uvg.edu.gt.smartfridge.ui.theme.smartFridgeTheme

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@ExperimentalMaterial3Api
@Composable
fun RecipeView(navController: NavHostController, modifier: Modifier = Modifier) {
    val recipeIngredients = remember { arrayListOf("Shrimp", "Butter", "Italian Seasoning","Salt and Pepper") }
    val userIngredients = remember { arrayListOf("Shrimp", "Italian Seasoning","Salt and Pepper") }
    val navItems = sequenceOf(
        NavItem.Fridge, NavItem.Home, NavItem.Settings
    )
    Scaffold(bottomBar = { BottomNavBar(items = navItems, navController = navController) }) {
    Column(

        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(10.dp, 0.dp)
            .height(796.dp)

    ) {
        Title("Shrimp with Garlic")
        Text(
            text = "Ingredients",
            textDecoration = TextDecoration.Underline,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.width(40.dp))
        CheckBoxList(recipeIngredients, userIngredients)
        Spacer(modifier = Modifier.width(24.dp))
        Text(
            text = "Procedure",
            textDecoration = TextDecoration.Underline,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.width(24.dp))
        Text(
            text = "Notes",
            textDecoration = TextDecoration.Underline,
            style = MaterialTheme.typography.bodyLarge
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = modifier.fillMaxWidth()
        ) {
            Text(
                text = "1. BlahblahblahblahblahblahblahblahblahBlahblahblahblahblah\n2. BlahBlahblah",
                fontSize = 12.sp
            )
        }


    }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun RecipeViewPreview() {
    smartFridgeTheme {
        RecipeView(rememberNavController())
    }
}