package com.app.manfaattumbuhan.presentation.siswa.profil

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.app.manfaattumbuhan.databinding.FragmentProfilBinding
import com.app.manfaattumbuhan.presentation.login.LoginActivity

class ProfilFragment : Fragment() {

    private var _binding: FragmentProfilBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfilViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.loadUser()

        viewModel.currentUser.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.tvNama.text = it.nama
                binding.tvKelas.text = it.kelas
                binding.tvSekolah.text = it.sekolah
                if (it.avatarRes != 0) {
                    binding.imgAvatar.setImageResource(it.avatarRes)
                }
                binding.tvStatusBadge.text = "SISWA AKTIF"
            }
        }

        viewModel.totalLatihan.observe(viewLifecycleOwner) { total ->
            binding.tvTotalLatihan.text = total.toString()
            binding.tvLabelLatihan.text = "Latihan"
        }

        viewModel.streak.observe(viewLifecycleOwner) { streak ->
            binding.tvStreak.text = "$streak Hari"
            binding.tvLabelStreak.text = "Beruntun"
        }

        binding.btnKeluar.setOnClickListener {
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
