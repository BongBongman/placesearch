package com.kakaobank.placesearch.dto

import com.fasterxml.jackson.annotation.JsonProperty

class KakaoResponse(val documents: List<KakaoPlace>)

class KakaoPlace(@JsonProperty("place_name") val placeName: String)