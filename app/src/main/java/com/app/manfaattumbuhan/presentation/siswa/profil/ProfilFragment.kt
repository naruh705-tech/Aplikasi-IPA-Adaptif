package com.app.manfaattumbuhan.presentation.siswa.profil

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.app.manfaattumbuhan.R
import com.app.manfaattumbuhan.data.local.TokenManager
import com.app.manfaattumbuhan.data.remote.ApiConfig
import com.app.manfaattumbuhan.data.remote.ApiService
import com.app.manfaattumbuhan.data.remote.model.UpdateProfilRequest
import com.app.manfaattumbuhan.databinding.FragmentProfilBinding
import com.app.manfaattumbuhan.presentation.login.LoginActivity
import kotlinx.coroutines.launch

class ProfilFragment : Fragment() {

    private var _binding: FragmentProfilBinding? = null
    private val binding get() = _binding!!
    private val apiService = ApiConfig.createService<ApiService>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        TokenManager.init(requireContext())

        loadProfilData()

        binding.btnSimpanProfil.setOnClickListener {
            simpanProfil()
        }

        binding.btnKeluar.setOnClickListener {
            TokenManager.clear()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun loadProfilData() {
        binding.tvNama.text = TokenManager.getUserName()
        binding.tvKelas.text = TokenManager.getUserKelas()
        binding.tvSekolah.text = "NISN: ${TokenManager.getUserNim()}"
        binding.imgAvatar.setImageResource(R.drawable.avatar_siswa)
        binding.tvStatusBadge.text = "SISWA AKTIF"

        binding.etEditNama.setText(TokenManager.getUserName())

        binding.tvTotalLatihan.text = "-"
        binding.tvLabelLatihan.text = "Latihan"
        binding.tvStreak.text = "-"
        binding.tvLabelStreak.text = "Beruntun"
    }

    private fun simpanProfil() {
        val nama = binding.etEditNama.text.toString().trim().ifBlank { null }
        val password = binding.etEditPassword.text.toString().trim().ifBlank { null }
        val fotoProfil = binding.etEditFoto.text.toString().trim().ifBlank { null }

        if (nama == null && password == null && fotoProfil == null) {
            Toast.makeText(requireContext(), "Tidak ada perubahan", Toast.LENGTH_SHORT).show()
            return
        }

        val token = TokenManager.getToken()
        val userId = TokenManager.getUserId()
        if (token.isBlank() || userId.isBlank()) {
            Toast.makeText(requireContext(), "Sesi tidak valid", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBarProfil.visibility = View.VISIBLE
        binding.btnSimpanProfil.isEnabled = false

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = apiService.updateProfil(
                    token, userId,
                    UpdateProfilRequest(nama, password, fotoProfil)
                )
                if (response.isSuccessful && response.body()?.success == true) {
                    val updatedSiswa = response.body()!!.data!!
                    TokenManager.saveSiswaLogin(
                        token.removePrefix("Bearer "),
                        updatedSiswa.id,
                        updatedSiswa.nama,
                        updatedSiswa.nim,
                        updatedSiswa.kelas
                    )
                    loadProfilData()
                    binding.etEditPassword.setText("")
                    Toast.makeText(requireContext(), "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        response.body()?.message ?: "Gagal memperbarui profil",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBarProfil.visibility = View.GONE
                binding.btnSimpanProfil.isEnabled = true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
