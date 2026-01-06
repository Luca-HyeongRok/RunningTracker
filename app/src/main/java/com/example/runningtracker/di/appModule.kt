package com.example.runningtracker.di

import com.example.runningtracker.data.local.RunningDatabase
import com.example.runningtracker.data.repository.RunningRepositoryImpl
import com.example.runningtracker.domain.repository.RunningRepository
import com.example.runningtracker.presentation.history.HistoryViewModel
import com.example.runningtracker.presentation.running.RunningViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { RunningDatabase.getDatabase(androidContext()) }
    single { get<RunningDatabase>().runningDao() }
    single<RunningRepository> { RunningRepositoryImpl(get()) }

    viewModel { RunningViewModel() }
    viewModel { HistoryViewModel(get()) }
}
