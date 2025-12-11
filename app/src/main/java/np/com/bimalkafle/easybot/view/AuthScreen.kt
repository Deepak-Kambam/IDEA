package np.com.bimalkafle.easybot.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import np.com.bimalkafle.easybot.R
import np.com.bimalkafle.easybot.ui.theme.Purple80

@Composable
fun AuthScreen(modifier: Modifier = Modifier, navController: NavHostController) {
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
            .padding(32.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    )
    {
        Image(
            painter = painterResource(R.drawable.login),
            contentDescription = null,
            modifier = Modifier.size(360.dp)
        )

        Spacer(modifier = Modifier.height(19.dp))

        Text(
            text = "Start your IDEA journey now",
            fontSize = 23.sp
        )

        Spacer(Modifier.height(12.dp))

        FilledTonalButton(
            onClick = {
                navController.navigate("login")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray.copy(alpha = 0.3f))
        ) {
            Text(text = "Login", fontSize = 23.sp, fontWeight = FontWeight.Bold,color = Color.White)
        }

        Spacer(modifier = Modifier.height(19.dp))

        Button(
            onClick = {
                navController.navigate("signUp")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Purple80)
        ) {
            Text(text = "Sign Up", fontSize = 23.sp, fontWeight = FontWeight.Bold,color = Color.White)
        }
    }
}