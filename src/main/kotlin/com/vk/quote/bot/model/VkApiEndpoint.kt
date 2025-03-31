package com.vk.quote.bot.model

enum class VkApiEndpoint(val url: String) {
    BASE_URL("https://api.vk.com/method"),
    GET_LONG_POLL_SERVER("${BASE_URL.url}/groups.getLongPollServer"),
}
