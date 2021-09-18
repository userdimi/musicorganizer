package de.colognecode.musicorganizer.search

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import de.colognecode.musicorganizer.repository.Repository
import de.colognecode.musicorganizer.repository.network.model.ArtistItem
import de.colognecode.musicorganizer.repository.network.model.Artistmatches
import de.colognecode.musicorganizer.util.CoroutineTestRule
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.io.IOException

@ExperimentalCoroutinesApi
@RunWith(JUnit4::class)
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
    private val testArtist = "testArtist"
    private val testArtistsMatches = Artistmatches(listOf(testArtistItem1, testArtistItem2))
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
        val testChannel = Channel<Artistmatches?>()
        val testFlow = testChannel.consumeAsFlow()
        coEvery { mockRepository.getArtistsSearchResult(testArtist) } returns testFlow
        launch {
            testChannel.send(testArtistsMatches)
        }

        // act
        searchViewModel.getSearchResults(testArtist)

        // assert
        verify { mockArtistsSearchResultsObserver.onChanged(testArtists) }
    }

    @Test
    fun `should emit error on artists search error`() = rule.dispatcher.runBlockingTest {
        // arrange
        val testChannel = Channel<Artistmatches?>()
        val testFlow = testChannel.consumeAsFlow()
        coEvery { mockRepository.getArtistsSearchResult(testArtist) } returns testFlow
        launch {
            testChannel.close(IOException())
        }

        // act 
        searchViewModel.getSearchResults(testArtist)

        // assert
        verify { mockErrorObserver.onChanged(true) }
        verify(exactly = 0) { mockArtistsSearchResultsObserver.onChanged(any()) }
    }
}
