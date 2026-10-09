package com.farmfeather.bridge.core.navigation

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.farmfeather.bridge.R
import com.farmfeather.bridge.presentation.game.GameFragment
import com.farmfeather.bridge.presentation.gameover.GameOverFragment
import com.farmfeather.bridge.presentation.menu.MenuFragment
import com.farmfeather.bridge.presentation.splash.SplashFragment

class Navigator(
    private val fragmentManager: FragmentManager,
    private val containerId: Int
) {

    fun showSplash(): Boolean =
        show(SplashFragment(), TAG_SPLASH, addToBackStack = false)

    fun showMenu(): Boolean {
        if (!isUsable()) return false
        if (fragmentManager.backStackEntryCount > 0) {
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
        return show(MenuFragment(), TAG_MENU, addToBackStack = false)
    }

    fun showGame(missionId: Int): Boolean =
        show(GameFragment.newInstance(missionId), TAG_GAME, addToBackStack = true)

    fun showGameOver(args: Bundle): Boolean =
        show(GameOverFragment.newInstance(args), TAG_RESULT, addToBackStack = true)

    fun goBack() {
        if (!isUsable()) return
        if (fragmentManager.backStackEntryCount > 0) {
            fragmentManager.popBackStack()
        } else {
            showMenu()
        }
    }

    private fun show(fragment: Fragment, tag: String, addToBackStack: Boolean): Boolean {
        if (!isUsable()) return false
        val transaction = fragmentManager.beginTransaction()
        transaction.setCustomAnimations(
            R.anim.fade_in,
            R.anim.fade_out,
            R.anim.fade_in,
            R.anim.fade_out
        )
        transaction.replace(containerId, fragment, tag)
        if (addToBackStack) {
            transaction.addToBackStack(tag)
        }
        transaction.commit()
        return true
    }

    private fun isUsable(): Boolean = !fragmentManager.isStateSaved && !fragmentManager.isDestroyed

    private companion object {
        const val TAG_SPLASH = "screen_splash"
        const val TAG_MENU = "screen_menu"
        const val TAG_GAME = "screen_game"
        const val TAG_RESULT = "screen_result"
    }
}
