package com.vk.quote.bot.service

import com.vk.quote.bot.config.VkBotConfig
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

@Service
class VkLongPollService(
    private val vkApiService: VkApiService,
    private val config: VkBotConfig
) {
    private val log: Logger = LoggerFactory.getLogger(VkLongPollService::class.java)
    private val restTemplate: RestTemplate = RestTemplate()

    fun startPolling() {
        val (server, key, initialTs) = getLongPollServerData()

        var ts = initialTs
        log.info("Starting Long Polling at: $server")

        while (true) {
            val pollUrl = buildPollUrl(server, key, ts)

            runCatching {
                val pollResponse = restTemplate.getForObject(pollUrl, Map::class.java)
                ts = (pollResponse?.get("ts") as? String) ?: ts
                val updates = (pollResponse?.get("updates") as? List<*>)
                    ?.filterIsInstance<Map<String, Any>>()
                    ?: emptyList()


                updates.forEach { update ->
                    if (update["type"] == "message_new") {
                        handleNewMessage(update)
                    }
                }
            }.onFailure {
                log.error("Polling error: ${it.message}")
                Thread.sleep(5000)
            }
        }
    }

    private fun getLongPollServerData(): Triple<String, String, String> {
        val url = UriComponentsBuilder.fromUriString("https://api.vk.com/method/groups.getLongPollServer")
            .queryParam("access_token", config.token)
            .queryParam("group_id", config.groupId)
            .queryParam("v", config.apiVersion)
            .build()
            .toUriString()

        val response = restTemplate.getForObject(url, Map::class.java)
        val responseData = (response?.get("response") as? Map<*, *>)
            ?: throw RuntimeException("Invalid VK API response")

        val server = responseData["server"]?.toString()?.let {
            if (it.startsWith("http")) it else "https://$it"
        } ?: throw RuntimeException("Server field missing")

        val key = responseData["key"]?.toString() ?: throw RuntimeException("Key field missing")
        val ts = responseData["ts"]?.toString() ?: throw RuntimeException("TS field missing")

        return Triple(server, key, ts)
    }

    private fun buildPollUrl(server: String, key: String, ts: String): String {
        return UriComponentsBuilder.fromUriString(server)
            .queryParam("act", "a_check")
            .queryParam("key", key)
            .queryParam("ts", ts)
            .queryParam("wait", 25)
            .build()
            .toUriString()
    }

    private fun handleNewMessage(update: Map<String, Any>) {
        val message = (update["object"] as? Map<*, *>)?.get("message") as? Map<*, *> ?: return
        val userId = message["from_id"]?.toString() ?: return
        val text = message["text"]?.toString() ?: return

        log.info("Received message from $userId: $text")
        vkApiService.sendMessage(userId, "Вы сказали: $text")
    }
}
