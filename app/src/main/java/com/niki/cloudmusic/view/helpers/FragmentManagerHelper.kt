package com.niki.cloudmusic.view.helpers

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.niki.cloudmusic.view.fragments.ListenFragment
import com.niki.cloudmusic.view.fragments.MyFragment
import com.niki.cloudmusic.view.fragments.PlaylistFragment
import com.niki.cloudmusic.view.fragments.SearchFragment
import com.niki.cloudmusic.view.fragments.TagFragment

class FragmentManagerHelper(
    private val fragmentManager: FragmentManager,
    private val containerId: Int,
) {

    private var listenFragment: ListenFragment? = null
    private var myFragment: MyFragment? = null
    private var searchFragment: SearchFragment? = null
    private var playlistFragment: PlaylistFragment? = null
    private var tagFragment: TagFragment? = null

    /**
     * 0-l 1-m 2-s
     */
    private var parentIndex = 0

    /**
     * 0-l 1-p 2-t
     */
    private var childIndex = 0

    init {
        /**
         * 初始化3个fragment
         * 并且隐藏其他的只留下listen fragment
         */
        listenFragment = ListenFragment()
        myFragment = MyFragment()
        searchFragment = SearchFragment()

        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.add(containerId, listenFragment!!, listenFragment!!.javaClass.name)
        fragmentTransaction.add(containerId, myFragment!!, myFragment!!.javaClass.name)
        fragmentTransaction.add(containerId, searchFragment!!, searchFragment!!.javaClass.name)

        hideFragmentByIndex(1)
        hideFragmentByIndex(2)
        fragmentTransaction.commit()
    }

    /**
     * 格局index获取fragment并隐藏之
     */
    private fun hideFragmentByIndex(index: Int) {
        val fragmentTransaction = fragmentManager.beginTransaction()
        try {
            fragmentTransaction.hide(getFragmentByIndex(index))
        } catch (ignored: Exception) {
            Log.e("llllllllllllllllllllllllllllllllll", "hideFragmentByIndex: BUG")
        }
        fragmentTransaction.commit()
    }

    fun getCurrentFragment(): Fragment {
        return getFragmentByIndex(parentIndex)
    }

    /**
     * 获取fragment，注意的是3、4代表playlist和tag
     */
    fun getFragmentByIndex(index: Int): Fragment {
        when (index) {
            0 -> {
                when (childIndex) {
                    0 -> {
                        if (listenFragment == null)
                            listenFragment = ListenFragment()
                        return listenFragment!!
                    }

                    1 -> return playlistFragment!!
                    2 -> return tagFragment!!
                    else -> return listenFragment!!
                }
            }

            1 -> {
                if (myFragment == null)
                    myFragment = MyFragment()
                return myFragment!!
            }

            2 -> {
                if (searchFragment == null)
                    searchFragment = SearchFragment()
                return searchFragment!!
            }

            else -> return listenFragment!!

        }
    }

    fun showListenFragment() {
        hideFragmentByIndex(0)
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.show(listenFragment!!)
        fragmentTransaction.commit()
    }

    fun hideListenFragment() {
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.hide(listenFragment!!)
        fragmentTransaction.commit()
    }

    fun switchToFragment(index: Int) {
        val fragmentTransaction = fragmentManager.beginTransaction()
        val fragment = getFragmentByIndex(index)

        val currentFragment = getCurrentFragment()

        if (fragment !== currentFragment) {
            fragmentTransaction.hide(currentFragment)
            if (!fragment.isAdded) {
                fragmentTransaction.add(
                    containerId,
                    fragment,
                    fragment.javaClass.name
                )
            } else {
                fragmentTransaction.show(fragment)
            }
        }
        parentIndex = index
        fragmentTransaction.commit()
    }

    /**
     * 安全打开playlist, tag的方法
     */
    fun openChildFragment(fragment: Fragment) {
        hideListenFragment()
        when (fragment) {
            is PlaylistFragment -> {
                playlistFragment = fragment
                childIndex = 1
            }

            is TagFragment -> {
                tagFragment = fragment
                childIndex = 2
            }
        }
        val fragmentTransaction = fragmentManager.beginTransaction()
        if (!fragment.isAdded) {
            fragmentTransaction.add(
                containerId,
                fragment,
                fragment.javaClass.name
            )
        } else {
            fragmentTransaction.show(fragment)
        }
        parentIndex = 0
        fragmentTransaction.commit()
    }

    /**
     * 处理首页的情况
     */
    fun dealWithFirstPage() {
        when (childIndex) {
            0 -> {
                switchToFragment(0)
            }

            1 -> {
                if (parentIndex == 0) {
                    showListenFragment()
                    childIndex = 0
                } else {
                    hideFragmentByIndex(parentIndex)
                    openChildFragment(playlistFragment!!)
                }
            }

            2 -> {
                if (parentIndex == 0) {
                    showListenFragment()
                    childIndex = 0
                } else {
                    hideFragmentByIndex(parentIndex)
                    openChildFragment(tagFragment!!)
                }
            }
        }
        parentIndex = 0
    }
}