package ru.radiationx.anilibria.ui.fragments.donation.jointeam

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.mapNotNull
import ru.radiationx.data.entity.domain.donation.DonationContentButton
import ru.radiationx.data.repository.DonationRepository
import ru.radiationx.quill.QuillExtra
import ru.radiationx.shared_app.common.SystemUtils
import javax.inject.Inject

data class DonationDialogExtra(
    val tag: String,
) : QuillExtra

class DonationDialogViewModel @Inject constructor(
    private val argExtra: DonationDialogExtra,
    donationRepository: DonationRepository,
    private val systemUtils: SystemUtils,
) : ViewModel() {

    val state = donationRepository
        .observerDonationInfo()
        .mapNotNull { data ->
            data.contentDialogs.find { it.tag == argExtra.tag }
        }

    fun onLinkClick(url: String) {
        systemUtils.externalLink(url)
    }

    fun onButtonClick(button: DonationContentButton) {
        button.link?.also {
            systemUtils.externalLink(it)
        }
    }
}