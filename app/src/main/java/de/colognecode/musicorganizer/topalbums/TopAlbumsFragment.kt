package de.colognecode.musicorganizer.topalbums

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import de.colognecode.musicorganizer.theme.MusicOrganizerTheme

class TopAlbumsFragment : Fragment() {

    private val viewModel: TopAlbumsViewModel by viewModels()
    private val args: TopAlbumsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent { }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val artist = args.artist
        if (!artist.isNullOrEmpty()) {
            this.viewModel.getTopAlbums(artist)
        } else {
            // TODO: 28.09.21 show error message 
        }
    }

    @Composable
    fun TopAlbums() {
        MusicOrganizerTheme {
            Scaffold(
                topBar = { AppBar() }
            ) {
            }
        }
    }

    @Preview
    @Composable
    fun AppBar() {
        TopAppBar(
            title = { Text(text = "Top Albums of ...") },
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
}
