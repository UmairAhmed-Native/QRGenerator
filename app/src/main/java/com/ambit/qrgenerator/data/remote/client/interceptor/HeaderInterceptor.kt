package com.ambit.qrgenerator.data.remote.client.interceptor

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class HeaderInterceptor:Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder: Request.Builder = chain.request().newBuilder()
        requestBuilder.header("Accept", "application/json");
        requestBuilder.header("channel_key", "002");

        return chain.proceed(requestBuilder.build());
    }
}