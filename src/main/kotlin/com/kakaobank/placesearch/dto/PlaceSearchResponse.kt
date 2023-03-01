package com.kakaobank.placesearch.dto

data class PlaceSearchResponse(val places: List<Place>)
data class Place(val title: String)