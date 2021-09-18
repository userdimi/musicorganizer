package de.colognecode.musicorganizer.favoritealbums

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider

class FavoriteAlbumsFragment : Fragment() {

    companion object {
        fun newInstance() = FavoriteAlbumsFragment()
    }

    private lateinit var viewModel: FavoriteAlbumsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                DefaultPreview()
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(FavoriteAlbumsViewModel::class.java)
        // TODO: Use the ViewModel
    }

    @Preview
    @Composable
    fun DefaultPreview() {
        FavoriteAlbumsLayout()
    }

    @Composable()
    fun FavoriteAlbumsLayout() {
        AppTopBar()
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            NoFavoriteAlbumsMessage()
        }
    }

    @Preview
    @Composable
    fun AppTopBar() {
        TopAppBar(
            title = { Text(text = "Favorite Albums") },
            actions = {
                IconButton(
                    onClick = {
                        // TODO: 18.09.21 display search widget  
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search"
                    )
                }
            }
        )
    }

    @Preview
    @Composable
    fun NoFavoriteAlbumsMessage() {
        Column(
            modifier = Modifier
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                )
        ) {
            Text(
                text = "You don't have any favorite albums at the moment.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.body1,
                modifier = Modifier.padding(
                    bottom = 8.dp
                )
            )
            Text(
                text = "Lets start a search add some albums of your favorite artists.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.body2
            )
        }
    }
}
