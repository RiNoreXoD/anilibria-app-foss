package ru.radiationx.anilibria.ui.fragments.feed

import android.Manifest
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.terrakok.cicerone.Router
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import ru.mintrocket.lib.mintpermissions.MintPermissionsController
import ru.mintrocket.lib.mintpermissions.ext.isGranted
import ru.mintrocket.lib.mintpermissions.flows.MintPermissionsDialogFlow
import ru.radiationx.anilibria.model.DonationCardItemState
import ru.radiationx.anilibria.model.ReleaseItemState
import ru.radiationx.anilibria.model.ScheduleItemState
import ru.radiationx.anilibria.model.YoutubeItemState
import ru.radiationx.anilibria.model.toState
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.anilibria.utils.ShortcutHelper
import ru.radiationx.data.datasource.holders.PreferencesHolder
import ru.radiationx.data.datasource.holders.ReleaseUpdateHolder
import ru.radiationx.data.entity.domain.feed.FeedItem
import ru.radiationx.data.entity.domain.feed.ScheduleItem
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.entity.domain.release.ReleaseUpdate
import ru.radiationx.data.entity.domain.types.ReleaseId
import ru.radiationx.data.entity.domain.types.YoutubeId
import ru.radiationx.data.entity.domain.youtube.YoutubeItem
import ru.radiationx.data.interactors.ReleaseInteractor
import ru.radiationx.data.repository.CheckerRepository
import ru.radiationx.data.repository.DonationRepository
import ru.radiationx.data.repository.FeedRepository
import ru.radiationx.data.repository.ScheduleRepository
import ru.radiationx.shared.ktx.asDayNameDeclension
import ru.radiationx.shared.ktx.asDayPretext
import ru.radiationx.shared.ktx.asMsk
import ru.radiationx.shared.ktx.coRunCatching
import ru.radiationx.shared.ktx.getDayOfWeek
import ru.radiationx.shared.ktx.isSameDay
import ru.radiationx.shared_app.common.SystemUtils
import ru.radiationx.shared_app.controllers.loaderpage.PageLoader
import ru.radiationx.shared_app.controllers.loaderpage.PageLoaderAction
import ru.radiationx.shared_app.controllers.loaderpage.PageLoaderParams
import ru.radiationx.shared_app.controllers.loaderpage.appendData
import ru.radiationx.shared_app.controllers.loaderpage.mapData
import ru.radiationx.shared_app.controllers.loaderpage.toDataAction
import timber.log.Timber
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/* Created by radiationx on 05.11.17. */

class FeedViewModel @Inject constructor(
    private val feedRepository: FeedRepository,
    private val releaseInteractor: ReleaseInteractor,
    private val scheduleRepository: ScheduleRepository,
    checkerRepository: CheckerRepository,
    releaseUpdateHolder: ReleaseUpdateHolder,
    private val appPreferences: PreferencesHolder,
    private val donationRepository: DonationRepository,
    private val router: Router,
    private val errorHandler: IErrorHandler,
    private val shortcutHelper: ShortcutHelper,
    private val systemUtils: SystemUtils,
    permissionsController: MintPermissionsController,
    private val permissionsDialogFlow: MintPermissionsDialogFlow,
) : ViewModel() {

    companion object {
        private const val DONATION_NEW_TAG = "donation_new"
    }

    private val appUpdateWarning = FeedAppWarning(
        "update",
        "Доступно обновление приложения",
        FeedAppWarningType.INFO
    )

    private val appNotificationsWarning = FeedAppWarning(
        "notifications",
        "Приложению требуется разрешение на отправку уведомлений о новых сериях и обновлениях приложения",
        FeedAppWarningType.WARNING
    )

    private val pageLoader = PageLoader(viewModelScope) {
        getDataSource(it)
    }

    private val _state = MutableStateFlow(FeedScreenState())
    val state = _state.asStateFlow()

    private val warningsController = AppWarningsController()

    private var randomJob: Job? = null


    init {

        checkerRepository
            .observeUpdate()
            .onEach {
                if (it.hasUpdate) {
                    warningsController.put(appUpdateWarning)
                } else {
                    warningsController.remove(appUpdateWarning.tag)
                }
            }
            .launchIn(viewModelScope)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsController
                .observe(Manifest.permission.POST_NOTIFICATIONS)
                .onEach {
                    if (!it.isGranted()) {
                        warningsController.put(appNotificationsWarning)
                    } else {
                        warningsController.remove(appNotificationsWarning.tag)
                    }
                }
                .launchIn(viewModelScope)
        }

        warningsController.warnings.onEach { warnings ->
            _state.update {
                it.copy(warnings = warnings.values.toList())
            }
        }.launchIn(viewModelScope)

        appPreferences
            .newDonationRemind
            .flatMapLatest { enabled ->
                donationRepository.observerDonationInfo().map {
                    Pair(it.cardNewDonations, enabled)
                }
            }
            .onEach { pair ->
                val newDonationState = if (pair.second) {
                    pair.first?.let {
                        DonationCardItemState(
                            tag = DONATION_NEW_TAG,
                            title = it.title,
                            subtitle = it.subtitle,
                            canClose = false
                        )
                    }
                } else {
                    null
                }
                _state.update {
                    it.copy(donationCardItemState = newDonationState)
                }
            }
            .launchIn(viewModelScope)

        combine(
            pageLoader.observeState(),
            releaseUpdateHolder.observeEpisodes()
        ) { loadingState, updates ->
            val updatesMap = updates.associateBy { it.id }
            loadingState.mapData {
                it.toState(updatesMap)
            }
        }
            .onEach { loadingState ->
                _state.update {
                    it.copy(data = loadingState)
                }
            }
            .launchIn(viewModelScope)

        pageLoader.refresh()
    }

    fun refreshReleases() {
        pageLoader.refresh()
    }

    fun loadMore() {
        pageLoader.loadMore()
    }

    fun onScheduleScroll(position: Int) {
    }

    fun onScheduleItemClick(item: ScheduleItemState, position: Int) {
        val releaseItem = findScheduleRelease(item.release.id) ?: return
        router.navigateTo(Screens.ReleaseDetails(releaseItem.id, releaseItem.code, releaseItem))
    }

    fun onItemClick(item: ReleaseItemState) {
        val releaseItem = findRelease(item.id) ?: return
        router.navigateTo(Screens.ReleaseDetails(releaseItem.id, releaseItem.code, releaseItem))
    }

    fun onYoutubeClick(item: YoutubeItemState) {
        val youtubeItem = findYoutube(item.id) ?: return
        systemUtils.externalLink(youtubeItem.link)
    }

    fun onSchedulesClick() {
        router.navigateTo(Screens.Schedule())
    }

    fun onRandomClick() {
        if (randomJob?.isActive == true) {
            return
        }
        randomJob = viewModelScope.launch {
            coRunCatching {
                releaseInteractor.getRandomRelease()
            }.onSuccess {
                router.navigateTo(Screens.ReleaseDetails(code = it.code))
            }.onFailure {
                errorHandler.handle(it)
            }
        }
    }

    fun onFastSearchOpen() {
    }

    fun appWarningClick(warning: FeedAppWarning) {
        when (warning.tag) {
            appUpdateWarning.tag -> {
                val screen = Screens.AppUpdateScreen(false)
                router.navigateTo(screen)
            }

            appNotificationsWarning.tag -> {
                requestNotificationsPermission()
            }
        }
    }

    fun appWarningCloseClick(warning: FeedAppWarning) {
        when (warning.tag) {
            appUpdateWarning.tag -> {
            }

            appNotificationsWarning.tag -> {
                // do nothing
            }
        }
        warningsController.close(warning.tag)
    }

    fun onDonationClick(state: DonationCardItemState) {
        when (state.tag) {
            DONATION_NEW_TAG -> {
                appPreferences.newDonationRemind.value = false
            }
        }
        router.navigateTo(Screens.DonationDetail())
    }

    fun onDonationCloseClick(state: DonationCardItemState) {
        when (state.tag) {
            DONATION_NEW_TAG -> {
                appPreferences.newDonationRemind.value = false
            }
        }
    }

    fun onCopyClick(item: YoutubeItemState) {
        val releaseItem = findYoutube(item.id) ?: return
        systemUtils.copyToClipBoard(releaseItem.link)
    }

    fun onShareClick(item: YoutubeItemState) {
        val releaseItem = findYoutube(item.id) ?: return
        systemUtils.shareText(releaseItem.link)
    }

    fun onCopyClick(item: ReleaseItemState) {
        val releaseItem = findRelease(item.id) ?: findScheduleRelease(item.id) ?: return
        systemUtils.copyToClipBoard(releaseItem.link.orEmpty())
    }

    fun onShareClick(item: ReleaseItemState) {
        val releaseItem = findRelease(item.id) ?: findScheduleRelease(item.id) ?: return
        systemUtils.shareText(releaseItem.link.orEmpty())
    }

    fun onShortcutClick(item: ReleaseItemState) {
        val releaseItem = findRelease(item.id) ?: findScheduleRelease(item.id) ?: return
        shortcutHelper.addShortcut(releaseItem)
    }

    private fun requestNotificationsPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return
        }
        viewModelScope.launch {
            permissionsDialogFlow.request(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun findScheduleRelease(id: ReleaseId): Release? {
        val scheduleItems = pageLoader.getData()?.schedule?.items
        return scheduleItems?.find { it.releaseItem.id == id }?.releaseItem
    }

    private fun findRelease(id: ReleaseId): Release? {
        val feedItems = pageLoader.getData()?.feedItems ?: return null
        return feedItems
            .mapNotNull { it.release }
            .find { it.id == id }
    }

    private fun findYoutube(id: YoutubeId): YoutubeItem? {
        val feedItems = pageLoader.getData()?.feedItems ?: return null
        return feedItems
            .mapNotNull { it.youtube }
            .find { it.id == id }
    }


    private suspend fun getFeedSource(page: Int): List<FeedItem> = feedRepository.getFeed(page)

    private suspend fun getScheduleSource(): FeedScheduleData = scheduleRepository
        .loadSchedule()
        .let { scheduleDays ->
            val currentTime = System.currentTimeMillis()
            val mskTime = System.currentTimeMillis().asMsk()

            val mskDay = mskTime.getDayOfWeek()

            val asSameDay = Date(currentTime).isSameDay(Date(mskTime))
            val dayTitle = if (asSameDay) {
                "Ожидается сегодня"
            } else {
                val preText = mskDay.asDayPretext()
                val dayName = mskDay.asDayNameDeclension().lowercase(Locale.getDefault())
                "Ожидается $preText $dayName (по МСК)"
            }

            val items = scheduleDays
                .firstOrNull { it.day == mskDay }
                ?.items
                .orEmpty()

            FeedScheduleData(dayTitle, items)
        }


    private suspend fun getDataSource(params: PageLoaderParams<FeedData>): PageLoaderAction.Data<FeedData> {
        return supervisorScope {
            val newPageAsync = async { getFeedSource(params.page) }
            val scheduleAsync = async {
                params.appendData { it?.schedule ?: getScheduleSource() }
            }

            coRunCatching {
                val newPage = newPageAsync.await()
                val schedule = scheduleAsync.await()

                val newFeedItems = params.appendData {
                    it?.feedItems.orEmpty() + newPage
                }

                params.toDataAction(newPage.isNotEmpty()) {
                    FeedData(
                        feedItems = newFeedItems,
                        schedule = schedule
                    )
                }
            }.onFailure {
                if (params.isFirstPage) {
                    errorHandler.handle(it)
                }
            }.getOrThrow()
        }
    }

    private data class FeedData(
        val feedItems: List<FeedItem> = emptyList(),
        val schedule: FeedScheduleData? = null,
    )

    private data class FeedScheduleData(
        val title: String,
        val items: List<ScheduleItem>,
    )

    private fun FeedData.toState(updates: Map<ReleaseId, ReleaseUpdate>): FeedDataState =
        FeedDataState(
            feedItems.map { it.toState(updates) },
            schedule?.toState()
        )

    private fun FeedScheduleData.toState(): FeedScheduleState = FeedScheduleState(
        title = title,
        items = items.map { it.toState() }
    )
}
