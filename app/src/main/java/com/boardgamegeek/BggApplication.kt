package com.boardgamegeek

import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.os.StrictMode.VmPolicy
import androidx.multidex.MultiDexApplication
import androidx.preference.PreferenceManager
import com.boardgamegeek.extensions.AccountPreferences
import com.boardgamegeek.extensions.PREFERENCES_KEY_SYNC_STATUSES
import com.boardgamegeek.extensions.getOldSyncStatuses
import com.boardgamegeek.extensions.setSyncStatuses
import com.boardgamegeek.pref.SyncPrefs
import com.boardgamegeek.util.CrashReportingTree
import com.boardgamegeek.util.HttpUtils
import com.boardgamegeek.util.NotificationUtils
import com.boardgamegeek.util.RemoteConfig
import com.facebook.stetho.Stetho
import com.google.android.gms.tasks.Task
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.messaging.FirebaseMessaging
import com.jakewharton.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import timber.log.Timber

class BggApplication : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        enableStrictMode()
        initializeStetho()
        initializeFirebase()
        initializeTimber()
        initializePicasso()
        migrateData()
    }

    private fun initializeFirebase() {
        if (BuildConfig.DEBUG) {
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(false)
        } else {
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
            val firebase = FirebaseCrashlytics.getInstance()
            PreferenceManager.getDefaultSharedPreferences(this).getString(AccountPreferences.KEY_USERNAME, "")?.let {
                if (it.isNotBlank()) firebase.setUserId(it.hashCode().toString())
            }
            firebase.setCustomKey("BUILD_TIME", BuildConfig.BUILD_TIME)
            firebase.setCustomKey("GIT_SHA", BuildConfig.GIT_SHA)
        }
        RemoteConfig.init()
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task: Task<String?> ->
            if (task.isSuccessful) {
                Timber.i("Firebase token is %s", task.result)
            } else {
                Timber.w(task.exception, "Fetching FCM registration token failed")
            }
        }
    }

    private fun initializeTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashReportingTree())
        }
    }

    private fun initializeStetho() {
        if (BuildConfig.DEBUG) {
            Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                    .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                    .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
                    .build()
            )
        }
    }

    private fun initializePicasso() {
        Picasso.setSingletonInstance(
            Picasso.Builder(this)
                .downloader(OkHttp3Downloader(HttpUtils.getHttpClientWithCache(this)))
                .build()
        )
    }

    private fun enableStrictMode() {
        if (BuildConfig.DEBUG) {
            val builder = VmPolicy.Builder()
                .detectActivityLeaks()
                .detectLeakedClosableObjects()
                .detectLeakedSqlLiteObjects()
                .detectFileUriExposure()
                .detectLeakedRegistrationObjects()
                .penaltyLog()
            if (VERSION.SDK_INT >= VERSION_CODES.M) {
                builder.detectCleartextNetwork()
            }
            StrictMode.setVmPolicy(builder.build())
            StrictMode.setThreadPolicy(
                ThreadPolicy.Builder()
                    .detectDiskWrites()
                    .detectNetwork()
                    .penaltyLog()
                    .penaltyFlashScreen()
                    .build()
            )
        }
    }

    private fun migrateData() {
        if (VERSION.SDK_INT >= VERSION_CODES.O) NotificationUtils.createNotificationChannels(applicationContext)
        migrateCollectionStatusSettings()
        SyncPrefs.migrate(this)
    }

    private fun migrateCollectionStatusSettings() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val set = prefs.getStringSet(PREFERENCES_KEY_SYNC_STATUSES, null)
        if (set == null) {
            val oldSyncStatuses = prefs.getOldSyncStatuses(applicationContext)
            if (oldSyncStatuses.isNotEmpty()) {
                prefs.setSyncStatuses(oldSyncStatuses)
            }
        }
    }
}