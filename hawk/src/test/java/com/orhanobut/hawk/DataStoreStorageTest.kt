package com.orhanobut.hawk

import com.google.common.truth.Truth.assertThat
import junit.framework.Assert.fail
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
class DataStoreStorageTest {

  private lateinit var storage: DataStoreStorage

  @Before fun setup() {
    storage = DataStoreStorage(RuntimeEnvironment.application)
    storage.deleteAll()
  }

  @Test fun putAndGet() {
    assertThat(storage.put("key", "value")).isTrue()
    assertThat(storage.get<String>("key")).isEqualTo("value")
  }

  @Test fun putReplacesExistingValue() {
    storage.put("key", "first")
    storage.put("key", "second")

    assertThat(storage.get<String>("key")).isEqualTo("second")
    assertThat(storage.count()).isEqualTo(1)
  }

  @Test fun throwExceptionOnNullKeyOnPut() {
    try {
      (storage as Storage).put(null, "value")
      fail("key should not be null")
    } catch (e: Exception) {
      assertThat(e).hasMessageThat().isEqualTo("key should not be null")
    }
  }

  @Test fun getMissingKeyReturnsNull() {
    assertThat(storage.get<String>("missing")).isNull()
  }

  @Test fun emptyStringValue() {
    storage.put("key", "")
    assertThat(storage.get<String>("key")).isEqualTo("")
  }

  @Test fun unicodeAndSpecialCharacters() {
    val value = "汉字 🦅 \"quotes\" \\ backslash \n newline \u0000 tab\t end"
    storage.put("key", value)
    assertThat(storage.get<String>("key")).isEqualTo(value)
  }

  @Test fun largeString() {
    val value = buildString { repeat(200_000) { append('a') } }
    storage.put("key", value)
    assertThat(storage.get<String>("key")).isEqualTo(value)
  }

  @Test fun delete() {
    storage.put("key", "value")

    assertThat(storage.delete("key")).isTrue()
    assertThat(storage.contains("key")).isFalse()
    assertThat(storage.count()).isEqualTo(0)
  }

  @Test fun deleteMissingKeySucceeds() {
    assertThat(storage.delete("missing")).isTrue()
  }

  @Test fun deleteAll() {
    storage.put("a", "1")
    storage.put("b", "2")

    assertThat(storage.deleteAll()).isTrue()
    assertThat(storage.count()).isEqualTo(0)
  }

  @Test fun contains() {
    storage.put("key", "value")

    assertThat(storage.contains("key")).isTrue()
    assertThat(storage.contains("other")).isFalse()
  }

  @Test fun count() {
    storage.put("a", "1")
    storage.put("b", "2")
    storage.put("c", "3")

    assertThat(storage.count()).isEqualTo(3)
  }

  @Test fun keys() {
    storage.put("a", "1")
    storage.put("b", "2")

    assertThat(storage.keys()).containsExactly("a", "b")
  }

  @Test fun keysAfterDeleteAllIsEmpty() {
    storage.put("a", "1")
    storage.deleteAll()

    assertThat(storage.keys()).isEmpty()
  }

  @Test fun concurrentOperationsDoNotThrow() {
    val threads = 8
    val operationsPerThread = 50
    val executor = Executors.newFixedThreadPool(threads)
    val latch = CountDownLatch(threads)
    val errors = AtomicInteger()

    for (t in 0 until threads) {
      executor.execute {
        try {
          for (i in 0 until operationsPerThread) {
            val key = "thread-$t-$i"
            storage.put(key, "value-$i")
            storage.get<String>(key)
            if (i % 5 == 0) {
              storage.delete(key)
            }
            storage.contains(key)
            storage.count()
            storage.keys()
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

  @Test fun concurrentWritesAreNotLost() {
    val threads = 8
    val perThread = 25
    val executor = Executors.newFixedThreadPool(threads)
    val latch = CountDownLatch(threads)

    for (t in 0 until threads) {
      executor.execute {
        try {
          for (i in 0 until perThread) {
            storage.put("k-$t-$i", "v-$t-$i")
          }
        } finally {
          latch.countDown()
        }
      }
    }

    assertThat(latch.await(60, TimeUnit.SECONDS)).isTrue()
    executor.shutdown()

    assertThat(storage.count()).isEqualTo((threads * perThread).toLong())
    for (t in 0 until threads) {
      for (i in 0 until perThread) {
        assertThat(storage.get<String>("k-$t-$i")).isEqualTo("v-$t-$i")
      }
    }
  }
}
