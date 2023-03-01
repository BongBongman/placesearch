package com.kakaobank.placesearch.service

import com.kakaobank.placesearch.api.PlaceApi
import com.kakaobank.placesearch.domain.SearchCountRepository
import com.kakaobank.placesearch.dto.SearchCountDto
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

private const val TARGET_RESULT = 10
private const val MAX_PAGE = 3

@Service
class PlaceSearchService(private val placeApis: List<PlaceApi>, private val searchCountRepository: SearchCountRepository) {
    fun search(keyword: String): Mono<List<String>> {
        return searchWithPage(keyword, 1)
            .map { places ->
                places.sortedWith(
                    Comparator.comparingInt<PlaceWithPriority> { it.duplicationPriority }
                        .thenComparingInt { it.apiPriority }
                        .reversed()
                        .thenComparingInt { it.indexPriority }
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
                    .index()
                    .flatMap {
                        val placeName = it.t2.key()
                        val apiPriority = api.priority()
                        val index = it.t1.toInt()
                        it.t2.count().map { count -> PlaceWithPriority(placeName, count.toInt(), apiPriority, index) }
                    }
            }
            .collectList()
            .flatMap { currentPage ->
                if (currentPage.size < TARGET_RESULT) searchWithPage(keyword, startPage + 1)
                else Mono.just(currentPage)
            }
    }

    fun keywords(): Mono<List<SearchCountDto>> {
        return searchCountRepository.findFirst10ByOrderByCountDesc()
            .map { SearchCountDto.from(it) }
            .collectList()
    }

}

private data class PlaceWithPriority(
    val placeName: String,
    var duplicationPriority: Int,
    val apiPriority: Int,
    val indexPriority: Int
)