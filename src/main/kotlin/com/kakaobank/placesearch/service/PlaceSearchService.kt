package com.kakaobank.placesearch.service

import com.kakaobank.placesearch.api.PlaceApi
import com.kakaobank.placesearch.aspect.RedisCacheable
import com.kakaobank.placesearch.domain.SearchCountRepository
import com.kakaobank.placesearch.dto.SearchCountDto
import com.kakaobank.placesearch.log
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

private const val TARGET_RESULT = 10
private const val MAX_PAGE = 3

@Service
class PlaceSearchService(
    private val placeApis: List<PlaceApi>,
    private val searchCountRepository: SearchCountRepository
) {
    @RedisCacheable
    fun search(keyword: String): Mono<List<String>> {
        return searchPages(keyword, 1)
            .map { places ->
                places.sortedWith(
                    Comparator.comparingInt<PlaceWithPriority> { it.duplicationPriority }
                        .thenComparingInt { it.apiPriority }
                        .reversed()
                        .thenComparingInt { it.indexPriority }
                ).map { it.placeName }
            }
            .map { if (it.size <= TARGET_RESULT) it else it.subList(0, TARGET_RESULT + 1) }
            .doOnError { log().error("[PlaceSearchService] Failed to search() : ${it.message}") }
    }

    private fun searchPages(keyword: String, startPage: Int): Mono<List<PlaceWithPriority>> {
        if (startPage >= MAX_PAGE) return Mono.empty()

        return Flux.fromIterable(placeApis)
            .flatMap { api -> api.searchPage(keyword, startPage) }
            .collectList()
            .flatMap { currentPage ->
                if (currentPage.size < TARGET_RESULT) searchPages(keyword, startPage + 1)
                else Mono.just(currentPage)
            }
    }

    private fun PlaceApi.searchPage(keyword: String, page: Int): Flux<PlaceWithPriority> {
        return Flux.from(this.search(keyword, page))
            .flatMap { Flux.fromIterable(it) }
            .groupBy { it }
            .index()
            .flatMap {
                val placeName = it.t2.key()
                val apiPriority = this.priority()
                val index = it.t1.toInt()
                it.t2.count().map { count -> PlaceWithPriority(placeName, count.toInt(), apiPriority, index) }
            }
    }

    fun keywords(): Mono<List<SearchCountDto>> {
        return searchCountRepository.findFirst10ByOrderByCountDesc()
            .map { SearchCountDto.from(it) }
            .collectList()
            .doOnError { log().error("[PlaceSearchService] Failed to keywords() : ${it.message}") }
    }
}

private data class PlaceWithPriority(
    val placeName: String,
    val duplicationPriority: Int,
    val apiPriority: Int,
    val indexPriority: Int
)