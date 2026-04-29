package com.app.manfaattumbuhan.presentation.guru.laporan

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.manfaattumbuhan.data.local.StaticData
import com.app.manfaattumbuhan.domain.model.NilaiSiswa

class LaporanViewModel : ViewModel() {

    private val _nilaiList = MutableLiveData<List<NilaiSiswa>>()
    val nilaiList: LiveData<List<NilaiSiswa>> = _nilaiList

    fun loadData() {
        _nilaiList.value = StaticData.getAllNilai().sortedByDescending { it.id }
    }
}
