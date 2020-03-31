package com.exuberant.audienceai

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface AudienceAiService {

    @Multipart
    @POST("upload")
    suspend fun uploadVideo(
        @Part("file_name") filename: RequestBody,
        @Part video: MultipartBody.Part
    ): Response<VideoUploadResponse>

    @POST("getStatus")
    suspend fun getVideoStatus(
        @Body filename: GetStatusRequestModel
    ): Response<GetStatusResponse>

    companion object {
        operator fun invoke(): AudienceAiService {
            return Retrofit.Builder()
                .baseUrl("http://13.66.231.181:4000/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(AudienceAiService::class.java)
        }
    }

}