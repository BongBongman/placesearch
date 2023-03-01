package com.kakaobank.placesearch.configuration

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisOperations
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer


@Configuration
class RedisConfiguration(
    @Value("\${spring.redis.host}") val redisHost: String,
    @Value("\${spring.redis.port}") val redisPort: Int
) {

    @Bean
    fun reactiveRedisConnectionFactory(): ReactiveRedisConnectionFactory {
        return LettuceConnectionFactory(redisHost, redisPort)
    }

    @Bean
    fun redisOperations(reactiveRedisConnectionFactory: ReactiveRedisConnectionFactory): ReactiveRedisOperations<String, Any> {
        val serializer = Jackson2JsonRedisSerializer(Any::class.java)
        val context =
            RedisSerializationContext.newSerializationContext<String, Any>(StringRedisSerializer())
                .value(serializer)
                .hashValue(serializer)
                .hashKey(serializer)
                .build()
        return ReactiveRedisTemplate(reactiveRedisConnectionFactory, context)
    }
}