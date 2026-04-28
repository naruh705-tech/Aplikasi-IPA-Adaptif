package com.app.manfaattumbuhan.presentation.siswa.profil

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.manfaattumbuhan.data.local.StaticData
import com.app.manfaattumbuhan.domain.model.User

class ProfilViewModel : ViewModel() {

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    private val _totalLatihan = MutableLiveData(12)
    val totalLatihan: LiveData<Int> = _totalLatihan

    private val _streak = MutableLiveData(5)
    val streak: LiveData<Int> = _streak

    fun loadUser() {
        _currentUser.value = StaticData.currentUser
    }
}
