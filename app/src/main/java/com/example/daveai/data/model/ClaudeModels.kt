package com.example.daveai.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MessageRequest(
    @Json(name = "model") val model: String = "claude-opus-4-7",
    @Json(name = "max_tokens") val maxTokens: Int = 4096,
    @Json(name = "messages") val messages: List<ClaudeMessage>,
    @Json(name = "system") val system: String? = null
)

@JsonClass(generateAdapter = true)
data class ClaudeMessage(
    @Json(name = "role") val role: String,
    @Json(name = "content") val content: List<ClaudeContent>
)

@JsonClass(generateAdapter = true)
data class ClaudeContent(
    @Json(name = "type") val type: String,
    @Json(name = "text") val text: String? = null,
    @Json(name = "source") val source: ClaudeContentSource? = null
)

@JsonClass(generateAdapter = true)
data class ClaudeContentSource(
    @Json(name = "type") val type: String = "base64",
    @Json(name = "media_type") val mediaType: String,
    @Json(name = "data") val data: String
)

@JsonClass(generateAdapter = true)
data class MessageResponse(
    @Json(name = "id") val id: String,
    @Json(name = "type") val type: String,
    @Json(name = "role") val role: String,
    @Json(name = "content") val content: List<ContentBlockResponse>,
    @Json(name = "model") val model: String,
    @Json(name = "stop_reason") val stopReason: String?,
    @Json(name = "stop_sequence") val stopSequence: String?,
    @Json(name = "usage") val usage: Usage
)

@JsonClass(generateAdapter = true)
data class ContentBlockResponse(
    @Json(name = "type") val type: String,
    @Json(name = "text") val text: String? = null
)

@JsonClass(generateAdapter = true)
data class Usage(
    @Json(name = "input_tokens") val inputTokens: Int,
    @Json(name = "output_tokens") val outputTokens: Int
)
