package com.app.manfaattumbuhan.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.manfaattumbuhan.databinding.ItemRiwayatNilaiBinding
import com.app.manfaattumbuhan.domain.model.NilaiSiswa

class RiwayatNilaiAdapter : ListAdapter<NilaiSiswa, RiwayatNilaiAdapter.ViewHolder>(DiffCallback()) {

    class ViewHolder(val binding: ItemRiwayatNilaiBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRiwayatNilaiBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.tvTingkat.text = item.tingkat
        holder.binding.tvDetail.text = "Benar ${item.benar} dari ${item.totalSoal} soal"
        holder.binding.tvTanggal.text = item.tanggal
        holder.binding.tvNilai.text = item.nilai.toString()
    }

    class DiffCallback : DiffUtil.ItemCallback<NilaiSiswa>() {
        override fun areItemsTheSame(oldItem: NilaiSiswa, newItem: NilaiSiswa) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: NilaiSiswa, newItem: NilaiSiswa) = oldItem == newItem
    }
}
