package de.colognecode.musicorganizer.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import de.colognecode.musicorganizer.search.SearchViewModel.Companion.ARTIST_SEARCH_RESULT_PAGE_SIZE
import de.colognecode.musicorganizer.theme.MusicOrganizerTheme
import de.colognecode.musicorganizer.components.MusicOrganizerLoadingSpinner

@ExperimentalCoilApi
@ExperimentalComposeUiApi
@AndroidEntryPoint
class SearchFragment : Fragment() {

    private val viewModel: SearchViewModel by viewModels()
    private var artist = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val isProgressbarVisible by viewModel.isLoading.observeAsState(false)
                val artistsSearchResults by viewModel.artistsSearchResults.observeAsState(
                    initial = emptyList()
                )
                val artistsSearchResultPage by viewModel.page
                ArtistsSearch(
                    isProgressbarVisible = isProgressbarVisible,
                    artistSearchResults = artistsSearchResults,
                    artistsSearchResultPage = artistsSearchResultPage
                )
            }
        }
    }

    @Composable
    fun ArtistsSearch(
        isProgressbarVisible: Boolean?,
        artistSearchResults: List<ArtistItem?>?,
        artistsSearchResultPage: Int
    ) {
        MusicOrganizerTheme {
            Scaffold(
                topBar = { AppBar() },
                content = {
                    Content(
                        isProgressbarVisible = isProgressbarVisible,
                        artistSearchResults = artistSearchResults,
                        artistsSearchResultPage = artistsSearchResultPage
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
    fun Content(
        isProgressbarVisible: Boolean?,
        artistSearchResults: List<ArtistItem?>?,
        artistsSearchResultPage: Int
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            SearchBar()
            ArtistsSearchResults(
                artistSearchResults = artistSearchResults,
                isProgressbarVisible = isProgressbarVisible,
                artistsSearchResultPage = artistsSearchResultPage

            )
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
                artist = it
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

    @Composable
    fun ArtistsSearchResults(
        artistSearchResults: List<ArtistItem?>?,
        artistsSearchResultPage: Int,
        isProgressbarVisible: Boolean?
    ) {
        Box(
            modifier = Modifier
                .background(color = MaterialTheme.colors.surface)
                .fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            artistSearchResults?.let {
                if (isProgressbarVisible == true && artistSearchResults.isEmpty()) {
                    MusicOrganizerLoadingSpinner.LoadingSpinnerComposable()
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(8.dp, vertical = 8.dp),
                    ) {
                        itemsIndexed(
                            items = artistSearchResults
                        ) { index: Int, artistSearchResultItem: ArtistItem? ->
                            var imageUrl = ""
                            artistSearchResultItem?.image?.let { imageItems ->
                                imageUrl = imageItems.find { imageItem ->
                                    imageItem?.size == "extralarge"
                                }?.text ?: ""
                            }
                            this@SearchFragment.viewModel.onArtistSearchResultScrollPositionChanged(
                                position = index
                            )
                            if ((index + 1) >=
                                (artistsSearchResultPage * ARTIST_SEARCH_RESULT_PAGE_SIZE) &&
                                isProgressbarVisible == false
                            ) {
                                this@SearchFragment.viewModel.getNextPageSearchResults(artist)
                            }
                            ArtistSearchResultCard(
                                artistImageUrl = imageUrl,
                                artistName = artistSearchResultItem?.name,
                                artistInfoUrl = artistSearchResultItem?.url
                            )
                        }
                    }
                }
            }
            if (isProgressbarVisible == true) {
                MusicOrganizerLoadingSpinner.LoadingSpinnerComposable()
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

    @Composable
    fun ArtistSearchResultCard(
        artistImageUrl: String?,
        artistName: String?,
        artistInfoUrl: String?
    ) {
        Card(
            modifier = Modifier
                .padding(
                    horizontal = 8.dp,
                    vertical = 8.dp
                )
                .clickable(
                    onClick = {
                        val action =
                            SearchFragmentDirections.actionSearchFragmentToTopAlbumsFragment(
                                artistName
                            )
                        findNavController().navigate(action)
                    }
                )
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
