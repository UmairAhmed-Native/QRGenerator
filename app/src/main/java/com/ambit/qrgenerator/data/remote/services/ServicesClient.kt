package com.ambit.qrgenerator.data.remote.services

import com.ambit.qrgenerator.data.remote.client.RetrofitClient

object ServicesClient {
    val apiService: QrServices by lazy {
        RetrofitClient.retrofitClientInstance().create(QrServices::class.java)
    }
}