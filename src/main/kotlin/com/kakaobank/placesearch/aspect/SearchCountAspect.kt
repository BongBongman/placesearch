package com.kakaobank.placesearch.aspect

import com.kakaobank.placesearch.domain.SearchCount
import com.kakaobank.placesearch.domain.SearchCountRepository
import com.kakaobank.placesearch.log
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import reactor.kotlin.core.util.function.*
import java.util.*

@Component
@Aspect
class SearchCountAspect(
    private val searchCountRepository: SearchCountRepository
) {
    @Around("execution(* com.kakaobank.placesearch.service.PlaceSearchService.search(..)) && args(keyword)")
    @Transactional
    fun countKeyword(pjp: ProceedingJoinPoint, keyword: String): Mono<Any?> {
        return (pjp.proceed() as Mono<*>).zipWith(countKeyword(keyword)).map { (result, _) -> result }
    }

    private fun countKeyword(keyword: String): Mono<SearchCount> {
        return searchCountRepository.findByKeyword(keyword)
            .switchIfEmpty(Mono.just(SearchCount(keyword = keyword, count = 1)))
            .map { it.copy(count = it.count + 1) }
            .flatMap { searchCountRepository.save(it) }
            .onErrorResume(OptimisticLockingFailureException::class.java) {
                log().warn("[${this::class.simpleName}] Optimistic lock failed. ${it.message}")
                countKeyword(keyword)
            }
            .doOnError { log().error("[${this::class.simpleName}] Failed to update count. ${it.message}") }
    }
}
