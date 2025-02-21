package com.akirus.snookounter

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.akirus.snookounter.databinding.FragmentSettingsBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Enables the action bar and its back button
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        view.findViewById<Button>(R.id.howToUseButton).setOnClickListener {
            findNavController().navigate(R.id.action_fragment_settings_main_to_fragment_settings_howtouse)
        }
        view.findViewById<Button>(R.id.rulesButton).setOnClickListener {
            findNavController().navigate(R.id.action_fragment_settings_main_to_settingsFragmentRules)
        }
        view.findViewById<Button>(R.id.aboutButton).setOnClickListener {
            findNavController().navigate(R.id.action_fragment_settings_main_to_settingsFragmentAbout)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}