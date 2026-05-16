package com.localide.viewmodel

import android.app.Application
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.localide.auth.AuthProvider
import com.localide.auth.GitHubAuthHelper
import com.localide.auth.SessionManager
import com.localide.auth.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// -----------------------------------------------------------------------
// GOOGLE SETUP INSTRUCTIONS
// 1. Go to https://console.cloud.google.com/
// 2. Create a project (or select an existing one)
// 3. Enable "Google Sign-In" API (or Identity Toolkit API)
// 4. Under "APIs & Services" > "Credentials", create an OAuth 2.0 Client ID
//    - Application type: Web application
//    - Copy the resulting Client ID
// 5. Also create an Android OAuth 2.0 Client ID:
//    - Application type: Android
//    - Package name: com.localide
//    - SHA-1: run  ./gradlew signingReport  and copy the debug SHA-1
// 6. Replace the placeholder below with your Web Client ID
// -----------------------------------------------------------------------
private const val GOOGLE_WEB_CLIENT_ID = "YOUR_GOOGLE_WEB_CLIENT_ID.apps.googleusercontent.com"

sealed class AuthState {
    object Loading : AuthState()
    object Unauthenticated : AuthState()
    data class Authenticated(val session: UserSession) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)
    private val githubHelper = GitHubAuthHelper(application)

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        viewModelScope.launch {
            sessionManager.session.collect { saved ->
                _authState.value = if (saved != null) {
                    AuthState.Authenticated(saved)
                } else {
                    AuthState.Unauthenticated
                }
            }
        }
    }

    fun signInWithGoogle(activityContext: Context) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            runCatching {
                val credentialManager = CredentialManager.create(activityContext)
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(GOOGLE_WEB_CLIENT_ID)
                    .setAutoSelectEnabled(false)
                    .build()
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()
                val result = credentialManager.getCredential(
                    request = request,
                    context = activityContext
                )
                val credential = result.credential
                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val session = UserSession(
                        uid = "google_${googleCredential.id}",
                        displayName = googleCredential.displayName ?: googleCredential.givenName ?: "",
                        email = googleCredential.id,
                        photoUrl = googleCredential.profilePictureUri?.toString() ?: "",
                        provider = AuthProvider.GOOGLE
                    )
                    sessionManager.save(session)
                } else {
                    error("Unexpected credential type")
                }
            }.onFailure { e ->
                _authState.value = AuthState.Error(
                    when {
                        e.message?.contains("No credentials available") == true ->
                            "No Google account found on this device. Add one in Settings first."
                        e.message?.contains("Cancel") == true ||
                        e.message?.contains("cancel") == true ->
                            "Sign-in cancelled."
                        e.message?.contains("YOUR_GOOGLE_WEB_CLIENT_ID") == true ||
                        GOOGLE_WEB_CLIENT_ID.startsWith("YOUR_") ->
                            "Google Sign-In is not configured yet. See setup instructions in AuthViewModel.kt."
                        else -> "Google sign-in failed: ${e.message}"
                    }
                )
            }
        }
    }

    fun launchGitHubSignIn() {
        if (GitHubOAuthConfig_clientIdIsPlaceholder()) {
            _authState.value = AuthState.Error(
                "GitHub OAuth is not configured yet. See setup instructions in GitHubOAuthConfig.kt."
            )
            return
        }
        githubHelper.launchBrowserAuth()
    }

    fun handleGitHubCallback(code: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            githubHelper.exchangeCodeForSession(code)
                .onSuccess { session -> sessionManager.save(session) }
                .onFailure { e ->
                    _authState.value = AuthState.Error(
                        "GitHub sign-in failed: ${e.message}"
                    )
                }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            sessionManager.clear()
        }
    }

    fun dismissError() {
        _authState.value = AuthState.Unauthenticated
    }

    private fun GitHubOAuthConfig_clientIdIsPlaceholder(): Boolean =
        com.localide.auth.GitHubOAuthConfig.CLIENT_ID.startsWith("YOUR_")
}
