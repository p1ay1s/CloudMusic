package com.niki.cloudmusic.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.niki.cloudmusic.databinding.FragmentListenBinding
import com.niki.cloudmusic.repository.model.Playlist
import com.niki.cloudmusic.view.activities.MainActivity
import com.niki.cloudmusic.view.recyclerviews.PlaylistAdapter
import com.niki.cloudmusic.view.recyclerviews.TagAdapter
import com.niki.cloudmusic.viewmodel.MusicViewModel

/**
 * 聆听
 **/
class ListenFragment : DefaultFragment() {
    private lateinit var binding: FragmentListenBinding

    private lateinit var playlistRecyclerView: RecyclerView
    private lateinit var tagRecyclerView: RecyclerView
    private lateinit var playlistListLayoutManager: LinearLayoutManager
    private lateinit var tagLayoutManager: LinearLayoutManager
    private lateinit var playlistAdapter: PlaylistAdapter
    private lateinit var tagAdapter: TagAdapter

    private val viewModel: MusicViewModel by activityViewModels<MusicViewModel>()

    private var playlistList: MutableList<Playlist> = mutableListOf()

    private var page = 0
    private var more = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentListenBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        playlistRecyclerView = binding.playlistRecyclerView
        tagRecyclerView = binding.recyclerView

        playlistListLayoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
        tagLayoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)

        /* 自动吸附 */
        PagerSnapHelper().attachToRecyclerView(playlistRecyclerView)
        PagerSnapHelper().attachToRecyclerView(tagRecyclerView)

        playlistRecyclerView.layoutManager = playlistListLayoutManager
        tagRecyclerView.layoutManager = tagLayoutManager

        /**
         * 传入的是回到listen fragment的函数
         */
        playlistAdapter = PlaylistAdapter { playlist ->
            val playlistFragment = PlaylistFragment(playlist)
            (activity as MainActivity).fragmentManagerHelper!!.openChildFragment(playlistFragment)
        }
        tagAdapter = TagAdapter(viewModel) { tag ->
            val tagFragment = TagFragment(tag)
            (activity as MainActivity).fragmentManagerHelper!!.openChildFragment(tagFragment)
        }

        playlistRecyclerView.adapter = playlistAdapter
        tagRecyclerView.adapter = tagAdapter

        loadPlaylists()
        loadTags()

        playlistRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    loadPlaylists()
                }
            }
        })
    }

    private fun loadPlaylists() {
        if (!more) {
            Toast.makeText(requireContext(), "已全部加载", Toast.LENGTH_SHORT).show()
            return
        }
        viewModel.getTopPlaylists(30, page) { data, more ->
            this@ListenFragment.more = more
            if (data != null) {
                playlistList.addAll(data.playlists!!)
                playlistAdapter.submitList(playlistList)
                page += 1
            } else {
                this@ListenFragment.more = false
            }
        }
    }

    private fun loadTags() {
        viewModel.getPlaylistByHot { data ->
            if (data != null)
                tagAdapter.submitList(data.tags)
        }
    }
}