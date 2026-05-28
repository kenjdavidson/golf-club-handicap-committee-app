package com.kenjdavidson.golf.handicap.config

import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.ValueDeserializer
import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.module.SimpleModule
import com.kenjdavidson.golf.handicap.golfcanada.invoker.ApiClient
import com.kenjdavidson.golf.handicap.golfcanada.model.ProfileClub
import com.kenjdavidson.golf.handicap.golfcanada.model.ProfileClubDeserializer
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter
import org.springframework.web.client.RestTemplate

class GolfCanadaApiClient : ApiClient() {
    override fun buildRestTemplate(): RestTemplate {
        val restTemplate = super.buildRestTemplate()
        val jsonMapper = profileObjectMapper()

        // 1. Evict any default message converters to avoid serialization conflicts
        restTemplate.messageConverters.removeIf {
            it is JacksonJsonHttpMessageConverter
        }

        // 2. Wrap your specific Jackson 3 JsonMapper directly into the constructor
        val modernV3Converter = JacksonJsonHttpMessageConverter(jsonMapper)

        restTemplate.messageConverters.add(0, modernV3Converter)
        return restTemplate
    }

    // Fix: Explicitly return the concrete JsonMapper type rather than the broad ObjectMapper interface
    internal fun profileObjectMapper(): JsonMapper {
        val profileModule = SimpleModule().apply {
            // Apply variant casting to satisfy Kotlin's invariant generics constraints
            addDeserializer(
                ProfileClub::class.java as Class<Any>,
                ProfileClubDeserializer() as ValueDeserializer<Any>
            )
        }

        return JsonMapper.builder()
            .findAndAddModules()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .addModule(profileModule)
            .build()
    }
}