package com.kakaobank.placesearch.service

import com.kakaobank.placesearch.log
import org.springframework.stereotype.Component

@Component
class EmergencyNotificationService {
    fun sendSlack(message: String) {
        log().info("[${this::class.simpleName}] Send slack message : $message")
    }
}