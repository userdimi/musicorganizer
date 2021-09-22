package de.colognecode.musicorganizer.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import coil.compose.rememberImagePainter
import coil.transform.CircleCropTransformation
import dagger.hilt.android.AndroidEntryPoint
import de.colognecode.musicorganizer.repository.network.model.ArtistItem
import de.colognecode.musicorganizer.theme.MusicOrganizerTheme

@AndroidEntryPoint
class SearchFragment : Fragment() {

    private val viewModel: SearchViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                ArtistsSearch()
            }
        }
    }

    @Preview
    @Composable
    fun ArtistsSearch() {
        MusicOrganizerTheme {
            Scaffold(
                topBar = { AppBar() },
                content = { Content() }
            )
        }
    }

    @Preview
    @Composable
    fun AppBar() {
        TopAppBar(
            title = { Text(text = "Search Artists") },
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

    @Preview
    @Composable
    fun Content() {
        Row {
            SearchBar()
            ArtistsResult()
        }
    }

    @Preview
    @Composable
    fun SearchBar() {
        var searchText by remember { mutableStateOf("") }
        TextField(
            value = searchText,
            onValueChange = {
                searchText = it
            },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 1,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search"
                )
            },
            label = {
                Text(text = "Search for artists")
            },
            keyboardActions = KeyboardActions(
                onDone = {
                    if (searchText.isNotEmpty()) {
                        viewModel.getSearchResults(searchText)
                    }
                }
            ),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Search
            )
        )
    }

    @Preview
    @Composable
    fun LoadingSpinner() {
        val isProgressbarVisible by viewModel.isProgressbarVisible.observeAsState(false)
        if (isProgressbarVisible) {
            CircularProgressIndicator(
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    @Preview
    @Composable
    fun ArtistsResult() {
        val artistSearchResults: State<List<ArtistItem?>?> =
            viewModel.artistsSearchResults.observeAsState(
                emptyList()
            )
        if (artistSearchResults.value?.isNotEmpty() == true) {
            artistSearchResults.value?.forEach { artistSearchResult ->
                val imageItem = artistSearchResult?.image?.find {
                    it?.size == "small"
                }
                ArtistResultItem(
                    imageUrl = imageItem?.text,
                    artistName = artistSearchResult?.name
                )
            }
        }
    }

    @Composable
    fun ArtistResultItem(
        imageUrl: String?,
        artistName: String?
    ) {
        Column(
            modifier = Modifier.apply {
                fillMaxWidth()
                padding(8.dp)
            }
        ) {
            Image(
                painter = rememberImagePainter(
                    data = imageUrl,
                    builder = {
                        crossfade(true)
                        transformations(CircleCropTransformation())
                    }
                ),
                contentDescription = "Image of the artist"
            )
            Text(text = artistName ?: "")
        }
    }
}
