package com.app.manfaattumbuhan.presentation.guru.soal

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.manfaattumbuhan.R
import com.app.manfaattumbuhan.data.repository.SoalRepositoryImpl
import com.app.manfaattumbuhan.databinding.FragmentKelolaSoalListBinding
import com.app.manfaattumbuhan.domain.model.Soal
import com.app.manfaattumbuhan.domain.usecase.GetSoalUseCase
import com.app.manfaattumbuhan.presentation.adapter.SoalGuruAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class KelolaSoalListFragment : Fragment() {

    private var _binding: FragmentKelolaSoalListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: KelolaSoalViewModel by viewModels {
        KelolaSoalViewModelFactory(GetSoalUseCase(SoalRepositoryImpl()))
    }
    private var selectedImageUri: Uri? = null
    private var imagePreview: ImageView? = null
    private var tingkat: String = "Mudah"

    private val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri = result.data?.data
            imagePreview?.setImageURI(selectedImageUri)
            imagePreview?.visibility = View.VISIBLE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKelolaSoalListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tingkat = arguments?.getString("tingkat") ?: "Mudah"
        binding.tvTitle.text = "Soal $tingkat"

        val adapter = SoalGuruAdapter(
            onEdit = { soal -> showEditSoalDialog(soal) },
            onDelete = { soal ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Hapus Soal")
                    .setMessage("Apakah Anda yakin ingin menghapus soal ini?")
                    .setPositiveButton("Hapus") { _, _ ->
                        viewModel.deleteSoal(soal.id)
                        loadFilteredSoal(binding.rvSoal.adapter as SoalGuruAdapter)
                    }
                    .setNegativeButton("Batal", null)
                    .show()
            }
        )

        binding.rvSoal.layoutManager = LinearLayoutManager(context)
        binding.rvSoal.adapter = adapter

        loadFilteredSoal(adapter)

        binding.btnKembali.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnBuatSoal.setOnClickListener {
            showAddSoalDialog(adapter)
        }
    }

    private fun loadFilteredSoal(adapter: SoalGuruAdapter) {
        viewModel.loadSoal()
        val filtered = viewModel.soalList.value?.filter { it.tingkatKesulitan == tingkat } ?: emptyList()
        if (filtered.isEmpty()) {
            binding.tvEmptyState.visibility = View.VISIBLE
            binding.rvSoal.visibility = View.GONE
        } else {
            binding.tvEmptyState.visibility = View.GONE
            binding.rvSoal.visibility = View.VISIBLE
        }
        adapter.submitList(filtered)

        viewModel.soalList.observe(viewLifecycleOwner) { list ->
            val filteredList = list.filter { it.tingkatKesulitan == tingkat }
            if (filteredList.isEmpty()) {
                binding.tvEmptyState.visibility = View.VISIBLE
                binding.rvSoal.visibility = View.GONE
            } else {
                binding.tvEmptyState.visibility = View.GONE
                binding.rvSoal.visibility = View.VISIBLE
            }
            adapter.submitList(filteredList)
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun showAddSoalDialog(adapter: SoalGuruAdapter) {
        selectedImageUri = null
        val layout = createSoalFormLayout()
        val views = layout.tag as SoalFormViews

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Buat Soal $tingkat")
            .setView(layout)
            .setPositiveButton("Simpan") { _, _ ->
                val pertanyaan = views.etPertanyaan.text.toString()
                val pilihan = listOf(
                    views.etPilihan1.text.toString(),
                    views.etPilihan2.text.toString(),
                    views.etPilihan3.text.toString(),
                    views.etPilihan4.text.toString()
                )

                if (pertanyaan.isNotBlank() && pilihan.all { it.isNotBlank() }) {
                    val newSoal = Soal(
                        id = System.currentTimeMillis().toInt(),
                        pertanyaan = pertanyaan,
                        pilihan = pilihan,
                        jawabanBenar = 0,
                        modul = "Manfaat Tumbuhan",
                        tingkatKesulitan = tingkat
                    )
                    viewModel.addSoal(newSoal)
                    Toast.makeText(context, "Soal berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showEditSoalDialog(soal: Soal) {
        selectedImageUri = null
        val layout = createSoalFormLayout()
        val views = layout.tag as SoalFormViews

        views.etPertanyaan.setText(soal.pertanyaan)
        if (soal.pilihan.size >= 4) {
            views.etPilihan1.setText(soal.pilihan[0])
            views.etPilihan2.setText(soal.pilihan[1])
            views.etPilihan3.setText(soal.pilihan[2])
            views.etPilihan4.setText(soal.pilihan[3])
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Edit Soal $tingkat")
            .setView(layout)
            .setPositiveButton("Simpan") { _, _ ->
                val pertanyaan = views.etPertanyaan.text.toString()
                val pilihan = listOf(
                    views.etPilihan1.text.toString(),
                    views.etPilihan2.text.toString(),
                    views.etPilihan3.text.toString(),
                    views.etPilihan4.text.toString()
                )

                if (pertanyaan.isNotBlank() && pilihan.all { it.isNotBlank() }) {
                    val updatedSoal = soal.copy(
                        pertanyaan = pertanyaan,
                        pilihan = pilihan
                    )
                    viewModel.updateSoal(updatedSoal)
                    Toast.makeText(context, "Soal berhasil diperbarui", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun createSoalFormLayout(): LinearLayout {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 32, 48, 16)
        }

        val etPertanyaan = EditText(requireContext()).apply { hint = "Pertanyaan" }
        val etPilihan1 = EditText(requireContext()).apply { hint = "Pilihan 1 (jawaban benar)" }
        val etPilihan2 = EditText(requireContext()).apply { hint = "Pilihan 2" }
        val etPilihan3 = EditText(requireContext()).apply { hint = "Pilihan 3" }
        val etPilihan4 = EditText(requireContext()).apply { hint = "Pilihan 4" }

        val imgPreview = ImageView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 300
            ).apply { topMargin = 16 }
            scaleType = ImageView.ScaleType.CENTER_CROP
            visibility = View.GONE
        }
        imagePreview = imgPreview

        val tvPilihGambar = TextView(requireContext()).apply {
            text = "Pilih Gambar (opsional)"
            setPadding(0, 24, 0, 8)
            setTextColor(requireContext().getColor(R.color.green_primary))
            setOnClickListener {
                val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
                pickImage.launch(intent)
            }
        }

        layout.addView(etPertanyaan)
        layout.addView(etPilihan1)
        layout.addView(etPilihan2)
        layout.addView(etPilihan3)
        layout.addView(etPilihan4)
        layout.addView(tvPilihGambar)
        layout.addView(imgPreview)

        layout.tag = SoalFormViews(etPertanyaan, etPilihan1, etPilihan2, etPilihan3, etPilihan4)
        return layout
    }

    private data class SoalFormViews(
        val etPertanyaan: EditText,
        val etPilihan1: EditText,
        val etPilihan2: EditText,
        val etPilihan3: EditText,
        val etPilihan4: EditText
    )

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
