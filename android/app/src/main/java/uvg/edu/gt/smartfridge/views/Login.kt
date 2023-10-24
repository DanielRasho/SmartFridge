package uvg.edu.gt.smartfridge.views

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uvg.edu.gt.smartfridge.R
import uvg.edu.gt.smartfridge.components.PrimaryButton
import uvg.edu.gt.smartfridge.components.textField
import uvg.edu.gt.smartfridge.data.ResponseException
import uvg.edu.gt.smartfridge.ui.theme.smartFridgeTheme
import uvg.edu.gt.smartfridge.viewModels.LoginViewModel
import uvg.edu.gt.smartfridge.viewModels.SharedViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@ExperimentalMaterial3Api
@Composable
fun LoginView(sharedViewModel: SharedViewModel, navController: NavHostController, modifier: Modifier = Modifier) {
    var username = remember { mutableStateOf("") }
    var password = remember { mutableStateOf("") }
    val loginViewModel : LoginViewModel = LoginViewModel()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(

        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp, 0.dp)
            .height(796.dp),
        horizontalAlignment = Alignment.CenterHorizontally

    ) {
        Spacer(modifier = Modifier.height(50.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = modifier.fillMaxWidth()
        ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = null,
            modifier = Modifier
                .width(150.dp)
                .height(150.dp)
        ) }
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = "SmartFridge",
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.displayLarge)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = modifier.fillMaxWidth()
        ) {

        }

        Spacer(modifier = Modifier.height(100.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = modifier.fillMaxWidth()
        ) {

            textField(label = "Username", textValue = username)
        }
        Spacer(modifier = Modifier.height(30.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = modifier.fillMaxWidth()
        ) {
            textField(label = "Password", textValue = password)
        }
        Spacer(modifier = Modifier.height(70.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = modifier.fillMaxWidth()
        ) {
            PrimaryButton(text = "Login",
                modifier = Modifier.width(300.dp))
            {//sharedViewModel.prueba = username.value
               // navController.navigate("Home")

                coroutineScope.launch(Dispatchers.IO) {
                    val result = loginViewModel.sendLoginCredentials(username.value, password.value)

                    when(result.isSuccess){
                        true -> {
                            val (JWT_TOKEN, userSettings) = result.getOrNull()!!
                            Log.i("JWT_TOKEN", JWT_TOKEN)
                            Log.i("userSettings", userSettings.toString())

                            sharedViewModel.jwtToken = JWT_TOKEN
                            withContext(Dispatchers.Main){
                                println(navController.toString())
                                navController.navigate("Home")
                            }

                        }
                        false -> {
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
            }
        }
    }
}




@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun LoginViewPreview() {
    val sharedViewModel = SharedViewModel()
    smartFridgeTheme {
        LoginView(sharedViewModel,rememberNavController())
    }
}