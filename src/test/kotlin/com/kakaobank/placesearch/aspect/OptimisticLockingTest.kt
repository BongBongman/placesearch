package com.kakaobank.placesearch.aspect

import com.kakaobank.placesearch.domain.SearchCountRepository
import org.junit.jupiter.api.DisplayName
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import java.util.*


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("SearchCountAspect")
@TestConfiguration
class OptimisticLockingTest {

    @Autowired
    private lateinit var searchCountRepository: SearchCountRepository

    /* Just to confirm if optimisitic locking works

    @Test
    fun `OptimisticLockingFailureException should be thrown`() {
        val keyword = "test"
        val first = searchCountRepository.save(SearchCount(keyword = keyword, count = 0)).block()!!
        val second = searchCountRepository.findById(first.id!!).block()!!
        searchCountRepository.save(first.copy(count = 10)).block()
        searchCountRepository.save(second.copy(count = 11)).block()
    }
    */
}