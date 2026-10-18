package ru.radiationx.anilibria.common

import com.github.terrakok.cicerone.Router
import ru.radiationx.anilibria.screen.DetailsScreen
import javax.inject.Inject

class LibriaCardRouter @Inject constructor(
    private val router: Router,
) {

    fun navigate(libriaCard: LibriaCard) {
        when (val type = libriaCard.type) {
            is LibriaCard.Type.Release -> {
                router.navigateTo(DetailsScreen(type.releaseId))
            }
        }
    }
}