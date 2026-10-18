package ru.radiationx.data.entity.response.feed

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import ru.radiationx.data.entity.response.release.ReleaseResponse

@JsonClass(generateAdapter = true)
data class FeedResponse(
    @Json(name = "release") val release: ReleaseResponse? = null
)