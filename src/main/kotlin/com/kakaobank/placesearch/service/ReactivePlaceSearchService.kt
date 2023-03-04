package com.kakaobank.placesearch.service

import com.kakaobank.placesearch.api.PlaceSearchApi
import com.kakaobank.placesearch.domain.SearchCountRepository
import com.kakaobank.placesearch.dto.SearchCountDto
import com.kakaobank.placesearch.log
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.util.function.component1
import reactor.kotlin.core.util.function.component2

private const val TARGET_RESULT = 10

@Service
class ReactivePlaceSearchService(
    private val placeSearchApis: List<PlaceSearchApi>,
    private val searchCountRepository: SearchCountRepository
):PlaceSearchService {
//    @Cacheable("search")
    override fun search(keyword: String): Mono<List<String>> {
        return searchWithPriority(keyword)
            .map { places ->
                places.sortedWith(
                    Comparator.comparingInt<PlaceWithPriority> { it.duplicationPriority }
                        .reversed()
                        .thenByDescending { it.apiPriority }
                        .thenComparingInt { it.indexPriority }
                ).map { it.placeName }
            }
            .map { if (it.size <= TARGET_RESULT) it else it.subList(0, TARGET_RESULT + 1) }
            .doOnError { log().error("[${this::class.simpleName}] Failed to search() : ${it.message}") }
//            .cache(Duration.ofDays(1))
    }

    private fun searchWithPriority(keyword: String): Mono<List<PlaceWithPriority>> {
        return Flux.fromIterable(placeSearchApis)
            .flatMap { api ->
                Flux.from(api.search(keyword))
                    .flatMap { Flux.fromIterable(it) }
                    .index()
                    .map { (index, placeName) -> PlaceWithPriority(placeName, 0, api.priority(), index.toInt()) }
            }
            .reduce(hashMapOf<String, PlaceWithPriority>()) { acc, new ->
                val old = acc[new.placeName]
                acc[new.placeName] = old?.let { pickMorePriorPlace(it, new) } ?: new
                acc
            }
            .map { it.values.toList() }
    }

    private fun pickMorePriorPlace(old: PlaceWithPriority, new: PlaceWithPriority): PlaceWithPriority {
        val picked = if (compareValuesBy(new, old, { it.apiPriority }, { it.indexPriority }) > 0) new else old
        return picked.copy(duplicationPriority = old.duplicationPriority + 1)
    }

    override fun keywords(): Mono<List<SearchCountDto>> {
        return searchCountRepository.findFirst10ByOrderByCountDesc()
            .map { SearchCountDto.from(it) }
            .collectList()
            .doOnError { log().error("[${this::class.simpleName}] Failed to keywords() : ${it.message}") }
    }
}

data class PlaceWithPriority(
    val placeName: String,
    val duplicationPriority: Int,
    val apiPriority: Int,
    val indexPriority: Int
)