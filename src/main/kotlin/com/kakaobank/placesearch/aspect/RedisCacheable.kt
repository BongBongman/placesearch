package com.kakaobank.placesearch.aspect

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RedisCacheable(val name: String)