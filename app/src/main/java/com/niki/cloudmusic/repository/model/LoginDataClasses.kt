package com.niki.cloudmusic.repository.model

/*游客登录*/
data class AnonymousLoginResponse(
    var code: Int = 777,
    var userId: String? = null,
    var cookie: String? = null
)

/*登出*/
data class LogoutResponse(
    var code: Int = 777
)

/*刷新 - cookie的打开方式不对，所以这个数据还是不用的好*/
data class RefreshResponse(
    var code: Int = 777,
    var cookie: String? = null
)

/*发验证码*/
data class SendCaptchaResponse(
    var code: Int = 777,
    var data: Boolean? = null
)

/*通过检测是否注册的接口获取头像*/
data class AvatarUrlResponse(
    var exist: Int = -1,
    var code: Int = 777,
    var avatarUrl: String? = null
)

/*登录状态*/
data class StateResponse(
    var data: LoginResponse? = null
) {
    data class LoginResponse(
        var code: Int = 777,
        var account: Account? = null,
        var profile: Profile? = null,
        var cookie: String? = null
    ) {
        data class Profile(
            var userId: String? = null,
            var avatarUrl: String? = null,
            var backgroundUrl: String? = null,
            var nickname: String? = null
        )

        data class Account(
            var anonimousUser: Boolean = true,
            var id: String
        )
    }
}
