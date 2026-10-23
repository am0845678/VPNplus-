package com.example.data.network

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.IOException
import java.util.Locale
import java.util.concurrent.TimeUnit

sealed class ValidationResult {
    data class Valid(val normalizedUrl: String) : ValidationResult()
    data class Invalid(val message: String) : ValidationResult()
}

object SubscriptionValidator {

    private val BLOCKED_HOSTS = setOf(
        "localhost",
        "127.0.0.1",
        "0.0.0.0",
        "::1"
    )

    const val RESTRICTED_APP_ERROR = "لینک اشتراک نامعتبر است"

    /**
     * Validates if the given URL is a legitimate, publicly accessible HTTP/HTTPS subscription link.
     */
    fun validateUrl(rawInput: String?): ValidationResult {
        if (rawInput.isNullOrBlank()) {
            return ValidationResult.Invalid("لطفاً لینک اشتراک را وارد کنید")
        }

        val trimmed = rawInput.trim()

        val httpUrl: HttpUrl = trimmed.toHttpUrlOrNull() ?: run {
            if (trimmed.contains("://")) {
                return ValidationResult.Invalid("پروتکل لینک باید HTTP یا HTTPS باشد")
            }
            val withScheme = "https://$trimmed"
            withScheme.toHttpUrlOrNull() ?: return ValidationResult.Invalid("آدرس لینک اشتراک نامعتبر است")
        }

        // Scheme verification: allow HTTPS and HTTP
        val scheme = httpUrl.scheme.lowercase(Locale.ROOT)
        if (scheme != "https" && scheme != "http") {
            return ValidationResult.Invalid("پروتکل لینک باید HTTP یا HTTPS باشد")
        }

        val host = httpUrl.host.lowercase(Locale.ROOT)
        if (BLOCKED_HOSTS.contains(host) || host.startsWith("192.168.") || host.startsWith("10.") || host.startsWith("172.16.")) {
            return ValidationResult.Invalid("آدرس‌های شبکه محلی مجاز نمی‌باشند")
        }

        return ValidationResult.Valid(httpUrl.toString())
    }

    /**
     * Creates an OkHttpClient with a timeout and safe redirect interceptor.
     */
    fun createSafeHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .addInterceptor(SafeRedirectInterceptor())
            .build()
    }

    private class SafeRedirectInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            val host = request.url.host.lowercase(Locale.ROOT)
            if (BLOCKED_HOSTS.contains(host)) {
                throw IOException("اتصال به این میزبان مجاز نیست")
            }
            return chain.proceed(request)
        }
    }
}
