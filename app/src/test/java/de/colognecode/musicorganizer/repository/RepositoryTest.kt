package de.colognecode.musicorganizer.repository

import de.colognecode.musicorganizer.repository.Repository.Companion.DELAY_ONE_SECOND
import de.colognecode.musicorganizer.repository.network.LastFMApiService
import de.colognecode.musicorganizer.repository.network.model.ArtistItem
import de.colognecode.musicorganizer.repository.network.model.ArtistSearchResponse
import de.colognecode.musicorganizer.repository.network.model.Artistmatches
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.IOException

@ExperimentalCoroutinesApi
internal class RepositoryTest {

    private val testDispatcher = TestCoroutineDispatcher()
    private val mockApiService = mockk<LastFMApiService>(relaxed = true)
    private val mockkSearchResponse = mockk<ArtistSearchResponse>(relaxed = true)
    private val testMethod = "testMethod"
    private val testArtist = "testArtist"
    private val testApikey = "12345"
    private val testFormat = "json"
    private val testArtistItem1 = ArtistItem(
        listOf(),
        "tesMbid",
        "1234567",
        "yes",
        "fooMusic",
        "https://fooMusic.com"
    )
    private val testArtistItem2 = ArtistItem(
        listOf(),
        "tesMbid",
        "1234567",
        "no",
        "barMusic",
        "https://barMusic.com"
    )
    private val testArtistMatches = Artistmatches(listOf(testArtistItem1, testArtistItem2))
    private val repository = Repository(mockApiService, testApikey, testFormat, testDispatcher)

    @Nested
    inner class ArtistSearch {
        @InternalCoroutinesApi
        @Test
        fun `artist search emit successfully`() = runBlocking {
            // arrange
            every { mockkSearchResponse.results?.artistmatches } returns testArtistMatches
            coEvery {
                mockApiService.getArtists(
                    any(),
                    any(),
                    any(),
                    any()
                )
            } returns mockkSearchResponse

            // act
            val flowResult = repository.getArtistsSearchResult(testMethod, testArtist)

            // assert
            flowResult.collect { result ->
                result.isSuccess.shouldBeTrue()
                result.onSuccess { artistMatches ->
                    artistMatches shouldBe artistMatches
                }
            }
        }

        @Test
        fun `artist search emit error`() = testDispatcher.runBlockingTest {
            // arrange
            coEvery {
                mockApiService.getArtists(
                    any(),
                    any(),
                    any(),
                    any()
                )
            } throws IOException()

            // act
            val flowResult = repository.getArtistsSearchResult(testMethod, testArtist)

            // assert
            flowResult.collect {
                it.isFailure.shouldBeTrue()
            }
        }

        @Test
        fun `artist search retry emit success`() = testDispatcher.runBlockingTest {
            // arrange
            var shouldThrowError = true
            coEvery {
                mockApiService.getArtists(
                    any(),
                    any(),
                    any(),
                    any()
                )
            } answers {
                if (shouldThrowError) throw IOException() else mockkSearchResponse
            }

            pauseDispatcher {
                // act
                val flowResult = repository.getArtistsSearchResult(testMethod, testArtist)
                // assert
                launch {
                    flowResult.collect {
                        it.isSuccess.shouldBeTrue()
                    }
                }
                // 1st retry
                advanceTimeBy(DELAY_ONE_SECOND)
                // 2st retry
                shouldThrowError = false
                advanceTimeBy(DELAY_ONE_SECOND)
            }
        }
    }
}
