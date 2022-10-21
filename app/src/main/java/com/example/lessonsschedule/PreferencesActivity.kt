package com.example.lessonsschedule

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton

// Now this is an empty activity, but in next builds it`s will be supplemented
class PreferencesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preferences)

        val button: ImageButton = findViewById(R.id.button)
        val previousActivity = intent.extras?.getString("previous_activity")

        if(previousActivity == "day_table") {
            button.setOnClickListener {
                val intent = Intent(this@PreferencesActivity, DayTableActivity::class.java)
                startActivity(intent)
            }
        } else {
            button.setOnClickListener {
                val intent = Intent(this@PreferencesActivity, MonthTableActivity::class.java)
                startActivity(intent)
            }
        }
    }
}
