package ru.radiationx.anilibria.ui.fragments.comments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.anilibria.ui.common.webpage.WebPageViewState
import ru.radiationx.anilibria.ui.fragments.release.details.ReleaseExtra
import ru.radiationx.data.interactors.ReleaseInteractor
import ru.radiationx.data.repository.AuthRepository
import ru.radiationx.data.repository.PageRepository
import ru.radiationx.shared.ktx.EventFlow
import ru.radiationx.shared.ktx.coRunCatching
import ru.radiationx.shared_app.controllers.loadersingle.SingleLoader
import timber.log.Timber
import javax.inject.Inject

class VkCommentsViewModel @Inject constructor(
    private val argExtra: ReleaseExtra,
    authRepository: AuthRepository,
    private val pageRepository: PageRepository,
    private val releaseInteractor: ReleaseInteractor,
    private val errorHandler: IErrorHandler,
) : ViewModel() {

    private var hasJsError = false
    private var jsErrorClosed = false

    private var hasVkBlockedError = false
    private var vkBlockedErrorClosed = false

    private val loader = SingleLoader(viewModelScope) {
        getDataSource()
    }

    private val _state = MutableStateFlow(VkCommentsScreenState())
    val state = _state.asStateFlow()

    private val _reloadEvent = EventFlow<Unit>()
    val reloadEvent = _reloadEvent.observe()

    init {
        authRepository
            .observeAuthState()
            .onEach { _reloadEvent.set(Unit) }
            .launchIn(viewModelScope)

        loader
            .observeState()
            .onEach { loadingData ->
                _state.update { it.copy(data = loadingData) }
            }
            .launchIn(viewModelScope)

        viewModelScope.launch {
            coRunCatching {
                pageRepository
                    .checkVkBlocked()
            }.onSuccess {
                hasVkBlockedError = it
                updateVkBlockedState()
            }.onFailure {
                Timber.e(it)
            }
        }

        loader.refresh()
    }


    fun refresh() {
        loader.refresh()
    }

    fun pageReload() {
        _reloadEvent.set(Unit)
    }

    fun onPageLoaded() {
    }

    fun onPageCommitError(error: Exception) {
    }

    fun notifyNewJsError() {
        hasJsError = true
        updateJsErrorState()
    }

    fun closeJsError() {
        jsErrorClosed = true
        updateJsErrorState()
    }

    fun closeVkBlockedError() {
        vkBlockedErrorClosed = true
        updateVkBlockedState()
    }

    fun onNewPageState(pageState: WebPageViewState) {
        _state.update { it.copy(pageState = pageState) }
    }

    private fun updateJsErrorState() {
        _state.update { it.copy(jsErrorVisible = hasJsError && !jsErrorClosed) }
    }

    private fun updateVkBlockedState() {
        _state.update { it.copy(vkBlockedVisible = hasVkBlockedError && !vkBlockedErrorClosed) }
    }

    private suspend fun getDataSource(): VkCommentsState {
        val commentsSource = flow { emit(pageRepository.getComments()) }
        val releaseSource = releaseInteractor.observeFull(argExtra.id, argExtra.code)
        return coRunCatching {
            combine(releaseSource, commentsSource) { release, comments ->
                VkCommentsState(
                    url = "${comments.baseUrl}release/${release.code.code}.html",
                    script = comments.script
                )
            }.first()
        }.onFailure {
            errorHandler.handle(it)
        }.getOrThrow()
    }
}