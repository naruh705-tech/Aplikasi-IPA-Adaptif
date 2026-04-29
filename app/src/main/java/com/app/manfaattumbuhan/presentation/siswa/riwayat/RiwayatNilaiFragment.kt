package com.app.manfaattumbuhan.presentation.siswa.riwayat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.manfaattumbuhan.data.local.StaticData
import com.app.manfaattumbuhan.databinding.FragmentRiwayatNilaiBinding
import com.app.manfaattumbuhan.presentation.adapter.RiwayatNilaiAdapter

class RiwayatNilaiFragment : Fragment() {

    private var _binding: FragmentRiwayatNilaiBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRiwayatNilaiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = RiwayatNilaiAdapter()
        binding.rvRiwayat.layoutManager = LinearLayoutManager(context)
        binding.rvRiwayat.adapter = adapter

        binding.btnKembali.setOnClickListener {
            findNavController().navigateUp()
        }

        loadData(adapter)
    }

    override fun onResume() {
        super.onResume()
        val adapter = binding.rvRiwayat.adapter as? RiwayatNilaiAdapter
        if (adapter != null) {
            loadData(adapter)
        }
    }

    private fun loadData(adapter: RiwayatNilaiAdapter) {
        val userId = StaticData.currentUser?.id ?: return
        val nilaiList = StaticData.getNilaiByUserId(userId).sortedByDescending { it.id }

        if (nilaiList.isEmpty()) {
            binding.tvEmptyState.visibility = View.VISIBLE
            binding.rvRiwayat.visibility = View.GONE
        } else {
            binding.tvEmptyState.visibility = View.GONE
            binding.rvRiwayat.visibility = View.VISIBLE
            adapter.submitList(nilaiList)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
