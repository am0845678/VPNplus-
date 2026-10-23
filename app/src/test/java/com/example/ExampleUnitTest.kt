package com.example

import com.wireguard.config.Config
import com.wireguard.crypto.Key
import com.wireguard.crypto.KeyPair
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun wireGuardKeyPairGeneration() {
    val keyPair = KeyPair()
    val privateKey = keyPair.privateKey.toBase64()
    val publicKey = keyPair.publicKey.toBase64()
    assertNotNull(privateKey)
    assertNotNull(publicKey)
    assertEquals(44, privateKey.length)
    assertEquals(44, publicKey.length)
    assertTrue(privateKey.endsWith("="))
    assertTrue(publicKey.endsWith("="))
  }

  @Test
  fun wireGuardConfigParsing() {
    val keyPair = KeyPair()
    val peerKeyPair = KeyPair()
    val ini = """
        [Interface]
        PrivateKey = ${keyPair.privateKey.toBase64()}
        Address = 10.0.0.2/32
        DNS = 1.1.1.1, 8.8.8.8
        MTU = 1420

        [Peer]
        PublicKey = ${peerKeyPair.publicKey.toBase64()}
        Endpoint = 198.51.100.1:51820
        AllowedIPs = 0.0.0.0/0, ::/0
        PersistentKeepalive = 25
    """.trimIndent()

    val config = Config.parse(ByteArrayInputStream(ini.toByteArray(StandardCharsets.UTF_8)))
    assertNotNull(config)
    assertEquals(1, config.peers.size)
    assertEquals("198.51.100.1:51820", config.peers[0].endpoint.get().toString())
    assertEquals(1420, config.`interface`.mtu.get().toInt())
  }

  @Test
  fun subscriptionParserParsesWireGuardIni() {
    val keyPair = KeyPair()
    val peerKeyPair = KeyPair()
    val ini = """
        [Interface]
        PrivateKey = ${keyPair.privateKey.toBase64()}
        Address = 10.14.0.2/32
        DNS = 1.1.1.1
        MTU = 1420

        [Peer]
        PublicKey = ${peerKeyPair.publicKey.toBase64()}
        Endpoint = 198.51.100.55:51820
        AllowedIPs = 0.0.0.0/0
        PersistentKeepalive = 25
    """.trimIndent()

    val parsed = com.example.data.network.SubscriptionParser.parseSubscription(ini)
    assertEquals(1, parsed.size)
    val entity = parsed[0]
    assertEquals("WireGuard", entity.protocol)
    assertEquals("198.51.100.55", entity.serverAddress)
    assertEquals(51820, entity.serverPort)
    assertTrue(entity.isSupported)

    // Verify entity can be converted back to a valid official Config
    val wgConfig = com.example.vpn.WireGuardTunnelManager.createConfigFromEntity(entity)
    assertNotNull(wgConfig)
    assertEquals(1, wgConfig.peers.size)
    assertEquals("198.51.100.55:51820", wgConfig.peers[0].endpoint.get().toString())
  }

  @Test
  fun sampleWireGuardConfigsAreValid() {
    val samples = com.example.data.network.SubscriptionParser.getSampleWireGuardConfigs()
    assertTrue(samples.isNotEmpty())
    for (sample in samples) {
      assertEquals("WireGuard", sample.protocol)
      assertTrue(sample.isSupported)
      val wgConfig = com.example.vpn.WireGuardTunnelManager.createConfigFromEntity(sample)
      assertNotNull(wgConfig)
      assertNotNull(wgConfig.`interface`.keyPair.privateKey)
      assertEquals(1, wgConfig.peers.size)
    }
  }

  @Test
  fun parseWireGuardConfWithFileName() {
    val keyPair = KeyPair()
    val peerKeyPair = KeyPair()
    val ini = """
        [Interface]
        PrivateKey = ${keyPair.privateKey.toBase64()}
        Address = 10.0.0.2/32
        DNS = 1.1.1.1

        [Peer]
        PublicKey = ${peerKeyPair.publicKey.toBase64()}
        Endpoint = de-fast.wg.server:51820
        AllowedIPs = 0.0.0.0/0
    """.trimIndent()

    val parsed = com.example.data.network.SubscriptionParser.parseWireGuardConf(ini, "Pars Ping WireGuard (Germany-Frankfurt)")
    assertNotNull(parsed)
    assertEquals("Pars Ping WireGuard (Germany-Frankfurt)", parsed!!.name)
    assertEquals("WireGuard", parsed.protocol)
    assertEquals("de-fast.wg.server", parsed.serverAddress)
    assertEquals(51820, parsed.serverPort)
  }
}
