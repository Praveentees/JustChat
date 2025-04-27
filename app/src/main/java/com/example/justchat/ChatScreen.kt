package com.example.justchat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

data class ChatMessage(
    val senderId: String = "",
    val message: String = "",
    val timestamp: Long = 0L
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(contactName: String, contactEmail: String) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    val currentUser = auth.currentUser ?: return
    val currentUserId = currentUser.uid
    val chatId = listOf(currentUserId, contactEmail).sorted().joinToString("_")

    var messageText by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf(listOf<ChatMessage>()) }

    // Load messages real-time
    LaunchedEffect(Unit) {
        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    messages = snapshot.documents.mapNotNull {
                        it.toObject(ChatMessage::class.java)
                    }
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = "Profile", modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = contactName, style = MaterialTheme.typography.titleLarge)
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Open camera */ }) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Camera")
                    }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = { Text("Type a message...") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            sendMessage(db, chatId, currentUserId, messageText)
                            messageText = ""
                        }
                    }
                ) {
                    Text("Send")
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(bottom = 70.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(8.dp)
        ) {
            items(messages) { message ->
                val isMine = message.senderId == currentUserId
                ChatBubble(message.message, message.timestamp, isMine)
            }
        }
    }
}

@Composable
fun ChatBubble(message: String, timestamp: Long, isMine: Boolean) {
    val backgroundColor = if (isMine) Color(0xFFD2F8D2) else Color.White
    val alignment = if (isMine) Alignment.End else Alignment.Start
    val timeFormatted = remember(timestamp) {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(timestamp))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalAlignment = alignment
    ) {
        Surface(
            color = backgroundColor,
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = message, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = timeFormatted, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }
    }
}

private fun sendMessage(
    db: FirebaseFirestore,
    chatId: String,
    senderId: String,
    message: String
) {
    if (message.isBlank()) return
    val messageMap = mapOf(
        "senderId" to senderId,
        "message" to message.trim(),
        "timestamp" to System.currentTimeMillis()
    )
    db.collection("chats")
        .document(chatId)
        .collection("messages")
        .add(messageMap)
}
