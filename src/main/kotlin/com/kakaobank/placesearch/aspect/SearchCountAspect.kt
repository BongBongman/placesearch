package com.kakaobank.placesearch.aspect

import com.kakaobank.placesearch.domain.SearchCount
import com.kakaobank.placesearch.domain.SearchCountRepository
import org.aspectj.lang.annotation.AfterReturning
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono

@Component
@Aspect
class SearchCountAspect(private val searchCountRepository: SearchCountRepository) {
    @Pointcut("execution(* com.kakaobank.placesearch.service.PlaceSearchService.search(..)) && args(keyword)")
    fun search(keyword: String) {
    }

    @AfterReturning(pointcut = "search(keyword)")
    @Transactional //TODO 이게 효과가 있는 것인지 확인 필요
    fun countSearch(keyword: String) {
        searchCountRepository.findByKeyword(keyword)
            .map { it.copy(count = it.count + 1) }
            .switchIfEmpty(Mono.just(SearchCount(keyword, 1)))
            .flatMap { searchCountRepository.save(it) }
            .subscribe()
    }
}
