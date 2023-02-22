package org.karasiksoftware.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.karasiksoftware.for_ui.*
import org.karasiksoftware.dataclasses.calendar.CalendarItem
import org.karasiksoftware.dataclasses.meeting.Meeting
import org.karasiksoftware.utils.CalendarUtils
import org.karasiksoftware.utils.DatabaseUtils
import org.karasiksoftware.utils.DateTranslatorUtils
import org.karasiksoftware.utils.RequestUtils
import java.time.LocalDate
import kotlin.properties.Delegates

class DayActivity : AppCompatActivity() {
    private lateinit var recycler: RecyclerView
    private lateinit var headerText: TextView
    private lateinit var centerText: TextView
    private lateinit var toScheduleActivityButton: ImageButton
    private lateinit var toPreferencesActivityButton: ImageButton
    private lateinit var downloadAnimation: Animation
    private lateinit var downloadAnimationItem: ImageView
    private lateinit var todayMeetings: CalendarItem
    private lateinit var database: DatabaseUtils
    private lateinit var token: String

    private var displayWidth by Delegates.notNull<Int>()

    private val requestUtils = RequestUtils()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_day)

        recycler = findViewById(R.id.recycler)
        headerText = findViewById(R.id.header_text)
        centerText = findViewById(R.id.center_text)
        toScheduleActivityButton = findViewById(R.id.button)
        toPreferencesActivityButton = findViewById(R.id.preferences_button)
        downloadAnimationItem = findViewById(R.id.animation_item)
        downloadAnimation = AnimationUtils.loadAnimation(this, R.anim.shedule_animation)
        displayWidth = Resources.getSystem().displayMetrics.widthPixels
        database = DatabaseUtils(this)
        token = database.getUserToken().token

        toScheduleActivityButton.setOnClickListener {
            val intent = Intent(this@DayActivity, ScheduleActivity::class.java)
            intent.putExtra("previous_activity", "day_activity")
            startActivity(intent)
        }

        toPreferencesActivityButton.setOnClickListener {
            val intent = Intent(this@DayActivity, PreferencesActivity::class.java)
            intent.putExtra("previous_activity", "day_activity")
            startActivity(intent)
        }

        if (intent.getStringExtra("previous_activity") == "service") {
            setMeetingsFromService(
                LocalDate.parse(intent.getStringExtra("service_time"))
            )
        } else {
            setMeetings(globalDate)
        }
    }

    private fun withRequestData(date: LocalDate, lambda: (Map<String, List<Meeting>>) -> Unit) {
        CoroutineScope(Dispatchers.Default).launch {
            val rawData = requestUtils.getData(date.monthValue, date.year, token)

            runOnUiThread {
                lambda(rawData)
            }
        }
    }

    private fun withRequestDataForService(date: LocalDate, lambda: (Map<String, List<Meeting>>) -> Unit) {
        CoroutineScope(Dispatchers.Default).launch {
            val rawData = requestUtils.getData(date.monthValue, date.year, token)

            runOnUiThread {
                lambda(rawData)
            }
        }
    }

    private fun setMeetings(date: LocalDate) {
        setHeaderText(date)

        downloadAnimationItem.alpha = 1f
        downloadAnimationItem.startAnimation(downloadAnimation)

        if (globalDate.monthValue == date.monthValue) {
            for (i in globalMonthMeetingsData.indices) {
                if (globalMonthMeetingsData[i].date == date) {
                    todayMeetings = globalMonthMeetingsData[i]
                    break
                }
            }

            setMeetingsWithThisData(todayMeetings)
        } else {
            withRequestData(date) { data ->
                val calendarUtils = CalendarUtils()
                val monthData = calendarUtils.getMonthSchedule(data, date)

                globalMonthMeetingsData = monthData

                for (i in monthData.indices) {
                    if (monthData[i].date == date) {
                        todayMeetings = monthData[i]
                        break
                    }
                }

                setMeetingsWithThisData(todayMeetings)
            }
        }

        globalDate = date
    }

    private fun setMeetingsFromService(date: LocalDate) {
        setHeaderText(date)

        downloadAnimationItem.alpha = 1f
        downloadAnimationItem.startAnimation(downloadAnimation)

        withRequestDataForService(date) { data ->
            val calendarUtils = CalendarUtils()
            val monthData = calendarUtils.getMonthSchedule(data, date)

            globalMonthMeetingsData = monthData

            for (i in monthData.indices) {
                if (monthData[i].date == date) {
                    todayMeetings = monthData[i]
                    break
                }
            }

            setMeetingsWithThisData(todayMeetings)
        }

        globalDate = date
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setMeetingsWithThisData(data: CalendarItem) {
        if (data.meetings.isEmpty()) {
            centerText.alpha = 1f
        }

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = CustomRecyclerAdapter(data.meetings)

        recycler.animate()
            .x(0f)
            .alpha(1f)
            .setDuration(300)
            .start()

        recycler.setOnTouchListener(object : AnimatedSwipeListener() {
            override fun onSwipeLeft() {
                recycler.animate()
                    .x(displayWidth.toFloat())
                    .alpha(0f)
                    .setDuration(300)
                    .start()

                recycler.animate()
                    .x(-displayWidth.toFloat())
                    .setDuration(0)
                    .start()

                // Skip day if it`s Sunday
                var newDate = globalDate.minusDays(1)
                if (newDate.dayOfWeek.value == 7) newDate = newDate.minusDays(1)

                centerText.alpha = 0f
                setMeetings(newDate)
            }

            override fun onSwipeRight() {
                recycler.animate()
                    .x(-displayWidth.toFloat())
                    .alpha(0f)
                    .setDuration(300)
                    .start()

                recycler.animate()
                    .x(displayWidth.toFloat())
                    .setDuration(0)
                    .start()

                var newDate = globalDate.plusDays(1)
                if (newDate.dayOfWeek.value == 7) newDate = newDate.plusDays(1)

                centerText.alpha = 0f
                setMeetings(newDate)
            }
        })

        downloadAnimationItem.alpha = 0f
        downloadAnimationItem.clearAnimation()
    }

    @SuppressLint("SetTextI18n")
    private fun setHeaderText(date: LocalDate) {
        val dateTranslatorUtils = DateTranslatorUtils()

        headerText.text = "${dateTranslatorUtils.getRusDayOfWeek(date.dayOfWeek.value)}, ${date.dayOfMonth} ${dateTranslatorUtils.getRusMonth(
            date.monthValue)}"
    }
}