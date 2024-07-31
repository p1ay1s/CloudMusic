package com.niki.cloudmusic.view.recyclerviews

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.niki.cloudmusic.databinding.LayoutPlaylistBinding
import com.niki.cloudmusic.repository.model.Playlist

class PlaylistAdapter(val callback: (Playlist) -> Unit) :
    ListAdapter<Playlist, PlaylistAdapter.ViewHolder>(PlaylistCallback()) {
    class ViewHolder(val binding: LayoutPlaylistBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            LayoutPlaylistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//        binding.root.layoutParams.width =
//            (parent.context.resources.displayMetrics.widthPixels * 0.9).toInt()
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val playlist = getItem(position)

        holder.binding.let {
            it.playlist = playlist
            it.cover.load(playlist.coverImgUrl)
            it.root.setOnClickListener {
                callback(playlist)
            }
            it.executePendingBindings()
        }
    }

    class PlaylistCallback : DiffUtil.ItemCallback<Playlist>() {
        override fun areItemsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
            return oldItem == newItem
        }

    }
}