package com.example.ritmofit.di

import com.example.ritmofit.network.RetrofitClient
import com.example.ritmofit.ui.classes.ClassViewModel

object ViewModelProvider {
    fun provideClassViewModel(): ClassViewModel {
        return ClassViewModel(RetrofitClient.classService)
    }
}
