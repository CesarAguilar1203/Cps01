package com.example.cps01

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.work.*
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit
import com.example.cps01.ui.theme.CpS01Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val prefs = getSharedPreferences("credentials", MODE_PRIVATE)

        setContent {
            CpS01Theme {
                var loggedIn by rememberSaveable {
                    mutableStateOf(prefs.getBoolean("loggedIn", false))
                }
                Surface(Modifier.fillMaxSize()) {
                    if (loggedIn) {
                        CameraScreen()
                    } else {
                        LoginScreen {
                            prefs.edit().putBoolean("loggedIn", true).apply()
                            loggedIn = true
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CameraScreen(streamUrl: String = MotionWorker.STREAM_URL) {
    var frameBytes by remember { mutableStateOf<ByteArray?>(null) }
    val context  = LocalContext.current
    val scope    = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            readMjpegStream(streamUrl) { frameBytes = it }
        }
        startMotionWorker(context)
    }

    Box(Modifier.fillMaxSize()) {
        AsyncImage(
            model            = frameBytes,
            contentDescription = null,
            modifier         = Modifier.fillMaxSize()
        )
        OutlinedButton(
            onClick  = { /* Config futura */ },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) { Text("Config") }
    }
}

// ­Lee MJPEG eternamente
private suspend fun readMjpegStream(url: String, onFrame: (ByteArray) -> Unit) {
    withContext(Dispatchers.IO) {
        while (true) {
            try {
                val conn = URL(url).openConnection() as HttpURLConnection
                conn.connectTimeout = 5000
                conn.readTimeout    = 5000
                val stream = conn.inputStream
                val buffer = ByteArrayOutputStream()
                var prev = -1
                var b: Int
                while (stream.read().also { b = it } != -1) {
                    if (prev == 0xFF && b == 0xD8) buffer.reset()
                    buffer.write(b)
                    if (prev == 0xFF && b == 0xD9) onFrame(buffer.toByteArray())
                    prev = b
                }
                stream.close(); conn.disconnect()
            } catch (_: Exception) { delay(1000) }
        }
    }
}

// ­Lanza WorkManager cada 15 min
private fun startMotionWorker(ctx: android.content.Context) {
    val req = PeriodicWorkRequestBuilder<MotionWorker>(15, TimeUnit.MINUTES).build()
    WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(
        "motion-check",
        ExistingPeriodicWorkPolicy.KEEP,
        req
    )
}

@Preview(showBackground = true)
@Composable
fun CameraScreenPreview() {
    CpS01Theme { CameraScreen() }
}
