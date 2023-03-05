package com.kakaobank.placesearch.service

import com.kakaobank.placesearch.api.PlaceSearchApi
import com.kakaobank.placesearch.configuration.*
import com.kakaobank.placesearch.domain.SearchCountRepository
import com.kakaobank.placesearch.dto.SearchCountDto
import com.kakaobank.placesearch.log
import com.kakaobank.placesearch.notification.EmergencyNotificationService
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.util.function.component1
import reactor.kotlin.core.util.function.component2
import java.time.Duration

private const val TARGET_RESULT = 10
private const val CIRCUIT_BREAKER_SEARCH = "search"
private const val CIRCUIT_BREAKER_KEYWORDS = "keywords"

@Service
class ReactivePlaceSearchService(
    private val placeSearchApis: List<PlaceSearchApi>,
    private val searchCountRepository: SearchCountRepository,
    private val emergencyNotificationService: EmergencyNotificationService
) : PlaceSearchService {
    @CircuitBreaker(name = CIRCUIT_BREAKER_SEARCH, fallbackMethod = "searchFallback")
    @Cacheable(CACHE_SEARCH, key = "#keyword")
    override fun search(keyword: String): Mono<List<String>> {
        return searchWithPriority(keyword)
            .map { places ->
                places.sortedWith(
                    Comparator.comparingInt<PlaceWithPriority> { it.duplicationPriority }
                        .thenComparingInt { it.apiPriority }
                        .thenByDescending { it.indexPriority }
                        .reversed()
                ).map { it.placeName }
            }
            .map { if (it.size <= TARGET_RESULT) it else it.subList(0, TARGET_RESULT + 1) }
            .doOnError { log().error("[${this::class.simpleName}] Failed to search() : ${it.message}") }
            .cache(getMonoCacheExpireTime())
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

    private fun getMonoCacheExpireTime() = Duration.ofSeconds(CACHE_EXPIRE_TIME_SECONDS * 2)


    private fun searchFallback(keyword: String, err: Exception): Mono<List<String>> {
        val message = "[${this::class.simpleName}#search()] Circuit breaker opend : ${err.message}"
        log().error(message)
        emergencyNotificationService.sendSlack(message)
        return Mono.error(err) // or return snapshot
    }

    @CircuitBreaker(name = CIRCUIT_BREAKER_KEYWORDS, fallbackMethod = "keywordFallback")
    @Cacheable(CACHE_KEYWORDS)
    override fun keywords(): Mono<List<SearchCountDto>> {
        return searchCountRepository.findFirst10ByOrderByCountDesc()
            .map { SearchCountDto.from(it) }
            .collectList()
            .doOnError { log().error("[${this::class.simpleName}] Failed to keywords() : ${it.message}") }
            .cache(getMonoCacheExpireTime())
    }

    private fun keywordFallback(err: Exception): Mono<List<String>> {
        val message = "[${this::class.simpleName}#search()] Circuit breaker opend : ${err.message}"
        log().error(message)
        emergencyNotificationService.sendSlack(message)
        return Mono.error(err) // or return snapshot
    }
}

data class PlaceWithPriority(
    val placeName: String,
    val duplicationPriority: Int,
    val apiPriority: Int,
    val indexPriority: Int
)