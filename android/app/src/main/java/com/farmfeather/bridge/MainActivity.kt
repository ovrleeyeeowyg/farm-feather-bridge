package com.farmfeather.bridge

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.farmfeather.bridge.core.di.ServiceLocator
import com.farmfeather.bridge.core.navigation.Navigator
import com.farmfeather.bridge.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding? = null
    private var navigatorHolder: Navigator? = null

    val navigator: Navigator
        get() = requireNotNull(navigatorHolder) { "Navigator is not ready" }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ServiceLocator.init(applicationContext)
        val created = ActivityMainBinding.inflate(layoutInflater)
        binding = created
        setContentView(created.root)
        navigatorHolder = Navigator(supportFragmentManager, R.id.fragment_container)
        if (savedInstanceState == null) {
            navigator.showSplash()
        }
    }

    override fun onDestroy() {
        navigatorHolder = null
        binding = null
        super.onDestroy()
    }
}
