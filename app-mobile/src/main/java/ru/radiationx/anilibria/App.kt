package ru.radiationx.anilibria

import android.app.Application
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.mintrocket.lib.mintpermissions.ext.initMintPermissions
import ru.mintrocket.lib.mintpermissions.flows.ext.initMintPermissionsFlow
import ru.radiationx.anilibria.di.AppModule
import ru.radiationx.data.datasource.holders.PreferencesHolder
import ru.radiationx.data.di.DataModule
import ru.radiationx.data.migration.MigrationDataSource
import ru.radiationx.quill.Quill
import ru.radiationx.quill.get
import timber.log.Timber

/*  Created by radiationx on 05.11.17. */
class App : Application() {

    override fun onCreate() {
        super.onCreate()

        if (isMainProcess()) {
            initInMainProcess()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun initInMainProcess() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        initDependencies()

        appVersionCheck()

        try {
            FirebaseMessaging.getInstance().apply {
                isAutoInitEnabled = true
            }
        } catch (ex: Throwable) {
            Timber.e(ex)
        }

        val preferencesHolder = get<PreferencesHolder>()

        preferencesHolder
            .notificationsAll
            .onEach {
                changeSubscribeStatus(it, "all")
            }
            .launchIn(GlobalScope)

        preferencesHolder
            .notificationsService
            .onEach {
                changeSubscribeStatus(it, "service")
                changeSubscribeStatus(it, "app_update")
                changeSubscribeStatus(it, "config")
            }
            .launchIn(GlobalScope)


        initMintPermissions()
        initMintPermissionsFlow()
    }

    private fun changeSubscribeStatus(enabled: Boolean, topic: String) {
        try {
            FirebaseMessaging.getInstance().apply {
                if (enabled) {
                    subscribeToTopic(topic)
                    subscribeToTopic("android_$topic")
                } else {
                    unsubscribeFromTopic(topic)
                    unsubscribeFromTopic("android_$topic")
                }
            }
        } catch (ex: Exception) {
            Timber.e(ex)
        }
    }

    private fun initDependencies() {
        Quill.getRootScope().installModules(AppModule(this), DataModule(this))
    }

    private fun appVersionCheck() {
        val migrationDataSource = get<MigrationDataSource>()
        migrationDataSource.update()
    }

    private fun isMainProcess() = packageName == getCurrentProcessName()

    private fun getCurrentProcessName(): String? {
        return Application.getProcessName()
    }

}
