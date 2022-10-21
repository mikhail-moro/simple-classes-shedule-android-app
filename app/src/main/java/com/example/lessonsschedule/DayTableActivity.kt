package com.example.lessonsschedule

// In this code "meeting" means: lesson, class. (рус. Пара, урок, занятие)

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.LocalDate


class DayTableActivity : AppCompatActivity() {
    private val origDisplayWidth = Resources.getSystem().displayMetrics.widthPixels // Width of phone display (need for correct animations)

    private val recycler: RecyclerView = findViewById(R.id.recycle)
    private val headerText: TextView = findViewById(R.id.header_text)
    private val toMonthTableButton: ImageButton = findViewById(R.id.button)
    private val preferencesButton: ImageButton = findViewById(R.id.preferences_button)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_day_table)

        toMonthTableButton.setOnClickListener {
            val intent = Intent(this@DayTableActivity, MonthTableActivity::class.java)
            startActivity(intent)
        }

        preferencesButton.setOnClickListener {
            val intent = Intent(this@DayTableActivity, PreferencesActivity::class.java)
            intent.putExtra("previous_activity", "day_table")
            startActivity(intent)
        }

        getDay()
    }

    // Executes code from changeTable arg after successful request to server
    @OptIn(DelicateCoroutinesApi::class)
    private fun withAsyncRequest(startDate: LocalDate, endDate: LocalDate, changeData: (data: String) -> Unit) {
        val request = Request()

        GlobalScope.launch {
            request.getData(startDate, endDate) { data ->
                runOnUiThread {
                    changeData(data)
                }
            }
        }
    }

    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    private fun getDay() {
        withAsyncRequest(currentDate, currentDate) { data ->
            val calendar = Calendar()
            val dateTranslator = DateTranslator()
            val day = calendar.getDaySchedule(data, currentDate)
            val centerText: TextView = findViewById(R.id.center_text)

            headerText.text = "${dateTranslator.getRusDayOfWeek(currentDate.dayOfWeek.value)}, " +
                    "${currentDate.dayOfMonth} ${dateTranslator.getRusMonth(currentDate.monthValue)}"

            // If in that day no meetings, display text "Нет пар"
            if(day.meetings.isEmpty()) { centerText.text = "Нет пар" } else { centerText.text = "" }

            // Init RecyclerLayout
            recycler.layoutManager = LinearLayoutManager(this@DayTableActivity)
            recycler.adapter = CustomRecyclerAdapter(day.meetings)

            recycler.animate()
                .x(0f)
                .alpha(1f)
                .setDuration(300)
                .start()


            recycler.setOnTouchListener(object : AnimatedSwipeListener() {
                override fun onSwipeLeft() {
                    super.onSwipeRight()

                    recycler.animate()
                        .x(origDisplayWidth.toFloat())
                        .alpha(0f)
                        .setDuration(300)
                        .start()

                    recycler.animate()
                        .x(-origDisplayWidth.toFloat())
                        .setDuration(0)
                        .start()

                    // Decrement day
                    currentDate = currentDate.minusDays(1)

                    // Skip day if it`s Sunday
                    if (currentDate.dayOfWeek.value == 7) {
                        currentDate = currentDate.minusDays(1)
                    }

                    getDay()
                }

                override fun onSwipeRight() {
                    super.onSwipeLeft()

                    recycler.animate()
                        .x(-origDisplayWidth.toFloat())
                        .alpha(0f)
                        .setDuration(300)
                        .start()

                    recycler.animate()
                        .x(origDisplayWidth.toFloat())
                        .setDuration(0)
                        .start()

                    // Increment day
                    currentDate = currentDate.plusDays(1)

                    // Skip day if it`s Sunday
                    if (currentDate.dayOfWeek.value == 7) {
                        currentDate = currentDate.plusDays(1)
                    }

                    getDay()
                }
            })
        }
    }
}