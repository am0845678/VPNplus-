package com.example

import com.example.data.network.SubscriptionParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SubscriptionParserTest {

    @Test
    fun testParseVlessReality() {
        val vlessUri = "vless://a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d@1.2.3.4:443?security=reality&type=tcp&sni=speed.cloudflare.com&fp=chrome&pbk=a8z_fake_public_key&sid=12345678#Pars+Ping+VLESS+Reality"
        val configs = SubscriptionParser.parseSubscription(vlessUri)
        assertEquals(1, configs.size)
        val cfg = configs[0]
        assertEquals("VLESS", cfg.protocol)
        assertEquals("1.2.3.4", cfg.serverAddress)
        assertEquals(443, cfg.serverPort)
        assertEquals("reality", cfg.security)
        assertEquals("a8z_fake_public_key", cfg.realityPublicKey)
        assertEquals("12345678", cfg.realityShortId)
    }

    @Test
    fun testParseTrojan() {
        val trojanUri = "trojan://mypassword123@trojan.parsping.net:443?security=tls&sni=trojan.parsping.net&type=ws#Pars+Ping+Trojan"
        val configs = SubscriptionParser.parseSubscription(trojanUri)
        assertEquals(1, configs.size)
        val cfg = configs[0]
        assertEquals("Trojan", cfg.protocol)
        assertEquals("trojan.parsping.net", cfg.serverAddress)
        assertEquals(443, cfg.serverPort)
        assertEquals("mypassword123", cfg.uuidOrPassword)
        assertEquals("tls", cfg.security)
        assertEquals("ws", cfg.networkType)
    }

    @Test
    fun testParseWireGuardConf() {
        val wgConf = """
            [Interface]
            PrivateKey = aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa=
            Address = 10.0.0.2/32
            DNS = 1.1.1.1
            MTU = 1420

            [Peer]
            PublicKey = bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb=
            Endpoint = wg.parsping.net:51820
            AllowedIPs = 0.0.0.0/0
            PersistentKeepalive = 25
        """.trimIndent()

        val entity = SubscriptionParser.parseWireGuardConf(wgConf, "Pars Ping WireGuard Main")
        assertNotNull(entity)
        assertEquals("WireGuard", entity?.protocol)
        assertEquals("wg.parsping.net", entity?.serverAddress)
        assertEquals(51820, entity?.serverPort)
        assertTrue(entity?.isSupported == true)
    }
}
