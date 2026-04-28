package com.app.manfaattumbuhan.presentation.guru.laporan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.app.manfaattumbuhan.data.repository.SoalRepositoryImpl
import com.app.manfaattumbuhan.databinding.FragmentLaporanBinding
import com.app.manfaattumbuhan.domain.usecase.GetSoalUseCase

class LaporanFragment : Fragment() {

    private var _binding: FragmentLaporanBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LaporanViewModel by viewModels {
        LaporanViewModelFactory(GetSoalUseCase(SoalRepositoryImpl()))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLaporanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.loadData()

        viewModel.rataRataKelas.observe(viewLifecycleOwner) { rata ->
            binding.tvRataRata.text = String.format("%.1f", rata)
        }

        viewModel.tugasSelesaiPercentage.observe(viewLifecycleOwner) { persen ->
            binding.tvTugasSelesai.text = "$persen%"
        }

        viewModel.peningkatan.observe(viewLifecycleOwner) { peningkatan ->
            binding.tvPeningkatan.text = "+${String.format("%.1f", peningkatan)}%"
        }

        binding.btnLihatDetail.setOnClickListener {
            // placeholder for detailed view
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
