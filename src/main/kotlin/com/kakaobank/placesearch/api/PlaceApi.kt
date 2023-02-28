package com.kakaobank.placesearch.api

import reactor.core.publisher.Mono

interface PlaceApi {
    fun priority(): Int
    fun search(keyword: String, page:Int): Mono<List<String>>
}