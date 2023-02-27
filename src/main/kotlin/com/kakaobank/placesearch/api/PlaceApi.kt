package com.kakaobank.placesearch.api

import reactor.core.publisher.Flux

interface PlaceApi {
    fun search(keyword: String): Flux<String>
}