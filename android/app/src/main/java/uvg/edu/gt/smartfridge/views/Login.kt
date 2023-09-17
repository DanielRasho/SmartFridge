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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import uvg.edu.gt.smartfridge.R
import uvg.edu.gt.smartfridge.components.IconPrimaryButton
import uvg.edu.gt.smartfridge.components.IconSecondaryButton
import uvg.edu.gt.smartfridge.ui.theme.smartFridgeTheme

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@ExperimentalMaterial3Api
@Composable
fun LoginView(navController: NavHostController, modifier: Modifier = Modifier) {

    Column(

        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp, 0.dp)
            .height(796.dp)

    ) {
        Spacer(modifier = Modifier.height(192.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = modifier.fillMaxWidth()
        ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = null,
            modifier = Modifier
                .width(240.dp)
                .height(240.dp)
        )}
        Spacer(modifier = Modifier.width(32.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = modifier.fillMaxWidth()
        ) {
        Divider(
            color = MaterialTheme.colorScheme.outline, // Customize the color as needed
            thickness = 1.dp,   // Customize the thickness as needed
            modifier = Modifier
                .padding(vertical = 8.dp)
                .width(240.dp)
        )}
        Spacer(modifier = Modifier.width(32.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = modifier.fillMaxWidth()
        ) {
        IconSecondaryButton(text = "Continue with Google", icon = R.drawable.google_icon) {
            navController.navigate("Home")
        }}
        Spacer(modifier = Modifier.width(24.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = modifier.fillMaxWidth()
        ) {
            IconPrimaryButton(text = "Exit", icon = Icons.Rounded.ExitToApp) {

            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun LoginViewPreview() {
    smartFridgeTheme {
        LoginView(rememberNavController())
    }
}