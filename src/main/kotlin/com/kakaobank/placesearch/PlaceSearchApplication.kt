package com.kakaobank.placesearch

import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PlaceSearchApplication

fun main(args: Array<String>) {
    runApplication<PlaceSearchApplication>(*args) {
        webApplicationType = WebApplicationType.REACTIVE
    }
}
