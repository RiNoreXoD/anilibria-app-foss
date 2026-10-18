package ru.radiationx.anilibria.screen.mainpages

import androidx.fragment.app.Fragment
import androidx.leanback.widget.Row
import ru.radiationx.anilibria.common.CachedRowsFragmentFactory
import ru.radiationx.anilibria.screen.main.MainFragment
import ru.radiationx.anilibria.screen.profile.ProfileFragment
import ru.radiationx.anilibria.screen.watching.WatchingFragment

class MainPagesFragmentFactory : CachedRowsFragmentFactory() {

    companion object {
        const val ID_MAIN = 1L
        const val ID_MY = 2L
        const val ID_SERIES = 3L
        const val ID_MOVIES = 4L
        const val ID_SEARCH = 5L
        const val ID_PROFILE = 7L

        val ids = listOf(
            ID_MAIN,
            ID_MY,
            //ID_SERIES,
            //ID_MOVIES,
            //ID_SEARCH,
            ID_PROFILE
        )

        val variant1 = mapOf(
            ID_MAIN to "Главная",
            ID_MY to "Я смотрю",
            ID_SERIES to "Сериалы",
            ID_MOVIES to "Фильмы",
            ID_SEARCH to "Поиск",
            ID_PROFILE to "Профиль"
        )
    }

    override fun getFragmentByRow(row: Row): Fragment {
        val fragment = when (row.id) {
            ID_MAIN -> MainFragment()
            ID_MY -> WatchingFragment()
            ID_PROFILE -> ProfileFragment()
            else -> super.getFragmentByRow(row)
        }
        return fragment
    }
}