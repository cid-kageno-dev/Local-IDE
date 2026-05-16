package com.localide.auth

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.authDataStore by preferencesDataStore(name = "auth_session")

data class UserSession(
    val uid: String,
    val displayName: String,
    val email: String,
    val photoUrl: String,
    val provider: AuthProvider
)

enum class AuthProvider { GOOGLE, GITHUB }

class SessionManager(private val context: Context) {

    companion object {
        private val KEY_UID = stringPreferencesKey("uid")
        private val KEY_NAME = stringPreferencesKey("display_name")
        private val KEY_EMAIL = stringPreferencesKey("email")
        private val KEY_PHOTO = stringPreferencesKey("photo_url")
        private val KEY_PROVIDER = stringPreferencesKey("provider")
    }

    val session: Flow<UserSession?> = context.authDataStore.data.map { prefs ->
        val uid = prefs[KEY_UID] ?: return@map null
        UserSession(
            uid = uid,
            displayName = prefs[KEY_NAME] ?: "",
            email = prefs[KEY_EMAIL] ?: "",
            photoUrl = prefs[KEY_PHOTO] ?: "",
            provider = prefs[KEY_PROVIDER]?.let {
                runCatching { AuthProvider.valueOf(it) }.getOrNull()
            } ?: return@map null
        )
    }

    suspend fun save(session: UserSession) {
        context.authDataStore.edit { prefs ->
            prefs[KEY_UID] = session.uid
            prefs[KEY_NAME] = session.displayName
            prefs[KEY_EMAIL] = session.email
            prefs[KEY_PHOTO] = session.photoUrl
            prefs[KEY_PROVIDER] = session.provider.name
        }
    }

    suspend fun clear() {
        context.authDataStore.edit { it.clear() }
    }
}
