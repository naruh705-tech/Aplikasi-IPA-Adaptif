package com.app.manfaattumbuhan.presentation.siswa.latihan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.app.manfaattumbuhan.domain.usecase.GetSoalUseCase

class LatihanViewModelFactory(
    private val getSoalUseCase: GetSoalUseCase
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LatihanViewModel::class.java)) {
            return LatihanViewModel(getSoalUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
