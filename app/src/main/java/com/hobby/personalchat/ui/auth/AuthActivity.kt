package com.hobby.personalchat.ui.auth

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.auth.GoogleAuthProvider
import com.hobby.personalchat.R
import com.hobby.personalchat.ui.theme.PersonalchatTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthActivity : ComponentActivity() {

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PersonalchatTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "auth") {
                    composable("auth") {
                        AuthScreen(viewModel = viewModel, navController = navController)
                    }
                    composable("success/{userName}") { backStackEntry ->
                        val userName = backStackEntry.arguments?.getString("userName") ?: ""
                        SuccessScreen(userName = userName)
                    }
                }
            }
        }
    }
}

@Composable
fun AuthScreen(viewModel: AuthViewModel, navController: NavController) {

    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showNetworkDialog by remember { mutableStateOf(false) }

    val googleSignInClient = remember {
        Identity.getSignInClient(context)
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
    ) { result ->
        val credential = googleSignInClient.getSignInCredentialFromIntent(result.data)
        viewModel.signInWithGoogleCredential(GoogleAuthProvider.getCredential(credential.googleIdToken, null))
    }

    if (showNetworkDialog) {
        AlertDialog(
            onDismissRequest = { showNetworkDialog = false },
            title = { Text("No Internet Connection") },
            text = { Text("Please enable your internet connection to sign in with Google.") },
            confirmButton = {
                TextButton(onClick = {
                    context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
                    showNetworkDialog = false
                }) {
                    Text("Enable Internet")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNetworkDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }


    when (uiState) {
        is AuthUiState.Success -> {
            val userName = (uiState as AuthUiState.Success).user.displayName ?: ""
            navController.navigate("success/$userName")
        }

        is AuthUiState.Error -> {
            Toast.makeText(context, (uiState as AuthUiState.Error).message, Toast.LENGTH_SHORT).show()
        }

        else -> Unit
    }

    val OrangeYellowGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFFFFB74D), Color(0xFFFFEB3B))
    )
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        if (uiState is AuthUiState.Loading) {
            CircularProgressIndicator()
        } else {
            // --- TITLE SECTION ---
            Row {
                Text(
                    text = "Welcome to ",
                    style = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold)
                )
                // Gradient applied to "Personal Chat"
                Text(
                    text = "Personal Chat",
                    style = TextStyle(
                        brush = OrangeYellowGradient,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Login Here",
                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- INPUT FIELDS ---
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image =
                        if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = image,
                            contentDescription = "Toggle password visibility"
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- LOGIN BUTTON ---
            Button(
                onClick = { /* Handle Login */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(OrangeYellowGradient, shape = RoundedCornerShape(25.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues()
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Login", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- DIVIDER ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    thickness = 1.dp,
                    color = Color.LightGray
                )
                Text(" or ", color = Color.Gray, modifier = Modifier.padding(horizontal = 8.dp))
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    thickness = 1.dp,
                    color = Color.LightGray
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- GOOGLE LOGIN ---
            OutlinedButton(
                onClick = {
                    if (isInternetAvailable(context)) {
                        val signInRequest =
                            com.google.android.gms.auth.api.identity.BeginSignInRequest.builder()
                                .setGoogleIdTokenRequestOptions(
                                    com.google.android.gms.auth.api.identity.BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                                        .setSupported(true)
                                        .setServerClientId(context.getString(R.string.default_web_client_id))
                                        .setFilterByAuthorizedAccounts(false)
                                        .build()
                                )
                                .build()

                        googleSignInClient.beginSignIn(signInRequest)
                            .addOnSuccessListener { result ->
                                launcher.launch(
                                    IntentSenderRequest.Builder(result.pendingIntent.intentSender)
                                        .build()
                                )
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(context, e.localizedMessage, Toast.LENGTH_SHORT)
                                    .show()
                            }
                    } else {
                        showNetworkDialog = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Login with Google", color = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.ic_google),
                        contentDescription = "Google Icon",
                        tint = Color.Unspecified
                    )
                }
            }
        }
    }
}

private fun isInternetAvailable(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val activeNetwork =
        connectivityManager.getNetworkCapabilities(network) ?: return false
    return when {
        activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
        activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
        activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
        else -> false
    }
}

@Composable
fun SuccessScreen(userName: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Hello $userName! Thanks for using our app.")
    }
}

@Preview(showBackground = true)
@Composable
fun AuthScreenPreview() {
    PersonalchatTheme {
        val navController = rememberNavController()
        AuthScreen(
            viewModel = AuthViewModel(FirebaseAuth.getInstance()),
            navController = navController
        )
    }
}

