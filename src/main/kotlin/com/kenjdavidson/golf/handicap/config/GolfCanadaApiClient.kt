package com.kenjdavidson.golf.handicap.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.kenjdavidson.golf.handicap.golfcanada.invoker.ApiClient
import com.kenjdavidson.golf.handicap.golfcanada.model.ProfileClub
import com.kenjdavidson.golf.handicap.golfcanada.model.ProfileClubDeserializer
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.client.RestTemplate

class GolfCanadaApiClient : ApiClient() {
    override fun buildRestTemplate(): RestTemplate {
        val restTemplate = super.buildRestTemplate()
        val objectMapper = profileObjectMapper()

        restTemplate.messageConverters.removeIf {
            it.javaClass == JacksonJsonHttpMessageConverter::class.java
        }
        restTemplate.messageConverters.add(0, MappingJackson2HttpMessageConverter(objectMapper))
        return restTemplate
    }

    internal fun profileObjectMapper(): ObjectMapper = JsonMapper.builder()
        .findAndAddModules()
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .addModule(
            SimpleModule().addDeserializer(
                ProfileClub::class.java,
                ProfileClubDeserializer()
            )
        )
        .build()
}
