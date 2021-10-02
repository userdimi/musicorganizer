package de.colognecode.musicorganizer.search

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.colognecode.musicorganizer.repository.Repository
import de.colognecode.musicorganizer.repository.network.model.ArtistItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(private val repository: Repository) : ViewModel() {

    companion object {
        private const val START_PAGE_INDEX = 1
        const val ARTIST_SEARCH_RESULT_PAGE_SIZE = 30
        private const val TAG = "SearchViewModel"
    }

    private val _artistsSearchResults = MutableLiveData<List<ArtistItem?>?>()
    val artistsSearchResults: LiveData<List<ArtistItem?>?> = _artistsSearchResults

    private val _isError = MutableLiveData<Boolean>()
    val isError: LiveData<Boolean> = _isError

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading = _isLoading

    val page = mutableStateOf(START_PAGE_INDEX)
    private var artistsSearchResultsScrollPosition = 0

    fun getSearchResults(artist: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getArtistsSearchResult(
                artist = artist,
                page = START_PAGE_INDEX
            )
                .catch {
                    _isError.value = true
                    _isLoading.value = false
                }
                .collect {
                    _isLoading.value = false
                    _artistsSearchResults.value = it.artist
                }
        }
    }

    fun getNextPageSearchResults(artist: String) {
        viewModelScope.launch {
            if ((artistsSearchResultsScrollPosition + 1) >=
                (page.value * ARTIST_SEARCH_RESULT_PAGE_SIZE)
            )
                _isLoading.value = true
            incrementPage()
            Log.d(TAG, "nextPage: triggered: ${page.value}")
            delay(1000)
            if (page.value > 1) {
                repository.getArtistsSearchResult(
                    artist = artist,
                    page = page.value
                )
                    .catch {
                        _isError.value = true
                        _isLoading.value = false
                    }
                    .collect {
                        _isLoading.value = false
                        appendNewArtistsSearchResults(it.artist)
                    }
            }
        }
    }

    private fun appendNewArtistsSearchResults(newArtistSearchResults: List<ArtistItem?>?) {
        val currentArtistSearchResults = mutableListOf<ArtistItem?>()
        _artistsSearchResults.value?.let { currentSearchResults ->
            currentArtistSearchResults.addAll(currentSearchResults)
        }
        newArtistSearchResults?.let { newSearchResults ->
            currentArtistSearchResults.addAll(newSearchResults)
        }
        _artistsSearchResults.value = currentArtistSearchResults
    }

    private fun incrementPage() {
        this.page.value = this.page.value + 1
    }

    fun onArtistSearchResultScrollPositionChanged(position: Int) {
        this.artistsSearchResultsScrollPosition = position
    }
}
