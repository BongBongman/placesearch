package com.kakaobank.placesearch.dto

data class KeywordResponse(val keywords: List<Keyword>)

data class Keyword(val keyword: String, val count: Long) {
    companion object {
        fun from(searchCountDto: SearchCountDto) = Keyword(searchCountDto.keyword, searchCountDto.count)
    }
}
