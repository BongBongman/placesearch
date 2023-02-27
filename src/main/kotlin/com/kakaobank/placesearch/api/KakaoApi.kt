package com.kakaobank.placesearch.api

import com.kakaobank.placesearch.api.dto.KakaoResult
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToFlux
import reactor.core.publisher.Flux

@Component
class KakaoApi : PlaceApi {
    //TODO API KEY property로 옮길 것
    override fun search(keyword: String): Flux<String> {
        return WebClient
            .create("https://dapi.kakao.com")
            .get()
            .uri {
                it.path("/v2/local/search/keyword.JSON")
                    .queryParam("query", keyword)
                    .queryParam("page", 1)
                    .queryParam("size", 10)
                    .build()
            }
            .headers { it.add("Authorization", "KakaoAK 870184a00c3c8c365e827b1d84559c0f") }
            .retrieve()
            .bodyToFlux<KakaoResult>()
            .flatMap { result -> Flux.fromIterable(result.documents) }
            .map { document -> document.placeName }
    }
}