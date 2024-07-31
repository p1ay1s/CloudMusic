package com.niki.cloudmusic.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.niki.cloudmusic.databinding.FragmentTagBinding
import com.niki.cloudmusic.repository.model.Song
import com.niki.cloudmusic.repository.model.Tag
import com.niki.cloudmusic.view.activities.MainActivity
import com.niki.cloudmusic.view.recyclerviews.SongAdapter
import com.niki.cloudmusic.viewmodel.MusicViewModel

class TagFragment(val tag: Tag) :
    DefaultFragment() {
    private lateinit var binding: FragmentTagBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var songAdapter: SongAdapter
    private lateinit var layoutManager: LinearLayoutManager

    private var list: MutableList<Song> = mutableListOf()

    private val viewModel: MusicViewModel by activityViewModels<MusicViewModel>()

    private var page = 0
    private var more = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTagBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = binding.recyclerView

        songAdapter = SongAdapter(viewModel)
        recyclerView.adapter = songAdapter

        layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
        recyclerView.layoutManager = layoutManager

        recyclerView.addItemDecoration(
            DividerItemDecoration(
                requireActivity(),
                DividerItemDecoration.VERTICAL
            )
        )

        binding.actionBar.apply {
            title.text = tag.name
            back.setOnClickListener {
                (activity as MainActivity).fragmentManagerHelper!!.dealWithFirstPage()
            }
        }

        loadMore()

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    loadMore()
                }
            }
        })
    }

    private fun loadMore() {
        if (!more) {
            Toast.makeText(requireContext(), "已全部加载", Toast.LENGTH_SHORT).show()
            return
        }
        viewModel.getSongsFromPlaylist(tag.id, 30, page) { data ->
            if (data != null) {
                list.addAll(data.songs!!)
                songAdapter.submitList(list)
                page += 1
            } else {
                more = false
            }
        }
    }
}
