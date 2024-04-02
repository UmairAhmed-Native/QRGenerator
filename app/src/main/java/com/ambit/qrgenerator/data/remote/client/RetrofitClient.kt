package com.ambit.qrgenerator.data.remote.client

import com.ambit.qrgenerator.data.remote.HttpRoutes
import com.ambit.qrgenerator.data.remote.client.interceptor.HeaderInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.util.concurrent.TimeUnit


object RetrofitClient {

    private fun getOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder().apply {
            connectTimeout(170, TimeUnit.SECONDS)
            readTimeout(170, TimeUnit.SECONDS)
            addInterceptor(HeaderInterceptor())
        }.build()
    }

    private fun getRetrofitClient(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(HttpRoutes.BASE_URL)
            .addConverterFactory(JacksonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }


    fun retrofitClientInstance(): Retrofit = getRetrofitClient(getOkHttpClient())
}