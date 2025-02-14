package com.codingblocks.clock.core.remote

import com.codingblocks.clock.core.model.clock.StatusResponse
import retrofit2.Response
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ClockApi {

    @GET("status")
    suspend fun status(): Response<StatusResponse>

    @GET("action/update")
    suspend fun resumeClock(): ResponseBody

    @GET("action/pause")
    suspend fun pauseClock(): ResponseBody

    @GET("lights/flash")
    suspend fun flashStandard(): ResponseBody

    @GET("lights/{color}")
    suspend fun lightColor(@Path("color") color: String): ResponseBody

    @GET("show/text//BAR")
    suspend fun setTextFoo(): ResponseBody

    @GET("ou_text/0/Nik/Hen")
    suspend fun setOverUnder0Text(): ResponseBody
    @GET("ou_text/1/las/ri")
    suspend fun setOverUnder1Text(): ResponseBody
    @GET("ou_text/2//")
    suspend fun setOverUnder2Text(): ResponseBody
    @GET("ou_text/3/8 J/5 J")
    suspend fun setOverUnder3Text(): ResponseBody
    @GET("ou_text/4/ahr/ahr")
    suspend fun setOverUnder4Text(): ResponseBody
    @GET("ou_text/5/e/e")
    suspend fun setOverUnder5Text(): ResponseBody


    @GET("show/text/{text}")
    suspend fun setText(@Path("text", encoded = true) text: String): ResponseBody

    @GET("show/number/{number}")
    suspend fun setNumber(@Path("number") number: String): ResponseBody

    @GET("show/text/{text}")
    suspend fun setTextTlBrPairSym(
        @Path("text", encoded=true) text: String,
        @Query("tl") tl: String?,
        @Query("br") br: String?,
        @Query("pair") pair: String,
        @Query("omit_line") omitLine: Int?,
    ): ResponseBody

    @GET("show/text/{text}")
    suspend fun setNumberTlBrPairSym(
        @Path("text") text: String,
        @Query("tl") tl: String?,
        @Query("br") br: String?,
        @Query("pair") pair: String,
        @Query("omit_line") omitLine: Int?,
        ): ResponseBody




}