package com.kakaobank.placesearch.configuration

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.CacheManager
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

const val CACHE_SEARCH = "search"
const val CACHE_KEYWORDS = "search"
const val CACHE_EXPIRE_TIME_SECONDS = 60L

@Configuration
class CacheConfiguration {
    @Bean
    fun cacheManager(): CacheManager {
        return CaffeineCacheManager().apply {
            setCacheNames(listOf(CACHE_SEARCH, CACHE_KEYWORDS))
            setCaffeine(Caffeine.newBuilder().expireAfterWrite(CACHE_EXPIRE_TIME_SECONDS, TimeUnit.SECONDS))
        }
    }
}