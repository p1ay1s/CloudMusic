package com.niki.cloudmusic.viewmodel

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.MutableLiveData
import com.niki.cloudmusic.repository.MusicRepository
import com.niki.cloudmusic.repository.PlaylistRepository
import com.niki.cloudmusic.repository.SearchRepository
import com.niki.cloudmusic.repository.model.CatPlaylistResponse
import com.niki.cloudmusic.repository.model.HotPlaylistResponse
import com.niki.cloudmusic.repository.model.PlaylistSongsResponse
import com.niki.cloudmusic.repository.model.SingleSongResponse
import com.niki.cloudmusic.repository.model.Song
import com.niki.cloudmusic.repository.model.TopPlaylistResponse
import kotlin.random.Random

class MusicViewModel(application: Application) : BaseViewModel(application) {
    private var currentSongList: MutableList<Song>

    private var filter: IntentFilter
    private var receiver: MusicReceiver

    lateinit var updateMusicProgress: () -> Unit
    lateinit var seekTo: (Int) -> Unit
    lateinit var playMusic: (String) -> Unit
    lateinit var switchStatus: () -> Unit

    private var currentIndex: Int = 0
    private var isSearching = false

    val isPlaying = MutableLiveData<Boolean>()
    val songPosition = MutableLiveData<Int>()
    var currentSong = MutableLiveData<Song>()
    var playMode = MutableLiveData<Int>()

    /**
     * 绑定了搜索的edit text
     */
    var keywords = MutableLiveData<String>()

    /**
     * 播放模式
     */
    val bySingle = 0
    val byLoop = 1
    val byRandom = 2

    private val musicRepository: MusicRepository by lazy {
        MusicRepository(preferencesRepository.getBaseUrl())
    }

    private val playlistRepository: PlaylistRepository by lazy {
        PlaylistRepository(preferencesRepository.getBaseUrl())
    }

    private val searchRepository: SearchRepository by lazy {
        SearchRepository(preferencesRepository.getBaseUrl())
    }

    init {
        TAG = "MusicRepository"

        filter = IntentFilter()
        filter.addAction("com.niki.cloudmusic.music.NEW_STATUS")
        filter.addAction("com.niki.cloudmusic.music.NEW_PROGRESS")
        filter.addAction("com.niki.cloudmusic.music.FINISHED")

        receiver = MusicReceiver()
        application.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)

        currentSongList = mutableListOf()
        playMode.value = byLoop
    }

    fun addSongToNext(song: Song) {
        if (currentSongList.size <= 1)
            currentSongList.add(song)
        else
            currentSongList.add(currentIndex + 1, song)
        toast("已添加")
    }

    fun setNewSongList(list: MutableList<Song>, index: Int? = 0) {
        currentSongList = list
        currentIndex = index ?: 0
        currentSong.value = currentSongList[currentIndex]
    }

    fun switchMode() {
        when (playMode.value) {
            byRandom -> playMode.value = bySingle
            else -> playMode.value = playMode.value?.plus(1)
        }
    }

    fun checkSong(song: Song) {
        musicRepository.checkSong(song.id!!) { data, code, msg ->
            if (debug(data, code, msg, "checkSong")) {
                getSongInfo(song.id!!) { info ->
                    if (info != null && info.data[0].code == 200 && info.data[0].url.isNotBlank()) {
                        playMusic(info.data[0].url)
                        currentSong.value = song
                    } else {
                        toast("暂无版权")
                        nextSong()
                    }
                }
            } else {
                toast("暂无版权")
            }
        }
    }

    fun nextSong() {
        when (playMode.value) {
            byRandom -> {
                if (currentSongList.isEmpty()) {
                    if (isPlaying.value == true)
                        isPlaying.value = false
                    return
                }
                currentIndex = Random.nextInt(currentSongList.size)
                currentSong.value = currentSongList[currentIndex]
            }

            byLoop -> {
                if (currentSongList.isEmpty()) {
                    if (isPlaying.value == true)
                        isPlaying.value = false
                    return
                } else if (currentIndex == currentSongList.size - 1) {
                    currentIndex = 0
                } else {
                    currentIndex += 1
                }
                currentSong.value = currentSongList[currentIndex]
            }
        }
        checkSong(currentSong.value!!)
    }

    fun previousSong() {
        when (playMode.value) {
            byRandom -> {
                if (currentSongList.isEmpty()) {
                    if (isPlaying.value == true)
                        isPlaying.value = false
                    return
                }
                currentIndex = Random.nextInt(currentSongList.size)
                currentSong.value = currentSongList[currentIndex]
            }

            byLoop -> {
                if (currentSongList.isEmpty()) {
                    if (isPlaying.value == true)
                        isPlaying.value = false
                    return
                } else if (currentIndex == 0) {
                    currentIndex = currentSongList.size - 1
                } else {
                    currentIndex -= 1
                }
                currentSong.value = currentSongList[currentIndex]
            }
        }
        checkSong(currentSong.value!!)
    }

    fun searchSongs(
        limit: Int,
        page: Int,
        keywords: String?,
        callback: (PlaylistSongsResponse?, Boolean) -> Unit
    ) {
        if (keywords.isNullOrBlank() || isSearching) {
            callback(null, false)
            return
        }
        val offset = limit * page
        searchRepository.searchSongs(limit, offset, keywords) { data, code, msg ->
            if (debug(data, code, msg, "searchSongs")) {
                if (data!!.code == 200 && data.result!!.songCount != 0) {
                    val list = mutableListOf<String>()
                    for (song in data.result!!.songs!!) {
                        list.add(song.id!!)
                    }
                    searchSongsByIds(list) { sData ->
                        callback(sData, data.result!!.hasMore)
                    }
                } else {
                    toast("搜索无结果")
                    callback(null, false)
                }
            } else {
                toast("搜索失败")
                callback(null, false)
            }
        }

    }

    private fun searchSongsByIds(ids: List<String>, callback: (PlaylistSongsResponse?) -> Unit) {
        val builder = StringBuilder()
        for (id in ids) {
            builder.append(id)
            if (ids.size - 1 != ids.indexOf(id))
                builder.append(",")
        }
        builder.toString()
        searchRepository.searchSongsByIds(builder.toString()) { data, code, msg ->
            if (debug(data, code, msg, "getSongsFromPlaylist")) {
                if (data!!.code == 200 && data.songs != null && data.songs!!.isNotEmpty()) {
                    callback(data)
                } else {
                    toast("getSongsFromPlaylist: 空的song list")
                    callback(null)
                }
            } else
                callback(null)
        }
    }

    fun getLikeList(callback: (PlaylistSongsResponse?) -> Unit) {
        val uid = preferencesRepository.getString(preferencesRepository.userIdString)
        val cookie = preferencesRepository.getCookie()
        musicRepository.getLikeList(uid, cookie!!) { data, code, msg ->
            if (debug(data, code, msg, "getLikeList")) {
                if (data!!.ids.isNotEmpty()) {
                    searchSongsByIds(data.ids) { sData ->
                        if (sData != null) {
                            callback(sData)
                        } else {
                            callback(null)
                        }
                    }
                } else {
                    callback(null)
                }
            } else {
                callback(null)
            }
        }
    }

    private fun getSongInfo(id: String, callback: (SingleSongResponse?) -> Unit) {
        musicRepository.getSongInfo(
            id,
            "jymaster",
            preferencesRepository.getCookie()
        ) { data, code, msg ->
            if (debug(data, code, msg, "getSongInfo")) {
                if (data!!.data[0].code == 200) {
                    callback(data)
                } else {
                    toast("getSongInfo: 无效的歌曲")
                    callback(null)
                }
            } else
                callback(null)
        }
    }

    fun getSongsFromPlaylist(
        id: String?,
        limit: Int,
        page: Int,
        callback: (PlaylistSongsResponse?) -> Unit
    ) {
        if (id.isNullOrBlank()) {
            toast("getSongsFromPlaylist: 空的歌单id")
            dismissDialog()
            callback(null)
            return
        }

        val offset = limit * page
        musicRepository.getSongsFromPlaylist(id, limit, offset) { data, code, msg ->
            if (debug(data, code, msg, "getSongsFromPlaylist")) {
                if (data!!.code == 200 && data.songs != null && data.songs!!.isNotEmpty()) {
                    callback(data)
                } else {
                    toast("getSongsFromPlaylist: 空的song list")
                    callback(null)
                }
            } else
                callback(null)
        }
    }

    fun getPlaylistByHot(callback: (HotPlaylistResponse?) -> Unit) {
        playlistRepository.getPlaylistByHot { data, code, msg ->
            if (debug(data, code, msg, "getPlaylistByHot")) {
                if (data!!.code == 200 && data.tags != null && data.tags!!.isNotEmpty()) {
                    callback(data)
                } else {
                    toast("getPlaylistByHot: 空的tag list")
                    callback(null)
                }
            } else
                callback(null)
        }
    }

    fun getPlaylistByCat(callback: (CatPlaylistResponse?) -> Unit) {
        playlistRepository.getPlaylistByCat { data, code, msg ->
            if (debug(data, code, msg, "getPlaylistByCat")) {
                if (data!!.code == 200 && data.sub != null && data.sub!!.isNotEmpty()) {
                    callback(data)
                } else {
                    toast("getPlaylistByCat: 空的sub list")
                    callback(null)
                }
            } else
                callback(null)
        }
    }

    /** 页码和长度必须同时传入
     *  "new" 用不了所以去除了
     */
    fun getTopPlaylists(limit: Int, page: Int, callback: (TopPlaylistResponse?, Boolean) -> Unit) {
        val offset = limit * page
        playlistRepository.getTopPlaylists(limit, "hot", offset) { data, code, msg ->
            if (debug(data, code, msg, "getTopPlaylists")) {
                if (data!!.code == 200 && data.playlists != null && data.playlists!!.isNotEmpty()) {
                    callback(data, data.more)
                } else {
                    toast("getTopPlaylists: 空的playlist list")
                    callback(null, false)
                }
            } else
                callback(null, false)
        }
    }

    inner class MusicReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                "com.niki.cloudmusic.music.NEW_STATUS" -> {
                    val status = intent.getBooleanExtra("status", false)
                    if (isPlaying.value != status)
                        isPlaying.value = status
                }

                "com.niki.cloudmusic.music.NEW_PROGRESS" -> {
                    val progress = intent.getIntExtra("progress", 0)
                    songPosition.value = progress
                }
            }
        }
    }
}