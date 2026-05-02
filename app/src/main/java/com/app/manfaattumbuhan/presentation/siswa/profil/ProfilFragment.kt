package com.app.manfaattumbuhan.presentation.siswa.profil

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.app.manfaattumbuhan.R
import com.app.manfaattumbuhan.data.local.TokenManager
import com.app.manfaattumbuhan.databinding.FragmentProfilBinding
import com.app.manfaattumbuhan.presentation.login.LoginActivity

class ProfilFragment : Fragment() {

    private var _binding: FragmentProfilBinding? = null
    private val binding get() = _binding!!

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

        binding.tvNama.text = TokenManager.getUserName()
        binding.tvKelas.text = TokenManager.getUserKelas()
        binding.tvSekolah.text = "NIM: ${TokenManager.getUserNim()}"
        binding.imgAvatar.setImageResource(R.drawable.avatar_siswa)
        binding.tvStatusBadge.text = "SISWA AKTIF"

        binding.tvTotalLatihan.text = "-"
        binding.tvLabelLatihan.text = "Latihan"

        binding.tvStreak.text = "-"
        binding.tvLabelStreak.text = "Beruntun"

        binding.btnKeluar.setOnClickListener {
            TokenManager.clear()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        binding.btnUbahSandi.setOnClickListener {
            // placeholder
        }

        binding.btnBantuan.setOnClickListener {
            // placeholder
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
