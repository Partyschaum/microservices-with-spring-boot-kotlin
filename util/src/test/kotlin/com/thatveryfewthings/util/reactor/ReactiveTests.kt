package com.thatveryfewthings.util.reactor

import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import reactor.test.StepVerifier

class ReactiveTests {

    @Test
    fun testFluxBlocking() {
        val list = listOf(1, 2, 3, 4).toFlux()
            .filter { it % 2 == 0 }
            .map { it * 2 }
            .log()
            .collectList()
            .block()

        assertIterableEquals(list, listOf(4, 8))
    }

    @Test
    fun testFluxSubscribe() {
        val mutableList = mutableListOf<Int>()

        listOf(1, 2, 3, 4).toFlux()
            .filter { it % 2 == 0 }
            .map { it * 2 }
            .log()
            .subscribe { mutableList.add(it) }

        assertIterableEquals(mutableList, listOf(4, 8))
    }

    @Test
    fun testFluxVerifyComplete() {
        StepVerifier.create(listOf(1, 2, 3, 4).toFlux())
            .expectNext(1)
            .expectNext(2)
            .expectNext(3)
            .expectNext(4)
            .verifyComplete()
    }

    @Test
    fun testFluxExpectNextCount() {
        StepVerifier.create(listOf(1, 2, 3, 4).toFlux())
            .expectNextCount(4)
            .verifyComplete()

        StepVerifier.create(emptyList<Int>().toFlux())
            .expectNextCount(0)
            .verifyComplete()

        StepVerifier.create(Mono.empty<Int>())
            .expectNextCount(0)
            .verifyComplete()
    }
}
