package np.com.bimalkafle.easybot.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import np.com.bimalkafle.easybot.viewModel.AuthViewModel
import np.com.bimalkafle.easybot.viewModel.ChatViewModel
import np.com.bimalkafle.easybot.R
import np.com.bimalkafle.easybot.Routes
import np.com.bimalkafle.easybot.ui.theme.Purple80

data class UserModel(
    val name: String = "",
    val email: String = "",
    val address: String = ""
)

@Composable
fun ProfilePage(
    modifier: Modifier = Modifier,
    chatViewModel: ChatViewModel,
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    var userModel by remember { mutableStateOf(UserModel()) }
    var nameInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var addressInput by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    // Load user data from Firestore once
    LaunchedEffect(Unit) {
        auth.currentUser?.uid?.let { uid ->
            firestore.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener { snapshot ->
                    val result = snapshot.toObject(UserModel::class.java)
                    if (result != null) {
                        userModel = result
                        nameInput = result.name
                        emailInput = result.email
                        addressInput = result.address
                    }
                    isLoading = false
                }
                .addOnFailureListener {
                    isLoading = false
                }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Purple80.copy(alpha = 0.15f),
                        Color.White.copy(alpha = 0.02f)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Purple80
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(20.dp))

                // Profile Header with Avatar
                ProfileHeader(userModel = userModel)

                Spacer(Modifier.height(32.dp))

                // Profile Info Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(24.dp))
                        .animateContentSize(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        ),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.95f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Profile Information",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Purple80
                        )

                        Divider(
                            color = Purple80.copy(alpha = 0.3f),
                            thickness = 1.dp
                        )

                        // Name field
                        EditableField(
                            value = nameInput,
                            label = "Full Name",
                            icon = Icons.Default.Person,
                            onValueChange = { nameInput = it }
                        ) { newName ->
                            auth.currentUser?.uid?.let { uid ->
                                firestore.collection("users").document(uid)
                                    .update("name", newName)
                            }
                        }

                        // Email field
                        EditableField(
                            value = emailInput,
                            label = "E-Mail",
                            icon = Icons.Default.Email,
                            onValueChange = { emailInput = it }
                        ) { newEmail ->
                            auth.currentUser?.uid?.let { uid ->
                                firestore.collection("users").document(uid)
                                    .update("email", newEmail)
                            }
                        }

                        // Address field
                        EditableField(
                            value = addressInput,
                            label = "Address",
                            icon = Icons.Default.Home,
                            onValueChange = { addressInput = it }
                        ) { newAddress ->
                            auth.currentUser?.uid?.let { uid ->
                                firestore.collection("users").document(uid)
                                    .update("address", newAddress)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Sign out button
                Button(
                    onClick = {
                        auth.signOut()
                        navController.navigate(Routes.AUTH) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(6.dp, RoundedCornerShape(28.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE53935)
                    ),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Sign Out",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Sign Out",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun ProfileHeader(userModel: UserModel) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Avatar with gradient border
        Box(
            modifier = Modifier
                .size(140.dp)
                .shadow(12.dp, CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Purple80.copy(alpha = 0.8f),
                            Color(0xFF9C27B0).copy(alpha = 0.6f)
                        )
                    ),
                    shape = CircleShape
                )
                .padding(6.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.img_1),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(Color.White)
            )
        }

        // Name
        Text(
            text = userModel.name.ifEmpty { "User" },
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Purple80,
            textAlign = TextAlign.Center
        )

        // Email badge
        if (userModel.email.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Purple80.copy(alpha = 0.15f),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = userModel.email,
                    fontSize = 14.sp,
                    color = Purple80,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun EditableField(
    value: String,
    label: String,
    icon: ImageVector,
    onValueChange: (String) -> Unit,
    onSave: (String) -> Unit
) {
    var input by remember { mutableStateOf(value) }
    var isEditing by remember { mutableStateOf(false) }
    var showSaveConfirmation by remember { mutableStateOf(false) }

    LaunchedEffect(value) {
        input = value
    }

    Column {
        OutlinedTextField(
            value = input,
            onValueChange = {
                input = it
                onValueChange(it)
                isEditing = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(),
            label = { Text(label) },
            leadingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Purple80.copy(alpha = 0.7f)
                )
            },
            trailingIcon = {
                AnimatedVisibility(
                    visible = isEditing && input.isNotEmpty() && input != value,
                    enter = fadeIn(tween(300)),
                    exit = fadeOut(tween(200))
                ) {
                    IconButton(
                        onClick = {
                            onSave(input)
                            isEditing = false
                            showSaveConfirmation = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = "Save",
                            tint = Color(0xFF4CAF50)
                        )
                    }
                }
                AnimatedVisibility(
                    visible = !isEditing || input == value,
                    enter = fadeIn(tween(300)),
                    exit = fadeOut(tween(200))
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = Color.Gray.copy(alpha = 0.5f),
                        modifier = Modifier.padding(end = 12.dp)
                    )
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Purple80,
                unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                focusedLabelColor = Purple80,
                cursorColor = Purple80
            )
        )

        // Save confirmation message
        AnimatedVisibility(
            visible = showSaveConfirmation,
            enter = fadeIn(tween(300)),
            exit = fadeOut(tween(300))
        ) {
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(2000)
                showSaveConfirmation = false
            }
            Text(
                text = " Saved successfully",
                color = Color(0xFF4CAF50),
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}
