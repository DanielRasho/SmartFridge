package uvg.edu.gt.smartfridge.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import uvg.edu.gt.smartfridge.ui.theme.smartFridgeTheme

/**
 * A Nav item for the nav bar.
 *
 * Represents a navigation screen and holds all the necessary information to navigate to it.
 *
 * @param route The route of the screen.
 * @param label A Label to show under the icon.
 * @param icon An Icon for display with the label.
 */
sealed class NavItem(val route: String, val label: String, val icon: ImageVector) {
    object Home : NavItem("Home", "Home", Icons.Filled.Home)
    object Settings : NavItem("Settings", "Settings", Icons.Filled.Settings)
    object Fridge : NavItem("Fridge", "Fridge", Icons.Filled.List)
}

/**
 * The Application Bottom navigation bar.
 *
 * For navigation to work, please supply a navController and a list of items of type NavItem.
 * You can use the already defined [NavItem.Home], [NavItem.Settings] and others to create a basic nav bar.
 *
 * @param navController The controller used for navigation.
 * @param items A sequence of NavItems.
 * @param modifier This element modifier.
 */
@Composable
fun BottomNavBar(
    navController: NavHostController = rememberNavController(),
    items: Sequence<NavItem>,
    modifier: Modifier = Modifier,
) {
    val currentRoute = currentRoute(navController)
    Row(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        items.forEach { screen ->
            val isSelected = screen.route == currentRoute

            if (isSelected) {
                Column() {
                    Column(
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        IconButton(onClick = { /*Do nothing*/ }) {
                            Icon(screen.icon, null, tint = MaterialTheme.colorScheme.onBackground)
                        }

                        Text(screen.label, color = MaterialTheme.colorScheme.onBackground)
                    }
                }
            } else {
                Column() {
                    Column(
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        IconButton(onClick = { navController.navigate(screen.route) }) {
                            Icon(screen.icon, null, tint = MaterialTheme.colorScheme.outlineVariant)
                        }

                        Text(screen.label, color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun BottomNavBarPreview() {
    val items = sequenceOf(NavItem.Fridge, NavItem.Home, NavItem.Settings)
    smartFridgeTheme {
        BottomNavBar(items = items)
    }
}

fun currentRoute(navController: NavHostController): String? {
    val currentEntry = navController.currentBackStackEntry
    return currentEntry?.destination?.route
}