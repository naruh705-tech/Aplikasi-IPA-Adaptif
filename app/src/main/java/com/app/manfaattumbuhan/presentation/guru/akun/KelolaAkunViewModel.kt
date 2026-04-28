package com.app.manfaattumbuhan.presentation.guru.akun

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.manfaattumbuhan.domain.model.User
import com.app.manfaattumbuhan.domain.usecase.GetSiswaUseCase

class KelolaAkunViewModel(private val getSiswaUseCase: GetSiswaUseCase) : ViewModel() {

    private val _siswaList = MutableLiveData<List<User>>()
    val siswaList: LiveData<List<User>> = _siswaList

    fun loadSiswa() {
        _siswaList.value = getSiswaUseCase.execute()
    }
}
