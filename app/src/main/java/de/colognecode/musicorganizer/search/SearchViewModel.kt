package de.colognecode.musicorganizer.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.colognecode.musicorganizer.repository.Repository
import de.colognecode.musicorganizer.repository.network.model.ArtistItem
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(private val repository: Repository) : ViewModel() {

    private val _artistsSearchResults = MutableLiveData<List<ArtistItem?>?>()
    val artistsSearchResults: LiveData<List<ArtistItem?>?> = _artistsSearchResults

    private val _isError = MutableLiveData<Boolean>()
    val isError: LiveData<Boolean> = _isError

    fun getSearchResults(artist: String) {
        viewModelScope.launch {
            repository.getArtistsSearchResult(artist)
                .catch {
                    _isError.value = true
                }
                .collect {
                    _artistsSearchResults.value = it?.artist
                }
        }
    }
}
