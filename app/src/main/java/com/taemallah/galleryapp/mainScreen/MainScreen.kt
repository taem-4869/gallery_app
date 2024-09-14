package com.taemallah.galleryapp.mainScreen

import android.content.pm.PackageManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import coil.compose.AsyncImage
import com.taemallah.galleryapp.R
import com.taemallah.galleryapp.core.presentation.MainEvent
import com.taemallah.galleryapp.core.presentation.MainState
import com.taemallah.galleryapp.utils.requiredPermissions

@Composable
fun MainScreen(state: MainState, onEvent: (MainEvent)->Unit) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        AnimatedContent(targetState = state.images.isEmpty(), label = "") {
            if (!it){
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(4),
                    modifier = Modifier
                        .padding(innerPadding)
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalItemSpacing = 8.dp,
                ) {
                    itemsIndexed(state.images){index,image->
                        OutlinedCard(
                            elevation = CardDefaults.cardElevation(4.dp),
                            border = BorderStroke(
                                width = 2.dp,
                                brush = Brush.linearGradient(
                                    listOf(
                                        Color.White,
                                        MaterialTheme.colorScheme.primary,
                                        Color.White,
                                    )
                                )
                            ),
                            onClick = {
                                onEvent(MainEvent.ShowDisplayImageScreen(index))
                            }
                        ) {
                            AsyncImage(
                                model = image.uri,
                                contentDescription = "image",
                                alignment = Alignment.Center,
                                clipToBounds = true,
                            )
                        }
                    }
                }
            }else if (ActivityCompat.checkSelfPermission(LocalContext.current,requiredPermissions[0])== PackageManager.PERMISSION_DENIED){
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
                    Text(
                        text = stringResource(R.string.images_access_permission_denied),
                        textAlign = TextAlign.Center
                    )
                }
            }else{
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
                    Text(
                        text = stringResource(R.string.no_images_found),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}