package com.example.justchat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(contactName: String, contactEmail: String) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val focusManager = LocalFocusManager.current

    val currentUser = auth.currentUser
    val currentUserId = currentUser?.uid ?: return
    val chatId = listOf(currentUserId, contactEmail).sorted().joinToString("_")

    var messageText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chat with $contactName") }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .background(Color.White),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = { Text("Type a message...") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (messageText.isNotBlank()) {
                                sendMessage(
                                    db = db,
                                    chatId = chatId,
                                    senderId = currentUserId,
                                    message = messageText.trim()
                                )
                                messageText = ""
                                focusManager.clearFocus()
                            }
                        }
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            sendMessage(
                                db = db,
                                chatId = chatId,
                                senderId = currentUserId,
                                message = messageText.trim()
                            )
                            messageText = ""
                            focusManager.clearFocus()
                        }
                    }
                ) {
                    Text("Send")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Text(
                text = "Messaging with $contactName",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

private fun sendMessage(
    db: FirebaseFirestore,
    chatId: String,
    senderId: String,
    message: String
) {
    val messageMap = mapOf(
        "senderId" to senderId,
        "message" to message,
        "timestamp" to System.currentTimeMillis()
    )
    db.collection("chats")
        .document(chatId)
        .collection("messages")
        .add(messageMap)
}
