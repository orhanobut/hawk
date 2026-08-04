package com.orhanobut.hawk

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

@RunWith(RobolectricTestRunner::class)
class HawkConcurrencyTest {

  @Before fun setup() {
    Hawk.init(RuntimeEnvironment.application).build()
    Hawk.deleteAll()
  }

  @After fun tearDown() {
    if (Hawk.isBuilt()) {
      Hawk.deleteAll()
    }
    Hawk.destroy()
  }

  @Test fun concurrentPutGetDeleteDoNotThrow() {
    val threads = 8
    val operations = 30
    val executor = Executors.newFixedThreadPool(threads)
    val latch = CountDownLatch(threads)
    val errors = AtomicInteger()

    for (t in 0 until threads) {
      executor.execute {
        try {
          for (i in 0 until operations) {
            val key = "shared-$i"
            if (t % 2 == 0) {
              Hawk.put(key, "writer-$t")
            } else {
              Hawk.get<String>(key)
            }
            if (i % 7 == 0) {
              Hawk.delete("temp-$i")
            }
            Hawk.contains(key)
            Hawk.count()
          }
        } catch (e: Exception) {
          errors.incrementAndGet()
        } finally {
          latch.countDown()
        }
      }
    }

    assertThat(latch.await(60, TimeUnit.SECONDS)).isTrue()
    executor.shutdown()
    assertThat(errors.get()).isEqualTo(0)
  }

  @Test fun concurrentWritersAllPersist() {
    val threads = 8
    val perThread = 20
    val executor = Executors.newFixedThreadPool(threads)
    val latch = CountDownLatch(threads)

    for (t in 0 until threads) {
      executor.execute {
        try {
          for (i in 0 until perThread) {
            Hawk.put("key-$t-$i", "value-$t-$i")
          }
        } finally {
          latch.countDown()
        }
      }
    }

    assertThat(latch.await(60, TimeUnit.SECONDS)).isTrue()
    executor.shutdown()

    assertThat(Hawk.count()).isEqualTo((threads * perThread).toLong())
    assertThat(Hawk.keys()).hasSize(threads * perThread)
    for (t in 0 until threads) {
      for (i in 0 until perThread) {
        assertThat(Hawk.get<String>("key-$t-$i")).isEqualTo("value-$t-$i")
      }
    }
  }
}
