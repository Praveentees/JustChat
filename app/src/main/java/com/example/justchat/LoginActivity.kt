package com.example.justchat

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import com.example.justchat.ui.theme.JustChatTheme
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        setContent {
            JustChatTheme {
                LoginScreen(auth)
            }
        }
    }
}

@Composable
fun LoginScreen(auth: FirebaseAuth) {
    val context = LocalContext.current

    var isEmailLogin by remember { mutableStateOf(true) }
    var emailOrPhone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val countryCodes = mapOf(
        "🇺🇸 USA" to "+1",
        "🇮🇳 India" to "+91",
        "🇬🇧 UK" to "+44",
        "🇨🇦 Canada" to "+1"
    )

    var selectedCountry by remember { mutableStateOf("🇮🇳 India") }
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White.copy(alpha = 0.4f))
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp)
                .align(Alignment.Center),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (isEmailLogin) "Login with Email" else "Login with Phone",
                style = MaterialTheme.typography.headlineMedium
            )

            if (!isEmailLogin) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box {
                        OutlinedButton(onClick = { expanded = true }) {
                            Text("${countryCodes[selectedCountry]}")
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            countryCodes.keys.forEach { country ->
                                DropdownMenuItem(
                                    text = { Text(country) },
                                    onClick = {
                                        selectedCountry = country
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = emailOrPhone,
                        onValueChange = { emailOrPhone = it },
                        label = { Text("Phone Number") },
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (isEmailLogin) {
                OutlinedTextField(
                    value = emailOrPhone,
                    onValueChange = { emailOrPhone = it },
                    label = { Text("Email") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    val phoneWithCode = "${countryCodes[selectedCountry]}$emailOrPhone"
                    val isEmailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(emailOrPhone).matches()
                    val isPhoneValid = android.util.Patterns.PHONE.matcher(phoneWithCode).matches()
                    val isPasswordValid = password.length >= 6

                    if (isEmailLogin) {
                        if (isEmailValid && isPasswordValid) {
                            auth.signInWithEmailAndPassword(emailOrPhone.trim(), password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()

                                        // ✅ Navigate to ChatsActivity (ChatListActivity)
                                        val intent = Intent(context, ChatListActivity::class.java)
                                        context.startActivity(intent)
                                    } else {
                                        Toast.makeText(context, "Login Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        } else {
                            Toast.makeText(context, "Invalid email or password", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Phone login is not implemented in Firebase yet", Toast.LENGTH_LONG).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Login")
            }


            TextButton(onClick = { isEmailLogin = !isEmailLogin }) {
                Text("Switch to ${if (isEmailLogin) "Phone" else "Email"} Login")
            }

            TextButton(
                onClick = {
                    val intent = Intent(context, RegisterActivity::class.java)
                    context.startActivity(intent)
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Don't have an account? Sign up")
            }
        }
    }
}
