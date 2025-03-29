package com.vk.quote.bot.service

import com.vk.quote.bot.config.VkBotConfig
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.RequestEntity
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

@Service
class VkApiService(private val config: VkBotConfig) {

    private val log = LoggerFactory.getLogger(VkApiService::class.java)
    private val apiUrl = "https://api.vk.com/method"

    private val restTemplate: RestTemplate = RestTemplate()

    fun sendMessage(userId: String, message: String) {
        val url = UriComponentsBuilder.fromUriString("$apiUrl/messages.send")
            .queryParam("access_token", config.token)
            .queryParam("v", config.apiVersion)
            .build()
            .toUriString()

        val body = LinkedMultiValueMap<String, String>().apply {
            add("user_id", userId)
            add("message", message)
            add("random_id", System.currentTimeMillis().toString())
        }

        val headers = HttpHeaders().apply {
            set("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
        }

        val requestEntity = RequestEntity.post(url).headers(headers).body(body)

        runCatching {
            val response: ResponseEntity<String> = restTemplate.exchange(requestEntity, String::class.java)
            log.info("Response: ${response.body}")
        }.onFailure { e ->
            log.error("Error occurred: ${e.message}")
        }
    }
}
