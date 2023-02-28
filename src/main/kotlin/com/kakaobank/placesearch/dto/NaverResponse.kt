package com.kakaobank.placesearch.dto

import com.fasterxml.jackson.annotation.JsonProperty

class NaverResponse(val items: List<NaverPlace>)

class NaverPlace(@JsonProperty("title") val placeName: String)