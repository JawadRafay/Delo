package com.statussaver.dele.ui.fragments

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.tabs.TabLayout
import com.statussaver.dele.R
import com.statussaver.dele.databinding.FragmentMyDownloadsBinding
import java.util.ArrayList

class MyDownloads : Fragment() {

    var activity: Activity? = null
    lateinit var tabs: Array<String?>
    lateinit var fragments : MutableList<Fragment>
    private lateinit var binding: FragmentMyDownloadsBinding
    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as Activity
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyDownloadsBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()

        for (pos in 0..1) {
            val tab: TabLayout.Tab = binding.tablayout2.newTab();
            tab.customView = getTabViewUn(pos)
            binding.tablayout2.addTab(tab)
        }

        //load first fragment
        val tabs: TabLayout.Tab? = binding.tablayout2.getTabAt(0)
        tabs?.customView = null
        tabs?.customView = getTabView(0)
        inflateFragment(0)

        binding.tablayout2.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val tabs: TabLayout.Tab? = binding.tablayout2.getTabAt(tab.position)
                tabs?.customView = null
                tabs?.customView = getTabView(tab.position)
                inflateFragment(tab.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                val tabs: TabLayout.Tab? = binding.tablayout2.getTabAt(tab.position)
                tabs?.customView = null
                tabs?.customView = getTabViewUn(tab.position)
            }

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    fun init(){
        tabs = arrayOfNulls<String?>(2)
        tabs[0] = resources.getString(R.string.images)
        tabs[1] = resources.getString(R.string.videos)

        fragments = ArrayList();
        fragments.add(MyDownloadPhotos())
        fragments.add(MyDownloadVideos())

    }

    fun getTabViewUn(pos: Int): View {
        val v: View = LayoutInflater.from(activity).inflate(R.layout.custom_tab, null)
        val txt = v.findViewById<TextView>(R.id.tab)
        txt.text = tabs[pos]
        txt.setTextColor(resources.getColor(R.color.tab_txt_unpress))
        txt.setBackgroundResource(R.drawable.unpress_tab)
        val tab = FrameLayout.LayoutParams(
            resources.displayMetrics.widthPixels * 438 / 1080,
            resources.displayMetrics.heightPixels * 140 / 1920
        )
        txt.layoutParams = tab
        return v
    }

    fun getTabView(pos: Int): View {
        val v = LayoutInflater.from(activity).inflate(R.layout.custom_tab, null)
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
        val fragmentManager: FragmentManager = childFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.container, fragments[pos]).commit()
    }
}