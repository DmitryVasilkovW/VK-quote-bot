package com.vk.quote.bot.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "vk")
class VkBotConfig {
    lateinit var token: String
    lateinit var groupId: String
    lateinit var apiVersion: String
}
