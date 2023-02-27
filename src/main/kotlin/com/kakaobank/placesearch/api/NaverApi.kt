package com.kakaobank.placesearch.api

import com.kakaobank.placesearch.api.dto.NaverResult
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToFlux
import reactor.core.publisher.Flux

@Component
class NaverApi : PlaceApi {
    //TODO API KEY property로 옮길 것
    override fun search(keyword: String): Flux<String> {
        return WebClient
            .create("https://openapi.naver.com")
            .get()
            .uri {
                it.path("/v1/search/local.json")
                    .queryParam("query", keyword)
                    .queryParam("display", 5)
                    .build()
            }
            .headers {
                it.add("X-Naver-Client-Id", "Z1hZm9s4K0BKKeFDQ313")
                it.add("X-Naver-Client-Secret", "7yCeTPJvfy")
            }
            .retrieve()
            .bodyToFlux<NaverResult>()
            .flatMap { Flux.fromIterable(it.items) }
            .map { it.placeName }

    }
}