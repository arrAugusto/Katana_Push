package com.army.katanapush.interfaces

import com.army.katanapush.UploadResponse
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

// Retrofit API Interface
interface ApiService {
    @Multipart
    @POST("index.php")
    suspend fun uploadImage(@Part file: MultipartBody.Part): UploadResponse
}