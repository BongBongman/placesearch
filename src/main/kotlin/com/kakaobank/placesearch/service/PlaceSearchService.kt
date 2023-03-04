package com.kakaobank.placesearch.service

import com.kakaobank.placesearch.dto.SearchCountDto
import reactor.core.publisher.Mono

interface PlaceSearchService{
    fun search(keyword: String): Mono<List<String>>

    fun keywords(): Mono<List<SearchCountDto>>
}