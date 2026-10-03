package com.orhanobut.benchmark

import android.app.Activity
import android.os.Bundle
import android.os.SystemClock
import com.orhanobut.hawk.Hawk

/** A small synchronous timing sample; results are not a formal benchmark. */
class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        time("init") { Hawk.init(this).build() }
        time("put") { Hawk.put("key", "value") }
        time("get") { Hawk.get<String>("key") }
        time("contains") { Hawk.contains("key") }
        time("count") { Hawk.count() }
        time("delete") { Hawk.delete("key") }
    }
    private inline fun time(operation: String, block: () -> Unit) {
        val start = SystemClock.elapsedRealtimeNanos()
        block()
        println("Hawk.$operation: ${(SystemClock.elapsedRealtimeNanos() - start) / 1_000_000.0}ms")
    }
}
