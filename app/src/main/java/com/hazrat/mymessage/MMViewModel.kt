package com.hazrat.mymessage

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import com.google.firebase.storage.FirebaseStorage
import com.hazrat.mymessage.data.CHATS
import com.hazrat.mymessage.data.ChatData
import com.hazrat.mymessage.data.ChatUser
import com.hazrat.mymessage.data.Event
import com.hazrat.mymessage.data.MESSAGE
import com.hazrat.mymessage.data.Message
import com.hazrat.mymessage.data.STATUS
import com.hazrat.mymessage.data.Status
import com.hazrat.mymessage.data.USER_NODE
import com.hazrat.mymessage.data.UserData
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class MMViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private var db: FirebaseFirestore,
    private val storage: FirebaseStorage
) : ViewModel() {


    var inProgress = mutableStateOf(false)
    var inProcessChats = mutableStateOf(false)
    private val eventMutableState = mutableStateOf<Event<String>?>(null)
    var signIn = mutableStateOf(false)
    val userData = mutableStateOf<UserData?>(null)
    val chats = mutableStateOf<List<ChatData>>(listOf())

    val chatMessage = mutableStateOf<List<Message>>(listOf())
    private val inProgressChatMessage = mutableStateOf(false)
    private var currentChatMessageListener: ListenerRegistration? = null

    val status = mutableStateOf<List<Status>>(listOf())
    val inProgressStatus = mutableStateOf(false)

    init {
        val currentUser = auth.currentUser
        signIn.value = currentUser != null
        currentUser?.uid?.let {
            getUserData(it)
        }
    }

    fun signUp(name: String, number: String, email: String, password: String) {
        inProgress.value = true
        if (name.isEmpty() or number.isEmpty() or email.isEmpty() or password.isEmpty()) {
            handleException(message = "Enter Fill All Fields")
            return
        }
        inProgress.value = true
        db.collection(USER_NODE).whereEqualTo("number", number).get().addOnSuccessListener { it ->
            if (it.isEmpty) {
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                    if (it.isSuccessful) {
                        signIn.value = true
                        createOrUpdateProfile(name, number)
                        Log.e("Login", "signUp: USer logged in")
                    } else {
                        handleException(it.exception, message = "SignUp failed")
                        Log.e("Login", "SignUp failed")
                    }
                }
            } else {
                handleException(message = "Number Already Exists")
            }
        }
    }

    fun login(email: String, password: String) {
        if (email.isEmpty() or password.isEmpty()) {
            handleException(message = "Enter email and password")
            return
        } else {
            inProgress.value = true
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { it ->
                    if (it.isSuccessful) {
                        signIn.value = true
                        inProgress.value = false
                        auth.currentUser?.uid?.let {
                            getUserData(it)
                        }
                    } else {
                        handleException(exception = it.exception, message = "Login Failed")
                    }
                }
        }
    }

    fun createOrUpdateProfile(
        name: String? = null,
        number: String? = null,
        imageUrl: String? = null
    ) {
        val uid = auth.currentUser?.uid
        val userData = UserData(
            userId = uid,
            name = name ?: userData.value?.name,
            number = number ?: userData.value?.number,
            imageUrl = imageUrl ?: userData.value?.imageUrl
        )
        uid.let {
            inProgress.value = true
            if (uid != null) {
                db.collection(USER_NODE).document(uid).get().addOnSuccessListener {
                    if (it.exists()) {
                        db.collection(USER_NODE).document(uid).set(userData, SetOptions.merge())
                            .addOnSuccessListener {
                                inProgress.value = false
                                getUserData(uid)
                            }
                    } else {
                        db.collection(USER_NODE).document(uid).set(userData)
                        inProgress.value = false
                        getUserData(uid)
                    }
                }.addOnFailureListener {
                    handleException(it, message = "Can not retrieve user")
                }
            }
        }
    }

    private fun getUserData(uid: String) {
        inProgress.value = true
        db.collection(USER_NODE).document(uid).addSnapshotListener { value, error ->
            if (error != null) {
                handleException(error, "Can not retrieve user")
            }
            if (value != null) {
                val user = value.toObject<UserData>()
                userData.value = user
                inProgress.value = false
                populateChats()
                populateStatuses()
            }
        }
    }

    private fun handleException(exception: Exception? = null, message: String = "") {
        Log.i("MyChatApp", "signUp: Live Chat Exception ")
        exception?.printStackTrace()
        val errorMsg = exception?.localizedMessage ?: ""
        val errorMessage = message.ifEmpty { errorMsg }
        eventMutableState.value = Event(errorMessage)
        inProgress.value = false
    }

    fun uploadProfileImage(uri: Uri) {
        uploadImage(uri) {
            createOrUpdateProfile(imageUrl = it.toString())
        }
    }

    private fun uploadImage(uri: Uri, onSuccess: (Uri) -> Unit) {
        inProgress.value = true
        val storageRef = storage.reference
        val uuid = UUID.randomUUID()
        val imageRef = storageRef.child("images/$uuid")
        val uploadTask = imageRef.putFile(uri)
        uploadTask.addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                onSuccess(uri)
            }
            inProgress.value = false // Update inProgress on success
        }
            .addOnFailureListener { exception ->
                handleException(exception)
                inProgress.value = false // Update inProgress on failure
            }
    }

    fun logout() {
        auth.signOut()
        signIn.value = false
        userData.value = null
        depopulateMessage()
        currentChatMessageListener = null
        eventMutableState.value = Event("Logged Out")

    }

    fun onAddChat(number: String) {
        if (number.isEmpty() or !number.isDigitsOnly()) {
            handleException(message = "Number must be contain digits only")
        } else {
            db.collection(CHATS).where(
                Filter.or(
                    Filter.and(
                        Filter.equalTo("user1.number", number),
                        Filter.equalTo("user2.number", userData.value?.number)
                    ),
                    Filter.and(
                        Filter.equalTo("user2.number", userData.value?.number),
                        Filter.equalTo("user1.number", number)
                    )
                )
            ).get().addOnSuccessListener {
                if (it.isEmpty) {
                    db.collection(USER_NODE).whereEqualTo("number", number).get()
                        .addOnSuccessListener {
                            if (it.isEmpty) {
                                handleException(message = "Number not found")
                            } else {
                                val chatPartner = it.toObjects<UserData>()[0]
                                val id = db.collection(CHATS).document().id
                                val chat = ChatData(
                                    chatId = id,
                                    ChatUser(
                                        userData.value?.userId,
                                        userData.value?.name,
                                        userData.value?.number,
                                        userData.value?.imageUrl
                                    ),
                                    ChatUser(
                                        chatPartner.userId,
                                        chatPartner.name,
                                        chatPartner.number,
                                        chatPartner.imageUrl,
                                    )
                                )
                                db.collection(CHATS).document(id).set(chat)
                            }
                        }
                        .addOnFailureListener {
                            handleException(it)
                        }
                } else {
                    handleException(message = "Chats already exist")
                }
            }
        }
    }

    fun populateMessages(chatId: String) {
        inProgressChatMessage.value = true
        currentChatMessageListener = db.collection(CHATS).document(chatId).collection(MESSAGE)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    handleException(error)
                }
                if (value != null) {
                    chatMessage.value = value.documents.mapNotNull {
                        it.toObject<Message>()
                    }.sortedBy { it.timeStamp }
                    inProgressChatMessage.value = false
                }
            }
    }

    fun depopulateMessage() {
        chatMessage.value = listOf()
        currentChatMessageListener = null
    }

    private fun populateChats() {
        inProcessChats.value = true
        db.collection(CHATS).where(
            Filter.or(
                Filter.equalTo("user1.userId", userData.value?.userId),
                Filter.equalTo("user2.userId", userData.value?.userId),
            )
        ).addSnapshotListener { value, error ->
            if (error != null) {
                handleException(error)
            }
            if (value != null) {
                chats.value = value.documents.mapNotNull {
                    it.toObject<ChatData>()

                }
                inProcessChats.value = false
            }
        }
    }

    fun onSendReply(chatId: String, message: String) {
        val time = Calendar.getInstance().time.toString()
        val msg = Message(userData.value?.userId, message = message, timeStamp = time)
        db.collection(CHATS).document(chatId).collection(MESSAGE).document().set(msg)
    }

    fun uploadStatus(uri: Uri) {
        uploadImage(uri) {
            createStatus(it.toString()) {newStatus ->
                val updateStatusList = mutableListOf<Status>().apply {
                    addAll(status.value ?: emptyList())
                    add(newStatus)
                }
                status.value = updateStatusList
            }
        }
    }

    //    private fun createStatus(imageUrl: String) {
//        val newStatus = Status(
//            ChatUser(
//                userData.value?.userId,
//                userData.value?.name,
//                userData.value?.number,
//                userData.value?.imageUrl,
//
//                ),
//            imageUrl,
//            System.currentTimeMillis()
//        )
//        db.collection(STATUS).document().set(newStatus)
//    }
    private fun createStatus(imageUrl: String, callback: (Status) -> Unit) {
        val newStatus = Status(
            ChatUser(
                userData.value?.userId,
                userData.value?.name,
                userData.value?.number,
                userData.value?.imageUrl,

                ),
            imageUrl,
            System.currentTimeMillis()
        )
        db.collection(STATUS).document().set(newStatus).addOnSuccessListener {
            callback(newStatus)
        }.addOnFailureListener { exception ->
            handleException(exception)
        }
    }

    private fun populateStatuses() {
        val timeDelta = 24L * 60 * 60 * 1000
        val cutOff = System.currentTimeMillis() - timeDelta
        inProgressStatus.value = true
        db.collection(CHATS).where(
            Filter.or(
                Filter.equalTo("user1.userId", userData.value?.userId),
                Filter.equalTo("user2.userId", userData.value?.userId),
            )
        ).addSnapshotListener { value, error ->
            if (error != null) {
                handleException(error)
                inProgressStatus.value = false
                return@addSnapshotListener
            }
            if (value != null) {
                val currentConnection = arrayListOf(userData.value?.userId)
                val chats = value.toObjects<ChatData>()
                chats.forEach { chat ->
                    if (chat.user1.userId == userData.value?.userId) {
                        currentConnection.add(chat.user2.userId)
                    } else {
                        currentConnection.add(chat.user1.userId)
                    }
                    db.collection(STATUS).whereGreaterThan("timeStamp", cutOff)
                        .whereIn("user.userId", currentConnection)
                        .addSnapshotListener { value, error ->
                            if (error != null) {
                                handleException(error)
                                inProgressStatus.value = false
                                return@addSnapshotListener
                            }
                            if (value != null) {
                                status.value = value.toObjects()
                                inProgressStatus.value = false
                            }
                        }
                }
            }
        }
    }
}
