package com.example.cps01

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import java.net.HttpURLConnection
import java.net.URL

class MotionWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val connection = URL(MOTION_URL).openConnection() as HttpURLConnection
            connection.connectTimeout = 5000
            val text = connection.inputStream.bufferedReader().use { it.readText().trim() }
            connection.disconnect()
            if (text == "1") {
                sendNotification()
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun sendNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            NotificationManagerCompat.from(applicationContext).createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("¡Movimiento detectado!")
            .setContentText("Se ha detectado movimiento frente a la cámara.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        NotificationManagerCompat.from(applicationContext).notify(1000, notification)
    }

    companion object {
        const val CHANNEL_ID = "motion"
        const val CHANNEL_NAME = "Motion Detection"
        const val MOTION_URL = "http://192.168.1.100/motion"
        const val STREAM_URL = "http://192.168.1.100:81/stream"
    }
}
