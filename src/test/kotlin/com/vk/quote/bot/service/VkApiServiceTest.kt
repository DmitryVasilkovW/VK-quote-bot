package com.vk.quote.bot.service

import com.vk.quote.bot.config.VkBotConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.slf4j.Logger
import org.springframework.http.HttpHeaders
import org.springframework.http.RequestEntity
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate

class VkApiServiceTest {
    private lateinit var vkApiService: VkApiService
    private lateinit var restTemplate: RestTemplate
    private lateinit var config: VkBotConfig
    private lateinit var log: Logger

    @BeforeEach
    fun setUp() {
        config = mock(VkBotConfig::class.java)
        `when`(config.token).thenReturn("test_token")
        `when`(config.apiVersion).thenReturn("5.131")

        restTemplate = mock(RestTemplate::class.java)
        vkApiService = VkApiService(config)
        vkApiService.javaClass.getDeclaredField("restTemplate").apply {
            isAccessible = true
            set(vkApiService, restTemplate)
        }

        log = mock(Logger::class.java)
        vkApiService.javaClass.getDeclaredField("log").apply {
            isAccessible = true
            set(vkApiService, log)
        }
    }

    @Test
    fun `getUrl should return correct VK API url`() {
        val expectedUrl = "https://api.vk.com/method/messages.send?access_token=test_token&v=5.131"
        val actualUrl = vkApiService.javaClass.getDeclaredMethod("getUrl").apply { isAccessible = true }
            .invoke(vkApiService) as String

        assertThat(actualUrl).isEqualTo(expectedUrl)
    }

    @Test
    fun `getBody should return correct request body`() {
        val method = vkApiService.javaClass.getDeclaredMethod("getBody", String::class.java, String::class.java)
        method.isAccessible = true

        val body = method.invoke(vkApiService, "123", "Hello!") as LinkedMultiValueMap<*, *>

        assertThat(body["user_id"]).containsExactly("123")
        assertThat(body["message"]).containsExactly("Hello!")
        assertThat(body["random_id"]).isNotEmpty()
    }

    @Test
    fun `getHeaders should return correct headers`() {
        val method = vkApiService.javaClass.getDeclaredMethod("getHeaders")
        method.isAccessible = true

        val headers = method.invoke(vkApiService) as HttpHeaders

        assertThat(headers["Content-Type"]).containsExactly("application/x-www-form-urlencoded; charset=UTF-8")
    }

    @Test
    fun `tryToSend should log error when exception occurs`() {
        `when`(restTemplate.exchange(any(RequestEntity::class.java), eq(String::class.java)))
            .thenThrow(RuntimeException("API error"))

        vkApiService.sendMessage("123", "Hello!")

        verify(log).error("Error occurred: {}", "API error")
    }
}
