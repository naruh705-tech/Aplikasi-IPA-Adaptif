package com.app.manfaattumbuhan.presentation.guru.soal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.manfaattumbuhan.R
import com.app.manfaattumbuhan.data.local.TokenManager
import com.app.manfaattumbuhan.data.remote.model.SoalApi
import com.app.manfaattumbuhan.databinding.FragmentKelolaSoalBinding
import com.app.manfaattumbuhan.presentation.adapter.SoalGuruAdapter

class KelolaSoalFragment : Fragment() {

    private var _binding: FragmentKelolaSoalBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: KelolaSoalViewModel
    private lateinit var adapter: SoalGuruAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKelolaSoalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        TokenManager.init(requireContext())
        viewModel = ViewModelProvider(this, KelolaSoalViewModelFactory())[KelolaSoalViewModel::class.java]

        setupRecyclerView()
        setupListeners()
        observeData()
        viewModel.loadSoal()
    }

    private fun setupRecyclerView() {
        adapter = SoalGuruAdapter(
            onEdit = { soal -> showEditDialog(soal) },
            onDelete = { soal -> showDeleteConfirmation(soal) }
        )
        binding.rvSoal.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSoal.adapter = adapter
    }

    private fun setupListeners() {
        binding.imgProfile.setOnClickListener {
            findNavController().navigate(R.id.action_soal_to_profil)
        }

        binding.btnBuatSoal.setOnClickListener {
            showCreateDialog()
        }
    }

    private fun observeData() {
        viewModel.soalList.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            binding.tvSoalCount.text = "${list.size} soal"
            binding.tvEmptyState.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { err ->
            err?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show() }
        }
    }

    private fun showCreateDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_soal, null)
        val etJudul = dialogView.findViewById<EditText>(R.id.etJudul)
        val etDeskripsi = dialogView.findViewById<EditText>(R.id.etDeskripsi)
        val etFotoUrl = dialogView.findViewById<EditText>(R.id.etFotoUrl)
        val etVideoUrl = dialogView.findViewById<EditText>(R.id.etVideoUrl)

        AlertDialog.Builder(requireContext())
            .setTitle("Buat Soal Baru")
            .setView(dialogView)
            .setPositiveButton("Simpan") { _, _ ->
                val judul = etJudul.text.toString().trim()
                val deskripsi = etDeskripsi.text.toString().trim()
                val fotoUrl = etFotoUrl.text.toString().trim().ifBlank { null }
                val videoUrl = etVideoUrl.text.toString().trim().ifBlank { null }

                if (judul.isBlank() || deskripsi.isBlank()) {
                    Toast.makeText(requireContext(), "Judul dan deskripsi harus diisi", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                viewModel.addSoal(judul, deskripsi, fotoUrl, videoUrl)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showEditDialog(soal: SoalApi) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_soal, null)
        val etJudul = dialogView.findViewById<EditText>(R.id.etJudul)
        val etDeskripsi = dialogView.findViewById<EditText>(R.id.etDeskripsi)
        val etFotoUrl = dialogView.findViewById<EditText>(R.id.etFotoUrl)
        val etVideoUrl = dialogView.findViewById<EditText>(R.id.etVideoUrl)

        etJudul.setText(soal.judul)
        etDeskripsi.setText(soal.deskripsi)
        etFotoUrl.setText(soal.foto_url ?: "")
        etVideoUrl.setText(soal.video_url ?: "")

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Soal")
            .setView(dialogView)
            .setPositiveButton("Simpan") { _, _ ->
                val judul = etJudul.text.toString().trim()
                val deskripsi = etDeskripsi.text.toString().trim()
                val fotoUrl = etFotoUrl.text.toString().trim().ifBlank { null }
                val videoUrl = etVideoUrl.text.toString().trim().ifBlank { null }

                if (judul.isBlank() || deskripsi.isBlank()) {
                    Toast.makeText(requireContext(), "Judul dan deskripsi harus diisi", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                viewModel.updateSoal(soal.id, judul, deskripsi, fotoUrl, videoUrl)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showDeleteConfirmation(soal: SoalApi) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Soal")
            .setMessage("Yakin ingin menghapus soal \"${soal.judul}\"?")
            .setPositiveButton("Hapus") { _, _ -> viewModel.deleteSoal(soal.id) }
            .setNegativeButton("Batal", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
