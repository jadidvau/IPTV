package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.PlaylistItem
import com.example.ui.components.CyberBackground
import com.example.ui.components.CyberGray
import com.example.ui.components.CyberSurface
import com.example.ui.components.CyberWhite
import com.example.ui.components.GlassmorphicPanel
import com.example.ui.components.ImmersiveBackground
import com.example.ui.components.NeonOrchid
import com.example.ui.components.NeonPurple
import com.example.ui.components.cyberNetGrid
import com.example.ui.components.getCyberAtmosphereBrush
import com.example.ui.player.VideoPlayer
import com.example.ui.viewmodel.IPTVViewModel
import com.example.ui.viewmodel.ImportState

@Composable
fun DashboardScreen(
    viewModel: IPTVViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Gather states
    val channels by viewModel.filteredChannels.collectAsState()
    val rawChannelsState by viewModel.allChannels.collectAsState(initial = emptyList())
    val favorites by viewModel.favoriteChannels.collectAsState(initial = emptyList())
    val selectedChannel by viewModel.selectedChannel.collectAsState()
    val playlists by viewModel.playlistNames.collectAsState(initial = emptyList())
    
    val searchQuery by viewModel.searchQuery.collectAsState()
    val currentCategory by viewModel.selectedCategory.collectAsState()
    val currentPlaylist by viewModel.selectedPlaylistFilter.collectAsState()
    val importState by viewModel.importState.collectAsState()
    val categories by viewModel.availableCategories.collectAsState()

    // Dialog state controllers
    var showImportDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }

    // Display Toast notification on import state matches
    LaunchedEffect(importState) {
        when (importState) {
            is ImportState.Success -> {
                Toast.makeText(context, "Successfully loaded ${(importState as ImportState.Success).count} channels!", Toast.LENGTH_LONG).show()
                viewModel.clearImportStatus()
                showImportDialog = false
            }
            is ImportState.Error -> {
                Toast.makeText(context, "Playback Error: ${(importState as ImportState.Error).message}", Toast.LENGTH_LONG).show()
                viewModel.clearImportStatus()
            }
            else -> {}
        }
    }

    ImmersiveBackground {
        Scaffold(
            containerColor = Color.Transparent,
            modifier = modifier.fillMaxSize()
        ) { innerPadding ->
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                val isTablet = maxWidth >= 768.dp

                if (isTablet) {
                    // Wide / Tablet Canonical Screen Composition (List-Detail Split screen)
                    Row(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Left Pane: Active Live Media Video section
                        Column(
                            modifier = Modifier
                                .weight(1.3f)
                                .fillMaxHeight()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.Top
                        ) {
                            JadidHeaderBlock(
                                onImportClick = { showImportDialog = true },
                                onInfoClick = { showInfoDialog = true },
                                onResetClick = { viewModel.resetToDefaults() }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            if (selectedChannel != null) {
                                GlassmorphicPanel(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    VideoPlayer(
                                        url = selectedChannel!!.url,
                                        channelName = selectedChannel!!.name,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                GlassmorphicPanel(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = selectedChannel!!.name,
                                            color = CyberWhite,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Category: ${selectedChannel!!.category}  |  Playlist Source: ${selectedChannel!!.playlistName}",
                                            color = NeonOrchid,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Button(
                                                onClick = { viewModel.toggleFavorite(selectedChannel!!) },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (selectedChannel!!.isFavorite) Color(0xFFDC2626) else NeonPurple
                                                ),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (selectedChannel!!.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                    contentDescription = "Favorite Toggle",
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = if (selectedChannel!!.isFavorite) "Bookmarked" else "Bookmark Channel",
                                                    fontSize = 12.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                EmptyPlayerWidget(
                                    totalChannels = rawChannelsState.size,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // Right Pane: Channels manager index controls
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(CyberSurface.copy(alpha = 0.6f))
                                .padding(16.dp)
                        ) {
                            SearchAndFiltersCluster(
                                searchQuery = searchQuery,
                                onQueryChange = { viewModel.updateSearchQuery(it) },
                                playlists = playlists,
                                currentPlaylist = currentPlaylist,
                                onSelectPlaylist = { viewModel.selectPlaylistFilter(it) },
                                categories = categories,
                                currentCategory = currentCategory,
                                onSelectCategory = { viewModel.selectCategory(it) },
                                onDeletePlaylist = { viewModel.deletePlaylist(it) }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            ChannelsIndexList(
                                channels = channels,
                                currentChannel = selectedChannel,
                                onChannelSelect = { viewModel.selectChannel(it) },
                                onFavoriteToggle = { viewModel.toggleFavorite(it) },
                                onChannelDelete = { viewModel.deleteChannel(it) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                } else {
                    // Mobile Portrait Screen layout flow (Grid Stack)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {
                        JadidHeaderBlock(
                            onImportClick = { showImportDialog = true },
                            onInfoClick = { showInfoDialog = true },
                            onResetClick = { viewModel.resetToDefaults() }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Video Player occupies prime spot at the top if loaded
                        if (selectedChannel != null) {
                            VideoPlayer(
                                url = selectedChannel!!.url,
                                channelName = selectedChannel!!.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                            )
                            
                            // Compact info box below player
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = selectedChannel!!.name,
                                        color = CyberWhite,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${selectedChannel!!.category} • ${selectedChannel!!.playlistName}",
                                        color = NeonOrchid,
                                        fontSize = 11.sp
                                    )
                                }
                                Row {
                                    IconButton(
                                        onClick = { viewModel.toggleFavorite(selectedChannel!!) },
                                        modifier = Modifier.testTag("bookmark_active_button")
                                    ) {
                                        Icon(
                                            imageVector = if (selectedChannel!!.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                            contentDescription = "Favorite active",
                                            tint = if (selectedChannel!!.isFavorite) Color(0xFFF43F5E) else CyberGray
                                        )
                                    }
                                    IconButton(onClick = { viewModel.selectChannel(null) }) {
                                        Icon(Icons.Default.Close, contentDescription = "Close Player", tint = CyberGray)
                                    }
                                }
                            }
                        } else {
                            EmptyPlayerWidget(totalChannels = rawChannelsState.size, modifier = Modifier.height(140.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        // Search and filter options
                        SearchAndFiltersCluster(
                            searchQuery = searchQuery,
                            onQueryChange = { viewModel.updateSearchQuery(it) },
                            playlists = playlists,
                            currentPlaylist = currentPlaylist,
                            onSelectPlaylist = { viewModel.selectPlaylistFilter(it) },
                            categories = categories,
                            currentCategory = currentCategory,
                            onSelectCategory = { viewModel.selectCategory(it) },
                            onDeletePlaylist = { viewModel.deletePlaylist(it) }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Dynamic channel inventory list
                        ChannelsIndexList(
                            channels = channels,
                            currentChannel = selectedChannel,
                            onChannelSelect = { viewModel.selectChannel(it) },
                            onFavoriteToggle = { viewModel.toggleFavorite(it) },
                            onChannelDelete = { viewModel.deleteChannel(it) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Add Channel/Playlist Dialogue Layer
        if (showImportDialog) {
            ImportPlaylistDialog(
                importState = importState,
                onDismiss = {
                    viewModel.clearImportStatus()
                    showImportDialog = false
                },
                onQuickAdd = { name, url, cat, logo, playlist ->
                    viewModel.addNewChannel(name, url, cat, logo, playlist)
                    showImportDialog = false
                    Toast.makeText(context, "Added single channel: $name", Toast.LENGTH_SHORT).show()
                },
                onImportM3U = { listName, url ->
                    viewModel.importM3U(url, listName)
                }
            )
        }

        // Info / About Dialog Screen Overlay
        if (showInfoDialog) {
            AboutDialog(onDismiss = { showInfoDialog = false })
        }
    }
}

/**
 * Top brand Header block component style
 */
@Composable
fun JadidHeaderBlock(
    onImportClick: () -> Unit,
    onInfoClick: () -> Unit,
    onResetClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF9333EA)) // Purple-600
                    .clickable { onInfoClick() }
                    .drawBehind {
                        // Soft glow outline representation
                        drawCircle(
                            color = Color(0xFFA855F7).copy(alpha = 0.35f),
                            radius = this.size.width * 0.7f,
                            center = this.center
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Tv,
                    contentDescription = "Jadid Icon",
                    tint = CyberWhite,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "JADID",
                        color = CyberWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        letterSpacing = (-0.75).sp
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "IPTV",
                        color = Color(0xFFA855F7), // Purple-500
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        letterSpacing = (-0.75).sp
                    )
                }
                Text(
                    text = "PREMIUM EXPERIENCE",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onResetClick,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f))
                    .size(36.dp)
                    .testTag("reset_db_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset Database",
                    tint = CyberWhite.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onImportClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9333EA)),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                modifier = Modifier
                    .height(36.dp)
                    .testTag("open_import_dialog_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Plus Icon",
                    tint = CyberWhite,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * Filter panel aggregation UI cluster
 */
@Composable
fun SearchAndFiltersCluster(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    playlists: List<String>,
    currentPlaylist: String,
    onSelectPlaylist: (String) -> Unit,
    categories: List<String>,
    currentCategory: String,
    onSelectCategory: (String) -> Unit,
    onDeletePlaylist: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Search Input Line
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onQueryChange,
            placeholder = { Text("Search channels, streams or categories...", fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "search", tint = CyberGray) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.Black.copy(alpha = 0.3f),
                unfocusedContainerColor = Color.Black.copy(alpha = 0.15f),
                focusedBorderColor = NeonPurple.copy(alpha = 0.8f),
                unfocusedBorderColor = Color.White.copy(alpha = 0.08f),
                focusedTextColor = CyberWhite,
                unfocusedTextColor = CyberWhite.copy(alpha = 0.8f),
                focusedPlaceholderColor = CyberGray,
                unfocusedPlaceholderColor = CyberGray
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_channels_input"),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Playlists Selector Swiper
        Text(
            text = "PLAYLIST SOURCES",
            color = CyberGray,
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 2.dp, bottom = 6.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                PlaylistFilterChip(
                    name = "All Streams",
                    isSelected = currentPlaylist == "All",
                    onSelect = { onSelectPlaylist("All") },
                    onDelete = null
                )
            }
            items(playlists) { plName ->
                PlaylistFilterChip(
                    name = plName,
                    isSelected = currentPlaylist == plName,
                    onSelect = { onSelectPlaylist(plName) },
                    onDelete = if (plName != "Default") { { onDeletePlaylist(plName) } } else null
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Category Rows Selector
        Text(
            text = "CATEGORIES",
            color = CyberGray,
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 2.dp, bottom = 6.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories) { cat ->
                val isSelected = currentCategory == cat
                val animatedColor by animateColorAsState(if (isSelected) NeonPurple else Color.White.copy(alpha = 0.05f))
                val borderCol = if (isSelected) NeonOrchid.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.05f)

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(animatedColor)
                        .border(1.dp, borderCol, RoundedCornerShape(8.dp))
                        .clickable { onSelectCategory(cat) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = cat,
                        color = if (isSelected) Color.White else CyberWhite.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun PlaylistFilterChip(
    name: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDelete: (() -> Unit)?
) {
    val animatedBg by animateColorAsState(if (isSelected) NeonPurple.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.03f))
    val animatedBorder by animateColorAsState(if (isSelected) NeonPurple else Color.White.copy(alpha = 0.08f))

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(animatedBg)
            .border(1.dp, animatedBorder, RoundedCornerShape(10.dp))
            .clickable { onSelect() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Tv,
            contentDescription = "Playlist filter icon",
            tint = if (isSelected) NeonOrchid else CyberGray,
            modifier = Modifier.size(11.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = name,
            color = if (isSelected) CyberWhite else CyberWhite.copy(alpha = 0.7f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
        if (onDelete != null) {
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete Playlist",
                tint = Color(0xFFEF4444).copy(alpha = 0.7f),
                modifier = Modifier
                    .size(13.dp)
                    .clickable { onDelete() }
            )
        }
    }
}

/**
 * List holding filtered channels index
 */
@Composable
fun ChannelsIndexList(
    channels: List<PlaylistItem>,
    currentChannel: PlaylistItem?,
    onChannelSelect: (PlaylistItem) -> Unit,
    onFavoriteToggle: (PlaylistItem) -> Unit,
    onChannelDelete: (PlaylistItem) -> Unit,
    modifier: Modifier = Modifier
) {
    if (channels.isEmpty()) {
        Box(
            modifier = modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "no channels",
                    tint = CyberGray,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No channels match filters",
                    color = CyberWhite.copy(alpha = 0.5f),
                    fontSize = 12.sp
                )
            }
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(channels, key = { it.id }) { item ->
                val isActive = currentChannel?.id == item.id
                val animatedBorder by animateColorAsState(if (isActive) Color(0xFFC084FC).copy(alpha = 0.45f) else Color.White.copy(alpha = 0.05f))
                val animatedBg by animateColorAsState(if (isActive) Color(0xFF9333EA).copy(alpha = 0.18f) else Color.White.copy(alpha = 0.04f))

                Card(
                    onClick = { onChannelSelect(item) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = animatedBg),
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem()
                        .border(1.dp, animatedBorder, RoundedCornerShape(16.dp))
                        .testTag("channel_item_${item.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Channel Logo thumbnail
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = 0.4f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!item.logoUrl.isNullOrEmpty()) {
                                AsyncImage(
                                    model = item.logoUrl,
                                    contentDescription = "${item.name} logo",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "TV",
                                    tint = if (isActive) NeonOrchid else CyberGray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.name,
                                color = CyberWhite,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Surface(
                                    color = if (isActive) NeonPurple.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = item.category.uppercase(),
                                        color = if (isActive) NeonOrchid else CyberGray,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                                if (item.playlistName != "Default") {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = item.playlistName,
                                        color = CyberGray,
                                        fontSize = 9.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        // Bookmark Favorite Heart + Delete Custom trigger row
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { onFavoriteToggle(item) },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = if (item.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Toggle favorite",
                                    tint = if (item.isFavorite) Color(0xFFF43F5E) else CyberGray.copy(alpha = 0.6f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            if (item.playlistName != "Default") {
                                IconButton(
                                    onClick = { onChannelDelete(item) },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Channel",
                                        tint = Color(0xFFEF4444).copy(alpha = 0.6f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Empty Video state fallback widget
 */
@Composable
fun EmptyPlayerWidget(
    totalChannels: Int,
    modifier: Modifier = Modifier
) {
    GlassmorphicPanel(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 24.dp,
        borderColor = Color.White.copy(alpha = 0.08f),
        backgroundColor = Color.White.copy(alpha = 0.04f)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp).align(Alignment.Center)
        ) {
            Icon(
                imageVector = Icons.Default.Tv,
                contentDescription = "Wireframe TV icon",
                tint = NeonPurple.copy(alpha = 0.4f),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Jadid Engine Idle",
                color = CyberWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Tap any of the $totalChannels channels below to launch real-time HLS streams, or hit 'Add' to upload custom playlists.",
                color = CyberGray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp),
                lineHeight = 16.sp
            )
        }
    }
}

/**
 * Dialog to add single channels OR import bulk M3U playlists
 */
@Composable
fun ImportPlaylistDialog(
    importState: ImportState,
    onDismiss: () -> Unit,
    onQuickAdd: (name: String, url: String, category: String, logoUrl: String?, playlistName: String) -> Unit,
    onImportM3U: (listName: String, url: String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }

    // Tab 1 state
    var singleName by remember { mutableStateOf("") }
    var singleUrl by remember { mutableStateOf("") }
    var singleCategory by remember { mutableStateOf("") }
    var singleLogo by remember { mutableStateOf("") }
    var singlePlaylistName by remember { mutableStateOf("My Custom") }

    // Tab 2 state
    var m3uPlaylistName by remember { mutableStateOf("") }
    var m3uUrl by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CyberSurface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(24.dp))
                .testTag("import_dialog_card")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add Stream Sources",
                        color = CyberWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "close", tint = CyberGray)
                    }
                }

                // Tab Switcher headers
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = NeonOrchid,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            color = NeonPurple,
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab])
                        )
                    },
                    divider = {},
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Single Channel", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("M3U Playlist URL", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                }

                if (selectedTab == 0) {
                    // Quick add forms
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = singleName,
                            onValueChange = { singleName = it },
                            label = { Text("Channel Name (Required)", fontSize = 11.sp) },
                            singleLine = true,
                            colors = getFormColors(),
                            modifier = Modifier.fillMaxWidth().testTag("add_channel_name_input")
                        )
                        OutlinedTextField(
                            value = singleUrl,
                            onValueChange = { singleUrl = it },
                            label = { Text("HLS / Video URL (Required)", fontSize = 11.sp) },
                            singleLine = true,
                            placeholder = { Text("https://url.to/stream.m3u8") },
                            colors = getFormColors(),
                            modifier = Modifier.fillMaxWidth().testTag("add_channel_url_input")
                        )
                        OutlinedTextField(
                            value = singleCategory,
                            onValueChange = { singleCategory = it },
                            label = { Text("Category (e.g. Sports, News)", fontSize = 11.sp) },
                            singleLine = true,
                            colors = getFormColors(),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = singlePlaylistName,
                            onValueChange = { singlePlaylistName = it },
                            label = { Text("Playlist Name (Group group name)", fontSize = 11.sp) },
                            singleLine = true,
                            colors = getFormColors(),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = singleLogo,
                            onValueChange = { singleLogo = it },
                            label = { Text("Channel Logo Thumbnail URL (Optional)", fontSize = 11.sp) },
                            singleLine = true,
                            colors = getFormColors(),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                if (singleName.isNotBlank() && singleUrl.isNotBlank()) {
                                    onQuickAdd(singleName, singleUrl, singleCategory, singleLogo, singlePlaylistName)
                                }
                            },
                            enabled = singleName.isNotBlank() && singleUrl.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonPurple, disabledContainerColor = CyberGray.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth().height(44.dp).testTag("save_single_channel_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Save Channel", fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // M3U internet download form
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Download IPTV playlist directory from any public .m3u or .txt playlist URL. Jadid Engine will automatically parse entries.",
                            color = CyberGray,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        OutlinedTextField(
                            value = m3uPlaylistName,
                            onValueChange = { m3uPlaylistName = it },
                            label = { Text("Playlist Label (e.g. BD-List)", fontSize = 11.sp) },
                            singleLine = true,
                            colors = getFormColors(),
                            modifier = Modifier.fillMaxWidth().testTag("add_playlist_name_input")
                        )

                        OutlinedTextField(
                            value = m3uUrl,
                            onValueChange = { m3uUrl = it },
                            label = { Text("M3U Playlist URL (Required)", fontSize = 11.sp) },
                            singleLine = true,
                            placeholder = { Text("https://some-service.net/list.m3u") },
                            colors = getFormColors(),
                            modifier = Modifier.fillMaxWidth().testTag("add_playlist_url_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        if (importState is ImportState.Loading) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(
                                        color = NeonOrchid,
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text("Fetching and Parsing stream catalogue...", color = CyberGray, fontSize = 12.sp)
                                }
                            }
                        } else {
                            Button(
                                onClick = {
                                    if (m3uPlaylistName.isNotBlank() && m3uUrl.isNotBlank()) {
                                        onImportM3U(m3uPlaylistName, m3uUrl)
                                    }
                                },
                                enabled = m3uPlaylistName.isNotBlank() && m3uUrl.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple, disabledContainerColor = CyberGray.copy(alpha = 0.2f)),
                                modifier = Modifier.fillMaxWidth().height(44.dp).testTag("save_playlist_button"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Import Playlist Links", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun getFormColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color.Black.copy(alpha = 0.2f),
    unfocusedContainerColor = Color.Black.copy(alpha = 0.1f),
    focusedBorderColor = NeonPurple,
    unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
    focusedTextColor = CyberWhite,
    unfocusedTextColor = CyberWhite.copy(alpha = 0.8f),
    focusedLabelColor = NeonOrchid,
    unfocusedLabelColor = CyberGray
)

@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CyberSurface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    color = NeonPurple,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(54.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Tv,
                            contentDescription = "Jadid logo",
                            tint = CyberWhite,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Jadid IPTV Player",
                    color = CyberWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "v1.0.0 (Jetpack Media3 Release)",
                    color = NeonOrchid,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Jadid IPTV is a highly responsive client-side IPTV streaming app designed for high fidelity playback in Android. Utilizing hardware accelerations, it decodes live HLS (.m3u8), MP4, and standard streaming configurations instantly.",
                    color = CyberGray,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Dismiss Info", color = CyberWhite, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
