package com.kenjdavidson.golf.handicap.verification

import com.kenjdavidson.golf.handicap.golfcanada.api.MembersApi
import com.kenjdavidson.golf.handicap.golfcanada.invoker.ApiClient
import org.springframework.stereotype.Component

@Component
class GolfCanadaMembersApiFactory(
    private val baseApiClient: ApiClient
) {
    fun create(accessToken: String): MembersApi {
        val apiClient = ApiClient().setBasePath(baseApiClient.basePath)
        apiClient.setBearerToken(accessToken)
        return MembersApi(apiClient)
    }
}
