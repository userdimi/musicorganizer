package de.colognecode.musicorganizer.albumdetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.colognecode.musicorganizer.repository.Repository
import de.colognecode.musicorganizer.repository.network.model.DetailedAlbum
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlbumDetailsViewModel @Inject constructor(private val repository: Repository) : ViewModel() {

    private val _albumDetails = MutableLiveData<DetailedAlbum>()
    val albumDetails: LiveData<DetailedAlbum> = _albumDetails

    private val _isError = MutableLiveData<Boolean>()
    val isError: LiveData<Boolean> = _isError

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun getAlbumDetails(artist: String, album: String) {
        viewModelScope.launch {
            repository.getAlbumDetails(
                artist = artist,
                album = album
            )
                .catch {
                    _isError.value = true
                    _isLoading.value = false
                }
                .collect { result ->
                    result.onSuccess { albumDetails ->
                        _isLoading.value = false
                        _albumDetails.value = albumDetails
                    }
                }
        }
    }
}