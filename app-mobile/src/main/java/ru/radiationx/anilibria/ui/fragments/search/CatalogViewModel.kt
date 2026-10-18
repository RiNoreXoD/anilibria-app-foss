package ru.radiationx.anilibria.ui.fragments.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.terrakok.cicerone.Router
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.model.ReleaseItemState
import ru.radiationx.anilibria.model.toState
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.anilibria.utils.ShortcutHelper
import ru.radiationx.data.datasource.holders.PreferencesHolder
import ru.radiationx.data.datasource.holders.ReleaseUpdateHolder
import ru.radiationx.data.entity.domain.release.GenreItem
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.entity.domain.release.SeasonItem
import ru.radiationx.data.entity.domain.release.YearItem
import ru.radiationx.data.entity.domain.search.SearchForm
import ru.radiationx.data.entity.domain.types.ReleaseId
import ru.radiationx.data.repository.SearchRepository
import ru.radiationx.quill.QuillExtra
import ru.radiationx.shared.ktx.EventFlow
import ru.radiationx.shared.ktx.coRunCatching
import ru.radiationx.shared_app.common.SystemUtils
import ru.radiationx.shared_app.controllers.loaderpage.PageLoader
import ru.radiationx.shared_app.controllers.loaderpage.PageLoaderAction
import ru.radiationx.shared_app.controllers.loaderpage.PageLoaderParams
import ru.radiationx.shared_app.controllers.loaderpage.mapData
import ru.radiationx.shared_app.controllers.loaderpage.toDataAction
import javax.inject.Inject

data class CatalogExtra(
    val genre: String?,
) : QuillExtra

class CatalogViewModel @Inject constructor(
    argExtra: CatalogExtra,
    private val searchRepository: SearchRepository,
    private val router: Router,
    private val errorHandler: IErrorHandler,
    private val releaseUpdateHolder: ReleaseUpdateHolder,
    private val appPreferences: PreferencesHolder,
    private val shortcutHelper: ShortcutHelper,
    private val systemUtils: SystemUtils,
) : ViewModel() {


    private val remindText =
        "Если не удаётся найти нужный релиз, попробуйте искать через Google или Yandex c приставкой \"AniLibria\".\nПо ссылке в поисковике можно будет открыть приложение."

    private val pageLoader = PageLoader(viewModelScope) {
        getDataSource(it)
    }


    private val _filterState = MutableStateFlow(
        CatalogFilterState(
            form = SearchForm(
                genres = argExtra.genre?.let { setOf(GenreItem(it, it)) }.orEmpty(),
            )
        )
    )
    val filterState = _filterState.asStateFlow()

    private val _state = MutableStateFlow(SearchScreenState())
    val state = _state.asStateFlow()

    val showFilterAction = EventFlow<CatalogFilterState>()

    init {
        initGenres()
        initYears()
        initSeasons()
        observeSearchRemind()
        observeLoadingState()
        pageLoader.refresh()
    }

    private fun initGenres() {
        viewModelScope.launch {
            coRunCatching {
                searchRepository.getGenres()
            }.onFailure {
                errorHandler.handle(it)
            }
        }
        searchRepository
            .observeGenres()
            .onEach { genres ->
                _filterState.update { it.copy(genres = genres) }
            }
            .launchIn(viewModelScope)
    }

    private fun initYears() {
        viewModelScope.launch {
            coRunCatching {
                searchRepository.getYears()
            }.onFailure {
                errorHandler.handle(it)
            }
        }
        searchRepository
            .observeYears()
            .onEach { years ->
                _filterState.update { it.copy(years = years) }
            }
            .launchIn(viewModelScope)
    }

    private fun initSeasons() {
        viewModelScope.launch {
            coRunCatching {
                searchRepository.getSeasons()
            }.onSuccess { seasons ->
                _filterState.update { it.copy(seasons = seasons) }
            }.onFailure {
                errorHandler.handle(it)
            }
        }
    }

    private fun observeSearchRemind() {
        appPreferences
            .searchRemind
            .onEach { remindEnabled ->
                val newRemindText = remindText.takeIf { remindEnabled }
                _state.update {
                    it.copy(remindText = newRemindText)
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeLoadingState() {
        combine(
            pageLoader.observeState(),
            releaseUpdateHolder.observeEpisodes()
        ) { loadingState, updates ->
            val updatesMap = updates.associateBy { it.id }
            loadingState.mapData { items ->
                items.map { it.toState(updatesMap) }
            }
        }
            .onEach { loadingState ->
                _state.update {
                    it.copy(data = loadingState)
                }
            }
            .launchIn(viewModelScope)
    }


    private suspend fun getDataSource(params: PageLoaderParams<List<Release>>): PageLoaderAction.Data<List<Release>> {
        return coRunCatching {
            val form = filterState.value.form
            val result = searchRepository.searchReleases(form, params.page)
            params.toDataAction(result.data.isNotEmpty()) {
                it.orEmpty() + result.data
            }
        }.onFailure {
            if (params.isFirstPage) {
                errorHandler.handle(it)
            }
        }.getOrThrow()
    }

    fun refreshReleases() {
        pageLoader.refresh()
    }

    fun loadMore() {
        pageLoader.loadMore()
    }

    fun showDialog() {

        showFilterAction.set(filterState.value)
    }

    fun onAcceptDialog(state: CatalogFilterState) {
        if (filterState.value != state) {
            _filterState.value = state
            refreshReleases()
        }
        _filterState.value = state
    }

    fun onCloseDialog() {
    }

    fun onFastSearchClick() {
    }

    fun onFastSearchOpen() {
    }

    fun onRemindClose() {
        appPreferences.searchRemind.value = false
    }

    fun onItemClick(item: ReleaseItemState) {
        val releaseItem = findRelease(item.id) ?: return
        router.navigateTo(Screens.ReleaseDetails(releaseItem.id, releaseItem.code, releaseItem))
    }

    fun onCopyClick(item: ReleaseItemState) {
        val releaseItem = findRelease(item.id) ?: return
        systemUtils.copyToClipBoard(releaseItem.link.orEmpty())
    }

    fun onShareClick(item: ReleaseItemState) {
        val releaseItem = findRelease(item.id) ?: return
        systemUtils.shareText(releaseItem.link.orEmpty())
    }

    fun onShortcutClick(item: ReleaseItemState) {
        val releaseItem = findRelease(item.id) ?: return
        shortcutHelper.addShortcut(releaseItem)
    }

    private fun findRelease(id: ReleaseId): Release? {
        return pageLoader.getData()?.find { it.id == id }
    }
}

data class CatalogFilterState(
    val genres: List<GenreItem> = emptyList(),
    val years: List<YearItem> = emptyList(),
    val seasons: List<SeasonItem> = emptyList(),
    val form: SearchForm = SearchForm(),
)