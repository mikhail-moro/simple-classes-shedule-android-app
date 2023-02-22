package org.karasiksoftware.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.work.WorkManager
import org.karasiksoftware.notifications.NotificationService
import org.karasiksoftware.utils.DatabaseUtils


class PreferencesActivity : AppCompatActivity() {
    private lateinit var backButton: ImageButton
    private lateinit var resetDataButton: LinearLayout
    private lateinit var enableNotificationButton: LinearLayout
    private lateinit var enableNotificationButtonCheck: CheckBox
    private lateinit var notificationPeriodBox: LinearLayout
    private lateinit var notificationPeriodField: EditText
    private lateinit var notificationPeriodComment: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preferences)

        backButton = findViewById(R.id.button)
        resetDataButton = findViewById(R.id.reset_data)
        enableNotificationButton = findViewById(R.id.enable_notification)
        enableNotificationButtonCheck = findViewById(R.id.check)
        notificationPeriodBox = findViewById(R.id.notification_period)
        notificationPeriodField = findViewById(R.id.notification_period_field)
        notificationPeriodComment = findViewById(R.id.notification_period_comment)

        enableNotificationButtonCheck.isClickable = false
        notificationPeriodField.isClickable = false

        val preferences = getSharedPreferences("data", MODE_PRIVATE)
        val isEnableNotifications = preferences.getBoolean("enable_notification", false)
        val isSaveUserData = preferences.getBoolean("is_save_user_data", false)

        changeNotificationPeriodFieldLayoutState(isEnableNotifications && isSaveUserData)

        if (isSaveUserData) {
            enableNotificationButtonCheck.isChecked = isEnableNotifications
        } else {
            val enableNotificationButtonTexts = enableNotificationButton.getChildAt(0) as LinearLayout
            (enableNotificationButtonTexts.getChildAt(0) as TextView).setTextColor(getColor(R.color.gray))
            (enableNotificationButtonTexts.getChildAt(1) as TextView).setTextColor(getColor(R.color.gray))

            enableNotificationButton.isEnabled = false
        }

        if (preferences.getInt("notification_period", 0) != 0) {
            notificationPeriodField.setText(preferences.getInt(
                "notification_period",
                0
            ).toString())
        } else {
            notificationPeriodField.setText("")
        }

        resetDataButton.setOnClickListener {
            val database = DatabaseUtils(this)
            database.setUserToken("")

            resetServices()

            val editor = preferences.edit()
            editor.putBoolean("is_save_user_data", false)
            editor.apply()

            val intent = Intent(this@PreferencesActivity, LoginActivity::class.java)
            intent.putExtra("previous_activity", "preferences_activity")
            startActivity(intent)
        }

        enableNotificationButton.setOnClickListener {
            val newState = !enableNotificationButtonCheck.isChecked

            val editor = preferences.edit()
            editor.putBoolean("enable_notification", newState)
            editor.apply()

            if (!newState) resetServices()

            enableNotificationButtonCheck.isChecked = newState
            changeNotificationPeriodFieldLayoutState(newState)
        }

        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        notificationPeriodBox.setOnClickListener {
            notificationPeriodField.requestFocus()
            inputMethodManager.showSoftInput(notificationPeriodField, InputMethodManager.SHOW_IMPLICIT)
        }

        notificationPeriodField.doOnTextChanged { text, _, _, _ ->
            try {
                val num = text.toString().toInt()
                val editor = preferences.edit()

                if (num >= 1) {
                    editor.putInt("notification_period", text.toString().toInt())
                } else {
                    editor.putInt("notification_period", 1)
                    notificationPeriodField.setText("1")
                }
                editor.apply()
            } catch (ex: java.lang.NumberFormatException) {
                return@doOnTextChanged
            }
        }

        val previousActivity = intent.extras?.getString("previous_activity")
        if (previousActivity == "day_table") {
            backButton.setOnClickListener {
                val intent = Intent(this@PreferencesActivity, DayActivity::class.java)
                intent.putExtra("previous_activity", "preferences_activity")
                startActivity(intent)
            }
        } else {
            backButton.setOnClickListener {
                val intent = Intent(this@PreferencesActivity, ScheduleActivity::class.java)
                intent.putExtra("previous_activity", "preferences_activity")
                startActivity(intent)
            }
        }
    }

    private fun changeNotificationPeriodFieldLayoutState(state: Boolean) {
        if (state) {
            notificationPeriodComment.setTextColor(getColor(R.color.black))
            notificationPeriodField.setTextColor(getColor(R.color.black))
        } else {
            notificationPeriodComment.setTextColor(getColor(R.color.gray))
            notificationPeriodField.setTextColor(getColor(R.color.gray))
        }

        notificationPeriodBox.isEnabled = state
        notificationPeriodField.isEnabled = state
    }

    private fun resetServices() {
        stopService(Intent(this, NotificationService::class.java))
        WorkManager.getInstance(this).cancelAllWork()
    }
}
