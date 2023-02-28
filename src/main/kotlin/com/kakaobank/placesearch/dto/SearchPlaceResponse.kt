package com.kakaobank.placesearch.dto

data class SearchPlaceResponse(val places: List<Place>)
data class Place(val title: String)