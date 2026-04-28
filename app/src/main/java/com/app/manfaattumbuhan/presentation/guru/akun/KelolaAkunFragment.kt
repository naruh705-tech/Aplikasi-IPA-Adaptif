package com.app.manfaattumbuhan.presentation.guru.akun

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.manfaattumbuhan.R
import com.app.manfaattumbuhan.data.repository.UserRepositoryImpl
import com.app.manfaattumbuhan.databinding.FragmentKelolaAkunBinding
import com.app.manfaattumbuhan.domain.usecase.GetSiswaUseCase
import com.app.manfaattumbuhan.presentation.adapter.SiswaAdapter

class KelolaAkunFragment : Fragment() {

    private var _binding: FragmentKelolaAkunBinding? = null
    private val binding get() = _binding!!
    private val viewModel: KelolaAkunViewModel by viewModels {
        KelolaAkunViewModelFactory(GetSiswaUseCase(UserRepositoryImpl()))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKelolaAkunBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = SiswaAdapter()
        binding.rvSiswa.layoutManager = LinearLayoutManager(context)
        binding.rvSiswa.adapter = adapter

        viewModel.loadSiswa()
        viewModel.siswaList.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            binding.tvJumlahSiswa.text = "${list.size} siswa"
        }

        binding.btnLihatLaporan.setOnClickListener {
            findNavController().navigate(R.id.laporanFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
