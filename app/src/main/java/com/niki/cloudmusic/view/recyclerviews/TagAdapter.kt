package com.niki.cloudmusic.view.recyclerviews

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.niki.cloudmusic.databinding.LayoutTagBinding
import com.niki.cloudmusic.repository.model.Tag
import com.niki.cloudmusic.viewmodel.MusicViewModel

class TagAdapter(val viewModel: MusicViewModel, val callback: (Tag) -> Unit) :
    ListAdapter<Tag, TagAdapter.ViewHolder>(TagCallback()) {
    class ViewHolder(val binding: LayoutTagBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = LayoutTagBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tag = getItem(position)

        viewModel.getSongsFromPlaylist(tag.id, 3, 0) { data ->
            if (data != null) {
                holder.binding.let {
                    it.tag = tag
                    it.root.setOnClickListener {
                        callback(tag)
                    }
                    SongAdapter.setSongItem(viewModel, it.song1, data.songs!![0], null)
                    SongAdapter.setSongItem(viewModel, it.song2, data.songs!![1], null)
                    SongAdapter.setSongItem(viewModel, it.song3, data.songs!![2], null)
                }
            }
        }

    }

    class TagCallback : DiffUtil.ItemCallback<Tag>() {
        override fun areItemsTheSame(oldItem: Tag, newItem: Tag): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Tag, newItem: Tag): Boolean {
            return oldItem == newItem
        }
    }
}