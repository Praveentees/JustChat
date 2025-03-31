package com.example.justchat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.justchat.ui.theme.JustChatTheme

class ChatActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get contact data from intent
        val contactName = intent.getStringExtra("name") ?: "Unknown"
        val contactEmail = intent.getStringExtra("email") ?: ""

        setContent {
            JustChatTheme {
                ChatScreen(contactName = contactName, contactEmail = contactEmail)
            }
        }
    }
}

