package com.example.daveai.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WikiSearchResponse(
    val query: WikiSearchQuery?
)

@JsonClass(generateAdapter = true)
data class WikiSearchQuery(
    val search: List<WikiSearchItem>?
)

@JsonClass(generateAdapter = true)
data class WikiSearchItem(
    val title: String,
    val pageid: Long,
    val snippet: String?
)

@JsonClass(generateAdapter = true)
data class WikiExtractResponse(
    val query: WikiExtractQuery?
)

@JsonClass(generateAdapter = true)
data class WikiExtractQuery(
    val pages: Map<String, WikiPage>?
)

@JsonClass(generateAdapter = true)
data class WikiPage(
    val pageid: Long,
    val title: String,
    val extract: String?
)
