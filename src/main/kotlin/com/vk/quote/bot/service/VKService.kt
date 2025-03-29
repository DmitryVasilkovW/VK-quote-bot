package com.vk.quote.bot.service

import com.vk.quote.bot.config.VkBotConfig
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class VkApiService(private val config: VkBotConfig) {
    private val restTemplate = RestTemplate()
    private val apiUrl = "https://api.vk.com/method"

    fun sendMessage(userId: String, message: String) {
        val url = "$apiUrl/messages.send"
        val params: MultiValueMap<String, String> = LinkedMultiValueMap()
        params.add("access_token", config.token)
        params.add("user_id", userId)
        params.add("message", message)
        params.add("random_id", System.currentTimeMillis().toString())
        params.add("v", config.apiVersion)

        // Отправляем POST-запрос с параметрами в теле запроса
        restTemplate.postForObject(url, params, String::class.java)
    }
}
