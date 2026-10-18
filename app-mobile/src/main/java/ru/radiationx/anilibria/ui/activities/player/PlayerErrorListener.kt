package ru.radiationx.anilibria.ui.activities.player

import android.content.Context
import androidx.media3.common.PlaybackException
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.source.LoadEventInfo
import androidx.media3.exoplayer.source.MediaLoadData
import timber.log.Timber
import java.io.IOException

/**
 * Локальный слушатель ошибок плеера: пишет ошибки только в лог (Timber),
 * никакая телеметрия никуда не отправляется.
 */
class PlayerErrorListener(
    private val context: Context,
) : AnalyticsListener {

    @UnstableApi
    override fun onLoadError(
        eventTime: AnalyticsListener.EventTime,
        loadEventInfo: LoadEventInfo,
        mediaLoadData: MediaLoadData,
        error: IOException,
        wasCanceled: Boolean,
    ) {
        Timber.e(error, "onLoadError: ${loadEventInfo.uri}")
    }

    @UnstableApi
    override fun onPlayerError(
        eventTime: AnalyticsListener.EventTime,
        error: PlaybackException,
    ) {
        Timber.e(error, "onPlayerError: ${error.errorCodeName}")
    }

    @UnstableApi
    override fun onAudioCodecError(
        eventTime: AnalyticsListener.EventTime,
        audioCodecError: Exception,
    ) {
        Timber.e(audioCodecError, "onAudioCodecError")
    }

    @UnstableApi
    override fun onAudioSinkError(
        eventTime: AnalyticsListener.EventTime,
        audioSinkError: Exception,
    ) {
        Timber.e(audioSinkError, "onAudioSinkError")
    }

    @UnstableApi
    override fun onVideoCodecError(
        eventTime: AnalyticsListener.EventTime,
        videoCodecError: Exception,
    ) {
        Timber.e(videoCodecError, "onVideoCodecError")
    }
}
