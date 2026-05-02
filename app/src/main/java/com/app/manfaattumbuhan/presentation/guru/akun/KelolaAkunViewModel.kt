package com.app.manfaattumbuhan.presentation.guru.akun

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.manfaattumbuhan.R
import com.app.manfaattumbuhan.data.local.StaticData
import com.app.manfaattumbuhan.domain.model.User
import com.app.manfaattumbuhan.domain.model.UserRole
import com.app.manfaattumbuhan.domain.usecase.GetSiswaUseCase

class KelolaAkunViewModel(private val getSiswaUseCase: GetSiswaUseCase) : ViewModel() {

    private val _siswaList = MutableLiveData<List<User>>()
    val siswaList: LiveData<List<User>> = _siswaList

    fun loadSiswa() {
        _siswaList.value = getSiswaUseCase.execute()
    }

    fun addSiswa(nama: String, username: String, password: String, kelas: String) {
        val newUser = User(
            id = System.currentTimeMillis().toInt(),
            nama = nama,
            username = username,
            role = UserRole.SISWA,
            kelas = kelas,
            sekolah = "SLB Negeri Harapan",
            avatarRes = R.drawable.avatar_siswa
        )
        StaticData.addUser(newUser, password)
        loadSiswa()
    }

    fun updateSiswa(user: User) {
        StaticData.updateUser(user)
        loadSiswa()
    }

    fun deleteSiswa(userId: Int) {
        StaticData.deleteUser(userId)
        loadSiswa()
    }
}
