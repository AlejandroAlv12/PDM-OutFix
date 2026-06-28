package com.pdm0126.outfix.data.api

import retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import com.pdm0126.outfix.data.prefs.SessionManager
import okhttp3.Interceptor
import okhttp3.Response

object RetrofitClient {

    // La IP se carga desde local.properties a través de BuildConfig para mayor seguridad.
    // IMPORTANTE: Tu celular y tu Mac deben estar conectados a la misma red Wi-Fi.
    private const val BASE_URL = com.pdm0126.outfix.BuildConfig.BASE_URL

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    var sessionManager: SessionManager? = null

    private val authInterceptor = Interceptor { chain ->
        val requestBuilder = chain.request().newBuilder()
        sessionManager?.fetchAuthToken()?.let { token ->
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }
        chain.proceed(requestBuilder.build())
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(authInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    val garmentApi: GarmentApi by lazy {
        retrofit.create(GarmentApi::class.java)
    }

    val authApi: AuthApi by lazy {
        retrofit.create(AuthApi::class.java)
    }

    val lentApi: LentApi by lazy {
        retrofit.create(LentApi::class.java)
    }
}
