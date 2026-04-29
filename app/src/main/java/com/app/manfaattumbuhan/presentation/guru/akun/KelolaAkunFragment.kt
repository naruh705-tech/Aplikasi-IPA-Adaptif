package com.app.manfaattumbuhan.presentation.guru.akun

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.manfaattumbuhan.R
import com.app.manfaattumbuhan.data.repository.UserRepositoryImpl
import com.app.manfaattumbuhan.databinding.FragmentKelolaAkunBinding
import com.app.manfaattumbuhan.domain.model.User
import com.app.manfaattumbuhan.domain.usecase.GetSiswaUseCase
import com.app.manfaattumbuhan.presentation.adapter.SiswaAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class KelolaAkunFragment : Fragment() {

    private var _binding: FragmentKelolaAkunBinding? = null
    private val binding get() = _binding!!
    private val viewModel: KelolaAkunViewModel by viewModels {
        KelolaAkunViewModelFactory(GetSiswaUseCase(UserRepositoryImpl()))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKelolaAkunBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = SiswaAdapter(
            onEdit = { user -> showEditDialog(user) },
            onDelete = { user -> showDeleteDialog(user) }
        )
        binding.rvSiswa.layoutManager = LinearLayoutManager(context)
        binding.rvSiswa.adapter = adapter

        viewModel.loadSiswa()
        viewModel.siswaList.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            binding.tvJumlahSiswa.text = "${list.size} siswa"
        }

        binding.btnLihatLaporan.setOnClickListener {
            findNavController().navigate(R.id.laporanFragment)
        }

        binding.btnTambahSiswa.setOnClickListener {
            showAddDialog()
        }

        binding.imgProfile.setOnClickListener {
            findNavController().navigate(R.id.action_akun_to_profil)
        }
    }

    private fun showAddDialog() {
        val layout = createUserFormLayout()
        val views = layout.tag as UserFormViews

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Tambah Siswa Baru")
            .setView(layout)
            .setPositiveButton("Simpan") { _, _ ->
                val nama = views.etNama.text.toString()
                val username = views.etUsername.text.toString()
                val password = views.etPassword.text.toString()
                val kelas = views.etKelas.text.toString()

                if (nama.isNotBlank() && username.isNotBlank() && password.isNotBlank()) {
                    viewModel.addSiswa(nama, username, password, kelas)
                    Toast.makeText(context, "Siswa berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Semua field wajib diisi", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showEditDialog(user: User) {
        val layout = createUserFormLayout()
        val views = layout.tag as UserFormViews

        views.etNama.setText(user.nama)
        views.etUsername.setText(user.username)
        views.etPassword.setText("***")
        views.etKelas.setText(user.kelas)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Edit Siswa")
            .setView(layout)
            .setPositiveButton("Simpan") { _, _ ->
                val nama = views.etNama.text.toString()
                val kelas = views.etKelas.text.toString()

                if (nama.isNotBlank()) {
                    val updatedUser = user.copy(nama = nama, kelas = kelas)
                    viewModel.updateSiswa(updatedUser)
                    Toast.makeText(context, "Siswa berhasil diperbarui", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showDeleteDialog(user: User) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Hapus Siswa")
            .setMessage("Apakah Anda yakin ingin menghapus ${user.nama}?")
            .setPositiveButton("Hapus") { _, _ ->
                viewModel.deleteSiswa(user.id)
                Toast.makeText(context, "Siswa berhasil dihapus", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun createUserFormLayout(): LinearLayout {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 32, 48, 16)
        }

        val etNama = EditText(requireContext()).apply { hint = "Nama Lengkap" }
        val etUsername = EditText(requireContext()).apply { hint = "Username" }
        val etPassword = EditText(requireContext()).apply { hint = "Password" }
        val etKelas = EditText(requireContext()).apply { hint = "Kelas" }

        layout.addView(etNama)
        layout.addView(etUsername)
        layout.addView(etPassword)
        layout.addView(etKelas)

        layout.tag = UserFormViews(etNama, etUsername, etPassword, etKelas)

        return layout
    }

    private data class UserFormViews(
        val etNama: EditText,
        val etUsername: EditText,
        val etPassword: EditText,
        val etKelas: EditText
    )

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
