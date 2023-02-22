package org.karasiksoftware.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import org.karasiksoftware.dataclasses.database.DatabaseMeetingsData
import org.karasiksoftware.activities.DayActivity
import org.karasiksoftware.activities.R
import org.karasiksoftware.utils.DatabaseUtils
import java.time.LocalDate
import kotlin.properties.Delegates

class NotificationService : Service() {
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var notificationManager: NotificationManagerCompat
    private lateinit var notificationChanel: NotificationChannel
    private lateinit var prevAction: NotificationCompat.Action
    private lateinit var nextAction: NotificationCompat.Action
    private lateinit var emptyPrevAction: NotificationCompat.Action
    private lateinit var emptyNextAction: NotificationCompat.Action
    private lateinit var stopAction: NotificationCompat.Action
    private lateinit var notificationTouchPendingIntent: PendingIntent
    private lateinit var database: DatabaseUtils
    private lateinit var meetings: DatabaseMeetingsData
    private lateinit var date: LocalDate
    private var meetingIndex by Delegates.notNull<Int>()

    companion object {
        const val REQUEST_CODE = 0
        const val NOTIFICATION_ID = 10
        const val CHANNEL_ID = "10"
        const val ACTION_NEXT_MEETING = "nextMeeting"
        const val ACTION_PREV_MEETING = "prevMeeting"
        const val ACTION_STOP_SERVICE = "stopService"
        const val ACTION_START = "startService"
        const val DESCRIPTION = "Today`s meetings"
    }

    override fun onCreate() {
        database = DatabaseUtils(this)
        val token = database.getUserToken()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED || !token.isTokenAvailable)
        {
            this.stopService(Intent(this, this::class.java))
        }

        notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
        notificationManager = NotificationManagerCompat.from(this)
        notificationChanel = NotificationChannel(CHANNEL_ID, DESCRIPTION, NotificationManager.IMPORTANCE_DEFAULT)

        notificationChanel.description = DESCRIPTION
        notificationChanel.setShowBadge(true)
        notificationChanel.enableVibration(false)
        notificationChanel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        notificationManager.createNotificationChannel(notificationChanel)

        date = LocalDate.now()
        meetings = database.getMeetingsTableData()
        meetingIndex = meetings.meetingsIndex

        val nextMeetingPendingIntent = PendingIntent.getService(
            this,
            REQUEST_CODE,
            Intent(this, this::class.java)
                .setAction(ACTION_NEXT_MEETING),
            PendingIntent.FLAG_IMMUTABLE
        )
        val prevMeetingPendingIntent = PendingIntent.getService(
            this,
            REQUEST_CODE,
            Intent(this, this::class.java)
                .setAction(ACTION_PREV_MEETING),
            PendingIntent.FLAG_IMMUTABLE
        )
        val stopServicePendingIntent = PendingIntent.getService(
            this,
            REQUEST_CODE,
            Intent(this, this::class.java)
                .setAction(ACTION_STOP_SERVICE),
            PendingIntent.FLAG_IMMUTABLE)
        notificationTouchPendingIntent = PendingIntent.getActivity(
            this,
            REQUEST_CODE,
            Intent(this, DayActivity::class.java)
                .putExtra("previous_activity", "service")
                .putExtra("service_time", date.toString()),
            PendingIntent.FLAG_IMMUTABLE
        )

        nextAction = NotificationCompat.Action(
            R.drawable.forward,
            getString(R.string.notification_action_next),
            nextMeetingPendingIntent
        )
        prevAction = NotificationCompat.Action(
            R.drawable.back,
            getString(R.string.notification_action_prev),
            prevMeetingPendingIntent
        )
        emptyNextAction = NotificationCompat.Action(
            R.drawable.forward,
            getString(R.string.notification_action_next),
            null
        )
        emptyPrevAction = NotificationCompat.Action(
            R.drawable.back,
            getString(R.string.notification_action_prev),
            null
        )
        stopAction = NotificationCompat.Action(
            R.drawable.back,
            getString(R.string.notification_action_stop),
            stopServicePendingIntent
        )

        super.onCreate()
    }

    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val notification = when (meetings.meetingsSize) {
                    0 -> getNoMeetingsNotification()

                    1 -> getNotification(emptyPrevAction, emptyNextAction, meetings)

                    else -> getNotification(emptyPrevAction, nextAction, meetings)
                }

                notificationManager.notify(NOTIFICATION_ID, notification)
            }

            ACTION_NEXT_MEETING -> {
                meetingIndex += 1

                val notification = if (meetingIndex == meetings.meetingsSize-1) {
                    getNotification(prevAction, emptyNextAction, meetings)
                } else {
                    getNotification(prevAction, nextAction, meetings)
                }

                notificationManager.notify(NOTIFICATION_ID, notification)
                setIndex()
            }

            ACTION_PREV_MEETING -> {
                meetingIndex -= 1

                val notification = if (meetingIndex == 0) {
                    getNotification(emptyPrevAction, nextAction, meetings)
                } else {
                    getNotification(prevAction, nextAction, meetings)
                }

                notificationManager.notify(NOTIFICATION_ID, notification)
                setIndex()
            }

            ACTION_STOP_SERVICE -> this.stopService(Intent(this, this::class.java))

            else -> {}
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun getNotification(
        prevActionArg: NotificationCompat.Action,
        nextActionArg: NotificationCompat.Action,
        meeting: DatabaseMeetingsData
    ): Notification {
        val meetingName = meeting.meetingsNames[meetingIndex]
        val meetingStart = meeting.meetingsStarts[meetingIndex]
        val meetingEnd = meeting.meetingsEnds[meetingIndex]
        val meetingAud = meeting.meetingsAuds[meetingIndex]

        notificationBuilder.clearActions()

        return notificationBuilder
            .setSmallIcon(R.mipmap.main_icon)
            .setContentTitle(meetingName)
            .setContentText("$meetingStart-$meetingEnd, $meetingAud")
            .setOngoing(true)
            .addAction(prevActionArg)
            .addAction(nextActionArg)
            .addAction(stopAction)
            .setContentIntent(notificationTouchPendingIntent)
            .setVibrate(null)
            .build()
    }

    private fun getNoMeetingsNotification(): Notification {
        return notificationBuilder
            .setSmallIcon(R.mipmap.main_icon)
            .setContentTitle(getString(R.string.no_meetings_notification))
            .setContentText("")
            .setOngoing(true)
            .addAction(emptyPrevAction)
            .addAction(emptyNextAction)
            .addAction(stopAction)
            .setContentIntent(notificationTouchPendingIntent)
            .setVibrate(null)
            .build()
    }

    private fun setIndex() { database.setMeetingsTableData(meetings) }
}