package com.niki.cloudmusic.view.custom

import android.app.Dialog
import android.os.Bundle
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.niki.cloudmusic.R

/** 封装的从下端弹出的fragment（类似ios）
 *  size： 占屏幕高度的百分比
 *  布局的ID
 **/
open class DrawerFragment(
    private val size: Double = 0.9,
    private val layoutId: Int
) : BottomSheetDialogFragment() {

    override fun onStart() {
        super.onStart()
        dialog?.let { dialog ->
            dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                ?.let { bottomSheet ->
                    val behavior = BottomSheetBehavior.from(bottomSheet)
                    behavior.state = BottomSheetBehavior.STATE_EXPANDED
                    behavior.skipCollapsed = true
                    bottomSheet.layoutParams = bottomSheet.layoutParams.apply {
                        height = (resources.displayMetrics.heightPixels * size).toInt()
                    }
                }
        }
    }

    /** https://blog.csdn.net/qq471208499/article/details/122350610
     *  使用了博主写的圆角style
     **/
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        setStyle(STYLE_NORMAL, R.style.AppBottomSheet)
        val dialog = BottomSheetDialog(requireContext(), theme)
        dialog.apply {
            setOnShowListener { l ->
                (l as BottomSheetDialog).findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                    ?.let {
                        BottomSheetBehavior.from(it).apply {
                            state = BottomSheetBehavior.STATE_EXPANDED
                            skipCollapsed = true
                        }
                    }
            }
            setContentView(layoutId)
        }
        return dialog
    }
}