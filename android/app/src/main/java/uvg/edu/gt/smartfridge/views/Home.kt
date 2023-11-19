package uvg.edu.gt.smartfridge.views

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import uvg.edu.gt.smartfridge.components.BottomNavBar
import uvg.edu.gt.smartfridge.components.NavItem
import uvg.edu.gt.smartfridge.components.Title
import uvg.edu.gt.smartfridge.components.searchBar
import uvg.edu.gt.smartfridge.data.ResponseException
import uvg.edu.gt.smartfridge.ui.theme.smartFridgeTheme
import uvg.edu.gt.smartfridge.viewModels.HomeViewModel
import uvg.edu.gt.smartfridge.viewModels.SharedViewModel
import uvg.edu.gt.smartfridge.viewModels.TokenManager
import kotlin.time.Duration.Companion.seconds

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@ExperimentalMaterial3Api
@Composable
fun HomeView(
    sharedViewModel: SharedViewModel,
    setUseDarkTheme: (Boolean) -> Unit,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val homeViewModel = viewModel<HomeViewModel>()
    val context = LocalContext.current
    val navItems = sequenceOf(
        NavItem.Fridge, NavItem.Home, NavItem.Settings
    )
    val jwtToken = sharedViewModel.jwtToken
    val (searchQuery, setSearchQuery) = remember { mutableStateOf("") }
    val (searching, setSearching) = remember { mutableStateOf(false) }

    LaunchedEffect(searchQuery) {
        coroutineScope.launch {
            if (searching) {
                return@launch
            }

            setSearching(true)
            delay(2.seconds)
            setSearching(false)
        }
    }

    if (searching && searchQuery.isNotBlank()) {
        LaunchedEffect(Unit) {
            coroutineScope.launch {
                val result = homeViewModel.searchRecipes(jwtToken, searchQuery)

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

    if (searchQuery.isBlank()) {
        LaunchedEffect(Unit) {

            setUseDarkTheme(sharedViewModel.useDarkTheme);

            coroutineScope.launch(Dispatchers.IO) {
                println("JWT: $jwtToken")
                val result = homeViewModel.fetchRecipesList(jwtToken)

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

    Scaffold(bottomBar = { BottomNavBar(items = navItems, navController = navController) }) {
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = modifier
                .fillMaxWidth()
                .padding(10.dp, 0.dp)
        ) {
            Title("Home")
            Spacer(modifier = Modifier.height(24.dp))
            searchBar("Search for Recipes...", searchQuery, setSearchQuery)
            Spacer(modifier = Modifier.height(60.dp))
            LazyColumn {
                homeViewModel.getRecipesList().forEach { recipe ->
                    item {
                        Card(
                            onClick = {
                                navController.navigate(
                                    "Recipe/${
                                        Json.encodeToString(recipe).replace("/", "@")
                                    }"
                                )
                            },
                            modifier = modifier
                                .fillMaxWidth()
                                .padding(15.dp, 0.dp, 15.dp, 40.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            AsyncImage(
                                model = recipe.Banner,
                                contentDescription = "Recipe description",
                                contentScale = ContentScale.Crop,
                                modifier = modifier.fillMaxWidth()
                            )
                            Text(
                                recipe.Recipe,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = modifier.padding(10.dp, 0.dp),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Start,
                                modifier = modifier.padding(10.dp)
                            ) {
                                recipe.Tags.forEach { tag ->
                                    Text(
                                        tag,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = modifier
                                            .background(
                                                color = MaterialTheme.colorScheme.secondaryContainer,
                                                shape = RoundedCornerShape(5.dp)
                                            )
                                            .padding(3.dp),
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                    Spacer(modifier = modifier.width(8.dp))
                                }
                            }
                        }
                        Spacer(modifier = modifier.height(15.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun HomeViewPreview() {
    val sharedViewModel = SharedViewModel()
    smartFridgeTheme {
        HomeView(sharedViewModel, {}, navController = rememberNavController())
    }
}