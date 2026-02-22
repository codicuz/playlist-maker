package com.practicum.playlistmaker.presentation.media

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.FragmentPlaylistsBinding
import com.practicum.playlistmaker.domain.playlist.Playlist
import com.practicum.playlistmaker.presentation.adapter.PlaylistAdapter
import com.practicum.playlistmaker.presentation.main.MainActivity
import com.practicum.playlistmaker.presentation.util.GridSpacingItemDecoration
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlaylistsFragment : Fragment() {

    private var _binding: FragmentPlaylistsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlaylistsViewModel by viewModel()
    private lateinit var adapter: PlaylistAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = PlaylistAdapter { playlist ->
            navigateToPlaylistFragment(playlist)
        }

        val layoutManager = GridLayoutManager(requireContext(), 2)
        binding.playlistsItems.layoutManager = layoutManager

        binding.playlistsItems.addItemDecoration(
            GridSpacingItemDecoration(
                spanCount = 2,
                spacingPx = resources.getDimensionPixelSize(R.dimen.grid_spacing_8)
            )
        )

        binding.playlistsItems.adapter = adapter

        binding.createPlaylistButton.setOnClickListener {
            findNavController().navigate(
                R.id.action_mediaFragment_to_newPlaylistFragment
            )
        }

        parentFragmentManager.setFragmentResultListener(
            "playlist_updated", viewLifecycleOwner
        ) { _, _ ->
            viewModel.refreshPlaylists()
        }

        parentFragmentManager.setFragmentResultListener(
            "playlist_deleted", viewLifecycleOwner
        ) { _, bundle ->
            if (bundle.getBoolean("deleted")) {
                viewModel.refreshPlaylists()
                Toast.makeText(
                    requireContext(),
                    getString(R.string.playlist_deleted_success),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        observeViewModel()
        viewModel.loadPlaylists()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.playlists.collectLatest { list ->
                adapter.submitList(list)
                updateEmptyState(list)
            }
        }
    }

    private fun updateEmptyState(playlists: List<Playlist>) {
        if (playlists.isEmpty()) {
            binding.emptyPlaylistImage.visibility = View.VISIBLE
            binding.mediaTab.visibility = View.VISIBLE
        } else {
            binding.emptyPlaylistImage.visibility = View.GONE
            binding.mediaTab.visibility = View.GONE
        }
    }

    private fun navigateToPlaylistFragment(playlist: Playlist) {
        val bundle = Bundle().apply {
            putLong("playlistId", playlist.id)
        }
        findNavController().navigate(
            R.id.action_mediaFragment_to_playlistFragment, bundle
        )
    }

    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.showBottomNav()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = PlaylistsFragment()
    }
}