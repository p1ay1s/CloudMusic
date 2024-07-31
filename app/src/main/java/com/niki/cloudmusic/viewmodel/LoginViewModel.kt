package com.niki.cloudmusic.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.niki.cloudmusic.repository.LoginRepository
import com.niki.cloudmusic.repository.model.AvatarUrlResponse
import com.niki.cloudmusic.repository.model.Song

open class LoginViewModel(application: Application) : BaseViewModel(application) {
    /**
     * 确保在用户填写baseurl后才初始化这玩意
     **/
    private val loginRepository: LoginRepository by lazy {
        LoginRepository(preferencesRepository.getBaseUrl())
    }

    var localList = listOf<Song>()

    var phone = MutableLiveData<String>()
    var captcha = MutableLiveData<String>()
    var avatarData = MutableLiveData<AvatarUrlResponse?>()

    init {
        TAG = "LoginFragmentViewModel"
    }

    /**
     * 本地登录状态由sp和livedata的登录值决定
     **/
    private fun localLogout() {
        preferencesRepository.setLoginStatus(false)
        isLoggedIn.value = 0
    }

    private fun localLogin() {
        preferencesRepository.setLoginStatus(true)
        isLoggedIn.value = 1
    }

    /**
     * 目前主要作用是覆盖先前的登录状态以及应用第一次打开时初始化
     **/
    private fun anonymousLogin() {
        loginRepository.anonymousLogin { data, code, msg ->
            if (debug(data, code, msg, "anonymousLogin")) {
                if (isSuccess(data!!.code)) {
                    preferencesRepository.saveLogout(data)
                    localLogout()
                }
            }
        }
    }

    fun getAvatarUrl() {
        loginRepository.getAvatarUrl(phone.value) { data, code, msg ->
            if (debug(data, code, msg, "getAvatarUrl")) {
                avatarData.value = data
            } else {
                avatarData.value = null
            }
        }
    }

    fun logout() {
        showDialog()
        loginRepository.logout(preferencesRepository.getCookie()) { data, code, msg ->
            if (debug(data, code, msg, "logout")) {
                loginStatusRefresh()
                anonymousLogin()
            }
        }
    }

    private fun loginStatusRefresh() {
        loginRepository.loginStatusRefresh(preferencesRepository.getCookie()) { data, code, msg ->
            debug(data, code, msg, "loginStatusRefresh")
        }
    }

    /**
     * 读取表示登录状态的值来判定
     **/
    fun getLoginState() {
        showDialog()
        loginRepository.getLoginState(preferencesRepository.getCookie()) { data, code, msg ->
            if (debug(data, code, msg, "getLoginState")) {
                if (!data!!.data!!.account!!.anonimousUser) {
                    localLogin()
                    preferencesRepository.saveLogin(data.data!!)
                } else
                    anonymousLogin()
            }
        }
    }

    fun captchaLogin() {
        showDialog()
        loginRepository.captchaLogin(
            phone.value,
            captcha.value
        ) { data, code, msg ->
            if (debug(data, code, msg, "captchaLogin")) {
                if (isSuccess(data!!.code)) {
                    preferencesRepository.saveLogin(data)
                    localLogin()
                    preferencesRepository.saveCookie(data.cookie!!)
                } else {
                    localLogout()
                }
            }
        }
    }

    fun sendCaptcha() {
        loginRepository.sendCaptcha(phone.value) { data, code, msg ->
            if (debug(data, code, msg, "sendCaptcha"))
                toast("已发送")
        }
    }

}