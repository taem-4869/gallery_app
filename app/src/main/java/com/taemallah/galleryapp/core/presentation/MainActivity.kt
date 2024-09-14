package com.taemallah.galleryapp.core.presentation

import android.content.ContentUris
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.ActivityCompat
import androidx.core.net.toFile
import androidx.lifecycle.viewModelScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.taemallah.galleryapp.R
import com.taemallah.galleryapp.core.domain.Image
import com.taemallah.galleryapp.displayScreen.DisplayScreen
import com.taemallah.galleryapp.mainScreen.MainScreen
import com.taemallah.galleryapp.ui.theme.GalleryAppTheme
import com.taemallah.galleryapp.utils.Routs
import com.taemallah.galleryapp.utils.requiredPermissions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.net.URI

class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestPermissions()
        setContent {
            enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.light(MaterialTheme.colorScheme.primaryContainer.toArgb(),MaterialTheme.colorScheme.primary.toArgb()),
                navigationBarStyle = SystemBarStyle.light(MaterialTheme.colorScheme.primaryContainer.toArgb(),MaterialTheme.colorScheme.primary.toArgb())
            )
            GalleryAppTheme {
                Surface {
                    val state by viewModel.state.collectAsState()
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = Routs.MainScreen){
                        composable<Routs.MainScreen> {
                            MainScreen(state = state, onEvent = viewModel::onEvent)
                        }
                        composable<Routs.DisplayScreen> {
                            val startPosition = it.toRoute<Routs.DisplayScreen>().startPosition
                            DisplayScreen(
                                state = state,
                                onEvent = viewModel::onEvent,
                                startPosition = startPosition
                            )
                        }
                    }
                    LaunchedEffect(key1 = Unit) {
                        viewModel.eventsChannel.collect{ channelEvent->
                            when(channelEvent){
                                ChannelEvent.BackRequest -> {
                                    onBackPressedDispatcher.onBackPressed()
                                }
                                is ChannelEvent.DeleteImage -> {
                                    deleteImage(channelEvent.image)
                                }
                                is ChannelEvent.Navigate -> {
                                    navController.navigate(route = channelEvent.route)
                                }
                            }
                        }
                    }
                    onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true){
                        override fun handleOnBackPressed() {
                            if (navController.currentBackStackEntry!=null && navController.currentBackStackEntry!!.toRoute<Routs>() is Routs.DisplayScreen){
                                navController.popBackStack()
                            }
                        }
                    })
                }
            }
        }
    }

    private fun requestPermissions(){
        ActivityCompat.requestPermissions(
            this,
            requiredPermissions,
            1
        ).also {
            loadImagesFromStorage()
        }
    }

    private fun loadImagesFromStorage(){
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_TAKEN,
        )
        val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC"
        contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )?.use {cursor->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)

            val images = mutableListOf<Image>()

            while (cursor.moveToNext()){
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val dateTaken = cursor.getLong(dateColumn)
                val uri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )
                images.add(Image(id,name,dateTaken,uri))
            }
            viewModel.onEvent(MainEvent.UpdateImages(images))
        }
    }

    private fun deleteImage(image: Image){
        try {
            val deletedImagesCount = contentResolver.delete(image.uri,null,null)
            if (deletedImagesCount<1) throw IOException("Something went wrong: nothing deleted")
            Toast.makeText(this, getString(R.string.deleted_successfully),Toast.LENGTH_LONG).show()
            loadImagesFromStorage()
        }catch (e: Exception){
            e.printStackTrace()
            Toast.makeText(this, getString(R.string.failed_to_delete_file),Toast.LENGTH_LONG).show()
        }
    }

}

