package com.army.katanapush.services

import com.army.katanapush.interfaces.ApiService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class UploadImage {

    suspend fun uploadImage(photoPath: String, apiService: ApiService): String {
        return try {
            val file = File(photoPath)
            val requestBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val multipartBody = MultipartBody.Part.createFormData("ruta", file.name, requestBody)

            val response = apiService.uploadImage(multipartBody)
            "Mensaje: ${response.message}, Ruta del archivo: ${response.file_path}, Comando: ${response.command}, Distancia: ${response.distance}"
        } catch (e: Exception) {
            "Error al subir la imagen: ${e.message}"
        }
    }
}