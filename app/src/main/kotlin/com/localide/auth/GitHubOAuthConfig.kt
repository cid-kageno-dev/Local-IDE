package com.localide.auth

object GitHubOAuthConfig {
    // -----------------------------------------------------------------------
    // SETUP INSTRUCTIONS
    // 1. Go to https://github.com/settings/developers
    // 2. Click "New OAuth App"
    // 3. Set Homepage URL to anything (e.g. https://example.com)
    // 4. Set Authorization callback URL to:  localide://auth
    // 5. Copy the Client ID below
    // 6. Generate a Client Secret and paste it below
    // -----------------------------------------------------------------------

    const val CLIENT_ID = "YOUR_GITHUB_CLIENT_ID"
    const val CLIENT_SECRET = "YOUR_GITHUB_CLIENT_SECRET"

    const val CALLBACK_SCHEME = "localide"
    const val CALLBACK_HOST = "auth"
    val CALLBACK_URI = "$CALLBACK_SCHEME://$CALLBACK_HOST"

    val AUTH_URL: String
        get() = "https://github.com/login/oauth/authorize" +
            "?client_id=$CLIENT_ID" +
            "&redirect_uri=$CALLBACK_URI" +
            "&scope=read:user,user:email"

    const val TOKEN_URL = "https://github.com/login/oauth/access_token"
    const val USER_API = "https://api.github.com/user"
    const val USER_EMAILS_API = "https://api.github.com/user/emails"
}
