package com.app.manfaattumbuhan.presentation.guru.akun

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.app.manfaattumbuhan.domain.usecase.GetSiswaUseCase

class KelolaAkunViewModelFactory(
    private val getSiswaUseCase: GetSiswaUseCase
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(KelolaAkunViewModel::class.java)) {
            return KelolaAkunViewModel(getSiswaUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
