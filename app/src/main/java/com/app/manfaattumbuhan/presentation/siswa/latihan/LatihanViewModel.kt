package com.app.manfaattumbuhan.presentation.siswa.latihan

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.manfaattumbuhan.domain.model.Soal
import com.app.manfaattumbuhan.domain.usecase.GetSoalUseCase

class LatihanViewModel(private val getSoalUseCase: GetSoalUseCase) : ViewModel() {

    private val _soalList = MutableLiveData<List<Soal>>()
    val soalList: LiveData<List<Soal>> = _soalList

    private val _currentIndex = MutableLiveData(0)
    val currentIndex: LiveData<Int> = _currentIndex

    private val _currentSoal = MutableLiveData<Soal>()
    val currentSoal: LiveData<Soal> = _currentSoal

    private val _selectedAnswer = MutableLiveData<Int?>()
    val selectedAnswer: LiveData<Int?> = _selectedAnswer

    private val _score = MutableLiveData(0)
    val score: LiveData<Int> = _score

    private val _isFinished = MutableLiveData(false)
    val isFinished: LiveData<Boolean> = _isFinished

    private val _progress = MutableLiveData(0)
    val progress: LiveData<Int> = _progress

    private var correctCount = 0

    fun loadSoal() {
        loadSoalByTingkat("Pre-test")
    }

    fun loadSoalByTingkat(tingkat: String) {
        correctCount = 0
        _currentIndex.value = 0
        _isFinished.value = false
        _selectedAnswer.value = null
        _score.value = 0

        val allSoal = getSoalUseCase.getAll()
        val filtered = if (tingkat == "Pre-test") {
            allSoal.shuffled().take(10)
        } else {
            allSoal.filter { it.tingkatKesulitan == tingkat }.let { list ->
                if (list.isEmpty()) allSoal.shuffled().take(5) else list
            }
        }

        _soalList.value = filtered
        if (filtered.isNotEmpty()) {
            _currentSoal.value = filtered[0]
            updateProgress()
        }
    }

    fun selectAnswer(index: Int) {
        _selectedAnswer.value = index
    }

    fun nextSoal() {
        val list = _soalList.value ?: return
        val current = _currentIndex.value ?: 0
        val selected = _selectedAnswer.value

        if (selected != null && selected == list[current].jawabanBenar) {
            correctCount++
        }

        if (current < list.size - 1) {
            val nextIdx = current + 1
            _currentIndex.value = nextIdx
            _currentSoal.value = list[nextIdx]
            _selectedAnswer.value = null
            updateProgress()
        } else {
            _score.value = (correctCount * 100) / list.size
            _isFinished.value = true
        }
    }

    fun previousSoal() {
        val current = _currentIndex.value ?: 0
        if (current > 0) {
            val prevIdx = current - 1
            _currentIndex.value = prevIdx
            _currentSoal.value = _soalList.value?.get(prevIdx)
            _selectedAnswer.value = null
            updateProgress()
        }
    }

    private fun updateProgress() {
        val list = _soalList.value ?: return
        val current = _currentIndex.value ?: 0
        _progress.value = ((current + 1) * 100) / list.size
    }

    fun getTotalSoal(): Int = _soalList.value?.size ?: 0

    fun getCorrectCount(): Int = correctCount
}
