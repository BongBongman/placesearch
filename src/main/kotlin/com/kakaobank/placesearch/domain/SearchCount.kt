package com.kakaobank.placesearch.domain

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("search_count")
data class SearchCount(
    @Id val id: Long? = null,
    val keyword: String,
    var count: Long = 0
) {
    constructor(keyword: String, count: Long) : this(null, keyword, count)
}
