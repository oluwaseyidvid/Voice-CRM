package com.neuralic.voicecrm

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.fragment.app.Fragment
import com.neuralic.voicecrm.ui.HomeFragment
import com.neuralic.voicecrm.ui.LogFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val homeFragment = HomeFragment()
        val logFragment = LogFragment()

        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, homeFragment).commit()

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setOnItemSelectedListener { item ->
            val selected: Fragment = when (item.itemId) {
                R.id.nav_home -> homeFragment
                R.id.nav_log -> logFragment
                else -> homeFragment
            }
            supportFragmentManager.beginTransaction().replace(R.id.fragment_container, selected).commit()
            true
        }
    }
}
