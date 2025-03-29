package com.vk.quote.bot

import com.vk.quote.bot.service.VkLongPollService
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class VkBotApplication(private val longPollService: VkLongPollService) : CommandLineRunner {
    override fun run(vararg args: String?) {
        longPollService.startPolling()
    }
}

fun main(args: Array<String>) {
    runApplication<VkBotApplication>(*args)
}