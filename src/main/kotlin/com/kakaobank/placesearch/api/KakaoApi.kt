package com.kakaobank.placesearch.api

import com.kakaobank.placesearch.dto.KakaoResponse
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono

@Component
class KakaoApi : PlaceApi {
    override fun priority() = 20

    //TODO API KEY property로 옮길 것
    override fun search(keyword: String, page: Int): Mono<List<String>> {
        return WebClient
            .create("https://dapi.kakao.com")
            .get()
            .uri {
                it.path("/v2/local/search/keyword.JSON")
                    .queryParam("query", keyword)
                    .queryParam("size", 5)
                    .build()
            }
            .headers { it.add("Authorization", "KakaoAK 870184a00c3c8c365e827b1d84559c0f") }
            .retrieve()
            .bodyToMono<KakaoResponse>()
            .map { it.documents }
            .map { it.map { document -> document.placeName } }
    }
}