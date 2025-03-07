package taco.scoop

import android.Manifest
import android.annotation.TargetApi
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.SystemClock
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import com.topjohnwu.superuser.Shell
import taco.scoop.detector.CrashDetectorService

class ScoopApplication : Application() {

    private val permissionLock = Object()

    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerCrashesChannel()
            registerStatusChannel()
        }

        if (!serviceActive()) {
            val thread = HandlerThread("startCrashDetector")
            thread.start()
            Handler(thread.looper).post(this@ScoopApplication::startService)
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun registerCrashesChannel() {
        val name = getString(R.string.crash_channel)
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel("crashes", name, importance)
        channel.createNotifChannel()
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun registerStatusChannel() {
        val name = getString(R.string.status_channel)
        val importance = NotificationManager.IMPORTANCE_MIN
        val channel = NotificationChannel("status", name, importance)
        channel.setShowBadge(false)
        channel.createNotifChannel()
    }

    @TargetApi(Build.VERSION_CODES.O)
    fun NotificationChannel.createNotifChannel() {
        val notificationManager = getSystemService<NotificationManager>()
        notificationManager?.createNotificationChannel(this)
    }

    fun startService(): Boolean {
        if (!isPermissionGranted()) return false
        startService(Intent(this, CrashDetectorService::class.java))
        return true
    }

    fun stopService() {
        stopService(Intent(this, CrashDetectorService::class.java))
    }

    private fun isPermissionGranted(tryGranting: Boolean = true): Boolean {
        synchronized(permissionLock) {
            val permission = Manifest.permission.READ_LOGS
            val granted = ContextCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_GRANTED
            return if (!granted && tryGranting) {
                Shell.su(
                    "pm grant ${BuildConfig.APPLICATION_ID} $permission"
                ).exec()
                isPermissionGranted(false)
            } else {
                granted
            }
        }
    }

    companion object {

        fun serviceActive() = false

        @JvmStatic
        val bootTime = System.currentTimeMillis() - SystemClock.uptimeMillis()
    }
}
