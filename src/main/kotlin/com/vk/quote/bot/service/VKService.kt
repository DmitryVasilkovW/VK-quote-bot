package com.vk.quote.bot.service

import com.vk.quote.bot.config.VkBotConfig
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

@Service
class VkApiService(private val config: VkBotConfig) {
    private val restTemplate = RestTemplate()
    private val apiUrl = "https://api.vk.com/method"

    fun sendMessage(userId: String, message: String) {
        val url = UriComponentsBuilder.fromHttpUrl("$apiUrl/messages.send")
            .queryParam("access_token", config.token)
            .queryParam("user_id", userId)
            .queryParam("message", message)
            .queryParam("random_id", System.currentTimeMillis())
            .queryParam("v", config.apiVersion)
            .toUriString()

        restTemplate.getForObject(url, String::class.java)
    }
}
