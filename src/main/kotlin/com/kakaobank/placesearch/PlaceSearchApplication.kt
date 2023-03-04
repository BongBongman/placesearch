package com.kakaobank.placesearch

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories

@SpringBootApplication
@EnableAspectJAutoProxy
@EnableR2dbcRepositories
class PlaceSearchApplication

fun main(args: Array<String>) {
    runApplication<PlaceSearchApplication>(*args) {
        webApplicationType = WebApplicationType.REACTIVE
    }
}

inline fun <reified T> T.log(): Logger {
    return LoggerFactory.getLogger(T::class.java)
}