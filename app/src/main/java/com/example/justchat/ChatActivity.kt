package com.example.justchat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.justchat.ui.theme.JustChatTheme

class ChatActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val name = intent.getStringExtra("contactName") ?: "Unknown"
        val email = intent.getStringExtra("contactEmail") ?: ""

        setContent {
            JustChatTheme {
                ChatScreen(contactName = name, contactEmail = email)
            }
        }
    }
}


