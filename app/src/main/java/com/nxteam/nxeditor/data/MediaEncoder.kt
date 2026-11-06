package com.nxteam.nxeditor.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

object MediaEncoder {
    suspend fun encode(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
        val mimeType = context.contentResolver.getType(uri) ?: ""
        val bitmap = if (mimeType.startsWith("video")) {
            extractVideoFrame(context, uri)
        } else {
            decodeImage(context, uri)
        } ?: return@withContext null

        val scaled = scale(bitmap, 1024)
        val stream = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 70, stream)
        val base64 = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
        "data:image/jpeg;base64,$base64"
    }

    fun decode(dataUrl: String): Bitmap? {
        val commaIndex = dataUrl.indexOf(',')
        if (commaIndex < 0) {
            return null
        }
        return try {
            val bytes = Base64.decode(dataUrl.substring(commaIndex + 1), Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (error: Exception) {
            null
        }
    }

    private fun decodeImage(context: Context, uri: Uri): Bitmap? {
        return context.contentResolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input)
        }
    }

    private fun extractVideoFrame(context: Context, uri: Uri): Bitmap? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, uri)
            retriever.getFrameAtTime(0)
        } catch (error: Exception) {
            null
        } finally {
            retriever.release()
        }
    }

    private fun scale(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val longest = maxOf(bitmap.width, bitmap.height)
        if (longest <= maxDimension) {
            return bitmap
        }
        val ratio = maxDimension.toFloat() / longest
        val width = (bitmap.width * ratio).toInt()
        val height = (bitmap.height * ratio).toInt()
        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }
}
