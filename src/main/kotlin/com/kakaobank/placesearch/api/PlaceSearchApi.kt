package com.kakaobank.placesearch.api

import reactor.core.publisher.Mono

interface PlaceSearchApi {
    fun priority(): Int
    fun search(keyword: String): Mono<List<String>>
}