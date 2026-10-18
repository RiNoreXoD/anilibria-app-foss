package ru.radiationx.anilibria.di

import android.content.Context
import ru.mintrocket.lib.mintpermissions.MintPermissions
import ru.mintrocket.lib.mintpermissions.flows.MintPermissionsFlow
import ru.radiationx.anilibria.AppBuildConfig
import ru.radiationx.anilibria.AppMigrationExecutor
import ru.radiationx.anilibria.TvCheckerSources
import ru.radiationx.data.SharedBuildConfig
import ru.radiationx.data.datasource.remote.common.CheckerReserveSources
import ru.radiationx.data.migration.MigrationExecutor
import ru.radiationx.quill.QuillModule
import ru.radiationx.shared_app.imageloader.LibriaImageLoader
import ru.radiationx.shared_app.imageloader.impls.CoilLibriaImageLoaderImpl

class AppModule(context: Context) : QuillModule() {


    init {
        instance { context }

        singleImpl<SharedBuildConfig, AppBuildConfig>()
        singleImpl<CheckerReserveSources, TvCheckerSources>()
        singleImpl<MigrationExecutor, AppMigrationExecutor>()

        singleImpl<LibriaImageLoader, CoilLibriaImageLoaderImpl>()

        instance {
            MintPermissions.controller
        }

        instance {
            MintPermissionsFlow.dialogFlow
        }
    }

}