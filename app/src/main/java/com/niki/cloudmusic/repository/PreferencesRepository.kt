package com.niki.cloudmusic.repository

import android.content.Context
import android.util.Log
import com.niki.cloudmusic.repository.model.AnonymousLoginResponse
import com.niki.cloudmusic.repository.model.StateResponse
import java.net.URLEncoder

class PreferencesRepository(context: Context) : BaseRepository() {
    init {
        TAG = "PreferencesRepository"
    }

    private var preferences = context.getSharedPreferences("CloudMusic", Context.MODE_PRIVATE)

    fun saveCookie(value: String) {
        preferences.edit().putString(cookieString, value)?.apply()
    }

    fun getCookie(): String? {
        val mCookie = preferences.getString(cookieString, "")
        // mCookie
        if (mCookie == "")
            return null
        else
            return URLEncoder.encode(preferences.getString(cookieString, ""), "utf-8")
    }

    fun saveBaseUrl(value: String) {
        preferences.edit().putString(baseUrlString, value)?.apply()
    }

    fun getBaseUrl(): String {
        return preferences.getString(baseUrlString, "") ?: ""
    }

    fun getLoginStatus(): Boolean {
        return preferences.getBoolean(isLoggedInString, false)
    }

    fun setLoginStatus(boolean: Boolean) {
        preferences.edit().putBoolean(isLoggedInString, boolean).apply()
    }

    fun getString(key: String): String {
        return preferences.getString(key, "") ?: ""
    }

    fun saveString(key: String, value: String) {
        preferences.edit().putString(key, value)?.apply()
    }

    /**
     * 保存一系列用户变量
     * */
    fun saveLogin(data: StateResponse.LoginResponse) {
        try {
            preferences.edit()
                ?.putString(avatarString, data.profile!!.avatarUrl)
                ?.putString(userIdString, data.account!!.id)
                ?.putString(loginAvatarString, data.profile!!.avatarUrl)
                ?.putString(backgroundString, data.profile!!.backgroundUrl)
                ?.putString(nicknameString, data.profile!!.nickname)
                ?.apply()
            Log.d(TAG, "saveLogin: success")
        } catch (e: Exception) {
            Log.e(TAG, "saveLogin: failed")
        }
    }

    /**
     * 保存一系列游客变量
     * */
    fun saveLogout(data: AnonymousLoginResponse) {
        try {
            preferences.edit()
                ?.putString(userIdString, data.userId)
                ?.putString(cookieString, data.cookie)
                ?.apply()
            Log.d(TAG, "saveLogout: success")
        } catch (e: Exception) {
            Log.e(TAG, "saveLogout: failed")
        }
    }

    /**
     * 获取本地的用户信息
     * */
    fun getLogin(): StateResponse.LoginResponse? {
        try {
            val profile = StateResponse.LoginResponse.Profile()
            val data = StateResponse.LoginResponse()
            preferences.let {
                profile.avatarUrl = it.getString(avatarString, "")
                profile.backgroundUrl = it.getString(backgroundString, "")
                profile.nickname = it.getString(nicknameString, "")
                data.cookie = it.getString(cookieString, "")
            }
            data.profile = profile
            Log.d(TAG, "getLogin: success")
            return data
        } catch (e: Exception) {
            Log.e(TAG, "getLogin: failed")
            return null
        }
    }
}