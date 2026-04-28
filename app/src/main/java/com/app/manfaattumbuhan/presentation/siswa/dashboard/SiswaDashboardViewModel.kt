package com.app.manfaattumbuhan.presentation.siswa.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.manfaattumbuhan.data.local.StaticData
import com.app.manfaattumbuhan.domain.model.User

class SiswaDashboardViewModel : ViewModel() {

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    fun loadUser() {
        _currentUser.value = StaticData.currentUser
    }
}
