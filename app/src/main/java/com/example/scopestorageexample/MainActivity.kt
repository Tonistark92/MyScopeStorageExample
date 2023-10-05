package com.example.scopestorageexample

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberImagePainter
import com.example.scopestorageexample.ui.theme.ScopeStorageExampleTheme
import com.example.scopestorageexample.ui.theme.dropDownMenu
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import java.util.UUID

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ScopeStorageExampleTheme {
                MyComposable()
            }
        }
    }

}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MyComposable() {
    val context = LocalContext.current
    val activity = LocalView.current.context as? ComponentActivity
    var photosShared by remember { mutableStateOf<List<SharedStoragePhoto>>(emptyList()) }

    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val viewModel: MyViewmodle = viewModel()
    val multiplePermissionsState = rememberMultiplePermissionsState(
        listOf(
            android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA
        )
    )

    if (multiplePermissionsState.allPermissionsGranted) {
        viewModel.loadShared(context)
        viewModel.loadShared(context)
        photosShared = viewModel.photosStateShared.value
        Log.d("TAGEff", photosShared.toString())
    }

    val takePictureLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.TakePicturePreview(),
            onResult = { bitmap: Bitmap? ->
                if (bitmap != null) {
                    // Photo captured successfully, you can display the bitmap
                    capturedBitmap = bitmap
                } else {
                    // Photo capture failed
                }
            })
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = {
            multiplePermissionsState.launchMultiplePermissionRequest()
            viewModel.getAllAlbums(context)
        }) {
            Text("Request Permissions")
        }
        Button(onClick = {
            takePictureLauncher.launch(null)
        }) {
            Text("Take photo")
        }
        Button(onClick = {
            viewModel.loadInternal(context)
        }) {
            Text("Load Internal photos")
        }
        capturedBitmap?.let { bitmap ->
            viewModel.saveInternal("MyImage" + UUID.randomUUID().toString(), bitmap, context)
            Image(
                bitmap = bitmap.asImageBitmap(), contentDescription = "Captured Photo"
            )
        }
        dropDownMenu(
            viewModel.albums.value,
            onSelected = {
                viewModel.selectedAlbum = it
                viewModel.loadShared(context)
            })
        if (photosShared.isNotEmpty()) {

//            Image(
//                painter = rememberImagePainter(photosShared[2].contentUri),
//                contentDescription = null,
//                contentScale = ContentScale.Crop
//            )
            Spacer(modifier = Modifier.size(10.dp))
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(photosShared.size) {
                    Column {
                        Image(

                            painter = rememberImagePainter(photosShared[it].contentUri),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(250.dp)
                        )
//                      Text(text = photosShared[it].album)
                    }

                }
            }
        }
//        if (viewModel.photosStateInternal.value.isNotEmpty()){
//            LazyColumn(modifier = Modifier.fillMaxSize()) {
//                items(viewModel.photosStateInternal.value.size) {
//                    Image(
//                        bitmap = viewModel.photosStateInternal.value[it].bmp.asImageBitmap(),
//                        contentDescription = null,
//                        contentScale = ContentScale.Crop,
//                        modifier = Modifier.size(100.dp)
//                    )
//
//                }
//            }
//        }
    }

}

