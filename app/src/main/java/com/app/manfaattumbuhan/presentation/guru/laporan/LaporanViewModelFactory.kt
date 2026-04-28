package com.app.manfaattumbuhan.presentation.guru.laporan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.app.manfaattumbuhan.domain.usecase.GetSoalUseCase

class LaporanViewModelFactory(
    private val getSoalUseCase: GetSoalUseCase
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LaporanViewModel::class.java)) {
            return LaporanViewModel(getSoalUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
