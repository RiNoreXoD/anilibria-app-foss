package ru.radiationx.anilibria.ui.activities.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.entity.common.AuthState
import ru.radiationx.data.repository.AuthRepository
import ru.radiationx.data.repository.ConfigurationRepository
import ru.radiationx.data.repository.DonationRepository
import ru.radiationx.shared.ktx.EventFlow
import ru.radiationx.shared.ktx.coRunCatching
import com.github.terrakok.cicerone.Router
import timber.log.Timber
import javax.inject.Inject


data class MainScreenState(
    val selectedTab: String? = null,
    val needConfig: Boolean = false,
    val mainLogicCompleted: Boolean = false,
)

class MainViewModel @Inject constructor(
    private val router: Router,
    private val authRepository: AuthRepository,
    private val donationRepository: DonationRepository,
    private val configurationRepository: ConfigurationRepository,
    private val apiConfig: ApiConfig,
) : ViewModel() {

    private val defaultScreen = Screens.MainFeed().screenKey

    private var firstLaunch = true

    private val _state = MutableStateFlow(MainScreenState())
    val state = _state.asStateFlow()

    val updateTabsAction = EventFlow<Unit>()

    fun init(savedScreen: String?) {
        apiConfig
            .observeNeedConfig()
            .distinctUntilChanged()
            .onEach { needConfig ->
                _state.update { it.copy(needConfig = needConfig) }
                if (!needConfig && firstLaunch) {
                    initMain(savedScreen)
                }
            }
            .launchIn(viewModelScope)

        if (apiConfig.needConfig) {
            _state.update { it.copy(needConfig = true) }
        } else {
            initMain(savedScreen)
        }
    }

    private fun initMain(savedScreen: String?) {
        firstLaunch = false

        // todo TR-274 move in scope after refactor screen
        runBlocking {
            if (authRepository.getAuthState() == AuthState.NO_AUTH) {
                router.navigateTo(Screens.Auth())
            }
        }


        selectTab(savedScreen ?: defaultScreen)
        authRepository
            .observeAuthState()
            .onEach { updateTabsAction.set(Unit) }
            .launchIn(viewModelScope)
        _state.update { it.copy(mainLogicCompleted = true) }

        viewModelScope.launch {
            coRunCatching {
                authRepository.loadUser()
            }.onFailure {
                Timber.e(it)
            }
        }
        viewModelScope.launch {
            coRunCatching {
                donationRepository.requestUpdate()
            }.onFailure {
                Timber.e(it)
            }
        }
    }

    fun onBackPressed() {
        router.exit()
    }

    // todo TR-274 refactor screen with no-getter viewmodel
    fun getAuthState() = runBlocking { authRepository.getAuthState() }

    fun selectTab(screenKey: String) {
        _state.update { it.copy(selectedTab = screenKey) }
    }

}
