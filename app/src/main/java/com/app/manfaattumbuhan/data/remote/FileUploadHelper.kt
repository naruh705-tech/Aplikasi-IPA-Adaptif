package com.app.manfaattumbuhan.data.remote

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import com.app.manfaattumbuhan.data.local.TokenManager
import com.app.manfaattumbuhan.data.remote.model.UploadResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

object FileUploadHelper {

    suspend fun uploadFile(
        context: Context,
        uri: Uri,
        type: String
    ): Result<UploadResponse> = withContext(Dispatchers.IO) {
        try {
            val file = uriToFile(context, uri)
            val token = TokenManager.getToken()

            val mimeType = getMimeType(context, uri, file.name)
            val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData("file", file.name, requestFile)
            val typePart = type.toRequestBody("text/plain".toMediaTypeOrNull())

            val apiService = ApiConfig.createService<ApiService>()
            val response = apiService.uploadFile(token, filePart, typePart)

            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Upload gagal"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getMimeType(context: Context, uri: Uri, fileName: String): String {
        var mimeType = context.contentResolver.getType(uri)
        if (mimeType == null || mimeType == "application/octet-stream") {
            val extension = MimeTypeMap.getFileExtensionFromUrl(fileName)
                ?: fileName.substringAfterLast('.', "")
            if (extension.isNotBlank()) {
                val fromExtension = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase())
                if (fromExtension != null) {
                    mimeType = fromExtension
                }
            }
        }
        return mimeType ?: "application/octet-stream"
    }

    private fun uriToFile(context: Context, uri: Uri): File {
        val fileName = getFileName(context, uri) ?: "upload_${System.currentTimeMillis()}"
        val tempFile = File(context.cacheDir, fileName)
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        }
        return tempFile
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        var name: String? = null
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && cursor.moveToFirst()) {
                name = cursor.getString(nameIndex)
            }
        }
        return name
    }
}
