package com.niki.cloudmusic.repository

import com.niki.cloudmusic.repository.model.AnonymousLoginResponse
import com.niki.cloudmusic.repository.model.AvatarUrlResponse
import com.niki.cloudmusic.repository.model.LogoutResponse
import com.niki.cloudmusic.repository.model.RefreshResponse
import com.niki.cloudmusic.repository.model.SendCaptchaResponse
import com.niki.cloudmusic.repository.model.StateResponse
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

/** 登录请求数据源
 *  666 input 777 init 999 network
 **/
class LoginRepository(private val baseUrl: String) : BaseRepository() {
    private interface LoginService {
        @GET("/register/anonimous")
        fun anonymousLogin(): Call<AnonymousLoginResponse>
        /* Call<T> 是 Retrofit 库中的一个接口，表示一个可执行的网络请求。T 代表预期返回的数据类型 */

        @GET("/captcha/sent")
        fun sendCaptcha(
            @Query("phone") phone: String,
        ): Call<SendCaptchaResponse>

        @GET("/login/cellphone")
        fun captchaLogin(
            @Query("phone") phone: String,
            @Query("captcha") captcha: String
        ): Call<StateResponse.LoginResponse>

        @GET("/cellphone/existence/check")
        fun getAvatarUrl(
            @Query("phone") phone: String,
        ): Call<AvatarUrlResponse>

        @GET("/login/status")
        fun loginState(
            @Query("cookie") cookie: String? = null
        ): Call<StateResponse>

        @GET("/login/refresh")
        fun loginRefresh(
            @Query("cookie") cookie: String? = null
        ): Call<RefreshResponse>

        @GET("/logout")
        fun logout(
            @Query("cookie") cookie: String? = null
        ): Call<LogoutResponse>
    }

    init {
        TAG = "LoginRepository"
    }

    /** 只有在第一次访问该属性才会执行 by lazy 后面的代码块，
     *  并将结果赋值给该属性。之后再访问该属性，直接返回之前保存的结果
     *  返回的并不是 LoginService 接口本身的实例，
     *  而是实现了 Call<CellphoneExistenceResponse> 接口的匿名内部类。
     *  Retrofit 在幕后完成了创建这个匿名类的过程
     **/
    private val loginService: LoginService by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClientBuilder())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LoginService::class.java)
    }

    fun logout(cookie: String?, call: (LogoutResponse?, Int?, String?) -> Unit) =
        enqueueCall(loginService.logout(cookie), call)

    fun loginStatusRefresh(
        cookie: String?,
        call: (RefreshResponse?, Int?, String?) -> Unit
    ) = enqueueCall(loginService.loginRefresh(cookie), call)

    fun getLoginState(
        cookie: String?,
        call: (StateResponse?, Int?, String?) -> Unit
    ) = enqueueCall(loginService.loginState(cookie), call)

    fun anonymousLogin(call: (AnonymousLoginResponse?, Int?, String?) -> Unit) =
        enqueueCall(loginService.anonymousLogin(), call)

    fun captchaLogin(
        phone: String?,
        captcha: String?,
        call: (StateResponse.LoginResponse?, Int?, String?) -> Unit
    ) {
        if (phone == null || captcha == null) {
            call(null, 666, "CloudMusic: 无效的输入")
            return
        }

        enqueueCall(loginService.captchaLogin(phone, captcha), call)
    }

    fun sendCaptcha(
        phone: String?,
        call: (SendCaptchaResponse?, Int?, String?) -> Unit
    ) {
        if (phone == null) {
            call(null, 666, "CloudMusic: 无效的输入")
            return
        }
        enqueueCall(loginService.sendCaptcha(phone), call)
    }

    fun getAvatarUrl(
        phone: String?,
        call: (AvatarUrlResponse?, Int?, String?) -> Unit
    ) {
        if (phone == null) {
            call(null, 666, "CloudMusic: 无效的输入")
            return
        }
        enqueueCall(loginService.getAvatarUrl(phone), call)
    }
}