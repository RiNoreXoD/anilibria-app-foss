package ru.radiationx.data.entity.mapper

import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.entity.domain.auth.OtpInfo
import ru.radiationx.data.entity.domain.other.ProfileItem
import ru.radiationx.data.entity.response.auth.OtpInfoResponse
import ru.radiationx.data.entity.response.other.ProfileResponse

fun OtpInfoResponse.toDomain(): OtpInfo = OtpInfo(
    code = code,
    description = description,
    expiresAt = expiredAt.secToDate(),
    remainingTime = remainingTime.secToMillis()
)

fun ProfileResponse.toDomain(apiConfig: ApiConfig): ProfileItem = ProfileItem(
    id,
    nick.orEmpty(),
    avatarUrl?.appendBaseUrl(apiConfig.baseImagesUrl)
)