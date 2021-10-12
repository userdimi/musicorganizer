package de.colognecode.musicorganizer.topalbums

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.colognecode.musicorganizer.repository.Repository
import de.colognecode.musicorganizer.repository.database.entities.FavoriteAlbum
import de.colognecode.musicorganizer.repository.database.entities.FavoriteAlbumDetails
import de.colognecode.musicorganizer.repository.network.model.AlbumItem
import de.colognecode.musicorganizer.repository.network.model.DetailedAlbum
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TopAlbumsViewModel @Inject constructor(private val repository: Repository) : ViewModel() {

    companion object {
        private const val START_PAGE_INDEX = 1
        const val TOP_ALBUM_PAGE_SIZE = 50
        const val TAG = "TopAlbumsViewModel"
    }

    private val _topAlbums = MutableLiveData<List<AlbumItem>>()
    val topAlbums: LiveData<List<AlbumItem>> = _topAlbums

    private val _isError = MutableLiveData<Boolean>()
    val isError: LiveData<Boolean> = _isError

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading = _isLoading

    private val _isFavorite = MutableLiveData<Boolean>()
    val isFavorite = _isFavorite

    private var totalPages = START_PAGE_INDEX

    val page = mutableStateOf(START_PAGE_INDEX)
    private var topAlbumsScrollPosition = 0

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
                    totalPages = it?.topAlbumAttr?.totalPages?.toInt() ?: START_PAGE_INDEX
                }
        }
    }

    fun getNextPageOfTopAlbums(artist: String) {
        viewModelScope.launch {
            if (page.value <= totalPages && (topAlbumsScrollPosition + 1) >=
                page.value * TOP_ALBUM_PAGE_SIZE
            ) {
                _isLoading.value = true
                incrementPage()
                delay(1000)
                Log.d(TAG, "nextPage: triggered: ${page.value}")
                if (page.value > START_PAGE_INDEX) {
                    repository.getTopAlbums(
                        artist = artist,
                        page = page.value
                    )
                        .catch {
                            _isError.value = true
                            _isLoading.value = false
                        }
                        .collect {
                            _isLoading.value = false
                            appendNewTopAlbums(it?.album)
                        }
                }
            }
        }
    }

    private fun appendNewTopAlbums(newTopAlbums: List<AlbumItem>?) {
        val currentTopAlbums = mutableListOf<AlbumItem>()
        _topAlbums.value?.let { currentAlbums ->
            currentTopAlbums.addAll(currentAlbums)
        }
        newTopAlbums?.let { newAlbums ->
            currentTopAlbums.addAll(newAlbums)
        }
        _topAlbums.value = currentTopAlbums
    }

    private fun incrementPage() {
        this.page.value = this.page.value + 1
    }

    fun onTopAlbumScrollPositionChanged(position: Int) {
        this.topAlbumsScrollPosition = position
    }

    fun saveAlbumAsFavorite(favoriteAlbum: FavoriteAlbum) {
        viewModelScope.launch {
            repository.getAlbumDetails(
                artist = favoriteAlbum.artistName,
                album = favoriteAlbum.albumName
            ).collect { result ->
                result.onSuccess { albumDetails ->
                    var totalDuration = 0L
                    albumDetails.tracks?.track?.forEach { trackItem ->
                        totalDuration += trackItem.duration
                    }
                    val favoriteAlbumDetails =
                        createFavoriteAlbumDetails(albumDetails, totalDuration)
                    repository.saveFavoriteAlbumToDatabase(
                        favoriteAlbum = favoriteAlbum,
                        favoriteAlbumDetails = favoriteAlbumDetails
                    )
                    _isFavorite.value = true
                }
            }
        }
    }

    private fun createFavoriteAlbumDetails(
        albumDetails: DetailedAlbum,
        totalDuration: Long
    ): FavoriteAlbumDetails {
        val favoriteAlbumDetails = FavoriteAlbumDetails(
            mbid = albumDetails.mbid ?: "",
            albumImageUrl = albumDetails.image.find { imageItem ->
                imageItem.size == "large"
            }?.text ?: "",
            albumName = albumDetails.name,
            artistName = albumDetails.artist,
            totalTracks = albumDetails.tracks?.track?.size,
            totalDuration = totalDuration,
            tracks = albumDetails.tracks?.track
        )
        return favoriteAlbumDetails
    }
}
