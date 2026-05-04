package com.app.manfaattumbuhan.presentation.siswa.latihan

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.app.manfaattumbuhan.data.local.TokenManager
import com.app.manfaattumbuhan.data.remote.ApiConfig
import com.app.manfaattumbuhan.data.remote.ApiService
import com.app.manfaattumbuhan.domain.model.Soal
import kotlinx.coroutines.launch
import org.json.JSONObject

class LatihanViewModel : ViewModel() {

    private val apiService = ApiConfig.createService<ApiService>()

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

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _loadError = MutableLiveData<String?>()
    val loadError: LiveData<String?> = _loadError

    private var correctCount = 0

    fun loadSoalByTingkat(tingkat: String) {
        correctCount = 0
        _currentIndex.value = 0
        _isFinished.value = false
        _selectedAnswer.value = null
        _score.value = 0
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val token = TokenManager.getToken()
                val response = apiService.getSoalList(token, limit = 100)
                if (response.isSuccessful && response.body()?.success == true) {
                    val soalApiList = response.body()!!.data!!.soal
                    val soalList = soalApiList.mapIndexedNotNull { index, soalApi ->
                        parseSoalFromApi(index, soalApi)
                    }

                    val filtered = if (tingkat == "Pre-test") {
                        soalList.shuffled().take(10)
                    } else {
                        soalList.shuffled().take(10)
                    }

                    _soalList.postValue(filtered)
                    if (filtered.isNotEmpty()) {
                        _currentSoal.postValue(filtered[0])
                        _progress.postValue(((0 + 1) * 100) / filtered.size)
                    } else {
                        _loadError.postValue("Belum ada soal tersedia")
                    }
                } else {
                    _loadError.postValue(response.body()?.message ?: "Gagal memuat soal")
                }
            } catch (e: Exception) {
                _loadError.postValue("Error: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    private fun parseSoalFromApi(index: Int, soalApi: com.app.manfaattumbuhan.data.remote.model.SoalApi): Soal? {
        return try {
            val json = JSONObject(soalApi.deskripsi)
            val pilihanArray = json.getJSONArray("pilihan")
            val pilihan = mutableListOf<String>()
            for (i in 0 until pilihanArray.length()) {
                pilihan.add(pilihanArray.getString(i))
            }
            val jawabanBenar = json.getInt("jawabanBenar")

            Soal(
                id = index,
                pertanyaan = soalApi.judul,
                imageUrl = soalApi.foto_url,
                videoUrl = soalApi.video_url,
                pilihan = pilihan,
                jawabanBenar = jawabanBenar,
                apiId = soalApi.id
            )
        } catch (e: Exception) {
            null
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

class LatihanViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LatihanViewModel() as T
    }
}
