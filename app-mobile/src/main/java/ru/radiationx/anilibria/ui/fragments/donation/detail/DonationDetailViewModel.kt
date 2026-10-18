package ru.radiationx.anilibria.ui.fragments.donation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.terrakok.cicerone.Router
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.radiationx.data.entity.domain.donation.DonationContentButton
import ru.radiationx.data.entity.domain.donation.DonationInfo
import ru.radiationx.data.repository.DonationRepository
import ru.radiationx.shared.ktx.EventFlow
import ru.radiationx.shared.ktx.coRunCatching
import ru.radiationx.shared_app.common.SystemUtils
import timber.log.Timber
import javax.inject.Inject

class DonationDetailViewModel @Inject constructor(
    private val router: Router,
    private val donationRepository: DonationRepository,
    private val systemUtils: SystemUtils
) : ViewModel() {

    private var currentData: DonationInfo? = null

    val state = donationRepository.observerDonationInfo()

    private val _yoomoneyEvent = EventFlow<Unit>()
    val yoomoneyEvent = _yoomoneyEvent.observe()

    private val _dialogEvent = EventFlow<String>()
    val dialogEvent = _dialogEvent.observe()

    init {
        state.onEach {
            currentData = it
        }.launchIn(viewModelScope)

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

    fun onLinkClick(url: String) {
        systemUtils.externalLink(url)
    }

    fun onButtonClick(button: DonationContentButton) {
        val info = currentData ?: return
        val buttonTag = button.tag
        val buttonLink = button.link

        val dialog = buttonTag?.let { tag -> info.contentDialogs.find { it.tag == tag } }
        val yoomoneyDialog = buttonTag
            ?.takeIf { it == DonationInfo.YOOMONEY_TAG }
            ?.let { info.yooMoneyDialog }

        when {
            yoomoneyDialog != null -> {
                _yoomoneyEvent.set(Unit)
            }

            dialog != null -> {
                _dialogEvent.set(dialog.tag)
            }

            buttonLink != null -> {
                systemUtils.externalLink(buttonLink)
            }
        }
    }

}