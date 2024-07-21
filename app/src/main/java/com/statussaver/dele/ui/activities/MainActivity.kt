package com.statussaver.dele.ui.activities

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.statussaver.dele.R
import com.statussaver.dele.database.PrefManager
import com.statussaver.dele.databinding.ActivityMainBinding
import com.statussaver.dele.ui.fragments.*
import java.util.ArrayList

class MainActivity : AppCompatActivity() {

    lateinit var tabs: Array<String?>
    lateinit var fragments : MutableList<Fragment>
    lateinit var prefManager: PrefManager
    private lateinit var binding:ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefManager = PrefManager(this)
        val nightModeFlags = applicationContext.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_NO) {
            if (prefManager.getDarkMode())
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()

        for (pos in 0..7) {
            val tab: TabLayout.Tab = binding.tablayout.newTab();
            tab.customView = getTabViewUn(pos)
            binding.tablayout.addTab(tab)
        }

        //load first fragment
        val tabs: TabLayout.Tab? = binding.tablayout.getTabAt(0)
        tabs?.customView = null
        tabs?.customView = getTabView(0)
        inflateFragment(0)

        binding.tablayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val tabs: TabLayout.Tab? = binding.tablayout.getTabAt(tab.position)
                tabs?.customView = null
                tabs?.customView = getTabView(tab.position)
                inflateFragment(tab.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                val tabs: TabLayout.Tab? = binding.tablayout.getTabAt(tab.position)
                tabs?.customView = null
                tabs?.customView = getTabViewUn(tab.position)
            }

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        binding.settingImg.setOnClickListener {
            startActivityForResult(Intent(applicationContext,SettingsActivity::class.java),10)
        }
    }

    fun init(){
        tabs = arrayOfNulls<String?>(8)
        tabs[0] = resources.getString(R.string.chat)
        tabs[1] = resources.getString(R.string.status)
        tabs[2] = resources.getString(R.string.images)
        tabs[3] = resources.getString(R.string.videos)
        tabs[4] = resources.getString(R.string.voice)
        tabs[5] = resources.getString(R.string.audio)
        tabs[6] = resources.getString(R.string.document)
        tabs[7] = resources.getString(R.string.download)

        fragments = ArrayList();
        fragments.add(MyChats())
        fragments.add(WhatsappStatus())
        fragments.add(MyPhotos())
        fragments.add(MyVideos())
        fragments.add(MyVoices())
        fragments.add(MyAudios())
        fragments.add(MyDocs())
        fragments.add(MyDownloads())

    }

    fun getTabViewUn(pos: Int): View {
        val v: View = LayoutInflater.from(this@MainActivity).inflate(R.layout.custom_tab, null)
        val txt = v.findViewById<TextView>(R.id.tab)
        txt.text = tabs[pos]
        txt.setTextColor(resources.getColor(R.color.gray_500))
        txt.background = null
        val tab = FrameLayout.LayoutParams(
            resources.displayMetrics.widthPixels * 438 / 1080,
            resources.displayMetrics.heightPixels * 140 / 1920
        )
        txt.layoutParams = tab
        return v
    }

    fun getTabView(pos: Int): View {
        val v = LayoutInflater.from(this@MainActivity).inflate(R.layout.custom_tab, null)
        val txt = v.findViewById<TextView>(R.id.tab)
        txt.text = tabs[pos]
        txt.setTextColor(resources.getColor(R.color.tab_txt_press))
        txt.setBackgroundResource(R.drawable.press_tab)
        val tab = FrameLayout.LayoutParams(
            resources.displayMetrics.widthPixels * 438 / 1080,
            resources.displayMetrics.heightPixels * 140 / 1920
        )
        txt.layoutParams = tab
        return v
    }

    fun inflateFragment(pos: Int){
        val fragmentManager: FragmentManager = supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.container, fragments[pos]).commit()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("TAG", "onActivityResult: ")
        if (requestCode == 10 && resultCode == Activity.RESULT_OK)
            recreate()
    }
}