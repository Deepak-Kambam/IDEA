package np.com.bimalkafle.easybot

import IncomingBluetoothMessageDialog
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import np.com.bimalkafle.easybot.view.AuthScreen
import np.com.bimalkafle.easybot.view.BluetoothDeviceListScreen
import np.com.bimalkafle.easybot.view.ChatPage
import np.com.bimalkafle.easybot.view.LoginPage
import np.com.bimalkafle.easybot.view.ProfilePage
import np.com.bimalkafle.easybot.view.SavedMessagesPage
import np.com.bimalkafle.easybot.view.SignUpPage
import np.com.bimalkafle.easybot.viewModel.AuthViewModel
import np.com.bimalkafle.easybot.viewModel.BluetoothViewModel
import np.com.bimalkafle.easybot.viewModel.ChatViewModel

object Routes {
    const val AUTH = "auth"
    const val LOGIN = "login"
    const val SIGNUP = "signUp"
    const val CHAT = "chat"
    const val SAVED = "saved"
    const val PROFILE = "profile"
    const val BLUETOOTH = "bluetooth?message={message}"
    const val RECEIVED = "received"
}

// ✅ Define CompositionLocal
val LocalNavController = staticCompositionLocalOf<NavHostController> {
    error("No NavController provided")
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavGraph(
    innerPadding: PaddingValues,
    authViewModel: AuthViewModel,
    chatViewModel: ChatViewModel,
    bluetoothViewModel: BluetoothViewModel
) {
    val navController = rememberAnimatedNavController()

    // ✅ Observe Auth State
    val authState by authViewModel.authState.collectAsState()
    val firebaseUser = FirebaseAuth.getInstance().currentUser

    // ✅ Choose start destination
    val startDestination = if (authState.isLoggedIn || firebaseUser != null) {
        Routes.CHAT
    } else {
        Routes.AUTH
    }

    // ✅ Provide navController globally
    CompositionLocalProvider(LocalNavController provides navController) {
        AnimatedNavHost(navController = navController, startDestination = startDestination) {

            // 🔹 Auth flow
            composable(
                route = Routes.AUTH,
                enterTransition = { fadeIn(animationSpec = tween(500)) },
                exitTransition = { fadeOut(animationSpec = tween(500)) },
                popEnterTransition = { fadeIn(animationSpec = tween(500)) },
                popExitTransition = { fadeOut(animationSpec = tween(500)) }
            ) {
                AuthScreen(
                    modifier = Modifier.padding(innerPadding),
                    navController = navController
                )
            }

            composable(
                route = Routes.LOGIN,
                enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(500)) },
                exitTransition = { slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(500)) },
                popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(500)) },
                popExitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(500)) }
            ) {
                LoginPage(
                    innerPadding = innerPadding,
                    viewModel = authViewModel,
                    navController = navController
                )
            }

            composable(
                route = Routes.SIGNUP,
                enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(500)) },
                exitTransition = { slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(500)) },
                popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(500)) },
                popExitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(500)) }
            ) {
                SignUpPage(
                    innerPadding = innerPadding,
                    viewModel = authViewModel,
                    navController = navController
                )
            }

            // 🔹 Main chat + features
            composable(
                route = Routes.CHAT,
                enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(500)) },
                exitTransition = { slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(500)) },
                popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(500)) },
                popExitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(500)) }
            ) {
                ChatPage(
                    modifier = Modifier.padding(innerPadding),
                    viewModel = chatViewModel,
                    navController = navController
                )
            }

            composable(
                route = Routes.SAVED,
                enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(500)) },
                exitTransition = { slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(500)) },
                popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(500)) },
                popExitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(500)) }
            ) {
                SavedMessagesPage(
                    modifier = Modifier.padding(innerPadding),
                    viewModel = chatViewModel,
                    navController = navController
                )
            }

            composable(
                route = Routes.BLUETOOTH,
                arguments = listOf(navArgument("message") {
                    type = NavType.StringType
                    defaultValue = ""
                }),
                enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(500)) },
                exitTransition = { slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(500)) },
                popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(500)) },
                popExitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(500)) }
            ) { backStackEntry ->
                val message = backStackEntry.arguments?.getString("message") ?: ""
                BluetoothDeviceListScreen(
                    modifier = Modifier.padding(innerPadding),
                    bluetoothViewModel = bluetoothViewModel,
                    chatViewModel = chatViewModel,
                    messageToSend = message,
                    onMessageSent = { /* handle success */ }
                )
            }

            composable(
                route = Routes.PROFILE,
                enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(500)) },
                exitTransition = { slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(500)) },
                popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(500)) },
                popExitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(500)) }
            ) {
                ProfilePage(
                    modifier = Modifier.padding(innerPadding),
                    authViewModel = authViewModel,
                    chatViewModel = chatViewModel,
                    navController = navController
                )
            }

            composable(
                route = Routes.RECEIVED,
                enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(500)) },
                exitTransition = { slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(500)) },
                popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(500)) },
                popExitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(500)) }
            ) {
                IncomingBluetoothMessageDialog(
                    chatViewModel = chatViewModel,
                    onNavigateToReceivedUI = {
                        navController.navigate(Routes.RECEIVED)
                    }
                )
                ChatPage(
                    modifier = Modifier.padding(innerPadding),
                    viewModel = chatViewModel,
                    navController = navController
                )
            }
        }
    }
}
