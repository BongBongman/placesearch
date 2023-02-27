package com.kakaobank.placesearch.controller

import com.kakaobank.placesearch.service.SearchPlaceService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
class SearchPlaceController(private val searchPlaceService: SearchPlaceService) {
    @GetMapping("/search")
    fun search(@RequestParam("keyword") keyword: String): Flux<String> {
        return searchPlaceService.search(keyword)
    }
}