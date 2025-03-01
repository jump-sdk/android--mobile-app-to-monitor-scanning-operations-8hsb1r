package com.jump.scanmonitor.service.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Implementation of the DatadogApiService interface that handles API communication with Datadog.
 *
 * This class configures the Retrofit client with authentication headers and network parameters
 * required for secure and reliable communication with the Datadog API.
 *
 * @property apiKey The Datadog API key used for authentication
 * @property applicationKey The Datadog application key used for authentication
 */
class DatadogApiServiceImpl(
    private val apiKey: String,
    private val applicationKey: String
) {
    
    /**
     * Lazily initialized Retrofit instance configured for Datadog API communication.
     */
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.datadoghq.com/")
            .addConverterFactory(MoshiConverterFactory.create()) // Version: 2.9.0
            .client(createOkHttpClient())
            .build()
    }
    
    /**
     * Creates and configures an OkHttpClient with authentication headers and timeouts.
     *
     * @return Configured HTTP client for API requests
     */
    private fun createOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                    .header("DD-API-KEY", apiKey)
                    .header("DD-APPLICATION-KEY", applicationKey)
                    .method(original.method, original.body)
                
                chain.proceed(requestBuilder.build())
            }
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }
    
    /**
     * Creates an implementation of the DatadogApiService interface.
     *
     * @return Implementation of the service interface for making API calls
     */
    fun create(): DatadogApiService {
        return retrofit.create(DatadogApiService::class.java)
    }
}