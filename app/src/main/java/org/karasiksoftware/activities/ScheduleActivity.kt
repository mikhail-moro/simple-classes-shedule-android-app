package org.karasiksoftware.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import org.karasiksoftware.dataclasses.database.DatabaseMeetingsData
import kotlinx.coroutines.*
import org.karasiksoftware.for_ui.*
import org.karasiksoftware.dataclasses.calendar.CalendarItem
import org.karasiksoftware.dataclasses.meeting.Meeting
import org.karasiksoftware.notifications.NotificationService
import org.karasiksoftware.notifications.RepeatedMeetingsRequestWorker
import org.karasiksoftware.utils.CalendarUtils
import org.karasiksoftware.utils.DatabaseUtils
import org.karasiksoftware.utils.DateTranslatorUtils
import org.karasiksoftware.utils.RequestUtils
import java.time.LocalDate
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

class ScheduleActivity : AppCompatActivity() {
    private lateinit var downloadAnimation: Animation
    private lateinit var downloadAnimationItem: ImageView
    private lateinit var grid: GridLayout
    private lateinit var headerText: TextView
    private lateinit var toDayActivityButton: ImageButton
    private lateinit var toPreferencesActivityButton: ImageButton
    private lateinit var token: String
    private lateinit var database: DatabaseUtils
    private var currentCellNumber by Delegates.notNull<Int>()

    private val requestUtils = RequestUtils()
    private val calendarUtils = CalendarUtils()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_shedule)

        downloadAnimation = AnimationUtils.loadAnimation(this, R.anim.shedule_animation)
        downloadAnimationItem = findViewById(R.id.download_animation_item)
        grid = findViewById(R.id.grid)
        headerText = findViewById(R.id.header_text)
        toDayActivityButton = findViewById(R.id.button)
        toPreferencesActivityButton = findViewById(R.id.preferences_button)
        database = DatabaseUtils(this)
        token = database.getUserToken().token

        when (intent.getStringExtra("previous_activity")) {
            "login_activity" -> {
                globalDate = LocalDate.now()

                grid.resetCellsMeetings()
                grid.resetCellsColors()

                withRequestData(globalDate) { cellsData ->
                    grid.changeCellsData(cellsData)

                    val preferences = getSharedPreferences("data", MODE_PRIVATE)
                    val isEnableNotifications = preferences.getBoolean("enable_notification", false)
                    val isSaveUserData = preferences.getBoolean("is_save_user_data", false)

                    if (isEnableNotifications && isSaveUserData) {
                        var notificationStartData: List<Meeting>? = null

                        for (i in cellsData.indices) {
                            if (cellsData[i].date == globalDate) {
                                notificationStartData = cellsData[i].meetings
                                break
                            }
                        }

                        if (notificationStartData != null) {
                            val meetings = DatabaseMeetingsData(
                                notificationStartData.size,
                                0,
                                notificationStartData.map { it.name!! },
                                notificationStartData.map { it.startTime },
                                notificationStartData.map { it.endTime },
                                notificationStartData.map { it.aud!! }
                            )
                            database.setMeetingsTableData(meetings)

                            val period = preferences.getInt("notification_period", 4).toLong()
                            val repeatedMeetingRequest = PeriodicWorkRequestBuilder<RepeatedMeetingsRequestWorker>(
                                period,
                                TimeUnit.HOURS
                            )
                                .setInitialDelay(period, TimeUnit.HOURS)
                                .build()

                            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                                "MeetingsRequest",
                                ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                                repeatedMeetingRequest
                            )

                            val intent = Intent(this, NotificationService::class.java)
                                .setAction(NotificationService.ACTION_START)
                            startService(intent)
                        }
                    }
                }
            }
            else -> {
                grid.resetCellsMeetings()
                grid.resetCellsColors()
                grid.changeCellsData(globalMonthMeetingsData)
            }
        }

        setHeaderText(globalDate)

        toDayActivityButton.setOnClickListener {
            val intent = Intent(this@ScheduleActivity, DayActivity::class.java)
            intent.putExtra("previous_activity", "schedule_activity")
            startActivity(intent)
        }

        toPreferencesActivityButton.setOnClickListener {
            val intent = Intent(this@ScheduleActivity, PreferencesActivity::class.java)
            intent.putExtra("previous_activity", "schedule_activity")
            startActivity(intent)
        }
    }

    private fun withRequestData(date: LocalDate, lambda: (List<CalendarItem>) -> Unit) {
        downloadAnimationItem.alpha = 1f
        downloadAnimationItem.startAnimation(downloadAnimation)

        CoroutineScope(Dispatchers.Default).launch {
            val rawData = requestUtils.getData(date.monthValue, date.year, token)
            val cellsData = calendarUtils.getMonthSchedule(rawData, date)

            runOnUiThread {
                lambda(cellsData)
            }
        }
    }

    private fun GridLayout.resetCellsColors() {
        (0..35).forEach { cellNumber ->
            val cell = this.getChildAt(cellNumber) as LinearLayout

            cell.setBackgroundResource(R.drawable.month_item_current_month)
        }
    }

    private fun GridLayout.resetCellsMeetings() {
        (0..35).forEach { cellNumber ->
            val cell = this.getChildAt(cellNumber) as LinearLayout
            val cellDate = cell.getChildAt(0) as TextView
            val cellMeetingsBox = cell.getChildAt(1) as LinearLayout

            cellDate.text = ""

            (0..2).forEach { meetingNumber ->
                val meetingLayout = cellMeetingsBox.getChildAt(meetingNumber) as LinearLayout
                meetingLayout.setBackgroundResource(R.drawable.meeting_item_background)
                meetingLayout.alpha = 0f

                (meetingLayout.getChildAt(0) as TextView).text = ""
            }

            val meetingLayout = cellMeetingsBox.getChildAt(3) as FrameLayout
            meetingLayout.setBackgroundResource(R.drawable.meeting_item_background)
            meetingLayout.alpha = 0f

            (meetingLayout.getChildAt(0) as TextView).text = ""
            (meetingLayout.getChildAt(1) as LinearLayout).alpha = 0f
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun GridLayout.changeCellsData(cellsData: List<CalendarItem>) {
        globalMonthMeetingsData = cellsData

        cellsData.forEachIndexed { cellNumber, cellData ->
            val cell = this.getChildAt(cellNumber) as LinearLayout

            val cellDate = cell.getChildAt(0) as TextView
            val cellMeetingsBox = cell.getChildAt(1) as LinearLayout

            cellDate.text = cellData.date.dayOfMonth.toString()

            if (cellData.status) {
                cellData.meetings.forEachIndexed { meetingNumber, meetingData ->
                    when (meetingNumber) {
                        in 0..2 -> {
                            val meetingLayout = cellMeetingsBox
                                .getChildAt(meetingNumber) as LinearLayout

                            meetingLayout.background.colorFilter =
                                BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                                    Color.parseColor(meetingData.color),
                                    BlendModeCompat.SRC_ATOP
                                )
                            meetingLayout.alpha = 1f

                            val meetingText = meetingLayout.getChildAt(0) as TextView
                            meetingText.text = meetingData.name
                        }
                        3 -> {
                            val meetingLayout = cellMeetingsBox
                                .getChildAt(meetingNumber) as FrameLayout

                            meetingLayout.background.colorFilter = BlendModeColorFilterCompat
                                .createBlendModeColorFilterCompat(
                                    Color.parseColor(meetingData.color),
                                    BlendModeCompat.SRC_ATOP
                                )
                            meetingLayout.alpha = 1f

                            (meetingLayout.getChildAt(0) as TextView).text = meetingData.name
                        }
                        else -> {
                            val meetingLayout = cellMeetingsBox
                                .getChildAt(3) as FrameLayout

                            meetingLayout.background.colorFilter = BlendModeColorFilterCompat
                                .createBlendModeColorFilterCompat(
                                    Color.parseColor(meetingData.color),
                                    BlendModeCompat.SRC_ATOP
                                )
                            meetingLayout.alpha = 1f

                            (meetingLayout.getChildAt(1) as LinearLayout).alpha = 1f
                        }
                    }
                }

                if (cellData.date.dayOfMonth == globalDate.dayOfMonth) {
                    cell.setBackgroundResource(R.drawable.month_item_current_month_today)
                    currentCellNumber = cellNumber
                }
            } else {
                cell.setBackgroundResource(R.drawable.month_item_wrong_month)
            }

            cell.setOnTouchListener(object : SwipeListener(this@ScheduleActivity) {
                override fun onSwipeRight()  {
                    globalDate = globalDate
                        .withDayOfMonth(1)
                        .minusMonths(1)

                    setHeaderText(globalDate)

                    grid.resetCellsMeetings()
                    grid.resetCellsColors()
                    withRequestData(globalDate) { cellsData ->
                        grid.changeCellsData(cellsData)
                    }
                }
                override fun onSwipeLeft()  {
                    globalDate = globalDate
                        .withDayOfMonth(1)
                        .plusMonths(1)

                    setHeaderText(globalDate)

                    grid.resetCellsMeetings()
                    grid.resetCellsColors()
                    withRequestData(globalDate) { cellsData ->
                        grid.changeCellsData(cellsData)
                    }
                }
                override fun onClick()  {
                    if (cellData.status) {
                        val previousCell = grid.getChildAt(currentCellNumber) as LinearLayout
                        globalDate = cellData.date
                        currentCellNumber = cellNumber

                        previousCell.setBackgroundResource(R.drawable.month_item_current_month)
                        cell.setBackgroundResource(R.drawable.month_item_current_month_today)

                        val intent = Intent(this@ScheduleActivity, DayActivity::class.java)
                        startActivity(intent)
                    }
                }
            })
        }

        downloadAnimationItem.clearAnimation()
        downloadAnimationItem.alpha = 0f
    }

    @SuppressLint("SetTextI18n")
    private fun setHeaderText(date: LocalDate) {
        val dateTranslatorUtils = DateTranslatorUtils()

        headerText.text = "${(date.year)}, ${date.dayOfMonth} ${dateTranslatorUtils.getRusMonth(
            date.monthValue)}"
    }
}