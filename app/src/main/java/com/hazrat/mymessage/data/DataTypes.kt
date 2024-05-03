package com.hazrat.mymessage.data

import com.google.firebase.firestore.PropertyName

data class UserData(
    @get:PropertyName("userId") @set:PropertyName("userId") var userId: String? = "",
    @get:PropertyName("name") @set:PropertyName("name") var name: String? = "",
    @get:PropertyName("number") @set:PropertyName("number") var number: String? = "",
    @get:PropertyName("imageUrl") @set:PropertyName("imageUrl") var imageUrl: String? = ""
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "userId" to userId,
            "name" to name,
            "number" to number,
            "imageUrl" to imageUrl
        )
    }
}

data class ChatData(
    val chatId: String? = "",
    val user1: ChatUser = ChatUser(),
    val user2: ChatUser = ChatUser()
)

data class ChatUser(
    val userId: String?= "",
    val name: String ? = "",
    val number: String? = "",
    val imageUrl: String? = "",
)

data class Message(
    val sendBy: String? ="",
    val message: String? ="",
    val timeStamp: String? =""
)

data class Status(
    val user: ChatUser = ChatUser(),
    val imageUrl: String? = "",
    val timeStamp: Long?=null
)