package com.anshul.employeeconnect

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class MainPagerAdapter(fm : FragmentManager) : FragmentPagerAdapter(fm) {


    override fun getItem(position: Int): Fragment {

        return when(position){
            0 -> { HomeFragment()
            }
            1 -> { TaskFragment()
            }
            2 -> {ProfileFragment()
            }
            else -> HomeFragment()
        }


    }

    override fun getCount(): Int {
        return 3
    }

}