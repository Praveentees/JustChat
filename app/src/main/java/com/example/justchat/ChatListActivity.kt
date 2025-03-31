package com.example.justchat

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.justchat.ui.theme.JustChatTheme
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

class ChatListActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        setContent {
            JustChatTheme {
                ChatListScreen(auth)
            }
        }
    }
}

data class ChatItem(val name: String, val lastMessage: String, val timestamp: Long)

@Composable
fun ChatListScreen(auth: FirebaseAuth) {
    val context = LocalContext.current
    val user = auth.currentUser
    val userName = user?.displayName ?: user?.email ?: "User"

    val dummyChats = listOf(
        ChatItem("Alice", "Hey! Are we meeting today?", System.currentTimeMillis() - 300000),
        ChatItem("Bob", "Sure, I’ll send the file soon.", System.currentTimeMillis() - 600000),
        ChatItem("Charlie", "😂😂", System.currentTimeMillis() - 900000)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(WindowInsets.systemBars.asPaddingValues()) // ✅ Add this to respect status bar
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Welcome, $userName 👋", style = MaterialTheme.typography.titleLarge)
            TextButton(
                onClick = {
                    auth.signOut()
                    Toast.makeText(context, "Logged out", Toast.LENGTH_SHORT).show()
                    val intent = Intent(context, LoginActivity::class.java)
                    context.startActivity(intent)
                }
            ) {
                Text("Logout")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Recent Chats", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(dummyChats) { chat ->
                ChatItemCard(chat)
            }
        }
    }
}


@Composable
fun ChatItemCard(chat: ChatItem) {
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val time = sdf.format(Date(chat.timestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                // TODO: Navigate to ChatScreen(chat.name)
            },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(chat.name, style = MaterialTheme.typography.titleMedium)
            Text(chat.lastMessage, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
            Text(time, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}
