package com.kakaobank.placesearch.controller

import com.kakaobank.placesearch.dto.Place
import com.kakaobank.placesearch.dto.SearchPlaceResponse
import com.kakaobank.placesearch.service.SearchPlaceService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
class SearchPlaceController(private val searchPlaceService: SearchPlaceService) {
    @GetMapping("/v1/place")
    fun search(@RequestParam("keyword") keyword: String): Mono<SearchPlaceResponse> {
        return searchPlaceService.search(keyword)
            .map { places-> SearchPlaceResponse(places.map { Place(it) }) }
    }
}