package com.hazrat.mymessage.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.modifier.modifierLocalMapOf
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.hazrat.mymessage.CommonImage
import com.hazrat.mymessage.MMViewModel
import com.hazrat.mymessage.data.Message
import kotlin.system.measureTimeMillis

@Composable
fun SingleChatScreen(navController: NavController, viewModel: MMViewModel, chatId: String) {
    var reply by rememberSaveable {
        mutableStateOf("")
    }
    val onSendReply = {
        viewModel.onSendReply(chatId, reply)
        reply = ""
    }
    val myUser = viewModel.userData.value
    val currentChat = viewModel.chats.value.first { it.chatId == chatId }
    val chatUser =
        if (myUser?.userId == currentChat.user1.userId) currentChat.user2 else currentChat.user1
    val chatMessage = viewModel.chatMessage


    LaunchedEffect(key1 = Unit) {
        viewModel.populateMessages(chatId)
        viewModel.depopulateMessage()
    }
    BackHandler {

    }

    Column(modifier = Modifier.fillMaxWidth()) {
        ChatHeader(name = chatUser.name ?: "", imageUrl = chatUser.imageUrl ?: "") {
            navController.popBackStack()
            viewModel.depopulateMessage()
        }
        Box(modifier = Modifier.weight(1f)) {

            MessageBox(
                chatMessage = chatMessage.value,
                currentUserId = myUser?.userId ?: ""
            )
        }
        ReplyBox(reply = reply, onReplyChange = { reply = it }, onSendReply)
    }
}

@Composable
fun MessageBox(modifier: Modifier = Modifier, chatMessage: List<Message>, currentUserId: String) {
    val listState = rememberLazyListState()
    LazyColumn(
        state = listState
    ) {
        items(chatMessage) { msg ->
            val alignment = if (msg.sendBy == currentUserId) Alignment.End else Alignment.Start
            val color = if (msg.sendBy == currentUserId) Color(0xFF94E742) else Color(0xFFFAF02F)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = alignment
            ) {
                Text(
                    text = msg.message ?: "", modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(color)
                        .padding(12.dp),
                    color = Color.Black, fontWeight = FontWeight.Bold
                )
            }
        }
    }
    LaunchedEffect(chatMessage) {
        if (chatMessage.isNotEmpty()) {
            listState.scrollToItem(chatMessage.size - 1)
        }
    }
}

@Composable
fun ChatHeader(
    name: String,
    imageUrl: String,
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null,
            modifier = Modifier
                .clickable(onClick = { onBackClick.invoke() })
                .padding(8.dp)
        )
        CommonImage(
            data = imageUrl, modifier = Modifier
                .padding(8.dp)
                .size(50.dp)
                .clip(CircleShape)
        )
        Text(text = name, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp))
    }
}

@Composable
fun ReplyBox(
    reply: String,
    onReplyChange: (String) -> Unit,
    onSendReply: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextField(
                value = reply, onValueChange = onReplyChange, maxLines = 3
            )
            Button(onClick = onSendReply) {
                Text(text = "Send")
            }
        }
    }
}