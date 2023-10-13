package uvg.edu.gt.smartfridge.views

import android.annotation.SuppressLint
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import uvg.edu.gt.smartfridge.ui.theme.smartFridgeTheme

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FridgeView(navHostController: NavHostController) {
    val navItems = sequenceOf(
        NavItem.Fridge, NavItem.Home, NavItem.Settings
    )
    Scaffold(bottomBar = { BottomNavBar(items = navItems, navController = navHostController) }) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            Title(text = "Fridge")
            Spacer(modifier = Modifier.height(24.dp))
            SearchBar()
        }
    }

}

@Preview
@Composable
fun testFridgeView(){
    smartFridgeTheme {
        FridgeView(rememberNavController())
    }
}