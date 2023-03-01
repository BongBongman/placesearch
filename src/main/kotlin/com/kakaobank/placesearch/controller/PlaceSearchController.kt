package com.kakaobank.placesearch.controller

import com.kakaobank.placesearch.dto.Keyword
import com.kakaobank.placesearch.dto.KeywordResponse
import com.kakaobank.placesearch.dto.Place
import com.kakaobank.placesearch.dto.PlaceSearchResponse
import com.kakaobank.placesearch.service.PlaceSearchService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
class PlaceSearchController(private val placeSearchService: PlaceSearchService) {
    @GetMapping("/v1/place")
    fun search(@RequestParam("keyword") keyword: String): Mono<PlaceSearchResponse> {
        return placeSearchService.search(keyword)
            .map { places -> PlaceSearchResponse(places.map { Place(it) }) }
    }

    @GetMapping("/v1/keywords")
    fun keywords(): Mono<KeywordResponse> {
        return placeSearchService.keywords()
            .map { searchCounts -> KeywordResponse(searchCounts.map { Keyword.from(it) }) }
    }
}