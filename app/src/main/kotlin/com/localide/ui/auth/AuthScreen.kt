package com.localide.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.localide.ui.theme.*
import com.localide.viewmodel.AuthState
import com.localide.viewmodel.AuthViewModel

@Composable
fun AuthScreen(vm: AuthViewModel = viewModel()) {
    val authState by vm.authState.collectAsState()
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(IdeBackground)
    ) {
        // Background gradient accent
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            IdeAccent.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInVertically(initialOffsetY = { -40 })
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(IdeAccent.copy(alpha = 0.15f))
                            .border(1.dp, IdeAccent.copy(alpha = 0.4f), RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Code,
                            contentDescription = null,
                            tint = IdeAccent,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "LocalIDE",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = IdeOnBackground
                    )
                    Text(
                        text = "Your pocket development environment",
                        fontSize = 13.sp,
                        color = IdeOnSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(56.dp))

            when (val state = authState) {
                is AuthState.Loading -> {
                    CircularProgressIndicator(
                        color = IdeAccent,
                        modifier = Modifier.size(36.dp),
                        strokeWidth = 2.dp
                    )
                }

                is AuthState.Error -> {
                    ErrorCard(
                        message = state.message,
                        onDismiss = vm::dismissError
                    )
                }

                else -> {
                    SignInButtons(
                        onGoogleClick = { vm.signInWithGoogle(context) },
                        onGitHubClick = { vm.launchGitHubSignIn() }
                    )
                }
            }

            Spacer(Modifier.height(48.dp))

            Text(
                text = "By signing in you agree to use this app\nfor personal development only.",
                fontSize = 11.sp,
                color = IdeOnSurfaceVariant.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun SignInButtons(
    onGoogleClick: () -> Unit,
    onGitHubClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        ProviderButton(
            onClick = onGoogleClick,
            icon = {
                // Google "G" logo rendered with text
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "G",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4285F4)
                    )
                }
            },
            label = "Continue with Google",
            containerColor = Color(0xFF1F1F1F),
            borderColor = IdeBorder
        )

        ProviderButton(
            onClick = onGitHubClick,
            icon = {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⌥",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            },
            label = "Continue with GitHub",
            containerColor = Color(0xFF161B22),
            borderColor = Color(0xFF30363D)
        )
    }
}

@Composable
private fun ProviderButton(
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    label: String,
    containerColor: Color,
    borderColor: Color
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        color = containerColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            icon()
            Spacer(Modifier.width(12.dp))
            Text(
                text = label,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = IdeOnBackground
            )
        }
    }
}

@Composable
private fun ErrorCard(message: String, onDismiss: () -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = IdeRed.copy(alpha = 0.12f)
        ) {
            Text(
                text = message,
                modifier = Modifier.padding(16.dp),
                color = IdeRed,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }
        TextButton(onClick = onDismiss) {
            Text("Try Again", color = IdeAccent)
        }
    }
}
