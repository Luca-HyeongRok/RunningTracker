package com.example.runningtracker.presentation.running

sealed class RunningEvent {
    data class ShowMessage(val message: String) : RunningEvent()
}
