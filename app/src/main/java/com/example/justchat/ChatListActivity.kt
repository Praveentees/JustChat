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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.justchat.ui.theme.JustChatTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.runtime.mutableIntStateOf

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

@Composable
fun ChatListScreen(auth: FirebaseAuth) {
    val context = LocalContext.current
    val user = auth.currentUser
    val userName = user?.displayName ?: user?.email ?: "User"

    var selectedTab by remember { mutableIntStateOf(0) }
    val db = FirebaseFirestore.getInstance()
    var contacts by remember { mutableStateOf(listOf<Map<String, Any>>()) }

    LaunchedEffect(Unit) {
        user?.uid?.let { uid ->
            db.collection("users")
                .document(uid)
                .collection("contacts")
                .addSnapshotListener { snapshot, e ->
                    if (e != null || snapshot == null) return@addSnapshotListener
                    contacts = snapshot.documents.mapNotNull { it.data }
                }
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(selectedTab) { selectedTab = it }
        },
        modifier = Modifier.padding(WindowInsets.systemBars.asPaddingValues())
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF5F5F5))
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
                        context.startActivity(Intent(context, LoginActivity::class.java))
                    }
                ) {
                    Text("Logout")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTab) {
                0 -> {
                    Text("Recent Chats", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (contacts.isEmpty()) {
                        Text("No contacts yet. Add some to start chatting.")
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(contacts) { contact ->
                                val name = contact["name"] as? String ?: "Unknown"
                                val email = contact["email"] as? String ?: return@items

                                ChatItemCard(name, email) {
                                    val intent = Intent(context, ChatActivity::class.java).apply {
                                        putExtra("contactName", name)
                                        putExtra("contactEmail", email)
                                    }
                                    context.startActivity(intent)
                                }
                            }
                        }
                    }
                }

                1 -> {
                    AddContactSection(
                        currentUserId = user?.uid ?: "",
                        userEmail = user?.email ?: ""
                    )
                }

                2 -> {
                    Text("Profile", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Name: $userName", style = MaterialTheme.typography.bodyLarge)
                    Text("Email: ${user?.email}", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
fun ChatItemCard(name: String, email: String, onClick: () -> Unit) {
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val time = sdf.format(Date())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(name, style = MaterialTheme.typography.titleMedium)
            Text("Tap to chat", style = MaterialTheme.typography.bodyMedium, maxLines = 1)
            Text(time, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}

@Composable
fun BottomNavigationBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    NavigationBar {
        NavigationBarItem(
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            icon = { Icon(Icons.Default.Chat, contentDescription = "Chats") },
            label = { Text("Chats") }
        )
        NavigationBarItem(
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            icon = { Icon(Icons.Default.PersonAdd, contentDescription = "Add Contact") },
            label = { Text("Add Contact") }
        )
        NavigationBarItem(
            selected = selectedTab == 2,
            onClick = { onTabSelected(2) },
            icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Profile") },
            label = { Text("Profile") }
        )
    }
}

@Composable
fun AddContactSection(currentUserId: String, userEmail: String) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var contactList by remember { mutableStateOf(listOf<Map<String, Any>>()) }

    LaunchedEffect(Unit) {
        db.collection("users")
            .document(currentUserId)
            .collection("contacts")
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener
                contactList = snapshot.documents.mapNotNull { it.data }
            }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Contact Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Contact Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Button(
            onClick = {
                if (name.isNotBlank() && email.isNotBlank()) {
                    val contact = mapOf(
                        "name" to name,
                        "email" to email,
                        "addedAt" to System.currentTimeMillis()
                    )
                    db.collection("users")
                        .document(currentUserId)
                        .collection("contacts")
                        .add(contact)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Contact added!", Toast.LENGTH_SHORT).show()
                            name = ""
                            email = ""
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Failed: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(context, "All fields are required", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Contact")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Your Contacts", style = MaterialTheme.typography.titleMedium)

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(contactList) { contact ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Name: ${contact["name"]}")
                        Text("Email: ${contact["email"]}")
                    }
                }
            }
        }
    }
}
