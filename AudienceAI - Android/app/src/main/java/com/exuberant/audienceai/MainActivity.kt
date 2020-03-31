package com.exuberant.audienceai

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.jaiselrahman.filepicker.activity.FilePickerActivity
import com.jaiselrahman.filepicker.config.Configurations
import com.jaiselrahman.filepicker.model.MediaFile
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File


class MainActivity : AppCompatActivity() {

    private val REQUEST_TAKE_GALLERY_VIDEO: Int = 9001
    private val MEDIA_STORAGE = "media"
    private var mediaWrapperList = mutableListOf<MediaWrapper>()
    val service = AudienceAiService()
    private lateinit var adapter: VideoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btn_upload_video.setOnClickListener {
            launchVideoPicker()
        }
        btn_refresh.setOnClickListener {
            updateRecyclerView()
        }
        val mediaWrapper = MediaWrapper("sample.webm", "/path/")
        if (!mediaWrapperList.contains(mediaWrapper)) {
            mediaWrapperList.add(mediaWrapper)
            updateRecyclerView()
        }
    }

    private fun launchVideoPicker() {
        val intent = Intent(this, FilePickerActivity::class.java)
        intent.putExtra(
            FilePickerActivity.CONFIGS, Configurations.Builder()
                .setCheckPermission(true)
                .setShowImages(false)
                .setShowFiles(false)
                .setShowAudios(false)
                .setShowVideos(true)
                .enableImageCapture(false)
                .setSkipZeroSizeFiles(true)
                .setSuffixes("mp4")
                .setSingleChoiceMode(true)
                .build()
        )
        startActivityForResult(intent, REQUEST_TAKE_GALLERY_VIDEO)
    }

    private fun uploadFile(mediaFile: MediaFile) = GlobalScope.launch {
        val file = File(mediaFile.path)
        val requestFile: RequestBody = RequestBody.create(
            "video/mp4".toMediaTypeOrNull(),
            file
        )
        val body: MultipartBody.Part =
            MultipartBody.Part.createFormData("video", file.name, requestFile)
        val fileName = file.name
        val fileNameBody = RequestBody.create(
            MultipartBody.FORM, fileName
        )
        runOnUiThread {
            Toast.makeText(applicationContext, "Upload started", Toast.LENGTH_SHORT).show()
        }
        val response = service.uploadVideo(fileNameBody, body)
        runOnUiThread {
            Toast.makeText(applicationContext, "Upload finished", Toast.LENGTH_SHORT).show()
        }
        val responseBody = response.body()
        mediaWrapperList.add(MediaWrapper(mediaFile.name, mediaFile.path))
        updateRecyclerView()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_TAKE_GALLERY_VIDEO) {
                val files: ArrayList<MediaFile> =
                    data!!.getParcelableArrayListExtra(FilePickerActivity.MEDIA_FILES)
                val mediaFile = files[0]
                uploadFile(mediaFile)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        saveMediaList()
    }

    override fun onResume() {
        super.onResume()
        retrieveMediaList()
    }

    private fun saveMediaList() {
        val sharedPreferences = getSharedPreferences("MediaStorage", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val mediaString = gson.toJson(MediaFileStorageModel(mediaWrapperList))
        editor.putString(MEDIA_STORAGE, mediaString)
        editor.apply()
    }

    private fun retrieveMediaList() {
        val sharedPreferences = getSharedPreferences("MediaStorage", Context.MODE_PRIVATE)
        val gson = Gson()
        val mediaString = sharedPreferences.getString(MEDIA_STORAGE, null)
        if (mediaString != null) {
            val mediaFileStorageModel =
                gson.fromJson(mediaString, MediaFileStorageModel::class.java)
            mediaWrapperList = mediaFileStorageModel.mediaList.toMutableList()
        }
        updateRecyclerView()
    }

    private fun updateRecyclerView() = runOnUiThread {
        adapter = VideoAdapter(mediaWrapperList, service)
        rv_video_list.adapter = adapter
    }
}
