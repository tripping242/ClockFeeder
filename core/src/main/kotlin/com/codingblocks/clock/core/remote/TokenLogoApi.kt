package com.codingblocks.clock.core.remote

import com.codingblocks.clock.core.model.tokenlogo.TokenLogoResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface TokenLogoApi {
    @GET("metadata/{unit}")
    suspend fun getTokenLogo(
        @Path("unit") unit: String
    ): Response<TokenLogoResponse>
}