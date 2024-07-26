package io.github.adithya2306.graphicaleq.utils

import android.util.Log
import io.github.adithya2306.graphicaleq.BuildConfig

fun dlog(tag: String, msg: String) {
    if (BuildConfig.DEBUG) {
        Log.d(tag, msg)
    }
}