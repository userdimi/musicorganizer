package de.colognecode.musicorganizer.topalbums

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import de.colognecode.musicorganizer.repository.Repository
import de.colognecode.musicorganizer.repository.network.model.AlbumItem
import de.colognecode.musicorganizer.repository.network.model.Artist
import de.colognecode.musicorganizer.repository.network.model.TopAlbumAttr
import de.colognecode.musicorganizer.repository.network.model.TopAlbums
import de.colognecode.musicorganizer.utils.testing.CoroutineTestRule
import io.kotest.matchers.collections.shouldContain
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import org.junit.Rule
import org.junit.Test
import java.io.IOException

@ExperimentalCoroutinesApi
class TopAlbumsViewModelTest {

    @get:Rule
    val rule = CoroutineTestRule()

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val mockTopAlbumsObserver = mockk<Observer<List<AlbumItem>>>(relaxed = true)
    private val mockIsErrorObserver = mockk<Observer<Boolean>>(relaxed = true)
    private val mockIsLoading = mockk<Observer<Boolean>>(relaxed = true)
    private val mockRepository = mockk<Repository>(relaxed = true)
    private val mockArtist = mockk<Artist>(relaxed = true)
    private val testArtist = "TestArtist"
    private val testPage = 1
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
    private val mockTopAlbumAttr = mockk<TopAlbumAttr>(relaxed = true)
    private val testAlbums = listOf(testAlbumItem1, testAlbumItem2)
    private val testTopAlbums = TopAlbums(
        topAlbumAttr = mockTopAlbumAttr,
        album = testAlbums
    )

    private val topAlbumsViewModel by lazy {
        TopAlbumsViewModel(mockRepository).apply {
            topAlbums.observeForever(mockTopAlbumsObserver)
            isError.observeForever(mockIsErrorObserver)
            isLoading.observeForever(mockIsLoading)
        }
    }

    @Test
    fun `should emit top albums on success`() = rule.dispatcher.runBlockingTest {
        // arrange
        val testChannel = Channel<TopAlbums>()
        val testFlow = testChannel.consumeAsFlow()

        coEvery {
            mockRepository.getTopAlbums(
                testArtist,
                testPage
            )
        } returns testFlow

        launch {
            testChannel.send(testTopAlbums)
        }

        // act
        topAlbumsViewModel.getTopAlbums(testArtist)

        // assert
        verify { mockTopAlbumsObserver.onChanged(testAlbums) }
        verify { mockIsLoading.onChanged(false) }
        topAlbumsViewModel.topAlbums.value?.shouldContain(testAlbumItem1)
        topAlbumsViewModel.topAlbums.value?.shouldContain(testAlbumItem2)
    }

    @Test
    fun `should emit error when getting top albums failed`() = rule.dispatcher.runBlockingTest {
        // arrange
        val testChannel = Channel<TopAlbums>()
        val testFlow = testChannel.consumeAsFlow()

        coEvery {
            mockRepository.getTopAlbums(
                testArtist,
                testPage
            )
        } returns testFlow

        launch {
            testChannel.close(IOException())
        }

        // act
        topAlbumsViewModel.getTopAlbums(testArtist)

        // assert
        verify { mockIsErrorObserver.onChanged(true) }
        verify { mockIsLoading.onChanged(false) }
    }
}
