package com.vk.quote.bot.exception

import com.vk.quote.bot.exception.model.VkApiErrorType

class VkApiException(errorType: VkApiErrorType): Exception(errorType.message)
