package com.example.cps01

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.work.*
import coil.compose.AsyncImage
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit
import com.example.cps01.ui.theme.CpS01Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CpS01Theme {
                var loggedIn by remember { mutableStateOf(false) }
                Surface(modifier = Modifier.fillMaxSize()) {
                    if (loggedIn) {
                        CameraScreen()
                    } else {
                        LoginScreen { loggedIn = true }
                    }
                }
            }
        }
    }
}

@Composable
fun CameraScreen(streamUrl: String = MotionWorker.STREAM_URL) {
    var frameBytes by remember { mutableStateOf<ByteArray?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            readMjpegStream(streamUrl) { bytes ->
                frameBytes = bytes
            }
        }
        startMotionWorker(context)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
            model = frameBytes,
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
        OutlinedButton(
            onClick = { /* future config */ },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Text("Config")
        }
    }
}

private suspend fun readMjpegStream(url: String, onFrame: (ByteArray) -> Unit) {
    withContext(Dispatchers.IO) {
        while (true) {
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                val stream = connection.inputStream
                val buffer = ByteArrayOutputStream()
                var prev = -1
                var b: Int
                while (stream.read().also { b = it } != -1) {
                    if (prev == 0xFF && b == 0xD8) buffer.reset()
                    buffer.write(b)
                    if (prev == 0xFF && b == 0xD9) {
                        onFrame(buffer.toByteArray())
                    }
                    prev = b
                }
                stream.close()
                connection.disconnect()
            } catch (_: Exception) {
                delay(1000)
            }
        }
    }
}

private fun startMotionWorker(context: android.content.Context) {
    val request = PeriodicWorkRequestBuilder<MotionWorker>(15, TimeUnit.MINUTES)
        .build()
    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "motion-check",
        ExistingPeriodicWorkPolicy.KEEP,
        request
    )
}

@Preview(showBackground = true)
@Composable
fun CameraScreenPreview() {
    CpS01Theme {
        CameraScreen()
    }
}
