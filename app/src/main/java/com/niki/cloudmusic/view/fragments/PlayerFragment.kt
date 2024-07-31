package com.niki.cloudmusic.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.activityViewModels
import coil.load
import com.niki.cloudmusic.R
import com.niki.cloudmusic.databinding.FragmentPlayerBinding
import com.niki.cloudmusic.view.custom.DrawerFragment
import com.niki.cloudmusic.viewmodel.MusicViewModel

class PlayerFragment :
    DrawerFragment(1.0, R.layout.fragment_player) {

    private lateinit var binding: FragmentPlayerBinding

    private val viewModel: MusicViewModel by activityViewModels<MusicViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPlayerBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.let {
            it.viewModel = viewModel
            it.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    if (fromUser)
                        viewModel.seekTo(progress)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {}

                override fun onStopTrackingTouch(seekBar: SeekBar) {}
            })
        }

        viewModel.isPlaying.observe(this) { isPlaying ->
            binding.play.load(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play)
        }

        viewModel.updateMusicProgress()


        viewModel.playMode.observe(this) { mode ->
            val drawable: Int = when (mode) {
                viewModel.bySingle -> R.drawable.ic_single
                viewModel.byRandom -> R.drawable.ic_random
                viewModel.byLoop -> R.drawable.ic_loop
                else -> R.drawable.ic_loop
            }
            binding.mode.load(drawable)
        }

        viewModel.currentSong.observe(this) { song ->
            if (song != null) {
                binding.apply {
                    name.text = song.name
                    val builder = StringBuilder()
                    for (artist in song.ar!!) {
                        if (artist.name.isNotBlank()) {
                            builder.append(artist.name)
                        }
                        if (song.ar!!.indexOf(artist) != song.ar!!.size - 1) {
                            builder.append(" & ")
                        }
                    }
                    singer.text = builder.toString()
                    cover.load(song.al!!.picUrl)
                }
            }
        }

        viewModel.songPosition.observe(this) { value ->
            binding.seekBar.progress = value
        }
    }
}