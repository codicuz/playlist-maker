package com.practicum.playlistmaker.presentation.search

import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.EmptySearchBinding
import com.practicum.playlistmaker.databinding.FragmentSearchBinding
import com.practicum.playlistmaker.databinding.NoInternetBinding
import com.practicum.playlistmaker.databinding.SearchHistoryBinding
import com.practicum.playlistmaker.domain.track.Track
import com.practicum.playlistmaker.presentation.adapter.TrackAdapter
import com.practicum.playlistmaker.presentation.main.MainActivity
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SearchViewModel by viewModel()

    private lateinit var adapter: TrackAdapter
    private lateinit var historyAdapter: TrackAdapter

    private lateinit var emptySearchBinding: EmptySearchBinding
    private lateinit var noInternetBinding: NoInternetBinding
    private lateinit var searchHistoryBinding: SearchHistoryBinding

    private var wasSearchFocused = false

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
        setupKeyboardListener()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initStubBindings() {
        emptySearchBinding = EmptySearchBinding.bind(binding.stubEmptySearchInc.root)
        noInternetBinding = NoInternetBinding.bind(binding.stubNoInternetInc.root)
        searchHistoryBinding = SearchHistoryBinding.bind(binding.stubSearchHistoryInc.root)
    }

    private fun initAdapters() {
        adapter = TrackAdapter { track ->
            viewModel.onTrackClicked(track) {
                openPlayer(it)
            }
        }

        binding.rcTrackData.layoutManager = LinearLayoutManager(requireContext())
        binding.rcTrackData.adapter = adapter

        historyAdapter = TrackAdapter { track ->
            viewModel.addTrackToHistory(track)
            openPlayer(track)
        }
        searchHistoryBinding.rcTrackDataHistory.layoutManager =
            LinearLayoutManager(requireContext())
        searchHistoryBinding.rcTrackDataHistory.adapter = historyAdapter
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.state.collectLatest { state ->
                adapter.submitList(state.tracks)
                historyAdapter.submitList(state.history)

                binding.stubEmptySearchInc.root.isVisible =
                    state.tracks.isEmpty() && state.hasSearched
                binding.stubNoInternetInc.root.isVisible = state.isError

                binding.progressBar.isVisible = state.isLoading
                updateSearchHistoryVisibility(state.history)
            }
        }
    }

    private fun setupListeners() {
        binding.clearButton.setOnClickListener {
            binding.searchEditText.text?.clear()
            viewModel.clearSearchResults()
            viewModel.loadHistory()
            binding.searchEditText.requestFocus()
            hideKeyboard()
        }

        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString() ?: ""
                binding.clearButton.isVisible = query.isNotEmpty()
                viewModel.onQueryChanged(query)
            }
        })

        binding.searchEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                viewModel.loadHistory()
                updateSearchHistoryVisibility(viewModel.state.value.history)
            }
        }

        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val query = binding.searchEditText.text.toString()
                if (query.isNotBlank()) {
                    viewModel.onSearchDone(query)
                    hideKeyboard()
                }
                true
            } else false
        }

        noInternetBinding.buttonRetry.setOnClickListener {
            val query = binding.searchEditText.text.toString()
            if (query.isNotBlank()) {
                binding.progressBar.isVisible = true
                viewModel.onSearchDone(query)
            }
        }

        searchHistoryBinding.buttonClearSearchHistory.setOnClickListener {
            viewModel.clearHistory()
        }
    }

    private fun updateSearchHistoryVisibility(history: List<Track>) {
        val isEmptyQuery = binding.searchEditText.text.isEmpty()
        val hasFocus = binding.searchEditText.hasFocus()

        val showHistory = hasFocus && isEmptyQuery && history.isNotEmpty()
        searchHistoryBinding.root.isVisible = showHistory
        searchHistoryBinding.rcTrackDataHistory.isVisible = showHistory
        searchHistoryBinding.buttonClearSearchHistory.isVisible = showHistory
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.searchEditText.windowToken, 0)
    }

    private fun openPlayer(track: Track) {
        (requireActivity() as? MainActivity)?.hideBottomNav()
        val bundle = Bundle().apply { putParcelable("track", track) }
        findNavController().navigate(R.id.action_searchFragment_to_audioPlayerFragment, bundle)
    }

    private fun setupKeyboardListener() {
        val rootView = requireActivity().findViewById<View>(R.id.container_view)
        rootView.viewTreeObserver.addOnGlobalLayoutListener {
            if (!isAdded) return@addOnGlobalLayoutListener
            val rect = android.graphics.Rect()
            rootView.getWindowVisibleDisplayFrame(rect)
            val screenHeight = rootView.rootView.height
            val keypadHeight = screenHeight - rect.bottom

            val bottomNav =
                requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationView)
            bottomNav.isVisible = keypadHeight < screenHeight * 0.15
        }
    }

    override fun onPause() {
        super.onPause()
        wasSearchFocused = binding.searchEditText.hasFocus()
    }

    override fun onResume() {
        super.onResume()
        if (wasSearchFocused) {
            binding.searchEditText.requestFocus()
            val imm = requireContext().getSystemService(InputMethodManager::class.java)
            imm.showSoftInput(binding.searchEditText, InputMethodManager.SHOW_IMPLICIT)
        }
        (activity as? MainActivity)?.showBottomNav()
    }
}
