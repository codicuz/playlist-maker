package com.practicum.playlistmaker.data.playlist

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class CoverFileManager(private val context: Context) {

    fun copyCoverToInternalStorage(uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            inputStream?.use { input ->
                val coversDir = File(context.filesDir, "playlist_covers")
                if (!coversDir.exists()) {
                    coversDir.mkdirs()
                }

                val fileName = "cover_${UUID.randomUUID()}.jpg"
                val outputFile = File(coversDir, fileName)

                FileOutputStream(outputFile).use { output ->
                    input.copyTo(output)
                }

                outputFile.absolutePath
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}