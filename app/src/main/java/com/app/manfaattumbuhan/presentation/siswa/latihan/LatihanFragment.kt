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
import com.app.manfaattumbuhan.data.local.StaticData
import com.app.manfaattumbuhan.data.repository.SoalRepositoryImpl
import com.app.manfaattumbuhan.databinding.FragmentLatihanBinding
import com.app.manfaattumbuhan.domain.model.NilaiSiswa
import com.app.manfaattumbuhan.domain.usecase.GetSoalUseCase
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LatihanFragment : Fragment() {

    private var _binding: FragmentLatihanBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LatihanViewModel by viewModels {
        LatihanViewModelFactory(GetSoalUseCase(SoalRepositoryImpl()))
    }
    private var tingkat: String = "Pre-test"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLatihanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tingkat = arguments?.getString("tingkat") ?: "Pre-test"
        viewModel.loadSoalByTingkat(tingkat)

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
                saveNilai(score)
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

    private fun saveNilai(score: Int) {
        val user = StaticData.currentUser ?: return
        val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id"))
        val nilai = NilaiSiswa(
            id = System.currentTimeMillis().toInt(),
            siswaId = user.id,
            namaSiswa = user.nama,
            tingkat = tingkat,
            nilai = score,
            totalSoal = viewModel.getTotalSoal(),
            benar = viewModel.getCorrectCount(),
            tanggal = dateFormat.format(Date())
        )
        StaticData.addNilaiSiswa(nilai)
    }

    private fun showResultDialog(score: Int) {
        if (tingkat == "Pre-test") {
            showPretestResultDialog(score)
        } else {
            showLevelResultDialog(score)
        }
    }

    private fun showPretestResultDialog(score: Int) {
        val userId = StaticData.currentUser?.id ?: return

        val assignedLevel: String
        val levelMessage: String

        when {
            score >= 80 -> {
                assignedLevel = "Sulit"
                StaticData.unlockLevel(userId, "Mudah")
                StaticData.unlockLevel(userId, "Sedang")
                StaticData.unlockLevel(userId, "Sulit")
                StaticData.setCurrentLevel(userId, "Sulit")
                levelMessage = "Selamat! Nilai kamu $score.\nSistem menempatkan kamu di level Sulit!"
            }
            score >= 60 -> {
                assignedLevel = "Sedang"
                StaticData.unlockLevel(userId, "Mudah")
                StaticData.unlockLevel(userId, "Sedang")
                StaticData.setCurrentLevel(userId, "Sedang")
                levelMessage = "Bagus! Nilai kamu $score.\nSistem menempatkan kamu di level Sedang!"
            }
            else -> {
                assignedLevel = "Mudah"
                StaticData.unlockLevel(userId, "Mudah")
                StaticData.setCurrentLevel(userId, "Mudah")
                levelMessage = "Nilai kamu $score.\nSistem menempatkan kamu di level Mudah.\nAyo terus belajar!"
            }
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Hasil Pre-test")
            .setMessage(levelMessage)
            .setPositiveButton("Lanjut ke Level $assignedLevel") { _, _ ->
                val bundle = Bundle().apply { putString("tingkat", assignedLevel) }
                findNavController().navigate(R.id.action_pilihLevel_to_latihan, bundle)
            }
            .setCancelable(false)
            .show()
    }

    private fun showLevelResultDialog(score: Int) {
        val userId = StaticData.currentUser?.id ?: return

        val nextLevel: String
        val message: String

        when {
            score >= 80 -> {
                val newLevel = when (tingkat) {
                    "Mudah" -> "Sedang"
                    "Sedang" -> "Sulit"
                    else -> "Sulit"
                }
                StaticData.unlockLevel(userId, newLevel)
                StaticData.setCurrentLevel(userId, newLevel)
                nextLevel = newLevel
                if (tingkat == "Sulit") {
                    message = "Hebat! Kamu mendapat nilai $score di level Sulit!\nKamu sudah menyelesaikan semua level!"
                } else {
                    message = "Hebat! Kamu mendapat nilai $score.\nKamu naik ke level $newLevel!"
                }
            }
            score >= 60 -> {
                nextLevel = tingkat
                StaticData.setCurrentLevel(userId, tingkat)
                message = "Bagus! Kamu mendapat nilai $score.\nKamu tetap di level $tingkat. Terus belajar!"
            }
            else -> {
                val downLevel = when (tingkat) {
                    "Sulit" -> "Sedang"
                    "Sedang" -> "Mudah"
                    else -> "Mudah"
                }
                StaticData.setCurrentLevel(userId, downLevel)
                nextLevel = downLevel
                if (tingkat == "Mudah") {
                    message = "Kamu mendapat nilai $score.\nTetap di level Mudah. Ayo belajar lagi!"
                } else {
                    message = "Kamu mendapat nilai $score.\nKamu turun ke level $downLevel. Ayo belajar lagi!"
                }
            }
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Hasil Latihan - $tingkat")
            .setMessage(message)
            .setPositiveButton("Lanjut ke Level $nextLevel") { _, _ ->
                val bundle = Bundle().apply { putString("tingkat", nextLevel) }
                findNavController().navigate(R.id.action_pilihLevel_to_latihan, bundle)
            }
            .setNegativeButton("Kembali") { _, _ ->
                findNavController().navigateUp()
            }
            .setCancelable(false)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
