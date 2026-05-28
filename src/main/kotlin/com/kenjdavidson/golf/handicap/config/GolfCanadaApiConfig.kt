package com.kenjdavidson.golf.handicap.config

import com.kenjdavidson.golf.handicap.golfcanada.api.AuthenticationApi
import com.kenjdavidson.golf.handicap.golfcanada.api.MembersApi
import com.kenjdavidson.golf.handicap.golfcanada.invoker.ApiClient
import com.kenjdavidson.golf.handicap.security.CurrentAuthenticatedUserProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GolfCanadaApiConfig(
    @Value($$"${app.golf-canada.base-url:https://scg.golfcanada.ca}") private val golfCanadaBaseUrl: String
) {
    @Bean
    fun golfCanadaApiClient(): ApiClient = GolfCanadaApiClient().setBasePath(golfCanadaBaseUrl.removeSuffix("/"))

    @Bean
    fun authenticationApi(apiClient: ApiClient): AuthenticationApi = AuthenticationApi(apiClient)

    @Bean
    fun membersApi(
        apiClient: ApiClient,
        currentAuthenticatedUserProvider: CurrentAuthenticatedUserProvider
    ): MembersApi {
        val authenticatedApiClient = GolfCanadaApiClient().setBasePath(apiClient.basePath)
        authenticatedApiClient.setBearerToken { currentAuthenticatedUserProvider.requireAccessToken() }
        return MembersApi(authenticatedApiClient)
    }
}
