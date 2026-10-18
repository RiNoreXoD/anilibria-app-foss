package ru.radiationx.anilibria.model

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import ru.radiationx.anilibria.R
import ru.radiationx.data.entity.domain.other.DataColor
import ru.radiationx.data.entity.domain.other.DataIcons

@DrawableRes
fun String.asDataIconRes(): Int? = when (this) {
    DataIcons.BOOSTY -> R.drawable.ic_logo_boosty
    DataIcons.RUSTORE -> R.drawable.ic_logo_rustore
    DataIcons.ANILIBRIA -> R.drawable.ic_logo_alib
    DataIcons.YOUTUBE -> R.drawable.ic_logo_youtube
    DataIcons.TELEGRAM -> R.drawable.ic_logo_telegram
    DataIcons.INFO -> R.drawable.ic_information
    DataIcons.RULES -> R.drawable.ic_book_open_variant
    DataIcons.PERSON -> R.drawable.ic_person
    DataIcons.SITE -> R.drawable.ic_link
    DataIcons.INFRA -> R.drawable.ic_server_plus
    else -> null
}

@ColorRes
fun String.asDataColorRes(): Int? = when (this) {
    DataColor.BOOSTY -> R.color.brand_boosty
    DataColor.RUSTORE -> R.color.brand_rustore
    DataColor.ANILIBRIA -> R.color.alib_red
    DataColor.YOUTUBE -> R.color.brand_youtube
    DataColor.TELEGRAM -> R.color.brand_telegram
    else -> null
}