package com.vk.quote.bot.exception.model

enum class VkApiErrorType(val message: String) {
    InvalidResponse("Invalid VK API response"),
    ServerMissing("Server field missing"),
    KeyMissing("Key field missing"),
    TSMissing("TS field missing"),
}
