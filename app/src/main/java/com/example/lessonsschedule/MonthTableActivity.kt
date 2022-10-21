package com.example.lessonsschedule

// In this code "meeting" means: lesson, class. (рус. Пара, урок, занятие)

import android.animation.*
import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.view.doOnLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.properties.Delegates

// Moving LinearLayout along Y-axis with value in arg
private fun LinearLayout.setTranslationYAnimation(arg: Float) {
    val animation = AnimatorSet()
    animation.play(
        ObjectAnimator.ofFloat(
            this,
            "y",
            arg
        )
    )

    animation.duration = 200
    animation.start()
}

// Changes height of Tablelayout to value in arg
private fun TableLayout.setTableHeightAnimation(arg: Int) {
    var tableHeight = 0
    this.doOnLayout{ tableHeight = this.measuredHeight }
    val valueAnimator = ValueAnimator.ofInt(tableHeight, arg)

    valueAnimator.duration = 200
    valueAnimator.addUpdateListener {
        val animatedValue = valueAnimator.animatedValue as Int
        val layoutParams = this.layoutParams
        layoutParams.height = animatedValue
        this.layoutParams = layoutParams
    }

    valueAnimator.start()
}

// Changes every cell in TableLayout
// If arg==True transform cells to display simplified data
// If arg==False transform cells to display complete data
private fun TableLayout.setChangeCellsAnimations(arg: Boolean) {
    for(rowCount in 0 until 6) {
        val row: LinearLayout = this.getChildAt(rowCount) as LinearLayout

        for(itemCount in 0 until 6) {
            val item: LinearLayout = row.getChildAt(itemCount) as LinearLayout
            val frame: FrameLayout = item.getChildAt(1) as FrameLayout

            val meetingsBox: LinearLayout = frame.getChildAt(0) as LinearLayout
            val meetingsMarks: LinearLayout = frame.getChildAt(1) as LinearLayout

            if (arg) {
                val animation = AnimatorSet()

                animation.play(
                    ObjectAnimator.ofFloat(
                        meetingsBox,
                        "alpha",
                        0f
                    )
                )
                animation.duration = 200

                animation.play(
                    ObjectAnimator.ofFloat(
                        meetingsMarks,
                        "alpha",
                        1f
                    )
                )
                animation.duration = 200

                animation.start()
            } else {
                val animation = AnimatorSet()

                animation.play(
                    ObjectAnimator.ofFloat(
                        meetingsBox,
                        "alpha",
                        1f
                    )
                )
                animation.duration = 200

                animation.play(
                    ObjectAnimator.ofFloat(
                        meetingsMarks,
                        "alpha",
                        0f
                    )
                )
                animation.duration = 200

                animation.start()
            }
        }
    }
}

// Executes code from changeGridItems arg for each cell in TableLayout with data in calendar arg
private inline fun TableLayout.tableCellsIterator(calendar: List<CalendarItem>, changeGridItems:(LinearLayout, CalendarItem) -> Unit) {
    var count = 0

    for(rowCount in 0 until 6) {
        val row: LinearLayout = this.getChildAt(rowCount) as LinearLayout

        for(itemCount in 0 until 6) {
            val item: LinearLayout = row.getChildAt(itemCount) as LinearLayout

            changeGridItems(item, calendar[count])

            count++
        }
    }
}

// Resets each cell in TableLayout
private fun TableLayout.clearTable() {
    for(rowCount in 0 until 6) {
        val row: LinearLayout = this.getChildAt(rowCount) as LinearLayout

        for (itemCount in 0 until 6) {
            val item: LinearLayout = row.getChildAt(itemCount) as LinearLayout
            val frame: FrameLayout = item.getChildAt(1) as FrameLayout

            val meetingsBox: LinearLayout = frame.getChildAt(0) as LinearLayout

            for (meetingItemCount in 0 until 4) {
                val meetingItem: LinearLayout = meetingsBox.getChildAt(meetingItemCount) as LinearLayout
                val meetingText: TextView = meetingItem.getChildAt(0) as TextView

                meetingItem.setBackgroundResource(R.drawable.meeting_item_invisible_background)
                meetingText.text = ""
            }

            val meetingsMarks: LinearLayout = frame.getChildAt(1) as LinearLayout

            for (meetingMarkCount in 1 until 5) {
                val meetingMark: LinearLayout = meetingsMarks.getChildAt(meetingMarkCount) as LinearLayout
                val markImage: ImageView = meetingMark.getChildAt(0) as ImageView

                markImage.setImageResource(R.drawable.meeting_item_invisible_background)
            }
        }
    }
}

// Sets background color for each cell in TableLayout according the data in arg
private fun TableLayout.setCellsColor(calendar: List<CalendarItem>) {
    var count = 0
    for(rowCount in 0 until 6) {
        val row: LinearLayout = this.getChildAt(rowCount) as LinearLayout
        for (itemCount in 0 until 6) {
            val item: LinearLayout = row.getChildAt(itemCount) as LinearLayout

            if (calendar[count].status) {
                item.setBackgroundResource(R.drawable.month_item_current_month)
            } else {
                item.setBackgroundResource(R.drawable.month_item_wrong_month)
            }

            count++
        }
    }
}

@SuppressLint("SetTextI18n")
private fun TextView.setHeaderText() {
    val dateTranslator = DateTranslator()

    this.text = "${(currentDate.year)}, ${currentDate.dayOfMonth} ${dateTranslator.getRusMonth(currentDate.monthValue)}"
}

// Contain data of user`s selected day
// This variable should be available in other activities
var currentDate: LocalDate = LocalDate.now()

class MonthTableActivity : AppCompatActivity() {
    private lateinit var table: TableLayout
    private lateinit var bottom: LinearLayout
    private lateinit var recyclerContainer: LinearLayout
    private lateinit var recycler: RecyclerView
    private lateinit var headerText: TextView
    private lateinit var toDayTableButton: ImageButton
    private lateinit var preferencesButton: ImageButton

    private var bottomMenuDown = true

    // Variables contains data for animations
    private var origTableHeight by Delegates.notNull<Int>()
    private var origBottomHeight by Delegates.notNull<Int>()
    private var bottomUpY by Delegates.notNull<Float>()
    private var recyclerUpY by Delegates.notNull<Float>()
    private var bottomDownY by Delegates.notNull<Float>()
    private var recyclerDownY by Delegates.notNull<Float>()
    private var reducedTableHeight by Delegates.notNull<Int>()

    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_month_table)

        val listOfMeetingsPartOfDisplay = 0.4f // Part of display that list of meetings occupies in raised state
        val displayHeight = Resources.getSystem().displayMetrics.heightPixels

        table = findViewById(R.id.table)
        bottom = findViewById(R.id.bottom)
        recyclerContainer = findViewById(R.id.recycle_container)
        recycler = findViewById(R.id.recycle)
        headerText = findViewById(R.id.header_text)
        toDayTableButton = findViewById(R.id.button)
        preferencesButton = findViewById(R.id.preferences_button)

        // Set height of list of meetings
        recyclerContainer.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            (displayHeight * listOfMeetingsPartOfDisplay).toInt()
        )

        toDayTableButton.setOnClickListener {
            val intent = Intent(this@MonthTableActivity, DayTableActivity::class.java)
            startActivity(intent)
        }

        preferencesButton.setOnClickListener {
            val intent = Intent(this@MonthTableActivity, PreferencesActivity::class.java)
            intent.putExtra("previous_activity", "month_table")
            startActivity(intent)
        }

        // Code in arg of View.doOnLayout will execute when this view is laid out
        // This is necessary cause if try to get height of view before it`s laid out, method
        // View.getMeasuredHeight will return 0
        bottom.doOnLayout {
            origBottomHeight = it.measuredHeight
            // When bottom is laid out table is also laid out
            origTableHeight = table.measuredHeight

            bottomUpY = displayHeight - origBottomHeight - displayHeight * listOfMeetingsPartOfDisplay
            recyclerUpY = displayHeight - displayHeight * listOfMeetingsPartOfDisplay
            reducedTableHeight = origTableHeight - (displayHeight * listOfMeetingsPartOfDisplay).toInt()
            bottomDownY = displayHeight.toFloat() - origBottomHeight
            recyclerDownY = displayHeight.toFloat()
        }

        headerText.setHeaderText()

        setDataInTable()
    }

    // Executes code from changeTable arg after successful request to server
    @OptIn(DelicateCoroutinesApi::class)
    private fun withAsyncRequest(startDate: LocalDate, endDate: LocalDate, changeTable: (data: String) -> Unit) {
        val request = Request()

        GlobalScope.launch {
            request.getData(startDate, endDate) { data ->
                runOnUiThread {
                    changeTable(data)
                }
            }
        }
    }
    
    fun setDataInTable() {
        val firstDayOfMonth = LocalDate.of(currentDate.year, currentDate.month, 1)
        val lastDayOfMonth = LocalDate.of(currentDate.year, currentDate.month, currentDate.lengthOfMonth())

        withAsyncRequest(firstDayOfMonth, lastDayOfMonth) { data ->
            val calendar = Calendar()
            // Contains list of structured data from server for each cell
            val tableData = calendar.getMonthSchedule(data, currentDate)

            table.tableCellsIterator(tableData) { cell, day ->
                val cellNumber = cell.getChildAt(0) as TextView
                val frame = cell.getChildAt(1) as FrameLayout

                val meetingsTitles = frame.getChildAt(0) as LinearLayout
                val meetingsMarks = frame.getChildAt(1) as LinearLayout

                // After changing data in cell, it`s size will be change
                // Therefore, necessary save original size of cell
                val originCellSizeParams = LinearLayout.LayoutParams(cell.measuredWidth, cell.measuredHeight)

                cellNumber.text = day.date.dayOfMonth.toString()

                if (day.status) { // If day.status == True cell contain data of the day from current month
                    if (day.meetings.size <= 4) { // The cell can display maximum 4 meetings
                        for ((index, meeting) in day.meetings.withIndex()) {
                            val meetingItem = meetingsTitles.getChildAt(index) as LinearLayout // LinearLayout contains data of meeting
                            val meetingItemText = meetingItem.getChildAt(0) as TextView
                            val meetingMark = meetingsMarks.getChildAt(index+1) as LinearLayout
                            val mark = meetingMark.getChildAt(0) as ImageView // Simplified representation of meeting

                            meetingItemText.text = meeting.name

                            // Cause color of the meeting`s background comes from server,
                            // necessary set back color using method setColorFilter
                            meetingItem.setBackgroundResource(R.drawable.meeting_item_background)
                            meetingItem.background.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                                Color.parseColor(meeting.color),
                                BlendModeCompat.SRC_ATOP
                            )

                            mark.setImageResource(R.drawable.meeting_mark)
                            mark.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                                Color.parseColor(meeting.color),
                                BlendModeCompat.SRC_ATOP
                            )
                        }
                    } else {
                        for (index in 0 until 4) {
                            val meetingItem = meetingsTitles.getChildAt(index) as LinearLayout
                            val meetingItemText = meetingItem.getChildAt(0) as TextView
                            val meeting = day.meetings[index]

                            val meetingMark = meetingsMarks.getChildAt(index+1) as LinearLayout
                            val mark = meetingMark.getChildAt(0) as ImageView

                            meetingItemText.text = meeting.name

                            meetingItem.setBackgroundResource(R.drawable.meeting_item_background)
                            meetingItem.background.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                                Color.parseColor(meeting.color),
                                BlendModeCompat.SRC_ATOP
                            )

                            mark.setImageResource(R.drawable.meeting_mark)
                            mark.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                                Color.parseColor(meeting.color),
                                BlendModeCompat.SRC_ATOP
                            )
                        }
                    }

                    // Set back color of cells
                    if (day.date.dayOfMonth == currentDate.dayOfMonth) {
                        cell.setBackgroundResource(R.drawable.month_item_current_month_today)
                    } else {
                        cell.setBackgroundResource(R.drawable.month_item_current_month)
                    }
                } else { // If day.status == False don`t set meetings in cell and set gray back color
                    for (index in 0 until 4) {
                        val meetingItem = meetingsTitles.getChildAt(index) as LinearLayout

                        meetingItem.setBackgroundResource(R.drawable.meeting_item_background)
                        meetingItem.background.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                            Color.parseColor("#E4E4E4"),
                            BlendModeCompat.SRC_ATOP
                        )

                        val meetingMark = meetingsMarks.getChildAt(index+1) as LinearLayout
                        val mark = meetingMark.getChildAt(0) as ImageView

                        mark.setImageResource(R.drawable.meeting_mark)
                        mark.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                            Color.parseColor("#E4E4E4"),
                            BlendModeCompat.SRC_ATOP
                        )
                    }

                    cell.setBackgroundResource(R.drawable.month_item_wrong_month)
                }

                // If set listener of right/left swipes in parent view (table), listener will not
                // detect swipes. Therefore, necessary set listener in all cells to correctly
                // swipes detecting
                cell.setOnTouchListener(object: SwipeListener(this@MonthTableActivity) {
                    val headerText: TextView = findViewById(R.id.header_text)

                    @SuppressLint("SetTextI18n")
                    override fun onSwipeRight() {
                        super.onSwipeRight()
                        table.clearTable()

                        // Decrement month
                        currentDate = LocalDate.of(currentDate.year, currentDate.month, 1).minusMonths(1)
                        headerText.setHeaderText()

                        // Set data in table for previous month
                        setDataInTable()
                    }

                    @SuppressLint("SetTextI18n")
                    override fun onSwipeLeft() {
                        super.onSwipeLeft()
                        table.clearTable()

                        // Increment month
                        currentDate = LocalDate.of(currentDate.year, currentDate.month, 1).plusMonths(1)
                        headerText.setHeaderText()

                        // Set data in table for next month
                        setDataInTable()
                    }


                    @SuppressLint("SetTextI18n")
                    override fun onClick() {
                        super.onClick()
                        if (day.status) {
                            if (bottomMenuDown) { // If list of meetings in bottom of display
                                // Init RecyclerLayout
                                recycler.layoutManager = LinearLayoutManager(this@MonthTableActivity)
                                recycler.adapter = CustomRecyclerAdapter(day.meetings)

                                // Move list of meetings up and change height of table, to make
                                // space for list of meetings
                                bottomMenuAnimation(bottomMenuDown)

                                table.setCellsColor(tableData)

                                cell.setBackgroundResource(R.drawable.month_item_current_month_today)

                                currentDate = day.date
                                headerText.setHeaderText()

                                bottomMenuDown = false
                            } else {
                                if (currentDate == day.date) {
                                    // If user clicked on one cell twice don`t refresh list, only
                                    // return elements and table size in original position
                                    bottomMenuAnimation(bottomMenuDown)

                                    bottomMenuDown = true
                                } else { // If user clicked on another cell only refresh list
                                    table.setCellsColor(tableData)
                                    cell.setBackgroundResource(R.drawable.month_item_current_month_today)

                                    recycler.layoutManager = LinearLayoutManager(this@MonthTableActivity)
                                    recycler.adapter = CustomRecyclerAdapter(day.meetings)

                                    currentDate = day.date
                                    headerText.setHeaderText()
                                }
                            }
                        }
                    }
                })

                // Set original size of cell
                cell.layoutParams = originCellSizeParams
            }
        }
    }

    private fun bottomMenuAnimation(arg: Boolean) {
        if (arg) {
            bottom.setTranslationYAnimation(bottomUpY)
            recyclerContainer.setTranslationYAnimation(recyclerUpY)

            table.setTableHeightAnimation(reducedTableHeight)
            table.setChangeCellsAnimations(true)
        } else {
            bottom.setTranslationYAnimation(bottomDownY)
            recyclerContainer.setTranslationYAnimation(recyclerDownY)

            table.setTableHeightAnimation(origTableHeight)
            table.setChangeCellsAnimations(false)
        }
    }
}