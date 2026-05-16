package com.localide.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.localide.auth.AuthProvider
import com.localide.auth.UserSession
import com.localide.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSheet(
    session: UserSession,
    onDismiss: () -> Unit,
    onSignOut: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = IdeSurface,
        dragHandle = {
            Box(
                Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(IdeBorder)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(IdeAccent.copy(alpha = 0.15f))
                    .border(2.dp, IdeAccent.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Person,
                    contentDescription = null,
                    tint = IdeAccent,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = session.displayName.ifBlank { "User" },
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = IdeOnBackground
            )
            if (session.email.isNotBlank()) {
                Text(
                    text = session.email,
                    fontSize = 13.sp,
                    color = IdeOnSurfaceVariant
                )
            }

            Spacer(Modifier.height(4.dp))

            ProviderBadge(provider = session.provider)

            Spacer(Modifier.height(20.dp))

            HorizontalDivider(color = IdeBorder)

            Spacer(Modifier.height(8.dp))

            Surface(
                onClick = {
                    onSignOut()
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = IdeRed.copy(alpha = 0.1f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Filled.Logout, null, tint = IdeRed, modifier = Modifier.size(18.dp))
                    Text("Sign Out", color = IdeRed, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun ProviderBadge(provider: AuthProvider) {
    val (label, color) = when (provider) {
        AuthProvider.GOOGLE -> "Google" to Color(0xFF4285F4)
        AuthProvider.GITHUB -> "GitHub" to Color(0xFF6E40C9)
    }
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                Icons.Filled.Code,
                null,
                tint = color,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = "Signed in with $label",
                fontSize = 11.sp,
                color = color
            )
        }
    }
}
