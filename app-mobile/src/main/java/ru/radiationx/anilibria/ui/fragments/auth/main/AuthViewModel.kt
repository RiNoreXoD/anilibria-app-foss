package ru.radiationx.anilibria.ui.fragments.auth.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.terrakok.cicerone.Router
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.model.SocialAuthItemState
import ru.radiationx.anilibria.model.toState
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.anilibria.utils.messages.SystemMessenger
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.entity.common.AuthState
import ru.radiationx.data.entity.domain.auth.EmptyFieldException
import ru.radiationx.data.repository.AuthRepository
import ru.radiationx.shared.ktx.EventFlow
import ru.radiationx.shared.ktx.coRunCatching
import ru.radiationx.shared_app.common.SystemUtils
import javax.inject.Inject

class AuthViewModel @Inject constructor(
    private val router: Router,
    private val systemMessenger: SystemMessenger,
    private val authRepository: AuthRepository,
    private val errorHandler: IErrorHandler,
    private val apiConfig: ApiConfig,
    private val systemUtils: SystemUtils,
) : ViewModel() {

    private val inputDataState = MutableStateFlow(AuthInputData())

    private val _state = MutableStateFlow(AuthScreenState())
    val state = _state.asStateFlow()

    private val _registrationEvent = EventFlow<Unit>()
    val registrationEvent = _registrationEvent.observe()

    init {
        inputDataState
            .map { it.login.isNotEmpty() && it.password.isNotEmpty() }
            .onEach { enabled ->
                _state.update { it.copy(actionEnabled = enabled) }
            }
            .launchIn(viewModelScope)

        authRepository
            .observeSocialAuth()
            .onEach { socialAuth ->
                val items = socialAuth.filter { !it.key.equals("vk", ignoreCase = true) }.map { it.toState() }
                _state.update { it.copy(socialItems = items) }
            }
            .launchIn(viewModelScope)

        viewModelScope.launch {
            coRunCatching {
                authRepository.loadSocialAuth()
            }.onFailure {
                errorHandler.handle(it)
            }
        }
    }

    fun onSocialClick(item: SocialAuthItemState) {
        router.navigateTo(Screens.AuthSocial(item.key))
    }

    fun setLogin(login: String) {
        inputDataState.update { it.copy(login = login) }
    }

    fun setPassword(password: String) {
        inputDataState.update { it.copy(password = password) }
    }

    fun signIn() {
        viewModelScope.launch {
            _state.update { it.copy(sending = true) }
            val inputData = inputDataState.value
            coRunCatching {
                authRepository.signIn(inputData.login, inputData.password, "")
                authRepository.getAuthState()
            }.onSuccess {
                decideWhatToDo(it)
            }.onFailure {
                if (isEmpty2FaCode(inputData, it)) {
                    router.navigateTo(Screens.Auth2FaCode(inputData.login, inputData.password))
                } else {
                    errorHandler.handle(it)
                }
            }
            _state.update { it.copy(sending = false) }
        }
    }

    private fun isEmpty2FaCode(inputData: AuthInputData, error: Throwable): Boolean {
        return inputData.login.isNotEmpty()
                && inputData.password.isNotEmpty()
                && error is EmptyFieldException
    }

    private fun decideWhatToDo(state: AuthState) {
        if (state == AuthState.AUTH) {
            router.finishChain()
        } else {
            systemMessenger.showMessage("Что-то пошло не так")
        }
    }

    fun onBackPressed() {
        viewModelScope.launch {
            val state = authRepository.getAuthState()
            if (state != AuthState.AUTH) {
                skip()
            } else {
                router.finishChain()
            }
        }
    }

    fun skip() {
        viewModelScope.launch {
            authRepository.setAuthSkipped(true)
            router.finishChain()
        }
    }

    fun registrationClick() {
        _registrationEvent.set(Unit)
    }

    fun registrationToSiteClick() {
        systemUtils.externalLink("${apiConfig.siteUrl}/pages/login.php")
    }

}

data class AuthInputData(
    val login: String = "",
    val password: String = "",
)

data class AuthScreenState(
    val actionEnabled: Boolean = false,
    val sending: Boolean = false,
    val socialItems: List<SocialAuthItemState> = emptyList(),
)
