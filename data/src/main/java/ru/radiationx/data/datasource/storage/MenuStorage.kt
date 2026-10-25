package ru.radiationx.data.datasource.storage

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import ru.radiationx.data.datasource.holders.MenuHolder
import ru.radiationx.data.entity.domain.other.DataIcons
import ru.radiationx.data.entity.domain.other.LinkMenuItem
import javax.inject.Inject

class MenuStorage @Inject constructor() : MenuHolder {

    private val fixedMenu = listOf(
        LinkMenuItem(
            "Сайт AniLibria",
            absoluteLink = "https://www.anilibria.tv/",
            icon = DataIcons.ANILIBRIA
        ),
        LinkMenuItem(
            "Канал Telegram",
            absoluteLink = "https://t.me/anilibria_tv",
            icon = DataIcons.TELEGRAM
        ),
        LinkMenuItem(
            "Канал YouTube",
            absoluteLink = "https://youtube.com/channel/UCuF8ghQWaa7K-28llm-K3Zg",
            icon = DataIcons.YOUTUBE
        )
    )

    override fun observe(): Flow<List<LinkMenuItem>> = flowOf(fixedMenu)

    override suspend fun save(items: List<LinkMenuItem>) {
        // Menu is fixed to site, telegram, and youtube
    }

    override suspend fun get(): List<LinkMenuItem> = fixedMenu
}
