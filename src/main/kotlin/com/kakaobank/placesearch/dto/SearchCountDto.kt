package com.kakaobank.placesearch.dto

import com.kakaobank.placesearch.domain.SearchCount

data class SearchCountDto(val keyword: String, val count: Long) {
    companion object {
        fun from(searchCount: SearchCount) = SearchCountDto(searchCount.keyword, searchCount.count)
    }
}


