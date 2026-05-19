package com.kenjdavidson.golf.handicap.verification

import com.kenjdavidson.golf.handicap.golfcanada.api.MembersApi
import com.kenjdavidson.golf.handicap.golfcanada.invoker.ApiClient
import com.kenjdavidson.golf.handicap.security.CurrentAuthenticatedUserProvider
import org.springframework.stereotype.Component

@Component
class GolfCanadaMembersApiFactory(
    private val baseApiClient: ApiClient,
    private val currentAuthenticatedUserProvider: CurrentAuthenticatedUserProvider
) {
    private val membersApi: MembersApi by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        val apiClient = ApiClient().setBasePath(baseApiClient.basePath)
        apiClient.setBearerToken { currentAuthenticatedUserProvider.requireAccessToken() }
        MembersApi(apiClient)
    }

    fun create(): MembersApi = membersApi
}
