/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.androiddevchallenge

import android.os.CountDownTimer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.zhuinden.eventemitter.EventEmitter
import com.zhuinden.eventemitter.EventSource

class MainViewModel() : ViewModel() {

    private var timer: CountDownTimer

    init {
        timer = object : CountDownTimer(5000, 1) {
            override fun onFinish() = onTimerFinished(false)

            override fun onTick(millisUntilFinished: Long) {
                msRemaining = millisUntilFinished.toInt()
            }
        }
    }

    enum class TimerState {
        Edit, Stopped, Running
    }
    var timerState: TimerState by mutableStateOf(TimerState.Stopped)

    var duration by mutableStateOf(10000)
    var msRemaining by mutableStateOf(10000)

    private val emitter: EventEmitter<Int> = EventEmitter()
    val events: EventSource<Int> = emitter

    fun setCountdown(countdown: Int = 10000) {
        stopTimer()
        duration = countdown
        msRemaining = countdown
        timerState = TimerState.Stopped
    }

    private fun onTimerFinished(stoppedManually: Boolean) {
        timerState = TimerState.Stopped
        msRemaining = 0
        if (!stoppedManually) emitter.emit(duration)
    }

    fun startTimer() {
        if (msRemaining <= 0) {
            msRemaining = duration
        }
        timerState = TimerState.Running

        timer = object : CountDownTimer(msRemaining.toLong(), 1000) {
            override fun onFinish() = onTimerFinished(false)

            override fun onTick(millisUntilFinished: Long) {
//                msRemaining = millisUntilFinished.toInt()
            }
        }.start()
    }

    fun stopTimer() {
        timer.cancel()
        onTimerFinished(true)
    }

    fun pauseTimer() {
        timer.cancel()
        timerState = TimerState.Stopped
    }

    fun enterEditMode() {
        timerState = TimerState.Edit
    }

    fun exitEditMode() {
        timerState = TimerState.Stopped
    }
}
