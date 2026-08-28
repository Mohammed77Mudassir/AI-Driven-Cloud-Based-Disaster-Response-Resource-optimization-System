package com.disaster.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Registers every external-API configuration class and exposes a shared,
 * time-bounded {@link RestTemplate} for outgoing third-party HTTP calls.
 *
 * <p>All credentials are injected via {@code @ConfigurationProperties}
 * classes (bound from environment variables) - no API key is hardcoded or
 * scattered through service classes. Adding a new provider is a purely
 * additive change: add a property in application.properties, optionally a
 * nested holder here, and a new service bean.</p>
 */
@Configuration
@EnableConfigurationProperties({
        ApiProperties.class,
        WeatherProperties.class,
        AIProperties.class,
        NotificationProperties.class,
        EarthquakeProperties.class,
        EonetProperties.class,
        EmailProperties.class,
        FireProperties.class,
        AirQualityProperties.class
})
public class ApiConfig {

    @Bean
    public RestTemplate externalApiRestTemplate(RestTemplateBuilder builder) {
        RestTemplate template = builder
                .setConnectTimeout(Duration.ofSeconds(8))
                .setReadTimeout(Duration.ofSeconds(15))
                .build();

        // NASA EONET v3 serves its JSON body with a misleading
        // `application/rss+xml` Content-Type (it keeps doing so even when the
        // client sends `Accept: application/json`). Without this, the Jackson
        // converter rejects the response and the EONET feed can never be
        // parsed from the real NASA API. Teaching the converter to accept the
        // media type is safe: it only widens what the shared feed template can
        // deserialize (both USGS and EONET payloads are JSON).
        template.getMessageConverters().stream()
                .filter(MappingJackson2HttpMessageConverter.class::isInstance)
                .map(MappingJackson2HttpMessageConverter.class::cast)
                .forEach(ApiConfig::acceptRssJsonContentType);

        return template;
    }

    private static void acceptRssJsonContentType(MappingJackson2HttpMessageConverter converter) {
        List<MediaType> mediaTypes = new ArrayList<>(converter.getSupportedMediaTypes());
        mediaTypes.add(MediaType.parseMediaType("application/rss+xml"));
        converter.setSupportedMediaTypes(mediaTypes);
    }

    /**
     * Dedicated time-bounded {@link RestTemplate} for email delivery so the
     * email timeouts can be tuned independently of the shared feed template.
     */
    @Bean
    public RestTemplate emailRestTemplate(RestTemplateBuilder builder, EmailProperties emailProperties) {
        long timeout = emailProperties.getResend().getTimeoutMs();
        return builder
                .setConnectTimeout(Duration.ofMillis(timeout))
                .setReadTimeout(Duration.ofMillis(timeout))
                .build();
    }
}
