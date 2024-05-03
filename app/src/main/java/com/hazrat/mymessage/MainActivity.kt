package com.hazrat.mymessage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hazrat.mymessage.screens.ChatListScreen
import com.hazrat.mymessage.screens.LoginScreen
import com.hazrat.mymessage.screens.ProfileScreen
import com.hazrat.mymessage.screens.SignUpScreen
import com.hazrat.mymessage.screens.SingleChatScreen
import com.hazrat.mymessage.screens.SingleStatusScreen
import com.hazrat.mymessage.screens.StatusScreen
import com.hazrat.mymessage.ui.theme.MyMessageTheme
import dagger.hilt.android.AndroidEntryPoint

sealed class DestinationScreen(
    var route: String
) {
    object SignUp : DestinationScreen("signup")
    object Login : DestinationScreen("login")
    object Profile : DestinationScreen("profile")
    object ChatList : DestinationScreen("chatList")
    object SingleChat : DestinationScreen("singleChat/{chatId}") {
        fun createRoute(id: String) = "singleChat/$id"
    }

    object StatusList : DestinationScreen("statusList")
    object SingleStatus : DestinationScreen("singleStatus/{userId}") {
        fun createRoute(userId: String) = "singleStatus/$userId"
    }


}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyMessageTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ChatAppNavigation()
                }
            }
        }
    }

    @Composable
    fun ChatAppNavigation() {
        val navController = rememberNavController()
        var viewModel = hiltViewModel<MMViewModel>()
        NavHost(navController = navController, startDestination = DestinationScreen.SignUp.route) {
            composable(DestinationScreen.SignUp.route) {
                SignUpScreen(navController, viewModel)
            }
            composable(DestinationScreen.Login.route) {
                LoginScreen(navController, viewModel)
            }
            composable(DestinationScreen.ChatList.route) {
                ChatListScreen(navController, viewModel)
            }
            composable(DestinationScreen.SingleChat.route) {
                val chatId = it.arguments?.getString("chatId")
                chatId?.let {
                    SingleChatScreen(navController, viewModel, chatId)
                }
            }
            composable(DestinationScreen.StatusList.route) {
                StatusScreen(navController, viewModel)
            }
            composable(DestinationScreen.Profile.route) {
                ProfileScreen(navController, viewModel)
            }
            composable(DestinationScreen.SingleStatus.route) {
                val userId = it.arguments?.getString("userId")
                userId?.let {
                    SingleStatusScreen(navController, viewModel, userId = it)
                }
            }
        }
    }

}

