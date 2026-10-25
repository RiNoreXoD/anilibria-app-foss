package ru.radiationx.data.datasource.remote.api

import ru.radiationx.data.ApiClient
import ru.radiationx.data.datasource.remote.IClient
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.datasource.remote.parsers.PagesParser
import ru.radiationx.data.entity.domain.page.PageLibria
import javax.inject.Inject

/**
 * Created by radiationx on 13.01.18.
 */
class PageApi @Inject constructor(
    @ApiClient private val client: IClient,
    private val pagesParser: PagesParser,
    private val apiConfig: ApiConfig
) {

    suspend fun getPage(pagePath: String): PageLibria {
        val args: Map<String, String> = emptyMap()
        return client
            .get("${apiConfig.baseUrl}/$pagePath", args)
            .let { pagesParser.baseParse(it) }
    }
}