package com.example

import com.example.data.network.SubscriptionValidator
import com.example.data.network.ValidationResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SubscriptionValidatorTest {

    @Test
    fun testValidAllowedUrls() {
        val url1 = "https://zerofee.ok-exor.ir/sub/token123"
        val res1 = SubscriptionValidator.validateUrl(url1)
        assertTrue(res1 is ValidationResult.Valid)
        assertEquals("https://zerofee.ok-exor.ir/sub/token123", (res1 as ValidationResult.Valid).normalizedUrl)

        val url2 = "https://panelsaz.mamadhp.ir:443/sub/uuid456?flag=1"
        val res2 = SubscriptionValidator.validateUrl(url2)
        assertTrue(res2 is ValidationResult.Valid)

        val url3 = "zerofee.ok-exor.ir/sub/my-sub"
        val res3 = SubscriptionValidator.validateUrl(url3)
        assertTrue(res3 is ValidationResult.Valid)
    }

    @Test
    fun testBlockedLocalUrls() {
        val localhostUrl = "http://localhost:8080/sub"
        val resLocalhost = SubscriptionValidator.validateUrl(localhostUrl)
        assertTrue(resLocalhost is ValidationResult.Invalid)

        val privateIpUrl = "http://192.168.1.1/sub"
        val resPrivate = SubscriptionValidator.validateUrl(privateIpUrl)
        assertTrue(resPrivate is ValidationResult.Invalid)

        val loopbackUrl = "http://127.0.0.1/sub"
        val resLoopback = SubscriptionValidator.validateUrl(loopbackUrl)
        assertTrue(resLoopback is ValidationResult.Invalid)
    }

    @Test
    fun testInvalidSchemeUrl() {
        val ftpUrl = "ftp://example.com/sub/token"
        val resFtp = SubscriptionValidator.validateUrl(ftpUrl)
        assertTrue(resFtp is ValidationResult.Invalid)
    }

    @Test
    fun testBlankUrl() {
        val res = SubscriptionValidator.validateUrl("   ")
        assertTrue(res is ValidationResult.Invalid)
    }
}
