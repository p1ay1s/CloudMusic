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
import com.niki.cloudmusic.databinding.FragmentSearchBinding
import com.niki.cloudmusic.repository.model.Song
import com.niki.cloudmusic.view.recyclerviews.SongAdapter
import com.niki.cloudmusic.viewmodel.MusicViewModel

/**
 * 搜索
 **/
class SearchFragment : DefaultFragment() {
    private lateinit var binding: FragmentSearchBinding

    private var page: Int = 0
    private var more = false
    private var isNewContent = false
    private var currentContent = ""

    private val viewModel: MusicViewModel by activityViewModels<MusicViewModel>()

    private lateinit var recyclerView: RecyclerView
    private lateinit var songAdapter: SongAdapter
    private lateinit var layoutManager: LinearLayoutManager

    private var list: MutableList<Song> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel
        recyclerView = binding.recyclerView

        songAdapter = SongAdapter(viewModel)
        layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)

        recyclerView.addItemDecoration(
            DividerItemDecoration(
                requireActivity(),
                DividerItemDecoration.VERTICAL
            )
        )
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = songAdapter

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    loadResult()
                }
            }
        })

        viewModel.keywords.observe(this.viewLifecycleOwner) { keywords ->
            currentContent = keywords
            isNewContent = true
            loadResult()
        }
    }

    private fun loadResult() {
        /**
         * 是否改变了搜索内容
         */
        if (isNewContent) {
            page = 0
            list = mutableListOf()
            songAdapter.submitList(list)
            isNewContent = false
            viewModel.searchSongs(30, page, currentContent) { data, more ->
                this@SearchFragment.more = more
                if (data != null) {
                    list.addAll(data.songs!!)
                    songAdapter.submitList(list)
                    page += 1
                }
            }
        } else {
            if (!more) {
                Toast.makeText(requireContext(), "已全部加载", Toast.LENGTH_SHORT).show()
                return
            }
            viewModel.searchSongs(30, page, currentContent) { data, more ->
                this@SearchFragment.more = more
                if (data != null) {
                    list.addAll(data.songs!!)
                    songAdapter.submitList(list)
                    page += 1
                }
            }
        }
    }
}