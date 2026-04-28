package com.app.manfaattumbuhan.presentation.siswa.latihan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.app.manfaattumbuhan.R
import com.app.manfaattumbuhan.data.repository.SoalRepositoryImpl
import com.app.manfaattumbuhan.databinding.FragmentLatihanBinding
import com.app.manfaattumbuhan.domain.usecase.GetSoalUseCase
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class LatihanFragment : Fragment() {

    private var _binding: FragmentLatihanBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LatihanViewModel by viewModels {
        LatihanViewModelFactory(GetSoalUseCase(SoalRepositoryImpl()))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLatihanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.loadSoal()

        viewModel.currentSoal.observe(viewLifecycleOwner) { soal ->
            binding.tvPertanyaan.text = soal.pertanyaan

            if (soal.imageRes != null) {
                binding.imgSoal.visibility = View.VISIBLE
                binding.imgSoal.setImageResource(soal.imageRes)
            } else {
                binding.imgSoal.visibility = View.GONE
            }

            binding.radioGroup.removeAllViews()
            soal.pilihan.forEachIndexed { index, pilihan ->
                val radioButton = RadioButton(requireContext()).apply {
                    id = index
                    text = pilihan
                    textSize = 16f
                    setPadding(16, 16, 16, 16)
                }
                binding.radioGroup.addView(radioButton)
            }

            binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
                viewModel.selectAnswer(checkedId)
            }
        }

        viewModel.currentIndex.observe(viewLifecycleOwner) { index ->
            val total = viewModel.getTotalSoal()
            binding.tvSoalCounter.text = "Soal ${index + 1} dari $total"
            binding.tvMotivasi.text = if (index < total / 2) "Ayo semangat!" else "Sedikit lagi!"
        }

        viewModel.progress.observe(viewLifecycleOwner) { progress ->
            binding.progressBar.progress = progress
            binding.tvProgress.text = "$progress%"
        }

        viewModel.isFinished.observe(viewLifecycleOwner) { finished ->
            if (finished) {
                val score = viewModel.score.value ?: 0
                showResultDialog(score)
            }
        }

        binding.btnKembali.setOnClickListener {
            viewModel.previousSoal()
        }

        binding.btnSelanjutnya.setOnClickListener {
            viewModel.nextSoal()
        }

        binding.btnClose.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun showResultDialog(score: Int) {
        val message = when {
            score >= 80 -> "Hebat! Kamu mendapat nilai $score. Pertahankan! 🎉"
            score >= 60 -> "Bagus! Kamu mendapat nilai $score. Terus belajar! 💪"
            else -> "Kamu mendapat nilai $score. Ayo belajar lagi! 📚"
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Hasil Latihan")
            .setMessage(message)
            .setPositiveButton("Kembali ke Beranda") { _, _ ->
                findNavController().navigate(R.id.siswaDashboardFragment)
            }
            .setNegativeButton("Ulangi") { _, _ ->
                viewModel.loadSoal()
            }
            .setCancelable(false)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
