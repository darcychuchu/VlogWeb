package org.vlog.web.config

import org.apache.hc.client5.http.classic.HttpClient
import org.apache.hc.client5.http.config.RequestConfig
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager
import org.apache.hc.core5.util.Timeout
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.client.RestTemplate


@Configuration
class RestTemplateConfig {
//    @Bean
//    fun httpClient(): CloseableHttpClient {
//        return HttpClients.createDefault()
//    }

    @Bean
    fun httpClient(): CloseableHttpClient {
        return HttpClientBuilder.create()
            .setDefaultRequestConfig(
                RequestConfig.custom()
                    .setConnectionRequestTimeout(Timeout.ofSeconds(3))
                    .setResponseTimeout(Timeout.ofSeconds(60))
                    .build()
            )
            .setConnectionManager(
                PoolingHttpClientConnectionManager().apply {
                    maxTotal = 100
                    defaultMaxPerRoute = 20
                }
            )
            .build()
    }


    @Bean
    fun restTemplate(httpClient: HttpClient): RestTemplate {
        val requestFactory = HttpComponentsClientHttpRequestFactory(httpClient)
        requestFactory.setConnectTimeout(10000)
        requestFactory.setReadTimeout(5000)
        //return RestTemplate(HttpComponentsClientHttpRequestFactory(httpClient))

        return RestTemplate(requestFactory).apply {
            val converter = MappingJackson2HttpMessageConverter()
            converter.supportedMediaTypes = listOf(
                MediaType.APPLICATION_JSON,
                MediaType.APPLICATION_OCTET_STREAM,
                MediaType.TEXT_PLAIN,
                MediaType.TEXT_HTML,
                MediaType.parseMediaType("application/json;charset=UTF-8")
            )
            messageConverters.add(converter)
        }
    }
}
