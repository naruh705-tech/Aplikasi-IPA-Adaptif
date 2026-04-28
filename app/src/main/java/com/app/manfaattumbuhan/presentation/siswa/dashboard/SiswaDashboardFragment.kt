package com.app.manfaattumbuhan.presentation.siswa.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.app.manfaattumbuhan.R
import com.app.manfaattumbuhan.databinding.FragmentSiswaDashboardBinding

class SiswaDashboardFragment : Fragment() {

    private var _binding: FragmentSiswaDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SiswaDashboardViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSiswaDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.loadUser()

        viewModel.currentUser.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.tvGreeting.text = "Halo, ${it.nama.split(" ").first()}! 👋"
            }
        }

        binding.cardSiswaMenu.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_materi)
        }

        binding.cardLatihanSoal.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_latihan)
        }

        binding.cardRiwayatNilai.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_profil)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
