package com.example.sentinel

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sentinel.ui.theme.SentinelTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SentinelTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LoginScreen()
                }
            }
        }
    }
}

private fun MainActivity.Surface(
    modifier: Modifier,
    color: Color,
    content: () -> Unit
) {
    TODO("Not yet implemented")
}

@Composable
fun LoginScreen() {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isHovered by remember { mutableStateOf(false) }

    val gradientColors = listOf(Color(0xFF2C2E43), Color(0xFF595260))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(gradientColors))
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 30.dp)
                .wrapContentHeight(Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Logo
            Image(
                painter = painterResource(id = R.drawable.imam_sunglass_pic),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // App Title
            Text(
                text = "Sentinel",
                fontSize = 32.sp,
                color = Color(0xFFC1BCD9),
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Username Input
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Enter e-mail or username") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFC1BCD9),
                    unfocusedBorderColor = Color.LightGray,
                    focusedLabelColor = Color(0xFFC1BCD9)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password Input
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Enter Password") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFC1BCD9),
                    unfocusedBorderColor = Color.LightGray,
                    focusedLabelColor = Color(0xFFC1BCD9)
                )
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Login Button with hover/press color effect
            Button(
                onClick = { /* TODO: Handle login action */ },
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { isHovered = !isHovered },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isHovered)
                        Color(0xFF6C63FF)
                    else
                        Color(0xFF5D54A4)
                )
            ) {
                Text(
                    "Login",
                    fontSize = 18.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Create Account Button
            TextButton(onClick = { /* TODO: Navigate to Sign Up */ }) {
                Text(
                    "Don't have an account?\nCreate one",
                    fontSize = 15.sp,
                    color = Color.White
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewLoginScreen() {
    SentinelTheme {
        LoginScreen()
    }
}
