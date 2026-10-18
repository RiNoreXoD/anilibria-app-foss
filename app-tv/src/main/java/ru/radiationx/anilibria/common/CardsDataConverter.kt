package ru.radiationx.anilibria.common

import android.content.Context
import ru.radiationx.data.entity.domain.feed.FeedItem
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.shared.ktx.android.relativeDate
import ru.radiationx.shared.ktx.capitalizeDefault
import ru.radiationx.shared.ktx.decapitalizeDefault
import java.util.Date

class CardsDataConverter(
    private val context: Context,
) {

    fun toCard(releaseItem: Release) = releaseItem.run {
        val torrentDate = torrentUpdate.takeIf { it != 0 }?.let { Date(it * 1000L) }
        val seasonText = "${year.orEmpty()} ${season.orEmpty()}"
        val genreText = genres.firstOrNull()?.capitalizeDefault()
        val seriesText = "Серии: ${series?.trim() ?: "Не доступно"}"
        val updateText = torrentDate?.let {
            "Обновлен ${it.relativeDate(context).decapitalizeDefault()}"
        }
        val descItems = listOfNotNull(seasonText, genreText, seriesText, updateText)
        LibriaCard(
            title.orEmpty(),
            descItems.joinToString(" • "),
            poster.orEmpty(),
            LibriaCard.Type.Release(releaseItem.id)
        )
    }

    fun toCard(feedItem: FeedItem): LibriaCard = feedItem.run {
        release?.let(::toCard) ?: throw RuntimeException("WataFuq")
    }
}