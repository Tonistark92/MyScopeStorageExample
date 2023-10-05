package com.example.scopestorageexample

import android.content.ContentUris
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.provider.MediaStore
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.io.IOException

class MyViewmodle() : ViewModel() {
    val photosStateShared = mutableStateOf<List<SharedStoragePhoto>>(emptyList())
    val photosStateInternal = mutableStateOf<List<InternalStoragePhoto>>(emptyList())
    val albums = mutableStateOf<List<String>>(mutableListOf("ALL"))
    var selectedAlbum = "ALL"
    val isSavedInternal = mutableStateOf<Boolean>(false)

    init {
        viewModelScope.launch {

        }
    }

    fun loadShared(c: Context) {
        viewModelScope.launch {
            val loadedPhotos = loadPhotosFromExternalStorage(c)
            photosStateShared.value = loadedPhotos
        }
    }

    fun loadInternal(c: Context) {
        viewModelScope.launch {
            val loadedPhotos = loadPhotosFromInternalStorage(c)
            photosStateInternal.value = loadedPhotos
        }
    }

    fun saveInternal(filename: String, bmp: Bitmap, c: Context) {
        viewModelScope.launch {
            val isSaved = savePhotoToInternalStorage(filename, bmp, c)
            isSavedInternal.value = isSaved
        }
    }

    suspend fun loadPhotosFromExternalStorage(c: Context): List<SharedStoragePhoto> {
        return withContext(Dispatchers.Default) {
            val collection = sdk29AndUp {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } ?: MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.WIDTH,
                MediaStore.Images.Media.HEIGHT,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            )
            val photos = mutableListOf<SharedStoragePhoto>()
            if (selectedAlbum == "ALL"){
                c.contentResolver.query(
                    collection,
                    projection,
                    null,
                    null,
                    "${MediaStore.Images.Media.DATE_ADDED} ASC"
                )?.use { cursor ->
                    val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                    val displayNameColumn =
                        cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                    val widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
                    val heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)

                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(idColumn)
                        val displayName = cursor.getString(displayNameColumn)
                        val width = cursor.getInt(widthColumn)
                        val height = cursor.getInt(heightColumn)
//                    val albumId = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
//                    val album =cursor.getString(albumId)
                        val contentUri = ContentUris.withAppendedId(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            id
                        )
                        photos.add(SharedStoragePhoto(id, displayName, width, height, contentUri))
                    }
                    photos.toList()
                }?: listOf()
            }
            else{
                val selection = "${MediaStore.Images.Media.BUCKET_DISPLAY_NAME} = ?"
                val selectionArgs = arrayOf(selectedAlbum)
                c.contentResolver.query(
                    collection,
                    projection,
                    selection,
                    selectionArgs,
                    "${MediaStore.Images.Media.DISPLAY_NAME} ASC"
                )?.use { cursor ->
                    val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                    val displayNameColumn =
                        cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                    val widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
                    val heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)

                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(idColumn)
                        val displayName = cursor.getString(displayNameColumn)
                        val width = cursor.getInt(widthColumn)
                        val height = cursor.getInt(heightColumn)
//                    val albumId = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
//                    val album =cursor.getString(albumId)
                        val contentUri = ContentUris.withAppendedId(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            id
                        )
                        photos.add(SharedStoragePhoto(id, displayName, width, height, contentUri))
                    }
                    photos.toList()
                } ?: listOf()
            }

        }
    }

    suspend fun loadPhotosFromInternalStorage(c: Context): List<InternalStoragePhoto> {
        return withContext(Dispatchers.IO) {
            val files = c.filesDir.listFiles()
            files?.filter { it.canRead() && it.isFile && it.name.endsWith(".jpg") }?.map {
                val bytes = it.readBytes()
                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                InternalStoragePhoto(it.name, bmp)
            } ?: listOf()
        }
    }

    suspend fun savePhotoToInternalStorage(filename: String, bmp: Bitmap, c: Context): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                c.openFileOutput("$filename.jpg", MODE_PRIVATE).use { stream ->
                    if (!bmp.compress(Bitmap.CompressFormat.JPEG, 95, stream)) {
                        throw IOException("Couldn't save bitmap.")
                    }
                }
                true
            } catch (e: IOException) {
                e.printStackTrace()
                false
            }
        }
    }

    fun getAllAlbums(context: Context) {
        val albumNames = mutableListOf<String>()

        val projection = arrayOf(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
        val sortOrder = "${MediaStore.Images.Media.BUCKET_DISPLAY_NAME} ASC"

        val cursor = context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )

        cursor?.use {
            val bucketNameColumn =
                it.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
            while (it.moveToNext()) {
                val albumName = it.getString(bucketNameColumn)
                if (albumName != null && !albumNames.contains(albumName)) {
                    albumNames.add(albumName)
                }
            }
        }
        Log.d("TAGALBUM",albumNames.toString())
        albums.value += albumNames
    }
}