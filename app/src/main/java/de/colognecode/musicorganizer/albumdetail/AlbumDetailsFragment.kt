package de.colognecode.musicorganizer.albumdetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import dagger.hilt.android.AndroidEntryPoint
import de.colognecode.musicorganizer.components.MusicOrganizerLoadingSpinner
import de.colognecode.musicorganizer.repository.network.model.DetailedAlbum
import de.colognecode.musicorganizer.repository.network.model.Tracks
import de.colognecode.musicorganizer.theme.MusicOrganizerTheme
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration

@ExperimentalTime
@ExperimentalCoilApi
@AndroidEntryPoint
class AlbumDetailsFragment : Fragment() {

    private val viewModel: AlbumDetailsViewModel by viewModels()
    private val args: AlbumDetailsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            val artist = args.artist ?: ""
            val album = args.album ?: ""
            this@AlbumDetailsFragment.viewModel.getAlbumDetails(
                artist = artist,
                album = album
            )
            setContent {
                val isLoading by viewModel.isLoading.observeAsState(false)
                val detailedAlbum by viewModel.albumDetails.observeAsState()
                if (isLoading && detailedAlbum == null) {
                    MusicOrganizerLoadingSpinner.LoadingSpinnerComposable()
                } else {
                    detailedAlbum?.let {
                        AlbumDetailsView(
                            albumDetails = it
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun AlbumDetailsView(albumDetails: DetailedAlbum) {
        MusicOrganizerTheme {
            Scaffold(
                topBar = { AppBar(albumDetails.name) },
                content = {
                    AlbumDetailsContent(
                        albumDetails = albumDetails
                    )
                }
            )
        }
    }

    @Composable
    fun AppBar(title: String) {
        TopAppBar(
            title = {
                Text(
                    text = title
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
    fun AlbumDetailsContent(
        albumDetails: DetailedAlbum
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                )
        ) {
            AlbumHeader(albumDetails)
            AlbumTracks(albumDetails.tracks)
        }
    }

    @Composable
    private fun AlbumHeader(albumDetails: DetailedAlbum) {
        val imageUrl = albumDetails.image.let { imageItems ->
            imageItems.find { imageItem ->
                imageItem.size == "large"
            }?.text ?: ""
        }
        AlbumImage(
            imageUrl = imageUrl
        )
        AlbumTitle(
            albumTitle = albumDetails.name
        )
        Row(
            modifier = Modifier
                .padding(
                    top = 4.dp
                )
                .fillMaxWidth(),
            verticalAlignment = Alignment.Bottom
        ) {
            ArtistName(
                artistName = albumDetails.artist
            )
            BulletPoint()
            TotalTracks(
                totalTracks = albumDetails.tracks.track.size
            )
            BulletPoint()
            TotalDuration(
                albumTracks = albumDetails.tracks
            )
        }
    }

    @Composable
    fun AlbumImage(imageUrl: String) {
        val painter = rememberImagePainter(data = imageUrl)

        Image(
            painter = painter,
            contentDescription = "Image of the detailed album",
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = 8.dp
                )
                .clip(
                    RoundedCornerShape(
                        corner = CornerSize(8.dp)
                    )
                ),
            contentScale = ContentScale.Crop
        )
    }

    @Composable
    fun AlbumTitle(albumTitle: String) {
        Text(
            modifier = Modifier.padding(
                top = 8.dp,
            ),
            style = MaterialTheme.typography.h4,
            text = albumTitle,
        )
    }

    @Composable
    fun ArtistName(artistName: String) {
        Text(
            text = artistName,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }

    @Composable
    fun BulletPoint() {
        Text(
            modifier = Modifier.padding(
                start = 8.dp,
                end = 8.dp,
            ),
            text = "•",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }

    @Composable
    fun TotalTracks(totalTracks: Int) {
        Text(
            text = "$totalTracks Songs",
            fontSize = 14.sp,
        )
    }

    @Composable
    fun TotalDuration(albumTracks: Tracks) {
        var totalDuration = 0L
        albumTracks.track.forEach {
            totalDuration += it.duration
        }
        val secondsDuration = totalDuration.toDuration(TimeUnit.SECONDS).inWholeSeconds
        val seconds = secondsDuration % 60
        val minutes = (secondsDuration % 3600) / 60
        val hours = secondsDuration / 3600
        val durationText = if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
        Text(
            text = durationText,
            fontSize = 14.sp
        )
    }

    @Composable
    fun AlbumTracks(tracks: Tracks) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(8.dp),

        ) {
            Text(
                modifier = Modifier.weight(1f, fill = true),
                text = "#"
            )
            Text(
                modifier = Modifier.weight(1f, fill = true),
                text = "Title"
            )
            Text(
                modifier = Modifier.weight(1f, fill = true),
                text = "Duration",
                textAlign = TextAlign.Center
            )
        }
    }

}
