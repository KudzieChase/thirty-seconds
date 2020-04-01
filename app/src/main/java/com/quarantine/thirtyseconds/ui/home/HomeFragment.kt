package com.quarantine.thirtyseconds.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.quarantine.thirtyseconds.databinding.FragmentHomeBinding
import com.quarantine.thirtyseconds.models.Home
import com.quarantine.thirtyseconds.models.homeItems

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentHomeBinding.inflate(inflater, container, false)
        binding.apply {
            val onClick: HomeItemClick = object : HomeItemClick {
                override fun onClick(view: View, homeItem: Home) {
                    //Navigate
                    Toast.makeText(
                        context,
                        "Navigate to ${homeItem.destinationName}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            homeGrid.apply {
                adapter = HomeAdapter(onClick).apply {
                    submitList(homeItems)
                }
            }
        }
        return binding.root
    }
}