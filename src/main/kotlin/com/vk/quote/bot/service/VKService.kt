package com.vk.quote.bot.service

import com.vk.quote.bot.config.VkBotConfig
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.RequestEntity
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

@Service
class VkApiService(private val config: VkBotConfig) {
    private val restTemplate = RestTemplate()
    private val apiUrl = "https://api.vk.com/method"
    private val log = LoggerFactory.getLogger(VkApiService::class.java)

    fun sendMessage(userId: String, message: String) {
        val url = UriComponentsBuilder.fromHttpUrl("$apiUrl/messages.send")
            .queryParam("access_token", config.token)
            .queryParam("v", config.apiVersion)
            .toUriString()

        val body: MultiValueMap<String, String> = LinkedMultiValueMap()
        body.add("user_id", userId)
        body.add("message", message)
        body.add("random_id", System.currentTimeMillis().toString())

        val headers = HttpHeaders()
        headers.set("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")

        val requestEntity = RequestEntity
            .post(url)
            .headers(headers)
            .body(body)

        try {
            val response: ResponseEntity<String> = restTemplate.exchange(requestEntity, String::class.java)
            log.info("Response: ${response.body}")
        } catch (e: Exception) {
            log.error("Error occurred: ${e.message}")
        }
    }
}
