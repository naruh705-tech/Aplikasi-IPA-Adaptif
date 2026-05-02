package com.app.manfaattumbuhan.presentation.guru.soal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.app.manfaattumbuhan.R
import com.app.manfaattumbuhan.data.local.StaticData
import com.app.manfaattumbuhan.databinding.FragmentKelolaSoalBinding

class KelolaSoalFragment : Fragment() {

    private var _binding: FragmentKelolaSoalBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKelolaSoalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateCounts()

        binding.imgProfile.setOnClickListener {
            findNavController().navigate(R.id.action_soal_to_profil)
        }

        binding.cardMudah.setOnClickListener {
            navigateToSoalList("Mudah")
        }

        binding.cardSedang.setOnClickListener {
            navigateToSoalList("Sedang")
        }

        binding.cardSulit.setOnClickListener {
            navigateToSoalList("Sulit")
        }
    }

    override fun onResume() {
        super.onResume()
        updateCounts()
    }

    private fun updateCounts() {
        val allSoal = StaticData.soalList
        binding.tvCountMudah.text = "${allSoal.count { it.tingkatKesulitan == "Mudah" }} soal"
        binding.tvCountSedang.text = "${allSoal.count { it.tingkatKesulitan == "Sedang" }} soal"
        binding.tvCountSulit.text = "${allSoal.count { it.tingkatKesulitan == "Sulit" }} soal"
    }

    private fun navigateToSoalList(tingkat: String) {
        val bundle = Bundle().apply { putString("tingkat", tingkat) }
        findNavController().navigate(R.id.action_soal_to_soalList, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
