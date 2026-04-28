package com.app.manfaattumbuhan.presentation.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.app.manfaattumbuhan.R
import com.app.manfaattumbuhan.data.repository.UserRepositoryImpl
import com.app.manfaattumbuhan.databinding.ActivityLoginBinding
import com.app.manfaattumbuhan.domain.model.UserRole
import com.app.manfaattumbuhan.domain.usecase.LoginUseCase
import com.app.manfaattumbuhan.presentation.guru.GuruActivity
import com.app.manfaattumbuhan.presentation.siswa.SiswaActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val factory = LoginViewModelFactory(LoginUseCase(UserRepositoryImpl()))
        viewModel = ViewModelProvider(this, factory)[LoginViewModel::class.java]

        setupRoleSelection()
        setupLoginButton()
        observeViewModel()
    }

    private fun setupRoleSelection() {
        binding.cardSiswa.setOnClickListener {
            viewModel.selectRole(UserRole.SISWA)
        }

        binding.cardGuru.setOnClickListener {
            viewModel.selectRole(UserRole.GURU)
        }

        viewModel.selectedRole.observe(this) { role ->
            when (role) {
                UserRole.SISWA -> {
                    binding.cardSiswa.setCardBackgroundColor(getColor(R.color.green_light))
                    binding.cardSiswa.strokeColor = getColor(R.color.green_primary)
                    binding.cardSiswa.strokeWidth = 4
                    binding.cardGuru.setCardBackgroundColor(getColor(R.color.white))
                    binding.cardGuru.strokeColor = getColor(R.color.gray_border)
                    binding.cardGuru.strokeWidth = 2
                    binding.layoutLoginForm.visibility = android.view.View.VISIBLE
                }
                UserRole.GURU -> {
                    binding.cardGuru.setCardBackgroundColor(getColor(R.color.green_light))
                    binding.cardGuru.strokeColor = getColor(R.color.green_primary)
                    binding.cardGuru.strokeWidth = 4
                    binding.cardSiswa.setCardBackgroundColor(getColor(R.color.white))
                    binding.cardSiswa.strokeColor = getColor(R.color.gray_border)
                    binding.cardSiswa.strokeWidth = 2
                    binding.layoutLoginForm.visibility = android.view.View.VISIBLE
                }
                else -> {}
            }
        }
    }

    private fun setupLoginButton() {
        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            viewModel.login(username, password)
        }
    }

    private fun observeViewModel() {
        viewModel.loginResult.observe(this) { result ->
            when (result) {
                is LoginResult.Success -> {
                    val intent = when (result.user.role) {
                        UserRole.SISWA -> Intent(this, SiswaActivity::class.java)
                        UserRole.GURU -> Intent(this, GuruActivity::class.java)
                    }
                    intent.putExtra("USER_NAME", result.user.nama)
                    intent.putExtra("USER_ID", result.user.id)
                    startActivity(intent)
                    finish()
                }
                is LoginResult.Error -> {
                    Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
