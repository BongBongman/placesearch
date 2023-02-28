package com.kakaobank.placesearch.service

import com.kakaobank.placesearch.api.PlaceApi
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

private const val TARGET_RESULT = 10
private const val MAX_PAGE = 3

@Service
class SearchPlaceService(private val placeApis: List<PlaceApi>) {
    fun search(keyword: String): Mono<List<String>> {
        return searchWithPage(keyword, 1)
            .map { places ->
                places.sortedWith(
                    Comparator.comparingInt<PlaceWithPriority> { it.duplicated }
                        .thenComparingInt { it.apiPriority }
                ).map { it.placeName }
            }
            .map { if (it.size <= TARGET_RESULT) it else it.subList(0, TARGET_RESULT + 1) }
    }

    private fun searchWithPage(keyword: String, startPage: Int): Mono<List<PlaceWithPriority>> {
        if (startPage >= MAX_PAGE) return Mono.empty()

        return Flux.fromIterable(placeApis)
            .flatMap { api ->
                Flux.from(api.search(keyword, startPage))
                    .flatMap { Flux.fromIterable(it) }
                    .groupBy { it }
                    .flatMap { group ->
                        group.count().map { count ->
                            PlaceWithPriority(group.key(), count.toInt(), api.priority())
                        }
                    }
            }
            .collectList()
            .flatMap { currentPage ->
                if (currentPage.size < TARGET_RESULT) searchWithPage(keyword, startPage + 1)
                else Mono.just(currentPage)
            }
    }

}

private data class PlaceWithPriority(val placeName: String, var duplicated: Int, val apiPriority: Int)