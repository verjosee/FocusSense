package com.focussense.app.domain.timer

import android.os.SystemClock

interface TimeProvider {
    fun elapsedRealtime(): Long
    fun currentTimeMillis(): Long
}

class DefaultTimeProvider : TimeProvider {
    override fun elapsedRealtime(): Long {
        return try {
            SystemClock.elapsedRealtime()
        } catch (_: Throwable) {
            System.nanoTime() / 1_000_000L
        }
    }

    override fun currentTimeMillis(): Long {
        return System.currentTimeMillis()
    }
}
