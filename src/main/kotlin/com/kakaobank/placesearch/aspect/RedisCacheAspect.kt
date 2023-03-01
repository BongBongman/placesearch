package com.kakaobank.placesearch.aspect

import com.kakaobank.placesearch.configuration.ReactiveCacheManager
import com.kakaobank.placesearch.log
import io.lettuce.core.dynamic.support.ResolvableType
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.lang.reflect.ParameterizedType
import java.util.function.Supplier


@Aspect
@Component
class ReactorCacheAspect(reactiveCacheManager: ReactiveCacheManager) {
    private val reactiveCacheManager: ReactiveCacheManager

    init {
        this.reactiveCacheManager = reactiveCacheManager
    }

    @Pointcut("@annotation(reactor.cache.ReactorCacheable)")
    fun pointcut() {
    }

    @Around("pointcut()")
    fun around(joinPoint: ProceedingJoinPoint): Any {
        val signature = joinPoint.signature as MethodSignature
        val method = signature.method
        val parameterizedType = method.genericReturnType as ParameterizedType
        val rawType = parameterizedType.rawType

        if (rawType != Mono::class.java && rawType != Flux::class.java) {
            throw IllegalArgumentException("The return type is not Mono/Flux. Use Mono/Flux for return type. method:${method.getName()}")
        }

        val reactorCacheable = method.getAnnotation(RedisCacheable::class.java)
        val cacheName: String = reactorCacheable.name
        val args = joinPoint.args
        val retriever = Supplier {

            joinPoint.proceed(args)

        }

        // 리턴타입이 Mono면
        return if (rawType.equals(Mono::class.java)) {
            val returnTypeInsideMono = parameterizedType.actualTypeArguments[0]
            val returnClass: Class<*> = ResolvableType.forType(returnTypeInsideMono).resolve()
            reactiveCacheManager
                .findCachedMono(cacheName, generateKey(*args), retriever, returnClass)
//                .doOnError { e -> log().error("Failed to processing mono cache. method: " + method.getName(), e) }
        } else {
            reactiveCacheManager
                .findCachedFlux(cacheName, generateKey(*args), retriever)
//                .doOnError { e -> log().error("Failed to processing flux cache. method: " + method.getName(), e) }
        }
    }

    private fun generateKey(vararg objects: Any): String {
        return objects.map { obj -> obj.toString() }.joinToString(separator = ":")
    }
}