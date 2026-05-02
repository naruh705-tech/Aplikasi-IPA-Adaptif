package com.app.manfaattumbuhan.presentation.guru.soal

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.manfaattumbuhan.domain.model.Soal
import com.app.manfaattumbuhan.domain.usecase.GetSoalUseCase

class KelolaSoalViewModel(private val getSoalUseCase: GetSoalUseCase) : ViewModel() {

    private val _soalList = MutableLiveData<List<Soal>>()
    val soalList: LiveData<List<Soal>> = _soalList

    fun loadSoal() {
        _soalList.value = getSoalUseCase.getAll()
    }

    fun deleteSoal(id: Int) {
        getSoalUseCase.delete(id)
        loadSoal()
    }

    fun addSoal(soal: Soal) {
        getSoalUseCase.add(soal)
        loadSoal()
    }

    fun updateSoal(soal: Soal) {
        getSoalUseCase.update(soal)
        loadSoal()
    }
}
