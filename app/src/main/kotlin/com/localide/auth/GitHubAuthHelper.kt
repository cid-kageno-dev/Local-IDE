package com.localide.auth

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject

class GitHubAuthHelper(private val context: Context) {

    private val httpClient = OkHttpClient()

    fun launchBrowserAuth() {
        val uri = Uri.parse(GitHubOAuthConfig.AUTH_URL)
        CustomTabsIntent.Builder()
            .setShowTitle(false)
            .build()
            .launchUrl(context, uri)
    }

    suspend fun exchangeCodeForSession(code: String): Result<UserSession> =
        withContext(Dispatchers.IO) {
            runCatching {
                val token = fetchAccessToken(code)
                val userJson = fetchUser(token)
                val email = userJson.optString("email").ifBlank {
                    fetchPrimaryEmail(token)
                }
                UserSession(
                    uid = "github_${userJson.getLong("id")}",
                    displayName = userJson.optString("name").ifBlank {
                        userJson.optString("login")
                    },
                    email = email,
                    photoUrl = userJson.optString("avatar_url"),
                    provider = AuthProvider.GITHUB
                )
            }
        }

    private fun fetchAccessToken(code: String): String {
        val body = FormBody.Builder()
            .add("client_id", GitHubOAuthConfig.CLIENT_ID)
            .add("client_secret", GitHubOAuthConfig.CLIENT_SECRET)
            .add("code", code)
            .add("redirect_uri", GitHubOAuthConfig.CALLBACK_URI)
            .build()
        val request = Request.Builder()
            .url(GitHubOAuthConfig.TOKEN_URL)
            .post(body)
            .header("Accept", "application/json")
            .build()
        val response = httpClient.newCall(request).execute()
        val json = JSONObject(response.body?.string() ?: error("Empty token response"))
        return json.getString("access_token")
    }

    private fun fetchUser(token: String): JSONObject {
        val request = Request.Builder()
            .url(GitHubOAuthConfig.USER_API)
            .header("Authorization", "Bearer $token")
            .header("Accept", "application/vnd.github+json")
            .build()
        val response = httpClient.newCall(request).execute()
        return JSONObject(response.body?.string() ?: error("Empty user response"))
    }

    private fun fetchPrimaryEmail(token: String): String {
        val request = Request.Builder()
            .url(GitHubOAuthConfig.USER_EMAILS_API)
            .header("Authorization", "Bearer $token")
            .header("Accept", "application/vnd.github+json")
            .build()
        val response = httpClient.newCall(request).execute()
        val emails = JSONArray(response.body?.string() ?: return "")
        for (i in 0 until emails.length()) {
            val obj = emails.getJSONObject(i)
            if (obj.optBoolean("primary") && obj.optBoolean("verified")) {
                return obj.optString("email")
            }
        }
        return ""
    }
}
