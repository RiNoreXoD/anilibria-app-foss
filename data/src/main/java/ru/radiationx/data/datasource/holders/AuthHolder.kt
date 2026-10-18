package ru.radiationx.data.datasource.holders

import kotlinx.coroutines.flow.Flow

/**
 * Created by radiationx on 30.12.17.
 */
interface AuthHolder {
    suspend fun getDeviceId(): String

    fun observeAuthSkipped(): Flow<Boolean>
    suspend fun getAuthSkipped(): Boolean
    suspend fun setAuthSkipped(value: Boolean)
}