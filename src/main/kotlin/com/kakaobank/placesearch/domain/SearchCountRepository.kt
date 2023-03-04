package com.kakaobank.placesearch.domain

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface SearchCountRepository : ReactiveCrudRepository<SearchCount, String> {
    fun findByKeyword(keyword: String): Mono<SearchCount>
    fun findById(id: Long): Mono<SearchCount>
    fun findFirst10ByOrderByCountDesc(): Flux<SearchCount>
}
