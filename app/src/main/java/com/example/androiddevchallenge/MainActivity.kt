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

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androiddevchallenge.ui.theme.MyTheme
import com.zhuinden.liveevent.observe

class MainActivity : AppCompatActivity() {

    private val mainViewModel by viewModels<MainViewModel>()

    @ExperimentalAnimationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyTheme {
                MyApp(mainViewModel)
            }
        }
        mainViewModel.events.observe(this) { event ->
            showAlertDialog(event)
        }
    }

    private fun showAlertDialog(duration: Int) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Times Up")
        builder.setMessage("Duration: ${duration / 1000} seconds")
        builder.setPositiveButton(android.R.string.ok, null)
        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }
}

// Start building your app here!
@ExperimentalAnimationApi
@Composable
fun MyApp(mainViewModel: MainViewModel) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Countdown Timer")
                }
            )
        }
    ) { innerPadding ->
        BodyContent(Modifier.padding(innerPadding), mainViewModel)
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@ExperimentalAnimationApi
@Composable
fun BodyContent(modifier: Modifier = Modifier, viewModel: MainViewModel) {
    val isTimerRunning = viewModel.timerState == MainViewModel.TimerState.Running
    val isInEditMode = viewModel.timerState == MainViewModel.TimerState.Edit

    val backgroundColor by animateColorAsState(
        if (isTimerRunning) Color(
            red = 197,
            green = 236,
            blue = 216
        ) else MaterialTheme.colors.surface
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(color = backgroundColor)
            .padding(top = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MyCountdownTimer(
            modifier = Modifier
                .padding(16.dp)
                .size(240.dp),
            fontSize = 36.sp,
            isTimerRunning = isTimerRunning,
            viewModel = viewModel
        )

        StartStopButton(
            modifier = Modifier.size(64.dp),
            isTimerRunning = isTimerRunning,
            viewModel = viewModel
        )

        AnimatedVisibility(
            visible = !isTimerRunning,
            enter = slideInHorizontally(
                initialOffsetX = { fullWidth -> -fullWidth },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy
                )
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { -200 },
                animationSpec = tween(durationMillis = 300)
            )
        ) {
            EditCountdownTimerSelections(
                modifier = Modifier.padding(top = 32.dp),
                isInEditMode = isInEditMode,
                viewModel = viewModel
            )
        }
    }
}

@ExperimentalAnimationApi
@Composable
fun EditCountdownTimerSelections(
    modifier: Modifier,
    isInEditMode: Boolean,
    viewModel: MainViewModel
) {
    val editModeButtonIcon = if (isInEditMode) Icons.Filled.Close else Icons.Filled.Edit
    val editButtonBackgroundColor by animateColorAsState(if (isInEditMode) MaterialTheme.colors.primary else MaterialTheme.colors.secondary)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
    ) {
        AnimatedVisibility(
            visible = isInEditMode,
            enter = slideInHorizontally(
                initialOffsetX = { fullWidth -> -fullWidth },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy
                )
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { -1000 },
                animationSpec = tween(durationMillis = 300)
            )
        ) {
            Row(
                modifier = Modifier
                    .padding(start = 64.dp)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                val countdownSelections = listOf(5, 30, 60, 100)
                countdownSelections.forEach {
                    Button(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .weight(1f)
                            .padding(2.dp),
                        shape = CircleShape,
                        onClick = { viewModel.setCountdown(it * 1000) }
                    ) {
                        Text(text = "$it")
                    }
                }
            }
        }
        Button(
            modifier = Modifier
                .padding(4.dp)
                .size(64.dp),
            enabled = viewModel.timerState != MainViewModel.TimerState.Running,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(backgroundColor = editButtonBackgroundColor),
            onClick = {
                if (viewModel.timerState == MainViewModel.TimerState.Edit) {
                    viewModel.exitEditMode()
                } else if (viewModel.timerState == MainViewModel.TimerState.Stopped) {
                    viewModel.enterEditMode()
                }
            }
        ) {
            Icon(
                imageVector = editModeButtonIcon,
                tint = Color.White,
                contentDescription = ""
            )
        }
    }
}

@Composable
fun MyCountdownTimer(
    modifier: Modifier,
    fontSize: TextUnit,
    isTimerRunning: Boolean,
    viewModel: MainViewModel
) {
    val transition = updateTransition(targetState = isTimerRunning)
    val value by transition.animateFloat(
        transitionSpec = {
            if (targetState) {
                tween(durationMillis = viewModel.duration, easing = LinearEasing)
            } else {
                snap()
            }
        }
    ) { state ->
        if (state)
            0f
        else
            1f
    }
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = value,
            strokeWidth = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            color = lerp(Color.Red, MaterialTheme.colors.primary, value),
        )
        val msRemaining = (value * viewModel.duration).toInt()
        val seconds = msRemaining / 1000
        val milliseconds = msRemaining - (seconds * 1000)
        Text(
            "$seconds." + "%03d".format(milliseconds),
//                "$seconds  .  $milliseconds",
//                "$value",
            color = lerp(Color.Red, MaterialTheme.colors.primary, value),
            fontSize = fontSize
        )
    }
}

@Composable
fun StartStopButton(modifier: Modifier, isTimerRunning: Boolean, viewModel: MainViewModel) {

    val buttonIcon = if (isTimerRunning) Icons.Filled.RestartAlt else Icons.Filled.PlayArrow
    val cornerRadius by animateDpAsState(if (isTimerRunning) 2.dp else 32.dp)
    val buttonBackgroundColor by animateColorAsState(if (isTimerRunning) MaterialTheme.colors.error else MaterialTheme.colors.primaryVariant)

    Button(
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        colors = ButtonDefaults.buttonColors(backgroundColor = buttonBackgroundColor),
        enabled = viewModel.timerState != MainViewModel.TimerState.Edit,
        onClick = {
            if (viewModel.timerState == MainViewModel.TimerState.Running) {
                viewModel.pauseTimer()
            } else {
                viewModel.startTimer()
            }
        }
    ) {
        Icon(
            imageVector = buttonIcon,
            tint = Color.White,
            contentDescription = ""
        )
    }
}

@ExperimentalAnimationApi
@Preview("Light Theme", widthDp = 360, heightDp = 640)
@Composable
fun LightPreview() {
    MyTheme {
        MyApp(MainViewModel())
    }
}

@ExperimentalAnimationApi
@Preview("Dark Theme", widthDp = 360, heightDp = 640)
@Composable
fun DarkPreview() {
    MyTheme(darkTheme = true) {
        MyApp(MainViewModel())
    }
}
