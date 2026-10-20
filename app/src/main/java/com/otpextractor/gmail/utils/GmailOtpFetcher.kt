package com.otpextractor.secureotp.utils

import android.text.Html
import android.util.Base64
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class GmailFetchResult(
    val accountEmail: String,
    val otp: String?
)

object GmailOtpFetcher {

    private const val GMAIL_API = "https://gmail.googleapis.com/gmail/v1/users/me"
    private const val MAX_MESSAGES_TO_SCAN = 20

    fun fetch(accessToken: String, fetchLatestOtp: Boolean): GmailFetchResult {
        val profile = getJson("$GMAIL_API/profile", accessToken)
        val email = profile.optString("emailAddress")
        if (email.isBlank()) {
            throw IllegalStateException("Google did not return a Gmail address")
        }

        return GmailFetchResult(
            accountEmail = email,
            otp = if (fetchLatestOtp) findLatestOtp(accessToken) else null
        )
    }

    private fun findLatestOtp(accessToken: String): String? {
        val query = URLEncoder.encode("in:inbox newer_than:1d", "UTF-8")
        val messages = getJson(
            "$GMAIL_API/messages?maxResults=$MAX_MESSAGES_TO_SCAN&q=$query",
            accessToken
        ).optJSONArray("messages") ?: return null

        for (index in 0 until messages.length()) {
            val messageId = messages.optJSONObject(index)?.optString("id")
                ?.takeIf { it.isNotBlank() } ?: continue
            val message = getJson("$GMAIL_API/messages/$messageId?format=full", accessToken)
            val text = StringBuilder().apply {
                append(message.optString("snippet"))
                append(' ')
                appendTextParts(message.optJSONObject("payload"))
            }.toString()
            OtpExtractor.extractOtp(text)?.let { return it }
        }

        return null
    }

    private fun StringBuilder.appendTextParts(part: JSONObject?) {
        if (part == null) return

        val mimeType = part.optString("mimeType")
        val encodedBody = part.optJSONObject("body")?.optString("data")
        if (mimeType == "text/plain" || mimeType == "text/html") {
            decodeBody(encodedBody)?.let { body ->
                append(' ')
                append(if (mimeType == "text/html") Html.fromHtml(body, Html.FROM_HTML_MODE_LEGACY) else body)
            }
        }

        val parts = part.optJSONArray("parts") ?: return
        for (index in 0 until parts.length()) {
            appendTextParts(parts.optJSONObject(index))
        }
    }

    private fun decodeBody(encodedBody: String?): String? {
        if (encodedBody.isNullOrBlank()) return null
        return try {
            String(Base64.decode(encodedBody, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING), Charsets.UTF_8)
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    private fun getJson(url: String, accessToken: String): JSONObject {
        val connection = URL(url).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 15_000
            connection.readTimeout = 15_000
            connection.setRequestProperty("Authorization", "Bearer $accessToken")
            connection.setRequestProperty("Accept", "application/json")

            val status = connection.responseCode
            if (status !in 200..299) {
                if (status == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    throw IllegalStateException("Gmail access expired. Reconnect Gmail and try again.")
                }
                throw IllegalStateException("Gmail returned an error ($status). Check your connection and try again.")
            }

            return connection.inputStream.bufferedReader(Charsets.UTF_8).use { reader ->
                JSONObject(reader.readText())
            }
        } finally {
            connection.disconnect()
        }
    }
}
