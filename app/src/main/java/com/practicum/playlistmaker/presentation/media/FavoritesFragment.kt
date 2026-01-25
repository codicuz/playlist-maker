package com.practicum.playlistmaker.presentation.media

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.FragmentFavoritesBinding
import com.practicum.playlistmaker.domain.track.Track
import com.practicum.playlistmaker.presentation.adapter.TrackAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel

class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FavoritesViewModel by viewModel()

    private lateinit var trackAdapter: TrackAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
        viewModel.loadFavorites()
    }

    private fun setupRecyclerView() {
        trackAdapter = TrackAdapter { track ->
            val bundle = Bundle().apply { putParcelable("track", track) }
            findNavController().navigate(R.id.action_mediaFragment_to_audioPlayerFragment, bundle)
        }

        binding.favoritesItems.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = trackAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.favorites.observe(viewLifecycleOwner) { tracks ->
            trackAdapter.submitList(tracks)
            updateEmptyState(tracks)
        }
    }

    private fun updateEmptyState(tracks: List<Track>) {
        if (tracks.isEmpty()) {
            binding.mediaIsEmptyImage.visibility = View.VISIBLE
            binding.mediaIsEmptyText.visibility = View.VISIBLE
        } else {
            binding.mediaIsEmptyImage.visibility = View.GONE
            binding.mediaIsEmptyText.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = FavoritesFragment()
    }
}
