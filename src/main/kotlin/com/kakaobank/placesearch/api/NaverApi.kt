package com.kakaobank.placesearch.api

import com.kakaobank.placesearch.dto.NaverResponse
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono

@Component
class NaverApi : PlaceApi {
    override fun priority() = 10

    //TODO API KEY property로 옮길 것
    override fun search(keyword: String, page:Int): Mono<List<String>> {
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
            .bodyToMono<NaverResponse>()
            .map { it.items }
            .map { it.map { item -> item.placeName } }

    }
}