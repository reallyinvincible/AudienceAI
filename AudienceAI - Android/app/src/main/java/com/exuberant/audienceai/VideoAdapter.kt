package com.exuberant.audienceai

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class VideoAdapter(
    private val mediaList: MutableList<MediaWrapper>,
    private val service: AudienceAiService
) : RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {

    class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val videoThumbnailImageView: ImageView = itemView.findViewById(R.id.iv_thumbnail)
        val videoNameTextView: TextView = itemView.findViewById(R.id.tv_file_name)
        val videoProcessingCompleted: TextView = itemView.findViewById(R.id.tv_status_completed)
        val videoProcessingNotCompleted: TextView = itemView.findViewById(R.id.tv_status_processing)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.list_item_videos, parent, false)
        return VideoViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mediaList.size
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val mediaFile = mediaList[position]
        holder.videoNameTextView.text = mediaFile.fileName
        holder.itemView.setOnClickListener {
            if (holder.videoProcessingCompleted.visibility == View.VISIBLE) {
                launchStatistics(holder, mediaFile)
            } else {
                Toast.makeText(holder.itemView.context, "Please wait while we process video", Toast.LENGTH_SHORT).show()
            }
        }
        setStatus(holder, mediaFile)
    }


    private fun setStatus(holder: VideoViewHolder, mediaFile: MediaWrapper) = GlobalScope.launch {
        val response = service.getVideoStatus(GetStatusRequestModel(mediaFile.fileName))
        val body = response.body()
        if (body != null && body.code == 200) {
            val status = body.status
            putStatus(status, holder)
        }
    }

    private fun putStatus(
        status: Status,
        holder: VideoViewHolder
    ) = (holder.itemView.context as MainActivity).runOnUiThread {
        if (status.status == "Processing") {
            holder.videoProcessingNotCompleted.visibility = View.VISIBLE
            holder.videoProcessingCompleted.visibility = View.GONE
        } else {
            holder.videoProcessingNotCompleted.visibility = View.GONE
            holder.videoProcessingCompleted.visibility = View.VISIBLE
        }
    }

    private fun launchStatistics(holder: VideoViewHolder, mediaFile: MediaWrapper) {
        val intent = Intent(holder.itemView.context, StatisticsActivity::class.java)
        val gson = Gson()
        intent.putExtra("media", gson.toJson(mediaFile))
        holder.itemView.context.startActivity(intent)
    }


}