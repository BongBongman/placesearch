package com.kakaobank.placesearch.api

import com.kakaobank.placesearch.dto.KakaoResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono

@Component
class KakaoApi(
    @Value("\${kakao.api.key}")
    private val apiKey: String
) : PlaceApi {
    override fun priority() = 20

    override fun search(keyword: String, page: Int): Mono<List<String>> {
        return WebClient
            .create("https://dapi.kakao.com")
            .get()
            .uri {
                it.path("/v2/local/search/keyword.JSON")
                    .queryParam("query", keyword)
                    .queryParam("size", 5)
                    .queryParam("page", page)
                    .build()
            }
            .headers { it.add("Authorization", "KakaoAK $apiKey") }
            .retrieve()
            .bodyToMono<KakaoResponse>()
            .map { it.documents }
            .map { it.map { document -> document.placeName } }
    }
}