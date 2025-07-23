package com.example.cps01

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.cps01.ui.theme.CpS01Theme
import com.example.cps01.R

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    var email     by remember { mutableStateOf("") }
    var password  by remember { mutableStateOf("") }
    val context   = LocalContext.current
    val prefs     = remember { context.getSharedPreferences("credentials", Context.MODE_PRIVATE) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement   = Arrangement.Center
    ) {
        // ­Logo y título
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier.size(72.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text  = stringResource(R.string.app_title),
                style = MaterialTheme.typography.headlineMedium
            )
        }

        Spacer(Modifier.height(24.dp))

        // ­Correo
        OutlinedTextField(
            value         = email,
            onValueChange = { email = it },
            label         = { Text(stringResource(R.string.email)) },
            modifier      = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        // ­Contraseña
        OutlinedTextField(
            value         = password,
            onValueChange = { password = it },
            label         = { Text(stringResource(R.string.password)) },
            visualTransformation = PasswordVisualTransformation(),
            modifier      = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))

        // ­Botón Iniciar sesión
        Button(
            onClick = {
                if (email == prefs.getString("email", null) &&
                    password == prefs.getString("password", null)
                ) {
                    prefs.edit().putBoolean("loggedIn", true).apply()
                    onLoginSuccess()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text(stringResource(R.string.login)) }

        Spacer(Modifier.height(8.dp))

        // ­Botón Registrarse
        OutlinedButton(
            onClick = {
                prefs.edit()
                    .putString("email", email)
                    .putString("password", password)
                    .apply()
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text(stringResource(R.string.register)) }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    CpS01Theme { LoginScreen {} }
}
