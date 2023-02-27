package com.kakaobank.placesearch.api.dto

import com.fasterxml.jackson.annotation.JsonProperty

class NaverResult(val items: List<NaverPlace>)

class NaverPlace(@JsonProperty("title") val placeName: String)