package de.colognecode.musicorganizer.search

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import de.colognecode.musicorganizer.repository.Repository
import de.colognecode.musicorganizer.repository.network.model.ArtistItem
import de.colognecode.musicorganizer.repository.network.model.Artistmatches
import de.colognecode.musicorganizer.util.CoroutineTestRule
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import java.io.IOException

@ExperimentalCoroutinesApi
internal class SearchViewModelTest {

    @get:Rule
    val rule = CoroutineTestRule()

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val mockRepository = mockk<Repository>(relaxed = true)
    private val mockArtistsSearchResultsObserver =
        mockk<Observer<List<ArtistItem?>?>>(relaxed = true)
    private val mockErrorObserver = mockk<Observer<Boolean>>(relaxed = true)
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
    private val testNextPageArtistItem1 = ArtistItem(
        listOf(),
        "tesMbid",
        "1234567",
        "yes",
        "fooMusic",
        "https://fooMusic.com"
    )
    private val testNextPageArtistItem2 = ArtistItem(
        listOf(),
        "tesMbid",
        "1234567",
        "no",
        "barMusic",
        "https://barMusic.com"
    )
    private val testArtist = "testArtist"
    private val testArtistsMatches = Artistmatches(listOf(testArtistItem1, testArtistItem2))
    private val testFirstPage = 1
    private val testArtistsNextPageMatches =
        Artistmatches(listOf(testNextPageArtistItem1, testNextPageArtistItem2))
    private val testArtists = listOf(testArtistItem1, testArtistItem2)
    private val searchViewModel by lazy {
        SearchViewModel(mockRepository).apply {
            artistsSearchResults.observeForever(mockArtistsSearchResultsObserver)
            isError.observeForever((mockErrorObserver))
        }
    }

    @Test
    fun `should emit artists search results on success`() = rule.dispatcher.runBlockingTest {
        // arrange
        val testChannel = Channel<Artistmatches>()
        val testFlow = testChannel.consumeAsFlow()
        coEvery {
            mockRepository.getArtistsSearchResult(
                testArtist,
                testFirstPage
            )
        } returns testFlow
        launch {
            testChannel.send(testArtistsMatches)
        }

        // act
        searchViewModel.getSearchResults(testArtist)

        // assert
        verify { mockArtistsSearchResultsObserver.onChanged(testArtists) }
        searchViewModel.artistsSearchResults.value.shouldNotBeEmpty()
        searchViewModel.artistsSearchResults.value.shouldContainExactly(
            testArtistItem1,
            testArtistItem2
        )
    }

    @Test
    fun `should emit error on artists search error`() = rule.dispatcher.runBlockingTest {
        // arrange
        val testChannel = Channel<Artistmatches>()
        val testFlow = testChannel.consumeAsFlow()
        coEvery {
            mockRepository.getArtistsSearchResult(
                testArtist,
                testFirstPage
            )
        } returns testFlow
        launch {
            testChannel.close(IOException())
        }

        // act 
        searchViewModel.getSearchResults(testArtist)

        // assert
        verify { mockErrorObserver.onChanged(true) }
        verify(exactly = 0) { mockArtistsSearchResultsObserver.onChanged(any()) }
    }
    @Ignore
    // FIXME: 26.09.21 test shoukld be fixed
    @Test
    fun `should merge next page search results`() = rule.dispatcher.runBlockingTest {
        // arrange
        val testChannel = Channel<Artistmatches>()
        val testFlow = testChannel.consumeAsFlow()
        var tempTestPage = 1
        coEvery {
            mockRepository.getArtistsSearchResult(
                any(),
                any()
            )
        } returns testFlow
        launch {
            testChannel.send(testArtistsMatches)
        }

        //
        searchViewModel.getNextPageSearchResults(testArtist)
        delay(1000)
        searchViewModel.artistsSearchResults.value?.size.shouldBe(2)

        searchViewModel.onArtistSearchResultScrollPositionChanged(2)
        searchViewModel.getNextPageSearchResults(testArtist)
        delay(1000)
        searchViewModel.artistsSearchResults.value?.size.shouldBe(4)
    }
}
