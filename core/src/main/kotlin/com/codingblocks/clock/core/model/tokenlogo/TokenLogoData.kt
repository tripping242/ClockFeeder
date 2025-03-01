package com.codingblocks.clock.core.model.tokenlogo

import kotlinx.serialization.Serializable

@Serializable
data class TokenLogoResponse(
    val logo: Logo
)

@Serializable
data class Logo(
    val value: String
)