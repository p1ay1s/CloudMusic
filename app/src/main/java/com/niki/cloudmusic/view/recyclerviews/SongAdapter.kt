package com.niki.cloudmusic.view.recyclerviews

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.niki.cloudmusic.databinding.LayoutSongBinding
import com.niki.cloudmusic.repository.model.Song
import com.niki.cloudmusic.viewmodel.MusicViewModel

class SongAdapter(var viewModel: MusicViewModel) :
    ListAdapter<Song, SongAdapter.ViewHolder>(SongDiffCallback()) {

    class ViewHolder(val binding: LayoutSongBinding) :
        RecyclerView.ViewHolder(binding.root)

    companion object {
        fun setSongItem(
            viewModel: MusicViewModel?,
            binding: LayoutSongBinding,
            song: Song,
            list: List<Song>?
        ) {
            binding.let {
                it.song = song
                if (viewModel != null) {
                    it.root.setOnClickListener {
                        if (!list.isNullOrEmpty()) viewModel.setNewSongList(
                            list.toMutableList(),
                            list.indexOf(song)
                        )
                        viewModel.checkSong(song)
                    }
                    it.cover.load(song.al!!.picUrl)
                }
                it.more.setOnClickListener {
                    viewModel!!.addSongToNext(song)
                }
                if (song.mark == 1048576L)
                    it.explicit.visibility = View.VISIBLE

                it.executePendingBindings()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = LayoutSongBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var song = getItem(position)
        if (song == null)
            song = Song(null, null, null, null, null, null)

        setSongItem(viewModel, holder.binding, song, currentList)
    }

    class SongDiffCallback : DiffUtil.ItemCallback<Song>() {
        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem == newItem
        }
    }
}