package uvg.edu.gt.smartfridge.views

import android.annotation.SuppressLint
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import uvg.edu.gt.smartfridge.components.BottomNavBar
import uvg.edu.gt.smartfridge.components.NavItem
import uvg.edu.gt.smartfridge.components.UnitSelector

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@ExperimentalMaterial3Api
@Composable
fun HomeView(navController: NavHostController) {
    val items = sequenceOf(
        NavItem.Fridge, NavItem.Home, NavItem.Settings
    )
    Scaffold(bottomBar = { BottomNavBar(items = items, navController = navController) }) {
        UnitSelector()
    }
}