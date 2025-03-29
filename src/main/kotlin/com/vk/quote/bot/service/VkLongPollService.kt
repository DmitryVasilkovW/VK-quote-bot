package com.vk.quote.bot.service

import com.vk.quote.bot.config.VkBotConfig
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

@Service
class VkLongPollService(
    private val vkApiService: VkApiService,
    private val config: VkBotConfig
) {
    private val log = LoggerFactory.getLogger(VkLongPollService::class.java)
    private val restTemplate = RestTemplate()

    fun startPolling() {
        val initUrl = UriComponentsBuilder.fromHttpUrl("https://api.vk.com/method/groups.getLongPollServer")
            .queryParam("access_token", config.token)
            .queryParam("group_id", config.groupId)
            .queryParam("v", config.apiVersion)
            .toUriString()

        val response = restTemplate.getForObject(initUrl, Map::class.java)
        log.info("Response from VK API: $response")
        val responseData = response?.get("response") as? Map<String, Any>
        if (responseData == null) {
            throw RuntimeException("Некорректный ответ от VK API")
        }

        var server = responseData["server"] as? String ?: throw RuntimeException("Поле server отсутствует")
        if (server.isBlank()) {
            throw RuntimeException("Поле server пустое")
        }
        if (!server.startsWith("http")) {
            server = "https://$server"
        }

        val key = responseData["key"] as? String ?: throw RuntimeException("Поле key отсутствует")
        var ts = responseData["ts"] as? String ?: throw RuntimeException("Поле ts отсутствует")

        log.info("Long Poll server URL: $server")

        while (true) {
            try {
                val pollUrl = UriComponentsBuilder.fromHttpUrl(server)
                    .queryParam("act", "a_check")
                    .queryParam("key", key)
                    .queryParam("ts", ts)
                    .queryParam("wait", 25)
                    .toUriString()

                log.info("Polling URL: $pollUrl")

                val pollResponse = restTemplate.getForObject(pollUrl, Map::class.java)
                val updates = pollResponse?.get("updates") as? List<Map<String, Any>>
                ts = pollResponse?.get("ts") as? String ?: ts

                updates?.forEach { update ->
                    val type = update["type"]
                    if (type == "message_new") {
                        val message = (update["object"] as Map<String, Any>)["message"] as Map<String, Any>
                        val userId = message["from_id"].toString()
                        val text = message["text"].toString()

                        log.info("Received message from $userId: $text")
                        vkApiService.sendMessage(userId, "Вы сказали: $text")
                    }
                }
            } catch (e: Exception) {
                log.error("Error in polling: ${e.message}")
                throw RuntimeException("Error in polling: ${e.message}")
            }
        }
    }
}
