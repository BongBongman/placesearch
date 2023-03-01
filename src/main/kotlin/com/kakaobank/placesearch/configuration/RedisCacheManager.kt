package com.kakaobank.placesearch.configuration

import org.springframework.cache.CacheManager
import org.springframework.stereotype.Component
import reactor.cache.CacheFlux
import reactor.cache.CacheMono
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Signal
import java.util.function.Supplier


@Component
class ReactiveCacheManager(private val cacheManager: CacheManager) {
    fun <T> findCachedMono(
        cacheName: String,
        key: String,
        retriever: Supplier<Mono<T>>,
        classType: Class<T>
    ): Mono<T> {
        val cache = cacheManager.getCache(cacheName)
        return CacheMono
            .lookup({ key ->
                val value = cache?.get(key, classType)
                Mono.justOrEmpty(value).map { t -> Signal.next(t) }
            }, key)
            .onCacheMissResume(retriever)
            .andWriteWith { key, signal ->
                Mono.fromRunnable {
                    if (!signal.isOnError) {
                        cache?.put(key, signal.get())
                    }
                }
            }
    }

    fun <T> findCachedFlux(cacheName: String, key: String, retriever: Supplier<Flux<T>>): Flux<T> {
        val cache = cacheManager.getCache(cacheName)
        return CacheFlux
            .lookup({ key ->
                val value = cache?.get(key, List::class.java)?.map { it as T }
                Mono.justOrEmpty(value)
                    .flatMap { list -> Flux.fromIterable(list).materialize().collectList() }
            }, key)
            .onCacheMissResume(retriever)
            .andWriteWith { key: Any, signalList: List<Signal<T>> ->
                Flux.fromIterable(signalList)
                    .dematerialize<Any>()
                    .collectList()
                    .doOnNext { list -> cache?.put(key, list) }
                    .then()
            }
    }
}