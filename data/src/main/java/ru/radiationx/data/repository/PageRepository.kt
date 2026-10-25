package ru.radiationx.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.radiationx.data.datasource.remote.api.PageApi
import ru.radiationx.data.entity.domain.page.PageLibria
import javax.inject.Inject

/**
 * Created by radiationx on 13.01.18.
 */
class PageRepository @Inject constructor(
    private val pageApi: PageApi
) {

    suspend fun getPage(pagePath: String): PageLibria = withContext(Dispatchers.IO) {
        pageApi
            .getPage(pagePath)
    }
}
