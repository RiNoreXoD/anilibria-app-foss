package ru.radiationx.data.entity.domain.feed

import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.entity.domain.types.FeedId

data class FeedItem(
    val id: FeedId,
    val release: Release?
)
