package com.practicum.playlistmaker.presentation.search

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.EmptySearchBinding
import com.practicum.playlistmaker.databinding.FragmentSearchBinding
import com.practicum.playlistmaker.databinding.NoInternetBinding
import com.practicum.playlistmaker.databinding.SearchHistoryBinding
import com.practicum.playlistmaker.domain.track.Track
import com.practicum.playlistmaker.presentation.adapter.TrackAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val searchViewModel: SearchViewModel by viewModel()

    private lateinit var adapter: TrackAdapter
    private lateinit var historyAdapter: TrackAdapter

    private lateinit var emptySearchBinding: EmptySearchBinding
    private lateinit var noInternetBinding: NoInternetBinding
    private lateinit var searchHistoryBinding: SearchHistoryBinding

    private var currentQuery = ""

    private val handler = Handler(Looper.getMainLooper())
    private var searchRunnableOnDone: Runnable? = null
    private var searchRunnableOnTextChanged: Runnable? = null

    private val debounceDelayDone = 500L
    private val debounceDelayTextChanged = 2000L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initStubBindings()
        initAdapters()
        observeViewModel()
        setupListeners()
        val activityRootView =
            requireActivity().findViewById<View>(R.id.container_view)
        activityRootView.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = android.graphics.Rect()
            activityRootView.getWindowVisibleDisplayFrame(rect)
            val screenHeight = activityRootView.rootView.height
            val keypadHeight = screenHeight - rect.bottom

            val bottomNav =
                requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(
                    R.id.bottomNavigationView
                )
            bottomNav.isVisible = keypadHeight < screenHeight * 0.15
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
        _binding = null
    }

    private fun initStubBindings() {
        emptySearchBinding = EmptySearchBinding.bind(binding.stubEmptySearchInc.root)
        noInternetBinding = NoInternetBinding.bind(binding.stubNoInternetInc.root)
        searchHistoryBinding = SearchHistoryBinding.bind(binding.stubSearchHistoryInc.root)
    }

    private fun initAdapters() {
        adapter = TrackAdapter { track ->
            searchViewModel.addTrackToHistory(track)
            openPlayer(track)
        }

        binding.rcTrackData.layoutManager = LinearLayoutManager(requireContext())
        binding.rcTrackData.adapter = adapter

        historyAdapter = TrackAdapter { track ->
            searchViewModel.addTrackToHistory(track)
            openPlayer(track)
        }

        searchHistoryBinding.rcTrackDataHistory.layoutManager =
            LinearLayoutManager(requireContext())
        searchHistoryBinding.rcTrackDataHistory.adapter = historyAdapter
    }

    private fun observeViewModel() {
        searchViewModel.state.observe(viewLifecycleOwner) { state ->

            if (state.tracks.isNotEmpty()) {
                adapter.submitList(state.tracks)
                binding.rcTrackData.visibility = View.VISIBLE
                emptySearchBinding.root.visibility = View.GONE
            } else {
                binding.rcTrackData.visibility = View.GONE
                emptySearchBinding.root.visibility =
                    if (state.hasSearched && !state.isError) View.VISIBLE else View.GONE
            }

            historyAdapter.submitList(state.history)
            updateSearchHistoryVisibility(state.history)

            noInternetBinding.root.visibility = if (state.isError) View.VISIBLE else View.GONE

            binding.progressBar.visibility = View.GONE
        }
    }

    private fun setupListeners() {
        binding.clearButton.setOnClickListener {
            binding.searchEditText.text?.clear()
            binding.searchEditText.text?.clear()
            searchViewModel.clearSearchResults()
            hideKeyboard()
        }

        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.clearButton.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE

                val query = s?.toString()?.trim().orEmpty()

                if (query.isEmpty()) {
                    searchRunnableOnTextChanged?.let { handler.removeCallbacks(it) }
                    searchRunnableOnDone?.let { handler.removeCallbacks(it) }
                    searchViewModel.clearSearchResults()
                    return
                }

                if (query != currentQuery) {
                    currentQuery = query

                    searchRunnableOnTextChanged?.let {
                        handler.removeCallbacks(it)
                    }

                    searchRunnableOnTextChanged = Runnable {
                        searchHistoryBinding.root.visibility = View.GONE
                        binding.progressBar.visibility = View.VISIBLE
                        searchViewModel.searchTracks(query)
                        hideKeyboard()
                    }

                    handler.postDelayed(
                        searchRunnableOnTextChanged!!, debounceDelayTextChanged
                    )
                }
            }
        })

        binding.searchEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                searchViewModel.loadHistory()
            }
        }

        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val query = binding.searchEditText.text.toString().trim()
                if (query.isNotEmpty()) {
                    searchRunnableOnTextChanged?.let { handler.removeCallbacks(it) }
                    searchRunnableOnDone?.let { handler.removeCallbacks(it) }

                    searchRunnableOnDone = Runnable {
                        searchHistoryBinding.root.visibility = View.GONE
                        binding.progressBar.visibility = View.VISIBLE
                        searchViewModel.searchTracks(query)
                        hideKeyboard()
                    }

                    handler.postDelayed(searchRunnableOnDone!!, debounceDelayDone)
                }
                true
            } else false
        }

        noInternetBinding.buttonRetry.setOnClickListener {
            if (currentQuery.isNotBlank()) {
                binding.progressBar.visibility = View.VISIBLE
                searchViewModel.searchTracks(currentQuery)
            }
        }

        searchHistoryBinding.buttonClearSearchHistory.setOnClickListener {
            searchViewModel.clearHistory()
        }
    }

    private fun updateSearchHistoryVisibility(history: List<Track>) {
        val isEmptyQuery = binding.searchEditText.text.isEmpty()
        val hasFocus = binding.searchEditText.hasFocus()

        if (hasFocus && isEmptyQuery && history.isNotEmpty()) {
            searchHistoryBinding.root.visibility = View.VISIBLE
            searchHistoryBinding.rcTrackDataHistory.visibility = View.VISIBLE
            searchHistoryBinding.buttonClearSearchHistory.visibility = View.VISIBLE
        } else {
            searchHistoryBinding.root.visibility = View.GONE
            searchHistoryBinding.rcTrackDataHistory.visibility = View.GONE
            searchHistoryBinding.buttonClearSearchHistory.visibility = View.GONE
        }
    }

    private fun hideKeyboard() {
        val imm =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.searchEditText.windowToken, 0)
    }

    private fun openPlayer(track: Track) {
        val bundle = Bundle().apply { putParcelable("track", track) }
        findNavController().navigate(R.id.action_searchFragment_to_audioPlayerFragment, bundle)
    }
}
