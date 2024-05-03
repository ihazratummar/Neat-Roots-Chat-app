package com.hazrat.mymessage.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.hazrat.mymessage.CommonProgressBar
import com.hazrat.mymessage.CommonRow
import com.hazrat.mymessage.DestinationScreen
import com.hazrat.mymessage.MMViewModel
import com.hazrat.mymessage.TitleText
import com.hazrat.mymessage.navigateTo

@Composable
fun ChatListScreen(navController: NavController, viewModel: MMViewModel) {

    val inProgress = viewModel.inProcessChats
    if (inProgress.value) {
        CommonProgressBar()
    } else {
        val chats = viewModel.chats.value
        val userData = viewModel.userData.value
        val showDialog = remember {
            mutableStateOf(false)
        }
        val oFabClick: () -> Unit = { showDialog.value = true }
        val onDismiss: () -> Unit = { showDialog.value = false }
        val onAddChat: (String) -> Unit = {
            viewModel.onAddChat(it)
            showDialog.value = false
        }
        Scaffold(
            floatingActionButton = {
                FAB(
                    showDialog = showDialog.value,
                    onFabClick = oFabClick,
                    onDismiss = onDismiss,
                    onAddChat = onAddChat
                )
            }, content = {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(it)
                ) {
                    TitleText(text = "Chats")
                    if (chats.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(text = "No Chat Available")
                        }
                    }else{
                        LazyColumn(
                            modifier = Modifier.weight(1f)
                        ) {
                            items(chats){chat->
                                val chatUser = if (chat.user1.userId == userData?.userId){
                                    chat.user2
                                }else{
                                    chat.user1
                                }
                                CommonRow(
                                    imageUrl = chatUser.imageUrl,
                                    name = chatUser.name
                                ) {
                                    chat.chatId?.let {
                                        navigateTo(navController, route = DestinationScreen.SingleChat.createRoute(id =it))
                                    }
                                }
                            }
                        }
                    }
                    BottomNavigationMenu(BottomNavigationItem.CHATLIST, navController)
                }
            }
        )
    }

}

@Composable
fun FAB(
    showDialog: Boolean,
    onFabClick: () -> Unit,
    onDismiss: () -> Unit,
    onAddChat: (String) -> Unit
) {
    val addChatNumber = remember {
        mutableStateOf("")
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                onDismiss.invoke()
                addChatNumber.value = ""
            },
            confirmButton = {
                Button(onClick = {
                    onAddChat(addChatNumber.value)
                    addChatNumber.value = " "
                }) {
                    Text(text = "Add Chat")
                }
            },
            title = { Text(text = "Add Chat") },
            text = {
                OutlinedTextField(
                    value = addChatNumber.value,
                    onValueChange = { addChatNumber.value = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        )
    }
    FloatingActionButton(
        onClick = onFabClick,
        containerColor = Color.Green,
        modifier = Modifier.padding(40.dp)
    ) {
        Icon(imageVector = Icons.Rounded.Add, contentDescription = null)
    }
}