package com.exuberant.audienceai


import com.google.gson.annotations.SerializedName

data class VideoUploadResponse(
    @SerializedName("code")
    val code: Int,
    @SerializedName("message")
    val message: String
)