package de.colognecode.musicorganizer.topalbums

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.colognecode.musicorganizer.repository.Repository
import de.colognecode.musicorganizer.repository.network.model.AlbumItem
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TopAlbumsViewModel @Inject constructor(private val repository: Repository) : ViewModel() {

    companion object {
        private const val START_PAGE_INDEX = 1
    }

    private val _topAlbums = MutableLiveData<List<AlbumItem>>()
    val topAlbums: LiveData<List<AlbumItem>> = _topAlbums

    private val _isError = MutableLiveData<Boolean>()
    val isError: LiveData<Boolean> = _isError

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading = _isLoading

    fun getTopAlbums(artist: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getTopAlbums(
                artist = artist,
                page = START_PAGE_INDEX
            )
                .catch {
                    _isError.value = true
                    _isLoading.value = false
                }
                .collect {
                    _isLoading.value = false
                    _topAlbums.value = it?.album
                }
        }
    }
}
