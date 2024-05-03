package com.hazrat.mymessage.data

open class Event<out T>(
    val content: T
) {
    var hasBeenHnadled = false

    fun getContentOrNull(): T? {
        return if (hasBeenHnadled) null
        else {
            hasBeenHnadled = true
            content
        }
    }
}