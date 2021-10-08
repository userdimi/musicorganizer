package de.colognecode.musicorganizer.topalbums

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import dagger.hilt.android.AndroidEntryPoint
import de.colognecode.musicorganizer.components.MusicOrganizerLoadingSpinner
import de.colognecode.musicorganizer.repository.database.entities.FavoriteAlbum
import de.colognecode.musicorganizer.repository.network.model.AlbumItem
import de.colognecode.musicorganizer.theme.MusicOrganizerTheme

@ExperimentalCoilApi
@ExperimentalFoundationApi
@AndroidEntryPoint
class TopAlbumsFragment : Fragment() {

    private val viewModel: TopAlbumsViewModel by viewModels()
    private val args: TopAlbumsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            val artist = args.artist ?: ""
            viewModel.getTopAlbums(artist)
            setContent {
                val isLoading by viewModel.isLoading.observeAsState(false)
                val topAlbums by viewModel.topAlbums.observeAsState(initial = emptyList())
                val topAlbumsPage by viewModel.page
                if (artist.isEmpty()) {
                    Snackbar {
                        Text(text = "Error, no artist provided.")
                    }
                }
                TopAlbumsView(
                    artist = artist,
                    isLoading = isLoading,
                    topAlbums = topAlbums,
                    topAlbumsPage = topAlbumsPage
                )
            }
        }
    }

    @Composable
    fun TopAlbumsView(
        artist: String,
        topAlbums: List<AlbumItem>,
        isLoading: Boolean?,
        topAlbumsPage: Int
    ) {
        MusicOrganizerTheme {
            Scaffold(
                topBar = { AppBar(artist = artist) },
                content = {
                    ContentTopAlbums(
                        isLoading = isLoading,
                        topAlbums = topAlbums,
                        topAlbumsPage = topAlbumsPage,
                        artist = artist
                    )
                }
            )
        }
    }

    @Composable
    fun AppBar(artist: String?) {
        TopAppBar(
            title = {
                var topAppBarText = "Top Albums"
                if (!artist.isNullOrEmpty()) {
                    topAppBarText = "$topAppBarText of $artist"
                }
                Text(
                    text = topAppBarText
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = {
                        findNavController().popBackStack()
                    },
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Arrow to previous screen"
                    )
                }
            }
        )
    }

    @Composable
    fun ContentTopAlbums(
        isLoading: Boolean?,
        topAlbums: List<AlbumItem>,
        topAlbumsPage: Int,
        artist: String
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Albums(
                isLoading = isLoading,
                topAlbums = topAlbums,
                topAlbumsPage = topAlbumsPage,
                artist = artist
            )
        }
    }

    @Composable
    fun Albums(
        isLoading: Boolean?,
        topAlbums: List<AlbumItem>,
        topAlbumsPage: Int,
        artist: String
    ) {
        if (isLoading == true && topAlbums.isEmpty()) {
            MusicOrganizerLoadingSpinner.LoadingSpinnerComposable()
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = MaterialTheme.colors.surface)
            ) {
                LazyVerticalGrid(cells = GridCells.Fixed(2)) {
                    itemsIndexed(
                        items = topAlbums
                    ) { index: Int, album: AlbumItem ->
                        val imageUrl =
                            album.topAlbumsImage.let { imageItems ->
                                imageItems?.find { imageItem ->
                                    imageItem.size == "large"
                                }?.text ?: ""
                            }
                        this@TopAlbumsFragment.viewModel.onTopAlbumScrollPositionChanged(
                            position = index
                        )
                        if ((index + 1) >=
                            (topAlbumsPage * TopAlbumsViewModel.TOP_ALBUM_PAGE_SIZE) &&
                            isLoading == false
                        ) {
                            this@TopAlbumsFragment.viewModel.getNextPageOfTopAlbums(
                                artist = artist
                            )
                        }
                        AlbumCard(
                            albumImageUrl = imageUrl,
                            albumName = album.name ?: "",
                            artistName = album.artist?.name ?: "",
                            playCount = album.playcount ?: 0,
                            mbid = album.mbid ?: ""
                        )
                    }
                }
            }
            if (isLoading == true) {
                MusicOrganizerLoadingSpinner.LoadingSpinnerComposable()
            }
        }
    }

    @Composable
    fun AlbumCard(
        albumImageUrl: String,
        albumName: String,
        artistName: String,
        playCount: Int,
        mbid: String
    ) {
        Card(
            modifier = Modifier
                .padding(
                    horizontal = 8.dp,
                    vertical = 8.dp
                )
                .size(280.dp)
                .clickable(
                    onClick = {
                        val action =
                            TopAlbumsFragmentDirections.actionTopAlbumsFragmentToAlbumDetailFragment(
                                artistName,
                                albumName
                            )
                        findNavController().navigate(action)
                    }
                ),
            shape = RoundedCornerShape(corner = CornerSize(16.dp)),
            elevation = 2.dp,
            backgroundColor = MaterialTheme.colors.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxSize()
            ) {
                AlbumImage(albumUrl = albumImageUrl)
                Text(
                    text = albumName,
                    maxLines = 2,
                    style = MaterialTheme.typography.h6,
                    color = MaterialTheme.colors.onSurface
                )
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Row {
                        Column {
                            Text(
                                text = artistName,
                                maxLines = 2,
                                style = MaterialTheme.typography.body1,
                                color = MaterialTheme.colors.onSurface
                            )
                            Text(
                                text = "Playcount: $playCount",
                                maxLines = 2,
                                style = MaterialTheme.typography.caption,
                                color = MaterialTheme.colors.onSurface
                            )
                        }
                        val interactionSource = remember { MutableInteractionSource() }
                        val isPressed by interactionSource.collectIsPressedAsState()
                        val color = if (isPressed) Color.Red else Color.LightGray
                        IconButton(
                            modifier = Modifier
                                .fillMaxWidth(),
                            onClick = {
                                val favoriteAlbum = FavoriteAlbum(
                                    mbid = mbid,
                                    albumImageUrl = albumImageUrl,
                                    albumName = albumName,
                                    artistName = artistName,
                                    playCount = playCount
                                )
                                this@TopAlbumsFragment.viewModel.saveAlbumAsFavorite(
                                    favoriteAlbum = favoriteAlbum
                                )
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Add to favorite albums",
                                tint = Color.LightGray
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun AlbumImage(
        albumUrl: String
    ) {
        val painter = rememberImagePainter(data = albumUrl)

        Image(
            painter = painter,
            contentDescription = "Image of the album",
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(corner = CornerSize(8.dp))),
            contentScale = ContentScale.Crop
        )
    }
}
