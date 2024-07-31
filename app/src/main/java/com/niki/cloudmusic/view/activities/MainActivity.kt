package com.niki.cloudmusic.view.activities

import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import coil.load
import coil.transform.CircleCropTransformation
import com.niki.cloudmusic.MusicService
import com.niki.cloudmusic.MyBroadcastReceiver
import com.niki.cloudmusic.MyService
import com.niki.cloudmusic.R
import com.niki.cloudmusic.databinding.ActivityMainBinding
import com.niki.cloudmusic.repository.model.Song
import com.niki.cloudmusic.view.custom.InputDialog
import com.niki.cloudmusic.view.custom.LoadingDialog
import com.niki.cloudmusic.view.fragments.LoginFragment
import com.niki.cloudmusic.view.fragments.PlayerFragment
import com.niki.cloudmusic.view.fragments.PlaylistFragment
import com.niki.cloudmusic.view.fragments.TagFragment
import com.niki.cloudmusic.view.helpers.FragmentManagerHelper
import com.niki.cloudmusic.viewmodel.LoginViewModel
import com.niki.cloudmusic.viewmodel.MusicViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


const val ip = "10.33.74.45"
const val natapp = "http://niki-android-dev.natapp1.cc"

class MainActivity : AppCompatActivity() {
    private var myService: MyService? = null
    private var musicService: MusicService? = null
    private lateinit var binding: ActivityMainBinding
    private var musicStarted = false

    private lateinit var rotateAnimation: Animation

    var fragmentManagerHelper: FragmentManagerHelper? = null

    private lateinit var filter: IntentFilter
    private lateinit var receiver: MyBroadcastReceiver
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var musicViewModel: MusicViewModel

    /**
     * 测试函数
     * 是否主动设置baseUrl
     */
    private fun option(choice: Boolean) {
        /* 输入框获取baseurl */
        if (choice) {
            InputDialog(this).showDialog(
                "baseUrl:",
                "http://$ip:3000",
            ) { mChoice ->
                /* 必须先获取baseurl再检测登录状态，否则可能获取不了 */
                loginViewModel.preferencesRepository.saveBaseUrl(if (mChoice) natapp else "http://$ip:3000")
                loginViewModel.getLoginState()

                fragmentManagerHelper?.switchToFragment(0)
            }
        } else {
            loginViewModel.preferencesRepository.saveBaseUrl(natapp)
            loginViewModel.getLoginState()

            fragmentManagerHelper?.switchToFragment(0)
        }
    }

    /**
     * 初始化viewmodel 包括实例、设置alter dialog、初始化fragment manager helper
     */
    private fun basicallyInit() {
        loginViewModel = ViewModelProvider(this)[LoginViewModel::class.java]
        musicViewModel = ViewModelProvider(this)[MusicViewModel::class.java]

        loginViewModel.loadingDialog = LoadingDialog(this)
        musicViewModel.loadingDialog = LoadingDialog(this)

        fragmentManagerHelper = FragmentManagerHelper(supportFragmentManager, R.id.frameLayout)

        // 初始化filter 动态注册接收器
        // 要exported
        filter = IntentFilter()
        filter.addAction("com.niki.cloudmusic.ACTION_PLAY")
        filter.addAction("com.niki.cloudmusic.ACTION_PREVIOUS")
        filter.addAction("com.niki.cloudmusic.ACTION_NEXT")

        receiver = MyBroadcastReceiver(musicViewModel)
        registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)

        // 旋转动画
        rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate_anim)
        rotateAnimation.interpolator = LinearInterpolator()
    }

    /**
     * 处于特殊fragment的返回事件
     */
    override fun onBackPressed() {
        val currentFragment = fragmentManagerHelper!!.getCurrentFragment()
        when (currentFragment) {
            is TagFragment -> {
                fragmentManagerHelper!!.dealWithFirstPage()
            }

            is PlaylistFragment -> {
                fragmentManagerHelper!!.dealWithFirstPage()
            }

            else -> {
                super.onBackPressed()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setContentView(binding.root)

        basicallyInit()

        val musicIntent = Intent(this, MusicService::class.java).also { intent ->
            bindService(intent, MusicConnection(), Context.BIND_AUTO_CREATE)
        }
        startService(musicIntent)

        option(true)

        /**
         *  当前歌曲监听-歌名、图片刷新
         *  通知栏播放器启动
         */
        musicViewModel.currentSong.observe(this) { song ->
            if (song != null) {
                binding.cover.load(song.al!!.picUrl) {
                    transformations(
                        CircleCropTransformation()
                    )
                }
                binding.name.text = song.name

                // 第一次播放音乐时启动通知栏控制器
                if (!musicStarted) {
                    val intent = Intent(this, MyService::class.java).also { intent ->
                        bindService(intent, MyConnection(), Context.BIND_AUTO_CREATE)
                    }
                    startService(intent)
                    musicStarted = true
                }
            }
        }

        /**
         * 播放-暂停图片切换
         * 封面旋转控制
         */
        musicViewModel.isPlaying.observe(this) { isPlaying ->
            binding.play.load(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play)
            if (isPlaying) {
                binding.cover.startAnimation(rotateAnimation)
            } else {
                binding.cover.animation.cancel()
            }
        }

        /**
         * 播放器点击事件：弹起 alter dialog fragment
         */
        binding.apply {
            viewModel = musicViewModel
            player.setOnClickListener {
                if (musicViewModel.currentSong.value != null) {
                    val playerFragment = PlayerFragment()
                    playerFragment.show(supportFragmentManager, "playerFragment")
                }
            }
            play.setOnClickListener {
                musicViewModel.switchStatus()
            }
        }

        /* 用navigation实现底部导航 */
        val bottomNav = binding.bottomNav
        bottomNav.setOnItemSelectedListener { item ->
            if (fragmentManagerHelper == null) return@setOnItemSelectedListener false

            when (item.itemId) {
                R.id.menu_listen -> {
                    fragmentManagerHelper!!.dealWithFirstPage()
                    true
                }

                R.id.menu_my -> {
                    if (!loginViewModel.preferencesRepository.getLoginStatus()) {
                        LoginFragment().show(supportFragmentManager, "loginFragment")
                        false
                    } else {
                        fragmentManagerHelper!!.switchToFragment(1)
                        true
                    }
                }

                R.id.menu_search -> {
                    fragmentManagerHelper!!.switchToFragment(2)
                    true
                }

                else -> false
            }
        }
    }

    private fun refreshRemotePlayer(song: Song) {
        if (myService == null) return
        GlobalScope.launch(Dispatchers.Main) {
            myService!!.setSong(song) {
                updateRemoteView()
            }
        }
    }

    private fun refreshPlayingStatus(isPlaying: Boolean) {
        if (myService == null) return
        myService!!.setPLayingStatus(isPlaying)
        updateRemoteView()
    }

    private fun updateRemoteView() {
        val notification = myService!!.createNotification()
        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(123, notification)
    }

    inner class MyConnection : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as MyService.MyBinder
            myService = binder.getService()
            musicViewModel.apply {
                currentSong.observe(this@MainActivity) { song ->
                    refreshRemotePlayer(song)
                }
                isPlaying.observe(this@MainActivity) { isPlaying ->
                    refreshPlayingStatus(isPlaying)
                }
            }
        }

        override fun onServiceDisconnected(className: ComponentName) {
            myService = null
        }
    }

    inner class MusicConnection : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as MusicService.MusicBinder
            musicService = binder.getService()
            musicViewModel.updateMusicProgress = { binder.updateMusicProgress() }
            musicViewModel.seekTo = { position -> binder.seekTo(position) }
            musicViewModel.playMusic = { url -> binder.playMusic(url) }
            musicViewModel.switchStatus = { binder.switchStatus() }
        }

        override fun onServiceDisconnected(className: ComponentName) {
            musicService = null
        }
    }
}