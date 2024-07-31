package com.niki.cloudmusic.repository

import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

open class BaseRepository {
    var TAG = "BaseRepository"

    val cookieString = "cookie"
    val avatarString = "avatarUrl"
    val loginAvatarString = "loginAvatarUrl"
    val backgroundString = "backgroundUrl"
    val nicknameString = "nickname"
    val isLoggedInString = "isLoggedIn"
    val baseUrlString = "baseUrl"
    val userIdString = "userId"

    fun okHttpClientBuilder(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .build()
                chain.proceed(request)
            }
            .build()
    }

    /**
     *  当调用 enqueue() 方法时，Retrofit 会将请求添加到一个队列中，并在后台线程执行该请求,它使用异步机制，不会阻塞主线程
     */
    fun <T> enqueueCall(call: Call<T>, callback: (T?, Int?, String?) -> Unit) {
        call.enqueue(object : Callback<T> {
            /* 这一坨是实现此名为 object 的 Callback 接口（包含成功和失败的函数）*/
            /* 搞懂了这个以后我很喜欢这样写函数 */
            override fun onResponse(call: Call<T>, response: Response<T>) {
                if (response.isSuccessful) {
                    callback(response.body(), response.code(), response.message())
                } else {
                    callback(null, response.code(), response.message())
                }
            }

            override fun onFailure(call: Call<T>, t: Throwable) =
                callback(null, 999, "onFailure: 检查网络和主机IP")
        })
    }
}