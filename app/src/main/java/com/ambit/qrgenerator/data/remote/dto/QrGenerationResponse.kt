package com.ambit.qrgenerator.data.remote.dto


data class QrGenerationResponse(
    val code: String? = null,
    val message: String?=null,
    val content :String?=null
)
