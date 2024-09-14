package com.taemallah.galleryapp.displayScreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import coil.compose.AsyncImage
import com.taemallah.galleryapp.R
import com.taemallah.galleryapp.core.domain.Image
import com.taemallah.galleryapp.core.presentation.MainEvent
import com.taemallah.galleryapp.core.presentation.MainState
import kotlin.math.absoluteValue

@Composable
fun DisplayScreen (
    state: MainState,
    onEvent: (MainEvent) -> Unit,
    startPosition: Int = 0
){
    val animatedOpacity by animateFloatAsState(targetValue = if(state.isVisibleDisplayScreenUi) 1f else 0f, label = "")
    val pagerState = rememberPagerState(initialPage = startPosition, pageCount = {state.images.count()})
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect{
            onEvent(MainEvent.ShowDisplayLayoutUi)
        }
    }
    Scaffold {padding->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .clickable {
                    onEvent(MainEvent.ShowDisplayLayoutUi)
                }
        ){
            HorizontalPager(
                state = pagerState,
                beyondViewportPageCount = 1,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxSize()
            ) {
                AsyncImage(
                    model = state.images[it].uri,
                    contentDescription = "image",
                    alignment = Alignment.Center,
                    clipToBounds = true,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            val pageOffset =
                                ((pagerState.currentPage - it) + pagerState.currentPageOffsetFraction).absoluteValue
                            alpha = lerp(
                                start = 0.5f,
                                stop = 1f,
                                fraction = 1f - pageOffset.coerceIn(0f, 1f)
                            )
                        }
                )
            }
            TopBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .alpha(animatedOpacity),
                image = state.images[pagerState.currentPage],
                onEvent = onEvent,
            )
        }
    }
    AnimatedVisibility(visible = state.isVisibleAlertDialog) {
        var pressHandled by remember {
            mutableStateOf(false)
        }
        AlertDialog(
            onDismissRequest = { onEvent(MainEvent.SetIsVisibleAlertDialog(false)) },
            confirmButton = {
                Button(
                    enabled = !pressHandled,
                    onClick = {
                    pressHandled = true
                    onEvent(MainEvent.DeleteImage(pagerState.currentPage))
                    onEvent(MainEvent.SetIsVisibleAlertDialog(false))
                }) {
                    Text(text = stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !pressHandled,
                    onClick = {
                    pressHandled = true
                    onEvent(MainEvent.SetIsVisibleAlertDialog(false))
                }) {
                    Text(text = stringResource(R.string.cancel))
                }
            },
            text = {
                Text(text = stringResource(R.string.are_you_sure_you_want_to_delete_this_image))
            }
        )
    }
}

@Composable
fun TopBar(modifier: Modifier = Modifier, image: Image, onEvent: (MainEvent) -> Unit) {
    Row(
        modifier
            .background(
                brush = Brush.verticalGradient(
                    listOf(MaterialTheme.colorScheme.primaryContainer,MaterialTheme.colorScheme.primary.copy(alpha = .7f))
                )
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        IconButton(
            modifier = Modifier.weight(1f,fill = true),
            onClick = {
                onEvent(MainEvent.HideDisplayImageScreen)
            },
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                tint = Color.White,
                contentDescription = "back"
            )
        }
        Column(
            Modifier
                .weight(4f, fill = true)
                .padding(8.dp)
                .animateContentSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = image.name,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
                softWrap = true,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = image.getFormattedDateTaken(),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
                softWrap = true,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        IconButton(
            modifier = Modifier.weight(1f,fill = true),
            onClick = {
                onEvent(MainEvent.SetIsVisibleAlertDialog(true))
            },
        ) {
            Icon(
                imageVector = Icons.Filled.Delete,
                tint = Color.White,
                contentDescription = "delete"
            )
        }
    }
}