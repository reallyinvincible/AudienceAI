package com.exuberant.audienceai

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.gson.Gson
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class StatisticsActivity : AppCompatActivity() {

    private val service = AudienceAiService()
    private lateinit var chart: LineChart
    private val angerList: ArrayList<Entry> = ArrayList()
    private val disgustList: ArrayList<Entry> = ArrayList()
    private val fearList: ArrayList<Entry> = ArrayList()
    private val happinessList: ArrayList<Entry> = ArrayList()
    private val sadList: ArrayList<Entry> = ArrayList()
    private val surpriseList: ArrayList<Entry> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)
        val mediaString = intent.getStringExtra("media")
        val gson = Gson()
        val mediaWrapper = gson.fromJson(mediaString, MediaWrapper::class.java)
        chart = findViewById(R.id.chart)
        findAndSetGraph(mediaWrapper)
    }


    private fun findAndSetGraph(mediaFile: MediaWrapper) = GlobalScope.launch {
        val response = service.getVideoStatus(GetStatusRequestModel(mediaFile.fileName))
        val body = response.body()
        val name = mediaFile.fileName
        if (body != null && body.code == 200) {
            val status = body.status
            if (status.status == "Processing") {
                val emotionString = status.emotionScoresJson
                val gson = Gson()
                val emotionListModel = gson.fromJson(emotionString, EmotionListModel::class.java)
                val c = 10
            } else {
                val emotionString = status.emotionScoresJson
                val gson = Gson()
                val emotionListModel = gson.fromJson(emotionString, EmotionListModel::class.java)
                processList(emotionListModel)
            }
        }
    }

    private fun processList(emotionListModel: EmotionListModel?) {
        var c = 0
        for (i in emotionListModel!!.data) {
            angerList.add(Entry(c.toFloat(), (i[0] * 100).toFloat()))
            disgustList.add(Entry(c.toFloat(), (i[1] * 100).toFloat()))
            fearList.add(Entry(c.toFloat(), (i[2] * 100).toFloat()))
            happinessList.add(Entry(c.toFloat(), (i[3] * 100).toFloat()))
            sadList.add(Entry(c.toFloat(), (i[4] * 100).toFloat()))
            surpriseList.add(Entry(c.toFloat(), (i[5] * 100).toFloat()))
            c++
        }
        processGraph()
    }

    private fun processGraph() {
        val angerDataSet = LineDataSet(angerList, "Anger")
        angerDataSet.setColor(Color.parseColor("#F44336"))
        angerDataSet.setCircleColor(Color.parseColor("#F44336"))

        val disgustDataSet = LineDataSet(disgustList, "Disgust")
        disgustDataSet.setColor(Color.parseColor("#E91E63"))
        disgustDataSet.setCircleColor(Color.parseColor("#E91E63"))

        val fearDataSet = LineDataSet(fearList, "Fear")
        fearDataSet.setColor(Color.parseColor("#9C27B0"))
        fearDataSet.setCircleColor(Color.parseColor("#9C27B0"))

        val happinessDataSet = LineDataSet(happinessList, "Happy")
        happinessDataSet.setColor(Color.parseColor("#3F51B5"))
        happinessDataSet.setCircleColor(Color.parseColor("#3F51B5"))

        val sadDataSet = LineDataSet(sadList, "Sad")
        sadDataSet.setColor(Color.parseColor("#2196F3"))
        sadDataSet.setCircleColor(Color.parseColor("#2196F3"))

        val surpriseDataSet = LineDataSet(surpriseList, "Surprise")
        surpriseDataSet.setColor(Color.parseColor("#4CAF50"))
        surpriseDataSet.setCircleColor(Color.parseColor("#4CAF50"))


        val dataSets: MutableList<ILineDataSet> =
            java.util.ArrayList()
        dataSets.add(angerDataSet)
        dataSets.add(disgustDataSet)
        dataSets.add(fearDataSet)
        dataSets.add(happinessDataSet)
        dataSets.add(sadDataSet)
        dataSets.add(surpriseDataSet)
        val data = LineData(dataSets)
        chart.data = data
        chart.invalidate()
    }

}
