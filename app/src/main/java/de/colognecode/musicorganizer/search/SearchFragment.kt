package de.colognecode.musicorganizer.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import dagger.hilt.android.AndroidEntryPoint
import de.colognecode.musicorganizer.repository.network.model.ArtistItem
import de.colognecode.musicorganizer.theme.MusicOrganizerTheme
import de.colognecode.musicorganizer.theme.Purple_700

@ExperimentalCoilApi
@ExperimentalComposeUiApi
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
                val isProgressbarVisible by viewModel.isProgressbarVisible.observeAsState(false)
                val artistsSearchResults by viewModel.artistsSearchResults.observeAsState(initial = emptyList())
                ArtistsSearch(
                    isProgressbarVisible = isProgressbarVisible,
                    artistSearchResults = artistsSearchResults
                )
            }
        }
    }

    @Composable
    fun ArtistsSearch(isProgressbarVisible: Boolean?, artistSearchResults: List<ArtistItem?>?) {
        MusicOrganizerTheme {
            Scaffold(
                topBar = { AppBar() },
                content = {
                    Content(
                        isProgressbarVisible = isProgressbarVisible,
                        artistSearchResults = artistSearchResults
                    )
                }
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

    @Composable
    fun Content(isProgressbarVisible: Boolean?, artistSearchResults: List<ArtistItem?>?) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            SearchBar()
            if (isProgressbarVisible == true) {
                LoadingSpinner()
            } else {
                ArtistsSearchResults(artistSearchResults = artistSearchResults)
            }
        }
    }


    @ExperimentalComposeUiApi
    @Preview
    @Composable
    fun SearchBar() {
        var searchText by remember { mutableStateOf("") }
        var searchHintText by remember { mutableStateOf("Search for artists") }
        val keyBoardController = LocalSoftwareKeyboardController.current
        val focusManager = LocalFocusManager.current
        TextField(
            value = searchText,
            onValueChange = {
                searchText = it
                searchHintText = it
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            colors = TextFieldDefaults.textFieldColors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            singleLine = true,
            shape = RoundedCornerShape(corner = CornerSize(16.dp)),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search"
                )
            },
            label = {
                Text(text = searchHintText)
            },
            keyboardActions = KeyboardActions(
                onSearch = {
                    if (searchText.isNotEmpty()) {
                        viewModel.getSearchResults(searchText)
                        searchText = ""
                        keyBoardController?.hide()
                        focusManager.clearFocus()
                    }
                },
            ),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Search
            )
        )
    }

    @Preview
    @Composable
    fun LoadingSpinner() {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            CircularProgressIndicator(
                color = Purple_700
            )
        }
    }


    @Composable
    fun ArtistsSearchResults(artistSearchResults: List<ArtistItem?>?) {
        artistSearchResults?.let {
            LazyColumn(
                contentPadding = PaddingValues(8.dp, vertical = 8.dp),
                //verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = artistSearchResults, itemContent = { artistSearchResultItem ->
                        var imageUrl = ""
                        artistSearchResultItem?.image?.let { imageItems ->
                            imageUrl = imageItems.find { imageItem ->
                                imageItem?.size == "extralarge"
                            }?.text ?: ""
                        }
                        ArtistSearchResultCard(
                            artistImageUrl = imageUrl,
                            artistName = artistSearchResultItem?.name,
                            artistInfoUrl = artistSearchResultItem?.url
                        )
                    }
                )
            }

        }
    }

    @Composable
    fun ArtistImage(artistImageUrl: String?) {
        val painter =
            rememberImagePainter(data = artistImageUrl)

        Image(
            painter = painter,
            contentDescription = "Image of the artist",
            modifier = Modifier
                .padding(8.dp)
                .size(84.dp)
                .clip(RoundedCornerShape(corner = CornerSize(16.dp))),
            contentScale = ContentScale.Crop
        )
    }

    //@Preview
    @Composable
    fun ArtistSearchResultCard(
        artistImageUrl: String?,
        artistName: String?,
        artistInfoUrl: String?
    ) {
        Card(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(corner = CornerSize(16.dp)),
            elevation = 2.dp,
            backgroundColor = MaterialTheme.colors.surface,
        ) {
            Row {
                ArtistImage(artistImageUrl = artistImageUrl)
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .align(Alignment.CenterVertically)
                ) {
                    Text(
                        text = artistName ?: "",
                        style = MaterialTheme.typography.h6,
                        color = MaterialTheme.colors.onSurface,
                    )
                    Text(
                        text = "More Info: $artistInfoUrl",
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.onSurface,

                    )
                }
            }
        }
    }
}

