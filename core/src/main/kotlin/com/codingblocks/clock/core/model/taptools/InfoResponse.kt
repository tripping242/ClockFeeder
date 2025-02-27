package com.codingblocks.clock.core.model.taptools

import kotlinx.serialization.Serializable

@Serializable
data class InfoResponse (
    val logo: String,
    val name: String,
)
