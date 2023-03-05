import com.kakaobank.placesearch.api.PlaceSearchApi
import com.kakaobank.placesearch.domain.SearchCount
import com.kakaobank.placesearch.domain.SearchCountRepository
import com.kakaobank.placesearch.dto.SearchCountDto
import com.kakaobank.placesearch.notification.EmergencyNotificationService
import com.kakaobank.placesearch.service.PlaceSearchService
import com.kakaobank.placesearch.service.ReactivePlaceSearchService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations.openMocks
import org.mockito.junit.jupiter.MockitoExtension
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

@ExtendWith(MockitoExtension::class)
class PlaceSearchServiceTests {
    @Mock
    lateinit var firstPlaceSearchApi: PlaceSearchApi

    @Mock
    lateinit var secondPlaceSearchApi: PlaceSearchApi

    lateinit var placeSearchApis: List<PlaceSearchApi>

    @Mock
    lateinit var searchCountRepository: SearchCountRepository

    @Mock
    lateinit var emergencyNotificationService: EmergencyNotificationService

    lateinit var placeSearchService: PlaceSearchService

    @BeforeEach
    fun setUp() {
        openMocks(this)
        placeSearchApis = listOf(firstPlaceSearchApi, secondPlaceSearchApi)
        placeSearchService = ReactivePlaceSearchService(placeSearchApis, searchCountRepository, emergencyNotificationService)
    }

    @Test
    fun `should return places for a given keyword`() {
        // given
        val keyword = "test"
        val firstPlaces = (0..4).map { "Test$it" }.toList()
        val secondPlaces = (3..7).map { "Test$it" }.toList()
        val expected = listOf(3, 4, 0, 1, 2, 5, 6, 7).map { "Test$it" }.toList()
        val firstPlacesReturn = Mono.just(firstPlaces)
        val secondPlacesReturn = Mono.just(secondPlaces)

        `when`(firstPlaceSearchApi.search(keyword)).thenReturn(firstPlacesReturn)
        `when`(firstPlaceSearchApi.priority()).thenReturn(20)
        `when`(secondPlaceSearchApi.search(keyword)).thenReturn(secondPlacesReturn)
        `when`(firstPlaceSearchApi.priority()).thenReturn(10)

        // when
        val resultMono = placeSearchService.search(keyword)

        // then
        StepVerifier.create(resultMono)
            .expectNextMatches { result ->
                assertNotNull(result)
                assertEquals(8, result.size)
                assertEquals(expected, result)
                true
            }
            .verifyComplete()
    }

    @Test
    fun `should handle errors correctly`() {
        // given
        val keyword = "test"
        val errorMessage = "Error message"
        `when`(firstPlaceSearchApi.search(keyword)).thenReturn(Mono.error(Throwable(errorMessage)))

        // when
        val resultMono = placeSearchService.search(keyword)

        // then
        StepVerifier.create(resultMono)
            .verifyErrorMatches { error -> error.message == errorMessage }
    }

    @Test
    fun `should return top 10 searched keywords`() {
        // given
        val searchCounts = (0..9).map { SearchCount(keyword = "Test$it", count = it.toLong()) }.toList()
        val expected = searchCounts.map { SearchCountDto.from(it) }
        `when`(searchCountRepository.findFirst10ByOrderByCountDesc()).thenReturn(Flux.fromIterable(searchCounts))

        // when
        val resultMono = placeSearchService.keywords()

        // then
        StepVerifier.create(resultMono)
            .assertNext { result ->
                assertNotNull(result)
                assertEquals(10, result.size)
                assertEquals(expected, result)
            }
            .verifyComplete()
    }

    @Test
    fun `should handle error when searching keywords`() {
        // given
        val errorMessage = "Error message"
        `when`(searchCountRepository.findFirst10ByOrderByCountDesc()).thenReturn(Flux.error(Throwable(errorMessage)))

        // when
        val resultMono = placeSearchService.keywords()

        // then
        StepVerifier.create(resultMono)
            .verifyErrorMatches { error -> error.message == errorMessage }
    }
}
