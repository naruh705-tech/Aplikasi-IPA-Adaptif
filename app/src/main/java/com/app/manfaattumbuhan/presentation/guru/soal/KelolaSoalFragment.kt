package com.app.manfaattumbuhan.presentation.guru.soal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
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
import org.json.JSONArray
import org.json.JSONObject

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

    private fun setupSpinner(spinner: Spinner, selectedIndex: Int = 0) {
        val options = listOf("A", "B", "C", "D")
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, options)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = spinnerAdapter
        if (selectedIndex in options.indices) {
            spinner.setSelection(selectedIndex)
        }
    }

    private fun buildDeskripsiJson(pilihan: List<String>, jawabanBenar: Int): String {
        val json = JSONObject()
        val pilihanArray = JSONArray()
        pilihan.forEach { pilihanArray.put(it) }
        json.put("pilihan", pilihanArray)
        json.put("jawabanBenar", jawabanBenar)
        return json.toString()
    }

    private fun parseDeskripsiJson(deskripsi: String): Pair<List<String>, Int>? {
        return try {
            val json = JSONObject(deskripsi)
            val pilihanArray = json.getJSONArray("pilihan")
            val pilihan = mutableListOf<String>()
            for (i in 0 until pilihanArray.length()) {
                pilihan.add(pilihanArray.getString(i))
            }
            val jawabanBenar = json.getInt("jawabanBenar")
            Pair(pilihan, jawabanBenar)
        } catch (e: Exception) {
            null
        }
    }

    private fun showCreateDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_soal, null)
        val etJudul = dialogView.findViewById<EditText>(R.id.etJudul)
        val etFotoUrl = dialogView.findViewById<EditText>(R.id.etFotoUrl)
        val etVideoUrl = dialogView.findViewById<EditText>(R.id.etVideoUrl)
        val etPilihanA = dialogView.findViewById<EditText>(R.id.etPilihanA)
        val etPilihanB = dialogView.findViewById<EditText>(R.id.etPilihanB)
        val etPilihanC = dialogView.findViewById<EditText>(R.id.etPilihanC)
        val etPilihanD = dialogView.findViewById<EditText>(R.id.etPilihanD)
        val spinnerJawaban = dialogView.findViewById<Spinner>(R.id.spinnerJawaban)

        setupSpinner(spinnerJawaban)

        AlertDialog.Builder(requireContext())
            .setTitle("Buat Soal Baru")
            .setView(dialogView)
            .setPositiveButton("Simpan") { _, _ ->
                val judul = etJudul.text.toString().trim()
                val fotoUrl = etFotoUrl.text.toString().trim().ifBlank { null }
                val videoUrl = etVideoUrl.text.toString().trim().ifBlank { null }
                val pilihanA = etPilihanA.text.toString().trim()
                val pilihanB = etPilihanB.text.toString().trim()
                val pilihanC = etPilihanC.text.toString().trim()
                val pilihanD = etPilihanD.text.toString().trim()
                val jawabanBenar = spinnerJawaban.selectedItemPosition

                if (judul.isBlank()) {
                    Toast.makeText(requireContext(), "Teks soal harus diisi", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val pilihan = listOf(pilihanA, pilihanB, pilihanC, pilihanD).filter { it.isNotBlank() }
                if (pilihan.size < 2) {
                    Toast.makeText(requireContext(), "Minimal 2 pilihan jawaban", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val deskripsi = buildDeskripsiJson(pilihan, jawabanBenar)
                viewModel.addSoal(judul, deskripsi, fotoUrl, videoUrl)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showEditDialog(soal: SoalApi) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_soal, null)
        val etJudul = dialogView.findViewById<EditText>(R.id.etJudul)
        val etFotoUrl = dialogView.findViewById<EditText>(R.id.etFotoUrl)
        val etVideoUrl = dialogView.findViewById<EditText>(R.id.etVideoUrl)
        val etPilihanA = dialogView.findViewById<EditText>(R.id.etPilihanA)
        val etPilihanB = dialogView.findViewById<EditText>(R.id.etPilihanB)
        val etPilihanC = dialogView.findViewById<EditText>(R.id.etPilihanC)
        val etPilihanD = dialogView.findViewById<EditText>(R.id.etPilihanD)
        val spinnerJawaban = dialogView.findViewById<Spinner>(R.id.spinnerJawaban)

        etJudul.setText(soal.judul)
        etFotoUrl.setText(soal.foto_url ?: "")
        etVideoUrl.setText(soal.video_url ?: "")

        val parsed = parseDeskripsiJson(soal.deskripsi)
        if (parsed != null) {
            val (pilihan, jawabanBenar) = parsed
            if (pilihan.size > 0) etPilihanA.setText(pilihan[0])
            if (pilihan.size > 1) etPilihanB.setText(pilihan[1])
            if (pilihan.size > 2) etPilihanC.setText(pilihan[2])
            if (pilihan.size > 3) etPilihanD.setText(pilihan[3])
            setupSpinner(spinnerJawaban, jawabanBenar)
        } else {
            setupSpinner(spinnerJawaban)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Soal")
            .setView(dialogView)
            .setPositiveButton("Simpan") { _, _ ->
                val judul = etJudul.text.toString().trim()
                val fotoUrl = etFotoUrl.text.toString().trim().ifBlank { null }
                val videoUrl = etVideoUrl.text.toString().trim().ifBlank { null }
                val pilihanA = etPilihanA.text.toString().trim()
                val pilihanB = etPilihanB.text.toString().trim()
                val pilihanC = etPilihanC.text.toString().trim()
                val pilihanD = etPilihanD.text.toString().trim()
                val jawabanBenar = spinnerJawaban.selectedItemPosition

                if (judul.isBlank()) {
                    Toast.makeText(requireContext(), "Teks soal harus diisi", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val pilihan = listOf(pilihanA, pilihanB, pilihanC, pilihanD).filter { it.isNotBlank() }
                if (pilihan.size < 2) {
                    Toast.makeText(requireContext(), "Minimal 2 pilihan jawaban", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val deskripsi = buildDeskripsiJson(pilihan, jawabanBenar)
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
