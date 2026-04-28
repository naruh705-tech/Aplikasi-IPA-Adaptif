package com.app.manfaattumbuhan.presentation.guru.laporan

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.manfaattumbuhan.domain.model.HasilBelajar
import com.app.manfaattumbuhan.domain.usecase.GetSoalUseCase

class LaporanViewModel(private val getSoalUseCase: GetSoalUseCase) : ViewModel() {

    private val _hasilBelajar = MutableLiveData<List<HasilBelajar>>()
    val hasilBelajar: LiveData<List<HasilBelajar>> = _hasilBelajar

    private val _rataRataKelas = MutableLiveData<Double>()
    val rataRataKelas: LiveData<Double> = _rataRataKelas

    private val _tugasSelesaiPercentage = MutableLiveData<Int>()
    val tugasSelesaiPercentage: LiveData<Int> = _tugasSelesaiPercentage

    private val _peningkatan = MutableLiveData<Double>()
    val peningkatan: LiveData<Double> = _peningkatan

    fun loadData() {
        val hasil = getSoalUseCase.getHasilBelajar()
        _hasilBelajar.value = hasil

        if (hasil.isNotEmpty()) {
            _rataRataKelas.value = hasil.map { it.nilaiRataRata }.average()
            val totalTugas = hasil.sumOf { it.totalTugas }
            val totalSelesai = hasil.sumOf { it.tugasSelesai }
            _tugasSelesaiPercentage.value = if (totalTugas > 0) (totalSelesai * 100) / totalTugas else 0
            _peningkatan.value = hasil.map { it.peningkatan }.average()
        }
    }
}
