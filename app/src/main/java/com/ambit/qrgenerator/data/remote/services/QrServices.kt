package com.ambit.qrgenerator.data.remote.services

import com.ambit.qrgenerator.data.remote.HttpRoutes
import com.ambit.qrgenerator.data.remote.dto.QrGenerationRequest
import com.ambit.qrgenerator.data.remote.dto.QrGenerationResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface QrServices {
    @POST(HttpRoutes.GENERATE_QR)
    fun generateQr(@Body qrGenerationRequest: QrGenerationRequest): Call<QrGenerationResponse>


}