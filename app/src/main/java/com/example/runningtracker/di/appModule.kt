package com.example.runningtracker.di

import com.example.runningtracker.presentation.running.RunningViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module() {

    // 기존 것들 유지

    viewModel {
        RunningViewModel()
    }
}
