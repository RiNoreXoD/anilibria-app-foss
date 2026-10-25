package ru.radiationx.anilibria.ui.fragments.feed

import ru.radiationx.anilibria.model.FeedItemState
import ru.radiationx.anilibria.model.ScheduleItemState
import ru.radiationx.shared_app.controllers.loaderpage.PageLoaderState


data class FeedScreenState(
    val data: PageLoaderState<FeedDataState> = PageLoaderState.empty(),
    val warnings: List<FeedAppWarning> = emptyList(),
)

data class FeedDataState(
    val feedItems: List<FeedItemState> = emptyList(),
    val schedule: FeedScheduleState? = null
)

data class FeedScheduleState(
    val title: String,
    val items: List<ScheduleItemState>
)

data class FeedAppWarning(
    val tag: String,
    val title: String,
    val type: FeedAppWarningType
)

enum class FeedAppWarningType {
    INFO,
    WARNING
}