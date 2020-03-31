package com.exuberant.audienceai


import com.google.gson.annotations.SerializedName

data class GetStatusResponse(
    @SerializedName("code")
    val code: Int,
    @SerializedName("message")
    val message: String,
    @SerializedName("status")
    val status: Status
)