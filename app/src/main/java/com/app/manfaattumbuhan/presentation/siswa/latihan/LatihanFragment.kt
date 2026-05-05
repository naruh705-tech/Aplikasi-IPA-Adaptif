package com.app.manfaattumbuhan.presentation.siswa.latihan

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.RadioButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.app.manfaattumbuhan.R
import com.app.manfaattumbuhan.data.local.StaticData
import com.app.manfaattumbuhan.data.local.TokenManager
import com.app.manfaattumbuhan.data.remote.ApiConfig
import com.app.manfaattumbuhan.data.remote.ApiService
import com.app.manfaattumbuhan.data.remote.model.CreateNilaiRequest
import com.app.manfaattumbuhan.databinding.FragmentLatihanBinding
import com.app.manfaattumbuhan.domain.model.NilaiSiswa
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LatihanFragment : Fragment() {

    private var _binding: FragmentLatihanBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LatihanViewModel by viewModels { LatihanViewModelFactory() }
    private var tingkat: String = "Pre-test"
    private val apiService = ApiConfig.createService<ApiService>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLatihanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        TokenManager.init(requireContext())
        tingkat = arguments?.getString("tingkat") ?: "Pre-test"
        viewModel.loadSoalByTingkat(tingkat)

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.btnSelanjutnya.isEnabled = !loading
            binding.btnKembali.isEnabled = !loading
            if (loading) {
                binding.tvPertanyaan.text = "Memuat soal..."
                binding.radioGroup.removeAllViews()
                binding.imgSoal.visibility = View.GONE
            }
        }

        viewModel.loadError.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                binding.tvPertanyaan.text = it
            }
        }

        viewModel.currentSoal.observe(viewLifecycleOwner) { soal ->
            if (soal.pertanyaan.isNotBlank()) {
                binding.tvPertanyaan.visibility = View.VISIBLE
                binding.tvPertanyaan.text = soal.pertanyaan
            } else {
                binding.tvPertanyaan.visibility = View.GONE
            }

            if (soal.imageUrl != null && soal.imageUrl.isNotBlank()) {
                binding.imgSoal.visibility = View.VISIBLE
                Glide.with(this)
                    .load(soal.imageUrl)
                    .placeholder(R.drawable.bg_rounded_gray)
                    .error(R.drawable.bg_rounded_gray)
                    .into(binding.imgSoal)
            } else if (soal.imageRes != null) {
                binding.imgSoal.visibility = View.VISIBLE
                binding.imgSoal.setImageResource(soal.imageRes)
            } else {
                binding.imgSoal.visibility = View.GONE
            }

            if (soal.videoUrl != null && soal.videoUrl.isNotBlank()) {
                binding.videoSoal.visibility = View.VISIBLE
                binding.videoSoal.setVideoURI(Uri.parse(soal.videoUrl))
                val mediaController = MediaController(requireContext())
                mediaController.setAnchorView(binding.videoSoal)
                binding.videoSoal.setMediaController(mediaController)
                binding.videoSoal.start()
            } else {
                binding.videoSoal.visibility = View.GONE
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
        val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id"))
        val userId = TokenManager.getUserId()
        val userName = TokenManager.getUserName()

        if (userId.isNotBlank()) {
            val nilai = NilaiSiswa(
                id = System.currentTimeMillis().toInt(),
                siswaId = userId,
                namaSiswa = userName,
                tingkat = tingkat,
                nilai = score,
                totalSoal = viewModel.getTotalSoal(),
                benar = viewModel.getCorrectCount(),
                tanggal = dateFormat.format(Date())
            )
            StaticData.addNilaiSiswa(nilai)
        }

        val token = TokenManager.getToken()
        if (token.isNotBlank() && userId.isNotBlank()) {
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    apiService.createNilai(
                        token, userId,
                        CreateNilaiRequest(
                            soal_id = "latihan-$tingkat",
                            nilai = score.toDouble(),
                            catatan = "Benar ${viewModel.getCorrectCount()} dari ${viewModel.getTotalSoal()} - Level $tingkat"
                        )
                    )
                } catch (_: Exception) {}
            }
        }
    }

    private fun showResultDialog(score: Int) {
        if (tingkat == "Pre-test") {
            showPretestResultDialog(score)
        } else {
            showLevelResultDialog(score)
        }
    }

    private fun showPretestResultDialog(score: Int) {
        val userIdStr = TokenManager.getUserId()
        val userIdInt = userIdStr.hashCode()

        val assignedLevel: String
        val levelMessage: String

        when {
            score >= 80 -> {
                assignedLevel = "Sulit"
                StaticData.unlockLevel(userIdInt, "Mudah")
                StaticData.unlockLevel(userIdInt, "Sedang")
                StaticData.unlockLevel(userIdInt, "Sulit")
                StaticData.setCurrentLevel(userIdInt, "Sulit")
                levelMessage = "Selamat! Nilai kamu $score.\nSistem menempatkan kamu di level Sulit!"
            }
            score >= 60 -> {
                assignedLevel = "Sedang"
                StaticData.unlockLevel(userIdInt, "Mudah")
                StaticData.unlockLevel(userIdInt, "Sedang")
                StaticData.setCurrentLevel(userIdInt, "Sedang")
                levelMessage = "Bagus! Nilai kamu $score.\nSistem menempatkan kamu di level Sedang!"
            }
            else -> {
                assignedLevel = "Mudah"
                StaticData.unlockLevel(userIdInt, "Mudah")
                StaticData.setCurrentLevel(userIdInt, "Mudah")
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
        val userIdStr = TokenManager.getUserId()
        val userIdInt = userIdStr.hashCode()

        val nextLevel: String
        val message: String

        when {
            score >= 80 -> {
                val newLevel = when (tingkat) {
                    "Mudah" -> "Sedang"
                    "Sedang" -> "Sulit"
                    else -> "Sulit"
                }
                StaticData.unlockLevel(userIdInt, newLevel)
                StaticData.setCurrentLevel(userIdInt, newLevel)
                nextLevel = newLevel
                if (tingkat == "Sulit") {
                    message = "Hebat! Kamu mendapat nilai $score di level Sulit!\nKamu sudah menyelesaikan semua level!"
                } else {
                    message = "Hebat! Kamu mendapat nilai $score.\nKamu naik ke level $newLevel!"
                }
            }
            score >= 60 -> {
                nextLevel = tingkat
                StaticData.setCurrentLevel(userIdInt, tingkat)
                message = "Bagus! Kamu mendapat nilai $score.\nKamu tetap di level $tingkat. Terus belajar!"
            }
            else -> {
                val downLevel = when (tingkat) {
                    "Sulit" -> "Sedang"
                    "Sedang" -> "Mudah"
                    else -> "Mudah"
                }
                StaticData.setCurrentLevel(userIdInt, downLevel)
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
