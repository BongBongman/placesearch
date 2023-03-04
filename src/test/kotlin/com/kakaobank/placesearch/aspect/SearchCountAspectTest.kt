package com.kakaobank.placesearch.aspect

import com.kakaobank.placesearch.domain.SearchCount
import com.kakaobank.placesearch.domain.SearchCountRepository
import org.aspectj.lang.ProceedingJoinPoint
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

@ExtendWith(MockitoExtension::class)
class SearchCountAspectTest {

    @Mock
    lateinit var searchCountRepository: SearchCountRepository

    @Mock
    lateinit var pjp: ProceedingJoinPoint

    @InjectMocks
    lateinit var searchCountAspect: SearchCountAspect

    @Test
    fun `should increment search count`() {
        // given
        val keyword = "test"
        val count = 10L
        val searchCount = SearchCount(keyword = keyword, count = count)
        val expected = searchCount.copy(count = count + 1)
        val serviceResult = listOf("testResult")

        `when`(searchCountRepository.findByKeyword(keyword)).thenReturn(Mono.just(searchCount))
        `when`(searchCountRepository.save(any())).thenReturn(Mono.just(expected))
        `when`(pjp.proceed()).thenReturn(Mono.just(serviceResult))

        // when
        val result = searchCountAspect.countKeyword(pjp, keyword)

        //then
        StepVerifier.create(result)
            .expectNextMatches {
                verify(searchCountRepository, times(1)).save(expected)
                true
            }.verifyComplete()
    }

}
