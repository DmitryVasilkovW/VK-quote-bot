package com.vk.quote.bot.service

import com.vk.quote.bot.config.VkBotConfig
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

@Service
class VkApiService(private val config: VkBotConfig) {
    companion object {
        private const val USER_ID_PARAM = "user_id"
        private const val MESSAGE_PARAM = "message"
        private const val RANDOM_ID_PARAM = "random_id"

        private const val ACCESS_TOKEN_PARAM = "access_token"
        private const val VERSION_PARAM = "v"

        private const val API_URL = "https://api.vk.com/method"
        private const val MESSAGES_SEND_ENDPOINT = "/messages.send"
        private const val CONTENT_TYPE_HEADER = "Content-Type"
        private const val CONTENT_TYPE_VALUE = "application/x-www-form-urlencoded; charset=UTF-8"
        private const val LOG_RESPONSE = "Response: {}"
        private const val LOG_ERROR = "Error occurred: {}"
    }

    private val log = LoggerFactory.getLogger(VkApiService::class.java)

    private val restTemplate: RestTemplate = RestTemplate()

    fun sendMessage(userId: String, message: String) {
        val url = getUrl()
        val body = getBody(userId, message)
        val headers = getHeaders()

        val requestEntity = RequestEntity
            .post(url)
            .headers(headers)
            .body(body)

        tryToSend(requestEntity)
    }

    private fun getUrl(): String {
        return UriComponentsBuilder.fromUriString("$API_URL$MESSAGES_SEND_ENDPOINT")
            .queryParam(ACCESS_TOKEN_PARAM, config.token)
            .queryParam(VERSION_PARAM, config.apiVersion)
            .build()
            .toUriString()
    }

    private fun getBody(userId: String, message: String): LinkedMultiValueMap<String, String> {
        return LinkedMultiValueMap<String, String>().apply {
            add(USER_ID_PARAM, userId)
            add(MESSAGE_PARAM, message)
            add(RANDOM_ID_PARAM, System.currentTimeMillis().toString())
        }
    }

    private fun getHeaders(): HttpHeaders {
        return HttpHeaders().apply {
            set(CONTENT_TYPE_HEADER, CONTENT_TYPE_VALUE)
        }
    }

    private fun tryToSend(requestEntity: RequestEntity<*>) {
        runCatching {
            val response = restTemplate.exchange(requestEntity, String::class.java)
            log.info(LOG_RESPONSE, response.body)
        }.onFailure { e ->
            log.error(LOG_ERROR, e.message)
        }
    }
}
