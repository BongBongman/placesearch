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
        val first = searchCountRepository.save(SearchCount(keyword = keyword, count = 0)).block()!! // Initially insert row. version is set to 0.
        val second = searchCountRepository.findById(first.id!!).block()!! // Load the just inserted row. version is still 0.
        searchCountRepository.save(first.copy(count = 10)).block() // Update the row with version = 0.Set the lastname and bump version to 1.
        searchCountRepository.save(second.copy(count = 11)).block() // Try to update the previously loaded row that still has version = 0.The operation fails with an OptimisticLockingFailureException, as the current version is 1.
    }
    */
}