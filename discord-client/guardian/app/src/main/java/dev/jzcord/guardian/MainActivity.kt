package dev.jzcord.guardian

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var tokenField: EditText
    private lateinit var passwordField: EditText
    private lateinit var actionButton: Button
    private lateinit var statusText: TextView
    private lateinit var logText: TextView
    private lateinit var prefs: Prefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = Prefs.get(this)
        tokenField = findViewById(R.id.token)
        passwordField = findViewById(R.id.password)
        actionButton = findViewById(R.id.action)
        statusText = findViewById(R.id.status)
        logText = findViewById(R.id.log)

        tokenField.setText(prefs.token)
        passwordField.setText(prefs.password)
        updateUi()

        actionButton.setOnClickListener {
            if (prefs.enabled) {
                stopGuardian()
            } else {
                startGuardian()
            }
        }

        findViewById<Button>(R.id.battery).setOnClickListener { openBatterySettings() }

        requestNotificationPermission()
    }

    @SuppressLint("BatteryLife")
    private fun openBatterySettings() {
        try {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                .setData(Uri.parse("package:$packageName"))
            startActivity(intent)
        } catch (e: Throwable) {
            try {
                startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
            } catch (e2: Throwable) {
                Toast.makeText(this, "Impossible d'ouvrir — va manuellement dans Paramètres → Apps → JZCord Guardian → Batterie", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun startGuardian() {
        val token = tokenField.text.toString().trim()
        val password = passwordField.text.toString()
        if (token.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Remplis les deux champs", Toast.LENGTH_LONG).show()
            return
        }
        prefs.token = token
        prefs.password = password
        prefs.clearKnown()
        prefs.enabled = true
        GuardianService.start(this)
        updateUi()
        logText.text = "Service démarré. Garde Discord fermé si tu veux tester : connecte-toi depuis un navigateur, la session doit être virée en quelques secondes."
    }

    private fun stopGuardian() {
        prefs.enabled = false
        GuardianService.stop(this)
        updateUi()
        logText.text = "Service arrêté. Les sessions ne sont plus surveillées."
    }

    private fun updateUi() {
        if (prefs.enabled) {
            statusText.text = getString(R.string.status_on)
            actionButton.text = getString(R.string.stop)
        } else {
            statusText.text = getString(R.string.status_off)
            actionButton.text = getString(R.string.start)
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
            }
        }
    }
}
