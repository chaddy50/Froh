package com.chaddy50.froh.data.api.deezer

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object DeezerClient {
    private const val BASE_URL = "https://api.deezer.com/"

    val service: DeezerService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DeezerService::class.java)
    }
}
