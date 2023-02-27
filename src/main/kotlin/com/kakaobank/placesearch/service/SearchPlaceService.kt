package com.kakaobank.placesearch.service

import com.kakaobank.placesearch.api.PlaceApi
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

@Service
class SearchPlaceService(private val context: ApplicationContext) {
    fun search(keyword: String): Flux<String> {
        return Flux.fromIterable(context.getBeansOfType(PlaceApi::class.java).values)
            .flatMap { api -> api.search(keyword) }
            .map { it.trim() }
            .map { it.filterNot { c -> c.isWhitespace() } }
            .sort()
    }
}