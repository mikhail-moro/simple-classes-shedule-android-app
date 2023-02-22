package org.karasiksoftware.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.karasiksoftware.utils.DatabaseUtils
import org.karasiksoftware.utils.RequestUtils
import org.karasiksoftware.dataclasses.calendar.CalendarItem
import java.time.LocalDate

lateinit var globalDate: LocalDate
lateinit var globalMonthMeetingsData: List<CalendarItem>

class LoginActivity : AppCompatActivity() {
    private lateinit var usernameInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var message: TextView
    private lateinit var loginButton: Button
    private lateinit var check: CheckBox
    private lateinit var downloadAnimationItem: ImageView
    private lateinit var downloadAnimation: Animation
    private lateinit var database: DatabaseUtils
    private lateinit var preferences: SharedPreferences
    private var isUserChangeSave = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_login)

        usernameInput = findViewById(R.id.username)
        passwordInput = findViewById(R.id.password)
        loginButton = findViewById(R.id.login)
        message = findViewById(R.id.message)
        check = findViewById(R.id.check)

        downloadAnimationItem = findViewById(R.id.login_animation_item)
        downloadAnimation = AnimationUtils.loadAnimation(this, R.anim.login_animation)

        globalDate = LocalDate.now()
        database = DatabaseUtils(this)
        preferences = getSharedPreferences("data", MODE_PRIVATE)

        val tokenData = database.getUserToken()

        val isResetToken = intent.extras?.getString("previous_activity") == "preferences_activity"
        val isSaveUserData = preferences.getBoolean("is_save_user_data", false)
        val isTokenAvailable = if (isSaveUserData) {
            val token = tokenData.token
            if (token != "") tokenData.endDate!! > globalDate else false
        } else {
            false
        }

        if (!isTokenAvailable && !isResetToken && isSaveUserData) {
            message.text = getString(R.string.token_is_not_available)
        }

        if (isTokenAvailable) {
            usernameInput.alpha = 0f
            passwordInput.alpha = 0f
            loginButton.alpha = 0f
            message.alpha = 0f
            check.alpha = 0f

            downloadAnimationItem.alpha = 1f
            downloadAnimationItem.startAnimation(downloadAnimation)

            val toastText = getString(R.string.next_auth_will_be) + " ${tokenData.endDate}"
            val toast = Toast.makeText(this, toastText, toastText.length)
            toast.show()

            val intent = Intent(this, ScheduleActivity::class.java)
            intent.putExtra("previous_activity", "login_activity")
            startActivity(intent)
        } else {
            check.setOnClickListener { isUserChangeSave = true }

            loginButton.isEnabled = true
            loginButton.setOnClickListener {
                val username = usernameInput.text.toString()
                val password = passwordInput.text.toString()

                if (username == "" || password == "") {
                    message.text = getString(R.string.write_mail_and_password)
                    usernameInput.setOnClickListener { message.text = "" }
                    passwordInput.setOnClickListener { message.text = "" }
                } else {
                    downloadAnimationItem.alpha = 1f
                    downloadAnimationItem.startAnimation(downloadAnimation)

                    val requestUtils = RequestUtils()
                    val database = DatabaseUtils(this@LoginActivity)

                    CoroutineScope(Dispatchers.Default).launch {
                        try {
                            val tokenRequestData = requestUtils.getToken(username, password)

                            if (tokenRequestData.state != -1) {
                                database.setUserToken(tokenRequestData.token)

                                val editor = preferences.edit()
                                editor.putBoolean("is_save_user_data", check.isChecked)
                                editor.apply()

                                val intent = Intent(this@LoginActivity, ScheduleActivity::class.java)
                                intent.putExtra("previous_activity", "login_activity")
                                startActivity(intent)
                            } else {
                                runOnUiThread {
                                    message.alpha = 1f
                                    loginButton.alpha = 1f

                                    message.text = getString(R.string.wrong_mail_or_password)
                                }
                            }
                        } catch (ex: Exception) {
                            runOnUiThread {
                                downloadAnimationItem.alpha = 0f
                                downloadAnimationItem.clearAnimation()

                                message.alpha = 1f
                                loginButton.alpha = 1f

                                message.text = getString(R.string.server_error)
                                loginButton.text = getString(R.string.try_again)
                            }
                        }
                    }
                }
            }
        }
    }
}