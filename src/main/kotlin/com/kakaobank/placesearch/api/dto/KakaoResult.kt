package com.kakaobank.placesearch.api.dto

import com.fasterxml.jackson.annotation.JsonProperty

class KakaoResult(val documents: List<KakaoPlace>)

class KakaoPlace(@JsonProperty("place_name") val placeName: String)