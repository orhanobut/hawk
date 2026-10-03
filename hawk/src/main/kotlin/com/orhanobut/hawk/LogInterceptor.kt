package com.orhanobut.hawk


/** Receives diagnostics, which may contain keys and values. Avoid logging secrets. */
fun interface LogInterceptor { fun onLog(message: String) }
