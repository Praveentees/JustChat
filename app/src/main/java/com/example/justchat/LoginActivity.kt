package com.example.justchat

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
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import com.example.justchat.ui.theme.JustChatTheme
import com.example.justchat.R

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JustChatTheme {
                LoginScreen()
            }
        }
    }
}

@Composable
fun LoginScreen() {
    val context = LocalContext.current

    var isEmailLogin by remember { mutableStateOf(true) }
    var emailOrPhone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Country codes map
    val countryCodes = mapOf(
        "🇺🇸 USA" to "+1",
        "🇮🇳 India" to "+91",
        "🇬🇧 UK" to "+44",
        "🇨🇦 Canada" to "+1"
    )

    var selectedCountry by remember { mutableStateOf("🇮🇳 India") }
    var expanded by remember { mutableStateOf(false) }

    val isValidEmail = android.util.Patterns.EMAIL_ADDRESS.matcher(emailOrPhone).matches()
    val isValidPhone = android.util.Patterns.PHONE.matcher(emailOrPhone).matches()
    val isPasswordValid = password.length >= 6

    Box(modifier = Modifier.fillMaxSize()) {
        // Background
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Overlay
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

            // Country code dropdown + Phone input (only for phone login)
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

            // Email input if email login
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

            // Password
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Existing Login Button
            Button(
                onClick = {
                    val phoneWithCode = "${countryCodes[selectedCountry]}$emailOrPhone"
                    val isPhoneValid = android.util.Patterns.PHONE.matcher(phoneWithCode).matches()

                    if ((isEmailLogin && isValidEmail || !isEmailLogin && isPhoneValid) && isPasswordValid) {
                        Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()
                        // TODO: Navigate to home
                    } else {
                        Toast.makeText(context, "Invalid credentials", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Login")
            }

// Toggle method
            TextButton(onClick = { isEmailLogin = !isEmailLogin }) {
                Text("Switch to ${if (isEmailLogin) "Phone" else "Email"} Login")
            }

//  New Register Button (Always visible)
            TextButton(
                onClick = {
                    val intent = android.content.Intent(context, RegisterActivity::class.java)
                    context.startActivity(intent)
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Don't have an account? Sign up")
            }

        }
    }
}


