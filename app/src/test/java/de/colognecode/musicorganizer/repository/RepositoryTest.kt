package de.colognecode.musicorganizer.repository

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
    private val repository = Repository(mockApiService, testApikey, testFormat, testDispatcher)

    @Nested
    inner class ArtistSearch {
        @InternalCoroutinesApi
        @Test
        fun `artist search emits successfully`() = runBlocking {
            // arrange
            val testArtistItem1 = ArtistItem(
                listOf(),
                "tesMbid",
                "1234567",
                "yes",
                "fooMusic",
                "https://fooMusic.com"
            )
            val testArtistItem2 = ArtistItem(
                listOf(),
                "tesMbid",
                "1234567",
                "no",
                "barMusic",
                "https://barMusic.com"
            )
            val artistMatches = Artistmatches(listOf(testArtistItem1, testArtistItem2))
            every { mockkSearchResponse.results?.artistmatches } returns artistMatches

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
        fun `should retry search after error`() = testDispatcher.runBlockingTest {
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
    }
}
