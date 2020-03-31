package com.exuberant.audienceai


import com.google.gson.annotations.SerializedName

data class Status(
    @SerializedName("emotion_scores")
    val emotionScores: Any,
    @SerializedName("emotion_scores_json")
    val emotionScoresJson: String,
    @SerializedName("file_name")
    val fileName: String,
    @SerializedName("file_path")
    val filePath: String,
    @SerializedName("num_frames")
    val numFrames: Int,
    @SerializedName("status")
    val status: String
)