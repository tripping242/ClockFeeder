package com.codingblocks.clock.core.model.clock

import kotlinx.serialization.Serializable

@Serializable
data class StatusResponse(
    val tags: List<String>,
    val showing: String,
    val version: String,
    val rendered: Rendered,
    val menu_active: Boolean
)

@Serializable
data class Rendered(
    val number: Double?,
    val pair: List<String>?,
    val tl_text: String?,
    val omit_line: Int?,
    val label: String?,
    val contents: List<String>?,
    val string: String?,
    val br_text: String?,
    val is_error: Boolean?,
    val tag: String?,
)