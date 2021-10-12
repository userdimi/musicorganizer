package de.colognecode.musicorganizer.albumdetail

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import de.colognecode.musicorganizer.repository.Repository
import de.colognecode.musicorganizer.repository.network.model.DetailedAlbum
import de.colognecode.musicorganizer.repository.network.model.Tags
import de.colognecode.musicorganizer.repository.network.model.Tracks
import de.colognecode.musicorganizer.repository.network.model.Wiki
import de.colognecode.musicorganizer.util.CoroutineTestRule
import io.kotest.matchers.comparables.shouldBeEqualComparingTo
import io.kotest.matchers.shouldBe
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
import java.io.IOException
import kotlin.time.ExperimentalTime

@ExperimentalCoroutinesApi
class AlbumDetailsViewModelTest {

    @get:Rule
    val rule = CoroutineTestRule()

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val mockRepository = mockk<Repository>(relaxed = true)
    private val mockAlbumDetailsObserver = mockk<Observer<DetailedAlbum>>(relaxed = true)
    private val mockIsErrorObserver = mockk<Observer<Boolean>>(relaxed = true)
    private val mockIsLoadingObserver = mockk<Observer<Boolean>>(relaxed = true)
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

    private val viewModel by lazy {
        AlbumDetailsViewModel(mockRepository).apply {
            albumDetails.observeForever(mockAlbumDetailsObserver)
            isError.observeForever(mockIsErrorObserver)
            isLoading.observeForever(mockIsLoadingObserver)
        }
    }

    @Test
    fun `album details should be loaded on success`() = rule.dispatcher.runBlockingTest {
        val testChannel = Channel<Result<DetailedAlbum>>()
        val testFlow = testChannel.consumeAsFlow()

        coEvery {
            mockRepository.getAlbumDetails(
                any(),
                any()
            )
        } returns testFlow

        launch {
            testChannel.send(Result.success(testAlbum))
        }

        // act
        viewModel.getAlbumDetails(testAlbum.artist, testAlbum.name)

        // assert
        verify { mockAlbumDetailsObserver.onChanged(testAlbum) }
        verify { mockIsLoadingObserver.onChanged(false) }
        viewModel.albumDetails.value.shouldBe(testAlbum)
    }

    @Test
    fun `album details should emit error on loading fail`() = rule.dispatcher.runBlockingTest {
        // arrange
        val testChannel = Channel<Result<DetailedAlbum>>()
        val testFlow = testChannel.consumeAsFlow()

        coEvery {
            mockRepository.getAlbumDetails(
                any(),
                any()
            )
        } returns testFlow

        launch {
            testChannel.close(IOException())
        }

        // act
        viewModel.getAlbumDetails(testAlbum.artist, testAlbum.name)

        // assert
        verify { mockIsErrorObserver.onChanged(true) }
        verify { mockIsLoadingObserver.onChanged(false) }
    }

    @ExperimentalTime
    @Test
    fun `should return as minute and seconds formatted string`() {
        // arrange
        val testDurationSeconds = 150L
        val expectedFormattedDuration = "02:30"

        // act
        val actualFormattedString = viewModel.getDurationAsFormatTimeString(testDurationSeconds)

        //
        actualFormattedString.shouldBeEqualComparingTo(expectedFormattedDuration)
    }

    @ExperimentalTime
    @Test
    fun `should return as hours, minute and seconds formatted string`() {
        // arrange
        val testDurationSeconds = 5400L
        val expectedFormattedDuration = "01:30:00"

        // act
        val actualFormattedString = viewModel.getDurationAsFormatTimeString(testDurationSeconds)

        //
        actualFormattedString.shouldBeEqualComparingTo(expectedFormattedDuration)
    }
}
