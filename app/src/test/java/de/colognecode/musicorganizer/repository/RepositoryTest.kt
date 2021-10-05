package de.colognecode.musicorganizer.repository

import de.colognecode.musicorganizer.repository.Repository.Companion.DELAY_ONE_SECOND
import de.colognecode.musicorganizer.repository.database.daos.FavoriteAlbumsDao
import de.colognecode.musicorganizer.repository.database.entities.FavoriteAlbum
import de.colognecode.musicorganizer.repository.network.LastFMApiService
import de.colognecode.musicorganizer.repository.network.model.*
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
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
    private val mockFavoriteAlbum = mockk<FavoriteAlbum>(relaxed = true)
    private val mockAlbumDetailResponse = mockk<AlbumDetailResponse>()
    private val mockFavoriteAlbumDao = mockk<FavoriteAlbumsDao>(relaxed = true)
    private val testPage = 1
    private val testArtist = "testArtist"
    private val repository = Repository(mockApiService, testDispatcher, mockFavoriteAlbumDao)

    @Nested
    inner class ArtistSearchTest {
        private val mockkSearchResponse = mockk<ArtistSearchResponse>(relaxed = true)

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

        @InternalCoroutinesApi
        @Test
        fun `artist search emit successfully`() = runBlocking {
            // arrange
            every { mockkSearchResponse.results?.artistmatches } returns testArtistMatches
            coEvery {
                mockApiService.getArtists(
                    any(),
                    any(),
                    any()
                )
            } returns mockkSearchResponse

            // act
            val flowResult = repository.getArtistsSearchResult(testArtist, testPage)

            // assert
            flowResult.collect {
                it shouldBe testArtistMatches
            }
        }

        @Test
        fun `artist search emit error`() = testDispatcher.runBlockingTest {
            // arrange
            coEvery {
                mockApiService.getArtists(
                    any(),
                    any(),
                    any()
                )
            } throws IOException()

            // act
            val flowResult = repository.getArtistsSearchResult(testArtist, testPage)

            // assert
            flowResult.collect {
                it shouldBe null
            }
        }

        @Test
        fun `artist search retry emit success`() = testDispatcher.runBlockingTest {
            // arrange
            var shouldThrowError = true
            every { mockkSearchResponse.results?.artistmatches } returns testArtistMatches
            coEvery {
                mockApiService.getArtists(
                    any(),
                    any(),
                    any()
                )
            } answers {
                if (shouldThrowError) throw IOException() else mockkSearchResponse
            }

            pauseDispatcher {
                // act
                val flowResult = repository.getArtistsSearchResult(testArtist, testPage)
                // assert
                launch {
                    flowResult.collect {
                        it shouldBe testArtistMatches
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

    @Nested
    inner class TopAlbumsTest {
        private val mockTopAlbumsResponse = mockk<TopAlbumsResponse>(relaxed = true)
        private val mockArtist = mockk<Artist>(relaxed = true)
        private val testAlbumItem1 = AlbumItem(
            topAlbumsImage = listOf(),
            artist = mockArtist,
            playcount = 12345,
            name = "fooAlbum",
            url = "https://foo-album.com",
            mbid = "1234567890"
        )
        private val testAlbumItem2 = AlbumItem(
            topAlbumsImage = listOf(),
            artist = mockArtist,
            playcount = 54321,
            name = "barAlbum",
            url = "https://bar-album.com",
            mbid = "0987654321"
        )
        private val testTopAlbums = listOf(testAlbumItem1, testAlbumItem2)

        @Test
        fun `top albums emit successfully`() = testDispatcher.runBlockingTest {
            // arrange
            every { mockTopAlbumsResponse.topAlbums.album } returns testTopAlbums
            coEvery {
                mockApiService.getTopAlbums(
                    any(),
                    any(),
                    any()
                )
            } returns mockTopAlbumsResponse

            // act
            val flowResult = repository.getTopAlbums(testArtist, testPage)

            // assert
            flowResult.collect {
                it?.album.shouldNotBeEmpty()
                it?.album.shouldBe(testTopAlbums)
                it?.album?.shouldContain(testAlbumItem1)
                it?.album?.shouldContain(testAlbumItem2)
            }
        }

        @Test
        fun `top albums emit error`() = testDispatcher.runBlockingTest {
            // arrange
            coEvery {
                mockApiService.getTopAlbums(
                    any(),
                    any(),
                    any()
                )
            } throws IOException()

            // act
            val flowResult = repository.getTopAlbums(testArtist, testPage)

            // assert
            flowResult.collect {
                it shouldBe null
            }
        }

        @Test
        fun `top albums retry emit success`() = testDispatcher.runBlockingTest {
            // arrange
            var shouldThrowError = true
            every { mockTopAlbumsResponse.topAlbums.album } returns testTopAlbums
            coEvery {
                mockApiService.getTopAlbums(
                    any(),
                    any(),
                    any()
                )
            } answers {
                if (shouldThrowError) throw IOException() else mockTopAlbumsResponse
            }

            pauseDispatcher {
                // act
                val flowResult = repository.getTopAlbums(testArtist, testPage)
                // assert
                launch {
                    flowResult.collect {
                        it?.album shouldBe testTopAlbums
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

    @Nested
    inner class FavoriteAlbumTest {

        @Test
        fun `should save favorite album to database`() = testDispatcher.runBlockingTest {
            // act
            repository.saveFavoriteAlbumToDatabase(mockFavoriteAlbum)

            // assert
            coVerify { mockFavoriteAlbumDao.saveFavoriteAlbum(mockFavoriteAlbum) }
        }
    }

    @Nested
    inner class AlbumDetailsTest {
        private val testAlbum = DetailedAlbum(
            image = listOf(),
            mbid = "123456",
            listeners = "2",
            artist = "Foo",
            playcount = "3",
            wiki = Wiki(
                summary = "",
                published = "",
                content = ""
            ),
            name = "FooAlbum",
            tracks = Tracks(track = listOf()),
            url = "https;//foo-album.de",
            tags = Tags(tag = listOf())
        )

        @Test
        fun `album details emit successfully`() = testDispatcher.runBlockingTest {
            // arrange
            coEvery {
                mockApiService.getAlbumDetails(any(), any(), any())
            } returns mockAlbumDetailResponse
            every { mockAlbumDetailResponse.album } returns testAlbum

            // act
            val flowResult = repository.getAlbumDetails(testArtist, testAlbum.name)

            // assert
            flowResult.collect { result ->
                result.isSuccess.shouldBeTrue()
                result.onSuccess { album ->
                    album.shouldBe(testAlbum)
                }
            }
        }

        @Test
        fun `album details emit on error`() = testDispatcher.runBlockingTest {
            // arrange
            coEvery {
                mockApiService.getAlbumDetails(
                    any(),
                    any(),
                    any()
                )
            } throws IOException()

            // act
            val flowResult = repository.getAlbumDetails(testArtist, testAlbum.name)

            // assert
            flowResult.collect {
                it.isFailure.shouldBeTrue()
            }
        }

        @Test
        fun `album details emit successfully on retry`() = testDispatcher.runBlockingTest {
            // arrange
            var shouldThrowError = true
            coEvery {
                mockApiService.getAlbumDetails(
                    any(),
                    any(),
                    any()
                )
            } answers { if (shouldThrowError) throw IOException() else mockAlbumDetailResponse }

            every { mockAlbumDetailResponse.album } returns testAlbum

            pauseDispatcher {
                // act
                val flowResult = repository.getAlbumDetails(testArtist, testAlbum.name)

                // assert
                launch {
                    flowResult.collect { result ->
                        result.isSuccess.shouldBeTrue()
                        result.onSuccess { album ->
                            album.shouldBe(testAlbum)
                        }
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

