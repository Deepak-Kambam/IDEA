package np.com.bimalkafle.easybot.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import np.com.bimalkafle.easybot.AppUtil
import np.com.bimalkafle.easybot.viewModel.AuthViewModel
import np.com.bimalkafle.easybot.R
import np.com.bimalkafle.easybot.ui.theme.Purple80
import androidx.compose.runtime.LaunchedEffect

@Composable
fun LoginPage(innerPadding: PaddingValues, viewModel: AuthViewModel, navController: NavController) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val authState by viewModel.authState.collectAsState()
    // Focus Requesters
    val passwordFocusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }

    // Navigate to chat when login succeeds
    LaunchedEffect(authState.isLoggedIn) {
        if (authState.isLoggedIn) {
            isLoading = false
            navController.navigate("chat") {
                popUpTo("auth") { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Purple80.copy(alpha = 0.25f), // Slightly more opaque for visibility
                        Color.White.copy(alpha = 0.05f)  // Fades to more transparent
                    ),
                    start = Offset(0f, .05f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            )
            .padding(22.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Login",
            fontSize = 47.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(29.dp))

        Image(
            painter = painterResource(R.drawable.login),
            contentDescription = null,
            modifier = Modifier.size(220.dp)
        )

        Spacer(modifier = Modifier.height(17.dp))

        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.5.dp, // Slightly thicker border for emphasis
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Purple80.copy(alpha = 0.5f), // Brighter edge for glossy look
                            Color.Gray.copy(alpha = 0.15f)
                        )
                    ),
                    shape = RoundedCornerShape(28.dp)
                ),
            value = email,
            onValueChange = { email = it.trim() },
            shape = RoundedCornerShape(28.dp),
            maxLines = 1,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = Purple80.copy(alpha = 0.8f)
            ),
            placeholder = {
                Text(
                    text = "Enter Email",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W400,
                    color = if (email.isNotEmpty()) Purple80.copy(alpha = 0.8f) else Color.Gray.copy(
                        alpha = 0.5f
                    )
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Send",
                    tint = if (email.isNotEmpty()) Purple80.copy(alpha = 0.8f) else Color.Gray.copy(
                        alpha = 0.5f
                    ),
                    modifier = Modifier
                        .padding(bottom = 4.dp, end = 8.dp)
                        .size(32.dp)
                )
            },
            singleLine = true,
            // Show Next button
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = {
                    // Move focus to password field
                    passwordFocusRequester.requestFocus()
                }
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.5.dp, // Slightly thicker border for emphasis
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Purple80.copy(alpha = 0.5f), // Brighter edge for glossy look
                            Color.Gray.copy(alpha = 0.15f)
                        )
                    ),
                    shape = RoundedCornerShape(28.dp)
                ).focusRequester(passwordFocusRequester),
            value = password,
            onValueChange = { password = it.trim() },
            shape = RoundedCornerShape(28.dp),
            maxLines = 1,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = Purple80.copy(alpha = 0.8f)
            ),
            placeholder = {
                Text(
                    text = "Enter Password",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W400,
                    color = if (password.isNotEmpty()) Purple80.copy(alpha = 0.8f) else Color.Gray.copy(
                        alpha = 0.5f
                    )
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Send",
                    tint = if (password.isNotEmpty()) Purple80.copy(alpha = 0.8f) else Color.Gray.copy(
                        alpha = 0.5f
                    ),
                    modifier = Modifier
                        .padding(bottom = 4.dp, end = 8.dp)
                        .size(32.dp)
                )
            },
            visualTransformation = PasswordVisualTransformation(),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (email.isNotEmpty() && password.isNotEmpty()) {
                        isLoading = true
                        viewModel.signUp(email, password)
                        AppUtil.showToast(context, "Sign Up Successful")
                        navController.navigate("chat")
                    } else {
                        AppUtil.showToast(context, "Please fill all the fields")
                    }
                    // Close keyboard on Done
                    focusManager.clearFocus()
                }
            )
        )

        Spacer(modifier = Modifier.height(34.dp))

        Button(
            onClick = {
                focusManager.clearFocus()
                if (email.isNotEmpty() && password.isNotEmpty()) {
                    isLoading = true
                    viewModel.login(email, password)
                } else {
                    AppUtil.showToast(context, "Please fill all the fields")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Purple80)
        ) {
            if (isLoading)
                CircularProgressIndicator(trackColor = MaterialTheme.colorScheme.onBackground)
            else
                Text(text = "Login", fontSize = 23.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        authState.error?.let { error ->
            Text(
                text = error,
                color = Color.Red,
                fontWeight = FontWeight.Bold
            )
        }
    }
}