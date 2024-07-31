package com.niki.cloudmusic.view.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.niki.cloudmusic.databinding.FragmentMyBinding
import com.niki.cloudmusic.repository.model.Song
import com.niki.cloudmusic.view.recyclerviews.SongAdapter
import com.niki.cloudmusic.viewmodel.LoginViewModel
import com.niki.cloudmusic.viewmodel.MusicViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 我的 TODO 图片缓存至本地
 **/
class MyFragment : DefaultFragment() {
    private lateinit var binding: FragmentMyBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var songAdapter: SongAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private var list: MutableList<Song> = mutableListOf()

    private val musicViewModel: MusicViewModel by activityViewModels<MusicViewModel>()
    private val loginViewModel: LoginViewModel by activityViewModels<LoginViewModel>()

    private var page: Int = 0
    private var noMore = false
    private var isLoading = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMyBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        binding.viewModel = loginViewModel

        val avatarView = binding.userAvatar
        val backgroundView = binding.background
        val nickname = binding.nickname
        val logout = binding.logout

        recyclerView = binding.recyclerView

        songAdapter = SongAdapter(musicViewModel)
        recyclerView.adapter = songAdapter

        layoutManager = LinearLayoutManager(
            requireActivity(),
            LinearLayoutManager.VERTICAL,
            false
        )
        recyclerView.layoutManager = layoutManager

        recyclerView.addItemDecoration(
            DividerItemDecoration(
                requireActivity(),
                DividerItemDecoration.VERTICAL
            )
        )

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE)
                    loadMore()
            }
        })

        /**
         *  监听登录状态以刷新界面
         */
        loginViewModel.isLoggedIn.observe(this.viewLifecycleOwner) { value ->
            avatarView.visibility = View.INVISIBLE
            backgroundView.visibility = View.INVISIBLE
            when (value) {
                0 -> {
                    logout.apply {
                        text = "点击登录"
                        setOnClickListener {
                            val loginFragment = LoginFragment()
                            loginFragment.show(childFragmentManager, "loginDialogFragment")
                        }
                    }
                    nickname.text = "未登录"
                    // 退出登录后把收藏歌单清除
                    loginViewModel.localList = mutableListOf()
                    songAdapter.submitList(mutableListOf())
                }

                1 -> {
                    loadAll()
                    logout.text = "登出"
                    loginViewModel.getLoginData()?.let {
                        // 头像
                        avatarView.run {
                            visibility = View.VISIBLE
                            load(it.profile!!.avatarUrl) {
                                transformations(CircleCropTransformation())   // 一个圆形图案的依赖
                            }
                        }
                        // 背景
                        backgroundView.run {
                            visibility = View.VISIBLE
                            load(it.profile!!.backgroundUrl)
                        }
                        // 用户名
                        nickname.text = it.profile!!.nickname
                    }
                }
            }
        }
    }

    /**
     * 从api获取全部收藏歌曲
     */
    private fun loadAll() {
        if (loginViewModel.isLoggedIn.value == 1 && loginViewModel.localList.isEmpty())
            musicViewModel.getLikeList { data ->
                Log.d("MyFragment", "getLikeList: finished")
                if (data != null) {
                    loginViewModel.localList = data.songs!!
                    loadMore()
                }
            }
    }

    /**
     * 分批加载已经存储的收藏歌单，以免卡顿
     */
    private fun loadMore() {
        if (isLoading) return
        if (noMore) {
            Toast.makeText(requireContext(), "已全部加载", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            isLoading = true
            val p = if (page == 0) 0 else page + 1
            for (i in p..page + 30) {
                if (i >= loginViewModel.localList.size - 1) {
                    noMore = true
                    break
                }
                list.add(loginViewModel.localList[i])
            }

            songAdapter.submitList(list)

            withContext(Dispatchers.IO) {
                if (!noMore)
                    page += 30
                Thread.sleep(300)
                isLoading = false
            }
        }
    }
}