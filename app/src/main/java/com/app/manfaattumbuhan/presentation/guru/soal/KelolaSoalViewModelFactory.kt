package com.app.manfaattumbuhan.presentation.guru.soal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.app.manfaattumbuhan.domain.usecase.GetSoalUseCase

class KelolaSoalViewModelFactory(
    private val getSoalUseCase: GetSoalUseCase
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(KelolaSoalViewModel::class.java)) {
            return KelolaSoalViewModel(getSoalUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
