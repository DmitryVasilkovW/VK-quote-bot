package com.vk.quote.bot.service

import com.vk.quote.bot.config.VkBotConfig
import com.vk.quote.bot.exception.VkApiException
import com.vk.quote.bot.exception.model.VkApiErrorType
import com.vk.quote.bot.model.VkApiEndpoint
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
        log.info(LOG_START_POLLING, server)

        while (true) {
            val pollUrl = getPollUrl(server, key, ts)

            runCatching {
                val pollResponse = restTemplate.getForObject(pollUrl, Map::class.java)
                ts = tryToUpdateTs(ts, pollResponse)
                val messages = getMessages(pollResponse)
                handleAllMessages(messages)
            }.onFailure {
                log.error(LOG_POLLING_ERROR, it.message)
                Thread.sleep(RETRY_DELAY_MS)
            }
        }
    }

    private fun getPollUrl(server: String, key: String, ts: String): String {
        return UriComponentsBuilder.fromUriString(server)
            .queryParam(POLL_ACT_PARAM, POLL_ACT_VALUE)
            .queryParam(POLL_KEY_PARAM, key)
            .queryParam(POLL_TS_PARAM, ts)
            .queryParam(POLL_WAIT_PARAM, POLL_WAIT_VALUE)
            .build()
            .toUriString()
    }

    private fun tryToUpdateTs(oldTs: String, pollResponse: Map<*, *>?): String {
        return (pollResponse?.get(TS_FIELD) as? String) ?: oldTs
    }

    private fun getMessages(pollResponse: Map<*, *>?): List<Map<String, Any>> {
        return (pollResponse?.get(UPDATES_FIELD) as? List<*>?)
            ?.filterIsInstance<Map<String, Any>>()
            ?: emptyList()
    }

    private fun handleAllMessages(updates: List<Map<String, Any>>) {
        updates.forEach { update ->
            if (update[TYPE_FIELD] == MESSAGE_NEW_TYPE) {
                handleNewMessage(update)
            }
        }
    }

    private fun handleNewMessage(update: Map<String, Any>) {
        val message = (update[OBJECT_FIELD] as? Map<*, *>)?.get(MESSAGE_FIELD) as? Map<*, *> ?: return
        val userId = message[FROM_ID_FIELD]?.toString() ?: return
        val text = message[TEXT_FIELD]?.toString() ?: return

        log.info(LOG_RECEIVED_MESSAGE, userId, text)
        vkApiService.sendMessage(userId, MESSAGE_RESPONSE_TEMPLATE.format(text))
    }

    private fun getLongPollServerData(): Triple<String, String, String> {
        val url = getLongPollServerUrl()

        val response = restTemplate.getForObject(url, Map::class.java)
        val responseData = tryToGetResponseData(response)
        val server = tryToGetServer(responseData)

        val key = responseData[KEY_FIELD]?.toString() ?: throw VkApiException(VkApiErrorType.KeyMissing)
        val ts = responseData[TS_FIELD]?.toString() ?: throw VkApiException(VkApiErrorType.TSMissing)

        return Triple(server, key, ts)
    }

    private fun tryToGetServer(responseData: Map<*, *>): String {
        return responseData[SERVER_FIELD]?.toString()?.let {
            if (it.startsWith(HTTP_PREFIX)) it else "$HTTPS_PREFIX$it"
        } ?: throw VkApiException(VkApiErrorType.ServerMissing)
    }

    private fun tryToGetResponseData(response: Map<*, *>?): Map<*, *> {
        return (response?.get(RESPONSE_FIELD) as? Map<*, *>)
            ?: throw VkApiException(VkApiErrorType.InvalidResponse)
    }

    private fun getLongPollServerUrl(): String {
        return UriComponentsBuilder.fromUriString(VkApiEndpoint.GET_LONG_POLL_SERVER.url)
            .queryParam(ACCESS_TOKEN_PARAM, config.token)
            .queryParam(GROUP_ID_PARAM, config.groupId)
            .queryParam(VERSION_PARAM, config.apiVersion)
            .build()
            .toUriString()
    }

    companion object {
        private const val LOG_START_POLLING = "Starting Long Polling at: {}"
        private const val LOG_POLLING_ERROR = "Polling error: {}"
        private const val LOG_RECEIVED_MESSAGE = "Received message from {}: {}"

        private const val ACCESS_TOKEN_PARAM = "access_token"
        private const val GROUP_ID_PARAM = "group_id"
        private const val VERSION_PARAM = "v"

        private const val POLL_ACT_PARAM = "act"
        private const val POLL_ACT_VALUE = "a_check"
        private const val POLL_KEY_PARAM = "key"
        private const val POLL_TS_PARAM = "ts"
        private const val POLL_WAIT_PARAM = "wait"
        private const val POLL_WAIT_VALUE = 25

        private const val RESPONSE_FIELD = "response"
        private const val SERVER_FIELD = "server"
        private const val KEY_FIELD = "key"
        private const val TS_FIELD = "ts"
        private const val UPDATES_FIELD = "updates"
        private const val TYPE_FIELD = "type"
        private const val MESSAGE_NEW_TYPE = "message_new"
        private const val OBJECT_FIELD = "object"
        private const val MESSAGE_FIELD = "message"
        private const val FROM_ID_FIELD = "from_id"
        private const val TEXT_FIELD = "text"

        private const val MESSAGE_RESPONSE_TEMPLATE = "Вы сказали: %s"
        private const val HTTPS_PREFIX = "https://"
        private const val HTTP_PREFIX = "http"
        private const val RETRY_DELAY_MS = 5000L
    }
}
