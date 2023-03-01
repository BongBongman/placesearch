package com.kakaobank.placesearch.controller

import com.kakaobank.placesearch.dto.Keyword
import com.kakaobank.placesearch.dto.KeywordResponse
import com.kakaobank.placesearch.dto.Place
import com.kakaobank.placesearch.dto.PlaceSearchResponse
import com.kakaobank.placesearch.service.PlaceSearchService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
class PlaceSearchController(private val placeSearchService: PlaceSearchService) {
    @GetMapping("/v1/place")
    fun search(@RequestParam("keyword") keyword: String?): Mono<ResponseEntity<PlaceSearchResponse>> {
        if (keyword.isNullOrBlank()) {
            return Mono.just(ResponseEntity.badRequest().build())
        }
        return placeSearchService.search(keyword)
            .map { places -> ResponseEntity.ok(PlaceSearchResponse(places.map { Place(it) })) }
            .onErrorResume { Mono.just(ResponseEntity.internalServerError().build()) }
    }

    @GetMapping("/v1/keywords")
    fun keywords(): Mono<ResponseEntity<KeywordResponse>> {
        return placeSearchService.keywords()
            .map { searchCounts -> ResponseEntity.ok(KeywordResponse(searchCounts.map { Keyword.from(it) })) }
            .onErrorResume { Mono.just(ResponseEntity.internalServerError().build()) }
    }
}