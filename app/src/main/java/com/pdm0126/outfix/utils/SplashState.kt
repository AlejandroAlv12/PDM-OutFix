package com.pdm0126.outfix.utils

/**
 * Tracks whether the splash screen has already been shown in this app process session.
 * This guarantees the splash is only shown on a true cold start.
 */
object SplashState {
    var isFinished = false
}
