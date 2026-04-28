package com.app.manfaattumbuhan.presentation.guru.soal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.manfaattumbuhan.data.repository.SoalRepositoryImpl
import com.app.manfaattumbuhan.databinding.FragmentKelolaSoalBinding
import com.app.manfaattumbuhan.domain.model.Soal
import com.app.manfaattumbuhan.domain.usecase.GetSoalUseCase
import com.app.manfaattumbuhan.presentation.adapter.SoalGuruAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class KelolaSoalFragment : Fragment() {

    private var _binding: FragmentKelolaSoalBinding? = null
    private val binding get() = _binding!!
    private val viewModel: KelolaSoalViewModel by viewModels {
        KelolaSoalViewModelFactory(GetSoalUseCase(SoalRepositoryImpl()))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKelolaSoalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = SoalGuruAdapter(
            onEdit = { /* placeholder for edit */ },
            onDelete = { soal ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Hapus Soal")
                    .setMessage("Apakah Anda yakin ingin menghapus soal ini?")
                    .setPositiveButton("Hapus") { _, _ ->
                        viewModel.deleteSoal(soal.id)
                    }
                    .setNegativeButton("Batal", null)
                    .show()
            }
        )

        binding.rvSoal.layoutManager = LinearLayoutManager(context)
        binding.rvSoal.adapter = adapter

        viewModel.loadSoal()
        viewModel.soalList.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
        }

        binding.fabBuatSoal.setOnClickListener {
            showAddSoalDialog()
        }
    }

    private fun showAddSoalDialog() {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 32, 48, 16)
        }

        val etPertanyaan = EditText(requireContext()).apply {
            hint = "Pertanyaan"
        }
        val etPilihan1 = EditText(requireContext()).apply { hint = "Pilihan 1 (benar)" }
        val etPilihan2 = EditText(requireContext()).apply { hint = "Pilihan 2" }
        val etPilihan3 = EditText(requireContext()).apply { hint = "Pilihan 3" }
        val etPilihan4 = EditText(requireContext()).apply { hint = "Pilihan 4" }

        layout.addView(etPertanyaan)
        layout.addView(etPilihan1)
        layout.addView(etPilihan2)
        layout.addView(etPilihan3)
        layout.addView(etPilihan4)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Buat Soal Baru")
            .setView(layout)
            .setPositiveButton("Simpan") { _, _ ->
                val pertanyaan = etPertanyaan.text.toString()
                val pilihan = listOf(
                    etPilihan1.text.toString(),
                    etPilihan2.text.toString(),
                    etPilihan3.text.toString(),
                    etPilihan4.text.toString()
                )
                if (pertanyaan.isNotBlank() && pilihan.all { it.isNotBlank() }) {
                    val newSoal = Soal(
                        id = System.currentTimeMillis().toInt(),
                        pertanyaan = pertanyaan,
                        pilihan = pilihan,
                        jawabanBenar = 0,
                        modul = "Manfaat Tumbuhan",
                        tingkatKesulitan = "Sedang"
                    )
                    viewModel.addSoal(newSoal)
                    Toast.makeText(context, "Soal berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
