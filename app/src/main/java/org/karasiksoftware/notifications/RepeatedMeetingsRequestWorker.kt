package org.karasiksoftware.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.*
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import org.karasiksoftware.activities.DayActivity
import org.karasiksoftware.activities.R
import org.karasiksoftware.dataclasses.database.DatabaseMeetingsData
import org.karasiksoftware.utils.CalendarUtils
import org.karasiksoftware.utils.DatabaseUtils
import org.karasiksoftware.utils.RequestUtils
import java.time.LocalDate


class RepeatedMeetingsRequestWorker(
    private val context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {

    companion object {
        const val REQUEST_CODE = 0
        const val NOTIFICATION_ID = 10
        const val CHANNEL_ID = "10"
        const val ACTION_NEXT_MEETING = "nextMeeting"
        const val ACTION_STOP_SERVICE = "stopService"
    }

    override suspend fun doWork(): Result {
        val database = DatabaseUtils(context)
        val userData = database.getUserToken()
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED || !userData.isTokenAvailable)
        {
            context.stopService(Intent(context, NotificationService::class.java))
            WorkManager.getInstance(context).cancelAllWork()

            return Result.failure()
        } else {
            return try {
                val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
                val notificationManager = NotificationManagerCompat.from(context)
                val calendarUtils = CalendarUtils()
                val requestUtils = RequestUtils()
                val date = LocalDate.now()

                val nextMeetingPendingIntent = PendingIntent.getService(
                    context,
                    REQUEST_CODE,
                    Intent(context, NotificationService::class.java)
                        .setAction(ACTION_NEXT_MEETING),
                    PendingIntent.FLAG_IMMUTABLE
                )
                val stopServicePendingIntent = PendingIntent.getService(
                    context,
                    REQUEST_CODE,
                    Intent(context, NotificationService::class.java)
                        .setAction(ACTION_STOP_SERVICE),
                    PendingIntent.FLAG_IMMUTABLE
                )
                val notificationTouchPendingIntent = PendingIntent.getActivity(
                    context,
                    REQUEST_CODE,
                    Intent(context, DayActivity::class.java)
                        .putExtra("previous_activity", "service")
                        .putExtra("service_time", date.toString()),
                    PendingIntent.FLAG_IMMUTABLE
                )

                val nextAction = NotificationCompat.Action(
                    R.drawable.forward,
                    context.getString(R.string.notification_action_next),
                    nextMeetingPendingIntent
                )
                val emptyNextAction = NotificationCompat.Action(
                    R.drawable.forward,
                    context.getString(R.string.notification_action_next),
                    null
                )
                val emptyPrevAction = NotificationCompat.Action(
                    R.drawable.back,
                    context.getString(R.string.notification_action_prev),
                    null
                )
                val stopAction = NotificationCompat.Action(
                    R.drawable.back,
                    context.getString(R.string.notification_action_stop),
                    stopServicePendingIntent
                )

                val rawMeetingsData = requestUtils.getData(
                    date.monthValue,
                    date.year,
                    userData.token
                )

                val meetingsData = calendarUtils.getDaySchedule(rawMeetingsData, date).meetings
                val meetings = DatabaseMeetingsData(
                    meetingsData.size,
                    0,
                    meetingsData.map { it.name!! },
                    meetingsData.map { it.startTime },
                    meetingsData.map { it.endTime },
                    meetingsData.map { it.aud!! }
                )

                database.setMeetingsTableData(meetings)
                val notification = if (meetings.meetingsSize == 0) {
                    notificationBuilder
                        .setSmallIcon(R.mipmap.main_icon)
                        .setContentTitle("Сегодня нет пар")
                        .setContentText("")
                        .setOngoing(true)
                        .addAction(emptyPrevAction)
                        .addAction(emptyNextAction)
                        .addAction(stopAction)
                        .setContentIntent(notificationTouchPendingIntent)
                        .setVibrate(null)
                        .build()
                } else {
                    val meetingName = meetings.meetingsNames[0]
                    val meetingStart = meetings.meetingsStarts[0]
                    val meetingEnd = meetings.meetingsEnds[0]
                    val meetingAud = meetings.meetingsAuds[0]

                    notificationBuilder.clearActions()

                    notificationBuilder
                        .setSmallIcon(R.mipmap.main_icon)
                        .setContentTitle(meetingName)
                        .setContentText("$meetingStart-$meetingEnd, $meetingAud")
                        .setOngoing(true)
                        .addAction(emptyPrevAction)
                        .addAction(nextAction)
                        .addAction(stopAction)
                        .setContentIntent(notificationTouchPendingIntent)
                        .setVibrate(null)
                        .build()
                }

                notificationManager.notify(NOTIFICATION_ID, notification)
                database.setMeetingsTableData(meetings)

                Result.success()
            } catch (ex: Exception) {
                ex.printStackTrace()
                Result.failure()
            }
        }
    }
}