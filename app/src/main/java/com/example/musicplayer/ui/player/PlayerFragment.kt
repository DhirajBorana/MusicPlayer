package com.example.musicplayer.ui.player

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.musicplayer.data.model.Song
import com.example.musicplayer.databinding.FragmentPlayerBinding
import com.example.musicplayer.databinding.ItemSongImageBinding
import com.example.musicplayer.ui.main.MainViewModel
import java.util.*

class PlayerFragment : Fragment() {

    private lateinit var binding: FragmentPlayerBinding
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var songs: List<Song>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        setupClickListeners()
        viewModel.songs.observe(viewLifecycleOwner) {
            songs = it
            initViewPager()
        }
        viewModel.currentSong.observe(viewLifecycleOwner) {
            it?.let {
                binding.song = it
                binding.viewPager.currentItem = viewModel.currentSongIndex.value ?: 0
            }
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            chevronDown.setOnClickListener { findNavController().navigateUp() }
            favorite.setOnCheckedChangeListener { btn, _ ->
                viewModel.update(viewModel.currentSong.value?.copy(favorite = btn.isChecked)!!)
            }
            skipNext.setOnClickListener { viewModel.skipNext() }
            skipPrevious.setOnClickListener { viewModel.skipPrevious() }
        }
    }

    private fun initViewPager() {
        binding.viewPager.apply {
            adapter = PlayerActivityAdapter(context)
            currentItem = viewModel.currentSongIndex.value ?: 0
            addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageSelected(position: Int) {
                    viewModel.setCurrentSongIndex(position)
                }

                override fun onPageScrollStateChanged(state: Int) {}
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                }
            })
        }
    }

    inner class PlayerActivityAdapter(context: Context) : PagerAdapter() {
        private val layoutInflater = LayoutInflater.from(context)

        override fun getCount() = songs.size

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view == `object` as LinearLayoutCompat
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val binding = ItemSongImageBinding.inflate(layoutInflater, container, false)
            binding.url = songs[position].thumbnailUri
            Objects.requireNonNull(container).addView(binding.root)
            return binding.root
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as LinearLayoutCompat)
        }
    }
}