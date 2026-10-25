package ru.radiationx.anilibria.ui.fragments.feed

import android.view.View
import ru.radiationx.anilibria.model.ReleaseItemState
import ru.radiationx.anilibria.model.ScheduleItemState
import ru.radiationx.anilibria.ui.adapters.AppInfoCardListItem
import ru.radiationx.anilibria.ui.adapters.AppWarningCardListItem
import ru.radiationx.anilibria.ui.adapters.DividerShadowListItem
import ru.radiationx.anilibria.ui.adapters.FeedListItem
import ru.radiationx.anilibria.ui.adapters.FeedRandomBtnListItem
import ru.radiationx.anilibria.ui.adapters.FeedSchedulesListItem
import ru.radiationx.anilibria.ui.adapters.FeedSectionListItem
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.LoadErrorListItem
import ru.radiationx.anilibria.ui.adapters.LoadMoreListItem
import ru.radiationx.anilibria.ui.adapters.PlaceholderDelegate
import ru.radiationx.anilibria.ui.adapters.PlaceholderListItem
import ru.radiationx.anilibria.ui.adapters.ShadowDirection
import ru.radiationx.anilibria.ui.adapters.feed.AppInfoCardDelegate
import ru.radiationx.anilibria.ui.adapters.feed.AppWarningCardDelegate
import ru.radiationx.anilibria.ui.adapters.feed.FeedRandomBtnDelegate
import ru.radiationx.anilibria.ui.adapters.feed.FeedReleaseDelegate
import ru.radiationx.anilibria.ui.adapters.feed.FeedSchedulesDelegate
import ru.radiationx.anilibria.ui.adapters.feed.FeedSectionDelegate
import ru.radiationx.anilibria.ui.adapters.global.LoadErrorDelegate
import ru.radiationx.anilibria.ui.adapters.global.LoadMoreDelegate
import ru.radiationx.anilibria.ui.adapters.other.DividerShadowItemDelegate
import ru.radiationx.anilibria.ui.common.adapters.ListItemAdapter
import ru.radiationx.shared_app.controllers.loaderpage.needShowPlaceholder

/* Created by radiationx on 31.10.17. */

class FeedAdapter(
    loadMoreListener: () -> Unit,
    loadRetryListener: () -> Unit,
    warningClickListener: (FeedAppWarning) -> Unit,
    warningClickCloseListener: (FeedAppWarning) -> Unit,
    schedulesClickListener: () -> Unit,
    scheduleScrollListener: (Int) -> Unit,
    randomClickListener: () -> Unit,
    releaseClickListener: (ReleaseItemState, View) -> Unit,
    releaseLongClickListener: (ReleaseItemState) -> Unit,
    scheduleClickListener: (ScheduleItemState, View, Int) -> Unit,
    private val emptyPlaceHolder: PlaceholderListItem,
    private val errorPlaceHolder: PlaceholderListItem,
) : ListItemAdapter() {

    companion object {
        private const val TAG_SCHEDULE_SECTION = "schedule"
        private const val TAG_FEED_SECTION = "feed"
    }

    private val sectionClickListener = { item: FeedSectionListItem ->
        if (item.tag == TAG_SCHEDULE_SECTION) {
            schedulesClickListener.invoke()
        }
    }

    init {
        addDelegate(AppInfoCardDelegate(warningClickListener, warningClickCloseListener))
        addDelegate(AppWarningCardDelegate(warningClickListener, warningClickCloseListener))
        addDelegate(LoadMoreDelegate(loadMoreListener))
        addDelegate(LoadErrorDelegate(loadRetryListener))
        addDelegate(FeedSectionDelegate(sectionClickListener))
        addDelegate(
            FeedSchedulesDelegate(
                scheduleClickListener,
                { releaseLongClickListener.invoke(it.release) },
                scheduleScrollListener
            )
        )
        addDelegate(FeedReleaseDelegate(releaseClickListener, releaseLongClickListener))
        addDelegate(FeedRandomBtnDelegate(randomClickListener))
        addDelegate(DividerShadowItemDelegate())
        addDelegate(PlaceholderDelegate())
    }

    fun bindState(state: FeedScreenState) {
        val loadingState = state.data
        val newItems = mutableListOf<ListItem>()

        if (loadingState.data != null || loadingState.error != null) {
            val warningItems = state.warnings.map { warning ->
                when (warning.type) {
                    FeedAppWarningType.INFO -> AppInfoCardListItem(warning)
                    FeedAppWarningType.WARNING -> AppWarningCardListItem(warning)
                }
            }
            newItems.addAll(warningItems)
        }

        getPlaceholder(state)?.also {
            newItems.add(it)
        }

        loadingState.data?.schedule?.also { scheduleState ->
            newItems.add(
                FeedSectionListItem(
                    TAG_SCHEDULE_SECTION,
                    scheduleState.title,
                    "Расписание",
                    null
                )
            )
            newItems.add(FeedSchedulesListItem("actual", scheduleState.items))
        }

        val feedItems = loadingState.data?.feedItems.orEmpty()
        if (feedItems.isNotEmpty()) {
            newItems.add(
                FeedSectionListItem(
                    TAG_FEED_SECTION,
                    "Обновления",
                    null,
                    null,
                    hasBg = true
                )
            )
            newItems.add(FeedRandomBtnListItem("random"))

            feedItems.filter { it.release != null }.forEach { data ->
                newItems.add(FeedListItem(data))
            }
        }

        if (loadingState.hasMoreData) {
            if (loadingState.error != null) {
                newItems.add(LoadErrorListItem("bottom"))
            } else {
                newItems.add(LoadMoreListItem("bottom", !loadingState.moreLoading))
            }
        }

        items = newItems
    }

    private fun getPlaceholder(state: FeedScreenState): PlaceholderListItem? {
        val loadingState = state.data
        val needPlaceholder = loadingState.needShowPlaceholder { data ->
            data?.let { it.feedItems.isNotEmpty() || it.schedule != null } ?: false
        }

        return when {
            needPlaceholder && loadingState.error != null -> errorPlaceHolder
            needPlaceholder && loadingState.error == null -> emptyPlaceHolder
            else -> null
        }
    }
}
