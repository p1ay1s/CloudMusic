package com.niki.cloudmusic.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import coil.load
import coil.transform.CircleCropTransformation
import com.niki.cloudmusic.R
import com.niki.cloudmusic.databinding.FragmentLoginBinding
import com.niki.cloudmusic.view.custom.DrawerFragment
import com.niki.cloudmusic.view.custom.LoadingDialog
import com.niki.cloudmusic.viewmodel.LoginViewModel

/**
 * 拉起登录界面，TODO 保存已经填写而未点击登录的信息
 **/
class LoginFragment :
    DrawerFragment(1.0, R.layout.fragment_login) {

    private lateinit var binding: FragmentLoginBinding

    private val viewModel: LoginViewModel by activityViewModels<LoginViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val avatarView = binding.userAvatar

        viewModel.loadingDialog = LoadingDialog(requireActivity())
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        viewModel.let {
            /* 监听登录状态来关闭此fragment */
            it.isLoggedIn.observe(this) { value ->
                if (value == 1) {
                    dismiss()
                }
            }

            /* 监听号码输入变化 */
            it.phone.observe(this) { _ ->
                viewModel.getAvatarUrl()
            }

            /* 监听头像url变化以实现实时更新登录用户头像 */
            it.avatarData.observe(this) { data ->
                if (data != null && data.exist == 1) {
                    avatarView.run {
                        visibility = View.VISIBLE
                        load(data.avatarUrl) {
                            transformations(CircleCropTransformation())
                        }
                    }
                } else {
                    avatarView.visibility = View.INVISIBLE
                }
            }
        }
    }
}
