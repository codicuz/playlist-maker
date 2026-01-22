package com.practicum.playlistmaker.presentation.settings

import android.app.ActivityOptions
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.FragmentSettingsBinding
import com.practicum.playlistmaker.presentation.theme.ThemeViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val themeViewModel: ThemeViewModel by viewModel()
    private var themeInitialized = false

    companion object {
        private const val MIME_TYPE_TEXT_PLAIN = "text/plain"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        themeViewModel.state.observe(viewLifecycleOwner) { state ->
            if (!themeInitialized) {
                binding.themeSwitcher.isChecked = state.isDarkMode
                themeInitialized = true
            }
        }

        binding.themeSwitcher.setOnCheckedChangeListener { _, isChecked ->
            if (themeInitialized && isChecked != themeViewModel.state.value?.isDarkMode) {
                themeViewModel.switchTheme(isChecked)
                restartWithTheme()
            }
        }

        themeViewModel.uiEvent.observe(viewLifecycleOwner) { event ->
            when (event) {
                is SettingsUiEvent.OpenPracticumOffer -> openPracticumOffer()
                is SettingsUiEvent.SendToHelpdesk -> openHelpdeskEmail()
                is SettingsUiEvent.ShareApp -> shareApp()
                is SettingsUiEvent.ShowError -> Toast.makeText(
                    requireContext(),
                    event.message,
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        binding.settingsHeader.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.practicumOffer.setOnClickListener {
            themeViewModel.onPracticumOfferClicked()
        }

        binding.sendToHelpdesk.setOnClickListener {
            themeViewModel.onSendToHelpdeskClicked()
        }

        binding.shareApp.setOnClickListener {
            themeViewModel.onShareAppClicked()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun openPracticumOffer() {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = getString(R.string.practicum_license).toUri()
        }
        startSafe(intent)
    }

    private fun openHelpdeskEmail() {
        val email = getString(R.string.email)
        val subject = getString(R.string.email_subject)
        val body = getString(R.string.email_text)
        val uri = "mailto:$email?subject=${Uri.encode(subject)}&body=${Uri.encode(body)}"
        val intent = Intent(Intent.ACTION_SENDTO, uri.toUri())
        startSafe(intent)
    }

    private fun restartWithTheme() {
        requireActivity().recreate()
    }

    private fun shareApp() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = MIME_TYPE_TEXT_PLAIN
            putExtra(
                Intent.EXTRA_TEXT,
                getString(R.string.https_practicum_yandex_ru_android_developer)
            )
        }
        startSafe(Intent.createChooser(intent, null))
    }

    private fun startSafe(intent: Intent) {
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                getString(R.string.no_intent_handle),
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
