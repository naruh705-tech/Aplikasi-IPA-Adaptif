package com.app.manfaattumbuhan.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.manfaattumbuhan.databinding.ItemSoalGuruBinding
import com.app.manfaattumbuhan.domain.model.Soal

class SoalGuruAdapter(
    private val onEdit: (Soal) -> Unit,
    private val onDelete: (Soal) -> Unit
) : ListAdapter<Soal, SoalGuruAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemSoalGuruBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(soal: Soal) {
            binding.tvPertanyaan.text = soal.pertanyaan
            binding.tvModul.text = "Modul: ${soal.modul}"
            binding.tvTingkat.text = soal.tingkatKesulitan
            binding.tvTerakhirDiubah.text = "Terakhir diubah: Hari ini"

            binding.btnEdit.setOnClickListener { onEdit(soal) }
            binding.btnDelete.setOnClickListener { onDelete(soal) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSoalGuruBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<Soal>() {
        override fun areItemsTheSame(oldItem: Soal, newItem: Soal) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Soal, newItem: Soal) = oldItem == newItem
    }
}
