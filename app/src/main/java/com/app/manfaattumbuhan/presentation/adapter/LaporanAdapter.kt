package com.app.manfaattumbuhan.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.manfaattumbuhan.databinding.ItemLaporanBinding
import com.app.manfaattumbuhan.domain.model.NilaiSiswa

class LaporanAdapter : ListAdapter<NilaiSiswa, LaporanAdapter.ViewHolder>(DiffCallback()) {

    class ViewHolder(val binding: ItemLaporanBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLaporanBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.tvNamaSiswa.text = item.namaSiswa
        holder.binding.tvTingkat.text = "Level: ${item.tingkat}"
        holder.binding.tvTanggal.text = item.tanggal
        holder.binding.tvNilai.text = item.nilai.toString()
        holder.binding.tvDetail.text = "${item.benar}/${item.totalSoal}"
    }

    class DiffCallback : DiffUtil.ItemCallback<NilaiSiswa>() {
        override fun areItemsTheSame(oldItem: NilaiSiswa, newItem: NilaiSiswa) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: NilaiSiswa, newItem: NilaiSiswa) = oldItem == newItem
    }
}
